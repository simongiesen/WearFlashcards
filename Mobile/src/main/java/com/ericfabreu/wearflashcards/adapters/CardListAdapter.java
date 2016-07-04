package com.ericfabreu.wearflashcards.adapters;

import android.content.Context;
import android.database.Cursor;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;

import com.ericfabreu.wearflashcards.R;

/**
 * Simple adapter for CardListFragment.
 */
public class CardListAdapter extends SimpleCursorAdapter {
    public CardListAdapter(Context context, int layout, Cursor c,
                           String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    @Override
    public void setViewImage(ImageView iv, String text) {
        if (text.equals("0")) {
            iv.setImageResource(R.drawable.ic_star_unselected);
        } else {
            iv.setImageResource(R.drawable.ic_star_selected);
        }
    }
}
