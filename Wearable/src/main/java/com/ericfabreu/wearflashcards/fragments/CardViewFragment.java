package com.ericfabreu.wearflashcards.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.utils.Constants;

/**
 * Fragment used to display cards.
 */
public class CardViewFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get term and definition
        Bundle bundle = getArguments();
        final String term = bundle.getString(Constants.TERM);
        final String definition = bundle.getString(Constants.DEFINITION);

        // Create card and get necessary views
        View card = inflater.inflate(R.layout.fragment_card_view, container, false);
        FrameLayout frame = (FrameLayout) card.findViewById(R.id.layout_card_frame);
        final ScrollView termScroll = (ScrollView) frame.findViewById(R.id.scroll_card_term);
        final ScrollView defScroll = (ScrollView) frame.findViewById(R.id.scroll_definition);
        TextView termView = (TextView) frame.findViewById(R.id.text_term);
        TextView definitionView = (TextView) frame.findViewById(R.id.text_card_definition);

        // Add text and click listeners
        termView.setText(term);
        definitionView.setText(definition);
        View.OnClickListener cardListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Flip term/definition visibility
                if (termScroll.getVisibility() == View.VISIBLE) {
                    termScroll.setVisibility(View.INVISIBLE);
                    defScroll.setVisibility(View.VISIBLE);
                } else {
                    termScroll.setVisibility(View.VISIBLE);
                    defScroll.setVisibility(View.INVISIBLE);
                }
            }
        };
        termView.setOnClickListener(cardListener);
        definitionView.setOnClickListener(cardListener);
        frame.setOnClickListener(cardListener);
        return card;
    }
}
