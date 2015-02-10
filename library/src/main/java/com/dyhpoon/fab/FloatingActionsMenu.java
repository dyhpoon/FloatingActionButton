package com.dyhpoon.fab;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.AbsListView;

import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

/**
 * Created by darrenpoon on 5/2/15.
 */
public class FloatingActionsMenu extends ViewGroup {

    public static final int EXPAND_UP = 0;
    public static final int EXPAND_DOWN = 1;

    private static final int ANIMATION_DURATION_TOGGLE = 300;
    private static final int ANIMATION_DURATION_BOUNCE = 200;

    private Drawable mMenuSelectedIcon;
    private Drawable mMenuUnSelectedIcon;
    private int mMenuButtonColorNormal;
    private int mMenuButtonColorPressed;
    private int mMenuButtonColorRipple;
    private int mExpandDirection;

    private boolean mExpanded;
    private boolean mVisible;

    private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
    private final AnimatorSet mExpandAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION_TOGGLE);
    private final AnimatorSet mCollapseAnimation = new AnimatorSet().setDuration(ANIMATION_DURATION_TOGGLE);

    private FloatingActionButton mMenuButton;

    private int mButtonSpacing;
    private int mMaxButtonWidth;
    private int mButtonsCount;
    private int mShadowOffset;
    private int mScrollThreshold;

    private OnFloatingActionsMenuUpdateListener mListener;

    public interface OnFloatingActionsMenuUpdateListener {
        void onMenuExpanded();

        void onMenuCollapsed();
    }

    public FloatingActionsMenu(Context context) {
        this(context, null);
    }

    public FloatingActionsMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FloatingActionsMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attributeSet) {
        mVisible = true;
        mScrollThreshold = getResources().getDimensionPixelOffset(R.dimen.fab_scroll_threshold);
        mShadowOffset = getResources().getDimensionPixelSize(R.dimen.fab_shadow_offset);
        mButtonSpacing = (int) (getResources().getDimension(R.dimen.fab_actions_spacing)
                - getResources().getDimension(R.dimen.fab_shadow_radius)
                - getResources().getDimension(R.dimen.fab_shadow_offset))
                + mShadowOffset;

        TypedArray attr = context.obtainStyledAttributes(attributeSet, R.styleable.FloatingActionsMenu, 0, 0);
        mMenuSelectedIcon = attr.getDrawable(R.styleable.FloatingActionsMenu_fab_menuButtonSelectedSrc);
        mMenuUnSelectedIcon = attr.getDrawable(R.styleable.FloatingActionsMenu_fab_menuButtonUnSelectedSrc);
        mMenuButtonColorNormal = attr.getColor(R.styleable.FloatingActionsMenu_fab_menuButtonColorNormal, getColor(android.R.color.holo_blue_light));
        mMenuButtonColorPressed = attr.getColor(R.styleable.FloatingActionsMenu_fab_menuButtonColorPressed, getColor(android.R.color.holo_blue_dark));
        mMenuButtonColorRipple = attr.getColor(R.styleable.FloatingActionsMenu_fab_menuButtonColorRipple, getColor(android.R.color.holo_blue_bright));
        mExpandDirection = attr.getInt(R.styleable.FloatingActionsMenu_fab_expandDirection, EXPAND_UP);
        attr.recycle();

        createMenuButton(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int width = 0;
        int height = 0;
        mMaxButtonWidth = 0;

        for (int i = 0; i < mButtonsCount; i++) {
            View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            mMaxButtonWidth = Math.max(mMaxButtonWidth, child.getMeasuredWidth());
            height += child.getMeasuredHeight();
            height = adjustForOvershoot(height);
        }

        height += mButtonSpacing * getChildCount();
        width += mMaxButtonWidth + (mShadowOffset * 2);

        setMeasuredDimension(width, height);
    }

    private int adjustForOvershoot(int dimension) {
        return dimension * 12 / 10;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        boolean expandUp = mExpandDirection == EXPAND_UP;

        final int adjustShadowOffsetX = -mShadowOffset;
        final int adjustShadowOffsetY = (expandUp) ? -mShadowOffset : mShadowOffset;

        int menuButtonY = expandUp ? b - t - mMenuButton.getMeasuredHeight() : 0;
        // Ensure mMenuButton is centered on the line where the buttons should be
        int buttonsHorizontalCenter = r - l - mMaxButtonWidth / 2; // mMaxButtonWidth / 2??
        int menuButtonLeft = buttonsHorizontalCenter - mMenuButton.getMeasuredWidth() / 2;
        mMenuButton.layout(
                menuButtonLeft + adjustShadowOffsetX,
                menuButtonY + adjustShadowOffsetY,
                menuButtonLeft + mMenuButton.getMeasuredWidth() + adjustShadowOffsetX,
                menuButtonY + mMenuButton.getMeasuredHeight() + adjustShadowOffsetY);

        int nextY = expandUp ?
                menuButtonY - mButtonSpacing :
                menuButtonY + mMenuButton.getMeasuredHeight() + mButtonSpacing;

        for (int i = mButtonsCount - 1; i >= 0; i--) {
            final View child = getChildAt(i);

            if (child == mMenuButton || child.getVisibility() == GONE) continue;

            int childX = buttonsHorizontalCenter - child.getMeasuredWidth() / 2;
            int childY = expandUp ? nextY - child.getMeasuredHeight() : nextY;
            child.layout(
                    childX + adjustShadowOffsetX,
                    childY + adjustShadowOffsetY,
                    childX + child.getMeasuredWidth() + adjustShadowOffsetX,
                    childY + child.getMeasuredHeight() + adjustShadowOffsetY);

            float collapsedTranslation = menuButtonY - childY;
            float expandedTranslation = 0f;

            child.setTranslationY(mExpanded ? expandedTranslation : collapsedTranslation);
            child.setAlpha(mExpanded ? 1f : 0f);

            LayoutParams params = (LayoutParams) child.getLayoutParams();
            params.mCollapseDir.setFloatValues(expandedTranslation, collapsedTranslation);
            params.mExpandDir.setFloatValues(collapsedTranslation, expandedTranslation);
            params.setAnimationsTarget(child);

            nextY = expandUp ?
                    childY - mButtonSpacing :
                    childY + child.getMeasuredHeight() + mButtonSpacing;
        }

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        bringChildToFront(mMenuButton);
        mButtonsCount = getChildCount();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.mExpanded = mExpanded;
        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState savedState = (SavedState) state;
            mExpanded = savedState.mExpanded;

            super.onRestoreInstanceState(savedState.getSuperState());
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(super.generateDefaultLayoutParams());
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(super.generateLayoutParams(attrs));
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(super.generateLayoutParams(p));
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return super.checkLayoutParams(p);
    }

    public boolean isExpanded() {
        return mExpanded;
    }

    public void toggle() {
        if (mExpanded) {
            collapse();
        } else {
            expand();
        }
    }

    public void collapse() {
        if (mExpanded) {
            mExpanded = false;
            mCollapseAnimation.start();
            mExpandAnimation.cancel();
            mMenuButton.setImageDrawable(mMenuUnSelectedIcon);

            if (mListener != null) {
                mListener.onMenuCollapsed();
            }
        }
    }

    public void expand() {
        if (!mExpanded) {
            mExpanded = true;
            mCollapseAnimation.cancel();
            mExpandAnimation.start();
            mMenuButton.setImageDrawable(mMenuSelectedIcon);

            if (mListener != null) {
                mListener.onMenuExpanded();
            }
        }
    }

    public boolean isVisible() {
        return mVisible;
    }

    public void show() {
        show(true);
    }

    public void hide() {
        hide(true);
    }

    public void show(boolean animate) {
        bounce(true, animate, false);
    }

    public void hide(boolean animate) {
        if (!mExpanded)
            bounce(false, animate, false);
    }

    private void bounce(final boolean visible, final boolean animate, boolean force) {
        if (mVisible != visible || force) {
            mVisible = visible;
            int height = getHeight();
            if (height == 0 && !force) {
                ViewTreeObserver vto = getViewTreeObserver();
                if (vto.isAlive()) {
                    vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            ViewTreeObserver currentVto = getViewTreeObserver();
                            if (currentVto.isAlive()) {
                                currentVto.removeOnPreDrawListener(this);
                            }
                            bounce(visible, animate, true);
                            return true;
                        }
                    });
                    return;
                }
            }
            int translationY = visible ? 0 : height + getMarginBottom();
            if (animate) {
                ViewPropertyAnimator.animate(this).setInterpolator(mInterpolator)
                        .setDuration(ANIMATION_DURATION_BOUNCE)
                        .translationY(translationY);
            } else {
                ViewHelper.setTranslationY(this, translationY);
            }
        }
    }

    private int getMarginBottom() {
        int marginBottom = 0;
        final ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            marginBottom = ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin;
        }
        return marginBottom;
    }

    private void createMenuButton(Context context) {
        mMenuButton = new FloatingActionButton(context) {
            @Override
            protected void updateBackground() {
                mColorNormal = mMenuButtonColorNormal;
                mColorPressed = mMenuButtonColorPressed;
                mColorRipple = mMenuButtonColorRipple;
                super.updateBackground();
                super.setImageDrawable(mExpanded ? mMenuSelectedIcon : mMenuUnSelectedIcon);
            }
        };

        mMenuButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle();
            }
        });

        addView(mMenuButton, super.generateDefaultLayoutParams());
    }

    private int getColor(@ColorRes int id) {
        return getResources().getColor(id);
    }

    private static Interpolator sExpandInterpolator = new OvershootInterpolator();
    private static Interpolator sCollapseInterpolator = new DecelerateInterpolator(3f);
    private static Interpolator sAlphaExpandInterpolator = new DecelerateInterpolator();

    private class LayoutParams extends ViewGroup.LayoutParams {

        private ObjectAnimator mExpandDir = new ObjectAnimator();
        private ObjectAnimator mExpandAlpha = new ObjectAnimator();
        private ObjectAnimator mCollapseDir = new ObjectAnimator();
        private ObjectAnimator mCollapseAlpha = new ObjectAnimator();
        private boolean animationsSetToPlay;

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);

            mExpandDir.setInterpolator(sExpandInterpolator);
            mExpandAlpha.setInterpolator(sAlphaExpandInterpolator);
            mCollapseDir.setInterpolator(sCollapseInterpolator);
            mCollapseAlpha.setInterpolator(sCollapseInterpolator);

            mCollapseAlpha.setProperty(View.ALPHA);
            mCollapseAlpha.setFloatValues(1f, 0f);

            mExpandAlpha.setProperty(View.ALPHA);
            mExpandAlpha.setFloatValues(0f, 1f);

            mCollapseDir.setProperty(View.TRANSLATION_Y);
            mExpandDir.setProperty(View.TRANSLATION_Y);
        }

        public void setAnimationsTarget(View view) {
            mCollapseAlpha.setTarget(view);
            mCollapseDir.setTarget(view);
            mExpandAlpha.setTarget(view);
            mExpandDir.setTarget(view);

            // Now that the animations have targets, set them to be played
            if (!animationsSetToPlay) {
                mCollapseAnimation.play(mCollapseAlpha);
                mCollapseAnimation.play(mCollapseDir);
                mExpandAnimation.play(mExpandAlpha);
                mExpandAnimation.play(mExpandDir);
                animationsSetToPlay = true;
            }
        }
    }

    public static class SavedState extends BaseSavedState {
        public boolean mExpanded;

        public SavedState(Parcelable parcel) {
            super(parcel);
        }

        private SavedState(Parcel in) {
            super(in);
            mExpanded = in.readInt() == 1;
        }

        @Override
        public void writeToParcel(@NonNull Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mExpanded ? 1 : 0);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    public void attachToListView(@NonNull AbsListView listView) {
        attachToListView(listView, null);
    }

    public void attachToListView(@NonNull AbsListView listView,
                                 ScrollDirectionListener listener) {
        attachToListView(listView, listener, null);
    }

    public void attachToListView(@NonNull AbsListView listView,
                                 ScrollDirectionListener listener,
                                 AbsListView.OnScrollListener scrollListener) {
        AbsListViewScrollDetectorImpl scrollDetector =
                new AbsListViewScrollDetectorImpl(scrollListener);
        scrollDetector.setListener(listener);
        scrollDetector.setListView(listView);
        scrollDetector.setScrollThreshold(mScrollThreshold);
        listView.setOnScrollListener(scrollDetector);
    }

    public void attachToRecyclerView(@NonNull RecyclerView recyclerView) {
        attachToRecyclerView(recyclerView, null);
    }

    public void attachToRecyclerView(@NonNull RecyclerView recyclerView,
                                     ScrollDirectionListener listener) {
        attachToRecyclerView(recyclerView, listener, null);
    }

    public void attachToRecyclerView(@NonNull RecyclerView recyclerView,
                                     ScrollDirectionListener listener,
                                     RecyclerView.OnScrollListener scrollListener) {
        RecyclerViewScrollDetectorImpl scrollDetector =
                new RecyclerViewScrollDetectorImpl(scrollListener);
        scrollDetector.setListener(listener);
        scrollDetector.setScrollThreshold(mScrollThreshold);
        recyclerView.setOnScrollListener(scrollDetector);
    }

    public void attachToScrollView(@NonNull ObservableScrollView scrollView) {
        attachToScrollView(scrollView, null);
    }

    public void attachToScrollView(@NonNull ObservableScrollView scrollView,
                                   ScrollDirectionListener listener) {
        attachToScrollView(scrollView, listener, null);
    }

    public void attachToScrollView(@NonNull ObservableScrollView scrollView,
                                   ScrollDirectionListener listener,
                                   ObservableScrollView.OnScrollChangedListener scrollListener) {
        ScrollViewScrollDetectorImpl scrollDetector =
                new ScrollViewScrollDetectorImpl(scrollListener);
        scrollDetector.setListener(listener);
        scrollDetector.setScrollThreshold(mScrollThreshold);
        scrollView.setOnScrollChangedListener(scrollDetector);
    }

    private class AbsListViewScrollDetectorImpl extends AbsListViewScrollDetector {
        private ScrollDirectionListener mListener;

        public AbsListViewScrollDetectorImpl(AbsListView.OnScrollListener l) {
            super(l);
        }

        private void setListener(ScrollDirectionListener scrollDirectionListener) {
            mListener = scrollDirectionListener;
        }

        @Override
        public void onScrollDown() {
            show();
            if (mListener != null) {
                mListener.onScrollDown();
            }
        }

        @Override
        public void onScrollUp() {
            hide();
            if (mListener != null) {
                mListener.onScrollUp();
            }
        }
    }

    private class RecyclerViewScrollDetectorImpl extends RecyclerViewScrollDetector {
        private ScrollDirectionListener mListener;

        public RecyclerViewScrollDetectorImpl(RecyclerView.OnScrollListener l) {
            super(l);
        }

        private void setListener(ScrollDirectionListener scrollDirectionListener) {
            mListener = scrollDirectionListener;
        }

        @Override
        public void onScrollDown() {
            show();
            if (mListener != null) {
                mListener.onScrollDown();
            }
        }

        @Override
        public void onScrollUp() {
            hide();
            if (mListener != null) {
                mListener.onScrollUp();
            }
        }
    }

    private class ScrollViewScrollDetectorImpl extends ScrollViewScrollDetector {
        private ScrollDirectionListener mListener;

        public ScrollViewScrollDetectorImpl(ObservableScrollView.OnScrollChangedListener l) {
            super(l);
        }

        private void setListener(ScrollDirectionListener scrollDirectionListener) {
            mListener = scrollDirectionListener;
        }

        @Override
        public void onScrollDown() {
            show();
            if (mListener != null) {
                mListener.onScrollDown();
            }
        }

        @Override
        public void onScrollUp() {
            hide();
            if (mListener != null) {
                mListener.onScrollUp();
            }
        }
    }

}
