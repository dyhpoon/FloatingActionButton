package com.dyhpoon.fab;

import android.widget.ScrollView;

abstract class ScrollViewScrollDetector implements ObservableScrollView.OnScrollChangedListener {
    private int mLastScrollY;
    private int mScrollThreshold;
    private ObservableScrollView.OnScrollChangedListener mScrollViewChangedListener;

    public ScrollViewScrollDetector(ObservableScrollView.OnScrollChangedListener l) {
        mScrollViewChangedListener = l;
    }

    abstract void onScrollUp();

    abstract void onScrollDown();

    @Override
    public void onScrollChanged(ScrollView who, int l, int t, int oldl, int oldt) {
        boolean isSignificantDelta = Math.abs(t - mLastScrollY) > mScrollThreshold;
        if (isSignificantDelta) {
            if (t > mLastScrollY) {
                onScrollUp();
            } else {
                onScrollDown();
            }
        }
        mLastScrollY = t;
        if (mScrollViewChangedListener != null)
            mScrollViewChangedListener.onScrollChanged(who, l, t, oldl, oldt);
    }

    public void setScrollThreshold(int scrollThreshold) {
        mScrollThreshold = scrollThreshold;
    }
}