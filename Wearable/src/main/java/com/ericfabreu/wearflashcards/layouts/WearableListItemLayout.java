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
    private TextView mName, mTitle, mDescription;
    private boolean mSettings = false;

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
        mCircle = (ImageView) findViewById(R.id.image_list_drawable);
        if (mCircle == null) {
            mSettings = true;
            mCircle = (ImageView) findViewById(R.id.image_list_settings);
            mTitle = (TextView) findViewById(R.id.text_list_title_settings);
            mDescription = (TextView) findViewById(R.id.text_list_description_settings);
        } else {
            mName = (TextView) findViewById(R.id.text_list_item);
        }
    }

    @Override
    public void onCenterPosition(boolean animate) {
        mCircle.setAlpha(mOpaqueAlpha);
        mCircle.setScaleX(mLargeScale);
        mCircle.setScaleY(mLargeScale);

        if (mSettings) {
            mTitle.setAlpha(mOpaqueAlpha);
            mDescription.setAlpha(mOpaqueAlpha);
        } else {
            mName.setAlpha(mOpaqueAlpha);
        }
    }

    @Override
    public void onNonCenterPosition(boolean animate) {
        mCircle.setAlpha(mFadedAlpha);
        mCircle.setScaleX(mRegularScale);
        mCircle.setScaleY(mRegularScale);

        if (mSettings) {
            mTitle.setAlpha(mFadedAlpha);
            mDescription.setAlpha(mFadedAlpha);
        } else {
            mName.setAlpha(mFadedAlpha);
        }
    }
}