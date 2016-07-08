package com.ericfabreu.wearflashcards.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ericfabreu.wearflashcards.R;

/**
 * Provides the ListView adapter used throughout the app.
 */
public final class ListViewAdapter extends WearableListView.Adapter {
    private final LayoutInflater mInflater;
    private String[] mDataSet, mOptions;
    private Drawable[] mIcons;
    private int mLayout;
    private boolean mSettings;

    public ListViewAdapter(Context context, int layout, String[] dataSet) {
        mInflater = LayoutInflater.from(context);
        mDataSet = dataSet;
        mLayout = layout;
        mSettings = false;
    }

    public ListViewAdapter(Context context, int layout, String[] dataSet,
                           String[] options, Drawable[] icons) {
        mInflater = LayoutInflater.from(context);
        mLayout = layout;
        mDataSet = dataSet;
        mOptions = options;
        mIcons = icons;
        mSettings = true;
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate custom layout for list items
        return new ItemViewHolder(mInflater.inflate(mLayout, parent, false), mSettings);
    }

    // Replace the contents of a list item
    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
        // Retrieve the text view and replace its contents
        ItemViewHolder itemHolder = (ItemViewHolder) holder;
        holder.itemView.setTag(position);

        if (mSettings) {
            RelativeLayout layout = (RelativeLayout) itemHolder.getView();
            ((ImageView) layout.findViewById(R.id.image_list_settings))
                    .setImageDrawable(mIcons[position]);
            ((TextView) layout.findViewById(R.id.text_list_title_settings))
                    .setText(mDataSet[position]);
            ((TextView) layout.findViewById(R.id.text_list_description_settings))
                    .setText(mOptions[position]);
        } else {
            TextView view = itemHolder.textView;
            view.setText(mDataSet[position]);
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet.length;
    }

    // Provide a reference to the views used in the ListView
    public static class ItemViewHolder extends WearableListView.ViewHolder {
        private TextView textView;
        private RelativeLayout relativeLayout;
        private boolean mSettings;

        public ItemViewHolder(View itemView, boolean settings) {
            super(itemView);
            mSettings = settings;
            if (settings) {
                relativeLayout = (RelativeLayout) itemView
                        .findViewById(R.id.layout_relative_settings);
            } else {
                textView = (TextView) itemView.findViewById(R.id.text_list_item);
            }
        }

        public View getView() {
            return mSettings ? relativeLayout : textView;
        }
    }
}
