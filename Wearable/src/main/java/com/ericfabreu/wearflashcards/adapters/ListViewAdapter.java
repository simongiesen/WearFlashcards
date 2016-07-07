package com.ericfabreu.wearflashcards.adapters;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ericfabreu.wearflashcards.R;

/**
 * Provides the ListView adapter used throughout the app.
 */
public final class ListViewAdapter extends WearableListView.Adapter {
    private final LayoutInflater mInflater;
    private String[] mDataSet;
    private int mLayout;

    public ListViewAdapter(Context context, int layout, String[] dataSet) {
        mInflater = LayoutInflater.from(context);
        mDataSet = dataSet;
        mLayout = layout;
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate custom layout for list items
        return new ItemViewHolder(mInflater.inflate(mLayout, parent, false));
    }

    // Replace the contents of a list item
    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
        // Retrieve the text view and replace its contents
        ItemViewHolder itemHolder = (ItemViewHolder) holder;
        TextView view = itemHolder.textView;
        view.setText(mDataSet[position]);
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return mDataSet.length;
    }

    // Provide a reference to the views used in the ListView
    public static class ItemViewHolder extends WearableListView.ViewHolder {
        private TextView textView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text_list_item);
        }

        public TextView getTextView() {
            return textView;
        }
    }
}
