package com.ericfabreu.wearflashcards.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.data.FlashcardContract.CardSet;
import com.ericfabreu.wearflashcards.data.FlashcardProvider;
import com.ericfabreu.wearflashcards.utils.Constants;
import com.thinkincode.utils.views.AutoResizeTextView;

/**
 * Displays cards on the screen.
 */
public class CardViewFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get term and definition
        Bundle bundle = getArguments();
        final String tableName = bundle.getString(Constants.TABLE_NAME);
        final String term = bundle.getString(Constants.TERM);
        final String definition = bundle.getString(Constants.DEFINITION);
        final boolean star = bundle.getBoolean(Constants.STAR);
        final long id = bundle.getLong(Constants.ID);

        // Create card and get necessary views
        View card = inflater.inflate(R.layout.fragment_card_view, container, false);
        FrameLayout frame = (FrameLayout) card.findViewById(R.id.layout_card_view);
        final AutoResizeTextView termView =
                (AutoResizeTextView) frame.findViewById(R.id.text_card_term);
        termView.setMinTextSize(14f);
        final AutoResizeTextView definitionView =
                (AutoResizeTextView) frame.findViewById(R.id.text_card_definition);
        definitionView.setMinTextSize(10f);
        final ImageView starView = (ImageView) frame.findViewById(R.id.image_star_card);
        final int starDrawable = star ? R.drawable.ic_star_selected : R.drawable.ic_star_unselected;
        starView.setImageDrawable(ContextCompat.getDrawable(getContext(), starDrawable));

        // Add term and definition
        termView.setText(term);
        definitionView.setText(definition);

        // Add click listeners to flip visibility
        View.OnClickListener cardListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (termView.getVisibility() == View.VISIBLE) {
                    termView.setVisibility(View.INVISIBLE);
                    definitionView.setVisibility(View.VISIBLE);
                } else {
                    termView.setVisibility(View.VISIBLE);
                    definitionView.setVisibility(View.INVISIBLE);
                }
            }
        };
        termView.setOnClickListener(cardListener);
        definitionView.setOnClickListener(cardListener);
        frame.setOnClickListener(cardListener);

        // Add click listener on the star
        starView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FlashcardProvider handle = new FlashcardProvider(getContext());
                Uri uri = Uri.withAppendedPath(CardSet.CONTENT_URI, tableName);
                handle.flipFlag(uri, id, CardSet.STAR);

                // Get new star value
                Cursor cursor = handle.query(uri,
                        new String[]{CardSet._ID, CardSet.STAR},
                        CardSet._ID + "=?",
                        new String[]{String.valueOf(id)},
                        null);

                // Update the star drawable
                if (cursor != null && cursor.moveToFirst()) {
                    final boolean flag = cursor.getInt(cursor.getColumnIndex(CardSet.STAR)) == 1;
                    final int newStarDrawable = flag ? R.drawable.ic_star_selected : R.drawable.ic_star_unselected;
                    starView.setImageDrawable(ContextCompat.getDrawable(getContext(), newStarDrawable));
                    cursor.close();
                }
            }
        });
        return card;
    }
}
