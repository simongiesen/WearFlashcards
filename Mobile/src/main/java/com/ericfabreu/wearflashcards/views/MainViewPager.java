package com.ericfabreu.wearflashcards.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * An customized version of ViewPager that allows scrolling to be disabled/enabled at any time.
 */
public class MainViewPager extends ViewPager {
    private boolean mScrollable = true;

    public MainViewPager(Context context) {
        super(context);
    }

    public MainViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScrollable(boolean scrollable) {
        mScrollable = scrollable;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mScrollable && super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mScrollable && super.onTouchEvent(event);
    }
}
