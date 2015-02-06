package com.dyhpoon.fab;

import android.support.v7.widget.RecyclerView;

abstract class RecyclerViewScrollDetector extends RecyclerView.OnScrollListener {
    private int mScrollThreshold;
    private RecyclerView.OnScrollListener mRecycleViewScrollListener;

    public RecyclerViewScrollDetector(RecyclerView.OnScrollListener l) {
        mRecycleViewScrollListener = l;
    }

    abstract void onScrollUp();

    abstract void onScrollDown();

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        boolean isSignificantDelta = Math.abs(dy) > mScrollThreshold;
        if (isSignificantDelta) {
            if (dy > 0) {
                onScrollUp();
            } else {
                onScrollDown();
            }
        }
        if (mRecycleViewScrollListener != null)
            mRecycleViewScrollListener.onScrolled(recyclerView, dx, dy);
    }

    public void setScrollThreshold(int scrollThreshold) {
        mScrollThreshold = scrollThreshold;
    }
}