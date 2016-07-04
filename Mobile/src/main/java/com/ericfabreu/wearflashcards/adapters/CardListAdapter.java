package com.ericfabreu.wearflashcards.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.data.FlashcardContract.CardSet;
import com.ericfabreu.wearflashcards.data.FlashcardProvider;

/**
 * Simple adapter for CardListFragment.
 */
public class CardListAdapter extends CursorAdapter {
    private Context mContext;
    private String mTableName;

    public CardListAdapter(Context context, String tableName, Cursor cursor, int flags) {
        super(context, cursor, flags);
        mContext = context;
        mTableName = tableName;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        // Override the click listener on the star
        if (view != null) {
            view.findViewById(R.id.layout_frame_star).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FlashcardProvider handle = new FlashcardProvider(mContext);
                    handle.flipStar(Uri.withAppendedPath(CardSet.CONTENT_URI, mTableName), getItemId(position));
                }
            });
        }
        return view;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.fragment_card_list, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView term = (TextView) view.findViewById(R.id.text_term);
        term.setText(cursor.getString(cursor.getColumnIndex(CardSet.TERM)));
        TextView definition = (TextView) view.findViewById(R.id.text_definition);
        definition.setText(cursor.getString(cursor.getColumnIndex(CardSet.DEFINITION)));
        ImageView star = (ImageView) view.findViewById(R.id.image_star);
        final boolean starFlag = cursor.getInt(cursor.getColumnIndex(CardSet.STAR)) == 1;
        final int starId = starFlag ? R.drawable.ic_star_selected : R.drawable.ic_star_unselected;
        star.setImageDrawable(ContextCompat.getDrawable(context, starId));
    }
}
