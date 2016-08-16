package com.ericfabreu.wearflashcards.fragments;

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
import com.ericfabreu.wearflashcards.adapters.StudySetAdapter;
import com.ericfabreu.wearflashcards.data.FlashcardContract.CardSet;
import com.ericfabreu.wearflashcards.data.FlashcardContract.SetList;
import com.ericfabreu.wearflashcards.data.FlashcardProvider;
import com.ericfabreu.wearflashcards.utils.Constants;
import com.ericfabreu.wearflashcards.utils.PreferencesHelper;
import com.thinkincode.utils.views.AutoResizeTextView;

/**
 * Displays cards on the screen.
 */
public class CardViewFragment extends Fragment {
    private StudySetAdapter mAdapter;
    private int mPosition;

    public CardViewFragment() {
        super();
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    public void setAdapter(StudySetAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        // Get term and definition
        final Bundle bundle = getArguments();
        final String tableName = bundle.getString(Constants.TAG_TABLE_NAME);
        final String term = bundle.getString(Constants.TAG_TERM);
        final String definition = bundle.getString(Constants.TAG_DEFINITION);
        final boolean star = bundle.getBoolean(Constants.TAG_STAR);
        final long id = bundle.getLong(Constants.TAG_ID);

        // Create card and get necessary views
        View card = inflater.inflate(R.layout.item_card_view, container, false);
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

        starView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FlashcardProvider handle = new FlashcardProvider(getContext());
                final Uri uri = Uri.withAppendedPath(CardSet.CONTENT_URI, tableName);
                PreferencesHelper.flipStar(getContext(), handle, uri, id, CardSet.STAR);

                // Delete card if star is removed and starred only mode is on
                if (tableName != null) {
                    final long tableId = Long.valueOf(tableName
                            .substring(1, tableName.length() - 1));
                    final boolean starredOnly = PreferencesHelper.getStar(getContext(), handle,
                            SetList.CONTENT_URI, tableId, SetList.STARRED_ONLY);
                    if (starredOnly) {
                        mAdapter.deleteItem(mPosition);
                        return;
                    }
                }

                // Switch star drawable
                final boolean flag = PreferencesHelper.getStar(getContext(), handle,
                        uri, id, CardSet.STAR);
                final int star = flag ? R.drawable.ic_star_selected : R.drawable.ic_star_unselected;
                starView.setImageDrawable(ContextCompat.getDrawable(getContext(), star));
                bundle.putBoolean(Constants.TAG_STAR, flag);
                onCreateView(inflater, container, savedInstanceState);
            }
        });
        return card;
    }
}
