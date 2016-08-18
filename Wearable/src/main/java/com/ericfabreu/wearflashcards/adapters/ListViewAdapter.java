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
import com.ericfabreu.wearflashcards.layouts.WearableListItemLayout;

/**
 * Provides the ListView adapter used throughout the app.
 */
public final class ListViewAdapter extends WearableListView.Adapter {
    private final LayoutInflater mInflater;
    private String[] mDataSet, mOptions;
    private Drawable[] mIcons;
    private int mLayout, mMode;

    /**
     * Uses {@param mode} to determine if the parent is MainActivity (0) trying to display the
     * initial sets/folders choices, MainActivity (1) trying to display either a list of sets or a
     * list of folders, or SettingsActivity (2).
     */
    public ListViewAdapter(Context context, int layout, String[] dataSet,
                           String[] options, Drawable[] icons, int mode) {
        mInflater = LayoutInflater.from(context);
        mLayout = layout;
        mDataSet = dataSet;
        mOptions = options;
        mIcons = icons;
        mMode = mode;
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate custom layout for list items
        return new ItemViewHolder(mInflater.inflate(mLayout, parent, false), mMode);
    }

    // Replace the contents of a list item
    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
        // Retrieve the text view and replace its contents
        ItemViewHolder itemHolder = (ItemViewHolder) holder;
        holder.itemView.setTag(position);

        // MainActivity displaying the original sets and folders options
        if (mMode == 0) {
            WearableListItemLayout layout = (WearableListItemLayout) itemHolder.getView();
            ((ImageView) layout.findViewById(R.id.image_list_drawable))
                    .setImageDrawable(mIcons[position]);
            TextView textView = ((TextView) layout.findViewById(R.id.text_list_item));
            textView.setText(mDataSet[position]);
            textView.setPadding(8, 0, 0, 0);
            textView.setTextSize(18f);
        }

        // MainActivity displaying a list of either sets or folders
        else if (mMode == 1) {
            TextView view = itemHolder.textView;
            view.setText(mDataSet[position]);
        }

        // SettingsActivity
        else {
            RelativeLayout layout = (RelativeLayout) itemHolder.getView();
            ((ImageView) layout.findViewById(R.id.image_list_settings))
                    .setImageDrawable(mIcons[position]);
            ((TextView) layout.findViewById(R.id.text_list_title_settings))
                    .setText(mDataSet[position]);
            ((TextView) layout.findViewById(R.id.text_list_description_settings))
                    .setText(mOptions[position]);
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
        private WearableListItemLayout wearableListItemLayout;
        private int mMode;

        // Save the proper view/layout depending on the mode
        public ItemViewHolder(View itemView, int mode) {
            super(itemView);
            mMode = mode;
            if (mMode == 0) {
                wearableListItemLayout = (WearableListItemLayout) itemView
                        .findViewById(R.id.layout_set_list_item);
            } else if (mMode == 1) {
                textView = (TextView) itemView.findViewById(R.id.text_list_item);
            } else {
                relativeLayout = (RelativeLayout) itemView
                        .findViewById(R.id.layout_relative_settings);
            }
        }

        public View getView() {
            return mMode == 0 ? wearableListItemLayout : (mMode == 1 ? textView : relativeLayout);
        }
    }
}
