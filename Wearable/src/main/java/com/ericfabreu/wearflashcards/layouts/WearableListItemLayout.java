package com.ericfabreu.wearflashcards.layouts;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ericfabreu.wearflashcards.R;

/**
 * Provides layout for the set list created in MainActivity.
 * Layout from http://developer.android.com/training/wearables/ui/lists.html
 */
public class WearableListItemLayout extends LinearLayout
        implements WearableListView.OnCenterProximityListener {
    private final float mFadedAlpha, mOpaqueAlpha, mLargeScale, mRegularScale;
    private ImageView mCircle;
    private TextView mName;

    public WearableListItemLayout(Context context) {
        this(context, null);
    }

    public WearableListItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WearableListItemLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mFadedAlpha = getResources()
                .getInteger(R.integer.action_text_faded_alpha) / 100f;
        mOpaqueAlpha = mRegularScale = 1f;
        mLargeScale = 3 / 2f;
    }


    protected void onFinishInflate() {
        super.onFinishInflate();

        // Get references to the icon and text in the item layout definition
        mCircle = (ImageView) findViewById(R.id.image_circle);
        mName = (TextView) findViewById(R.id.text_name);
    }

    @Override
    public void onCenterPosition(boolean animate) {
        mName.setAlpha(mOpaqueAlpha);
        mCircle.setAlpha(mOpaqueAlpha);
        mCircle.setScaleX(mLargeScale);
        mCircle.setScaleY(mLargeScale);
    }

    @Override
    public void onNonCenterPosition(boolean animate) {
        mName.setAlpha(mFadedAlpha);
        mCircle.setAlpha(mFadedAlpha);
        mCircle.setScaleX(mRegularScale);
        mCircle.setScaleY(mRegularScale);
    }
}