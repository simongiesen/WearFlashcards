package com.ericfabreu.wearflashcards.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.utils.Constants;

/**
 * Displays cards on the screen.
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
        FrameLayout frame = (FrameLayout) card.findViewById(R.id.layout_card_view);
        final ScrollView termScroll = (ScrollView) frame.findViewById(R.id.scroll_term);
        final ScrollView definitionScroll = (ScrollView) frame.findViewById(R.id.scroll_definition);
        TextView termView = (TextView) frame.findViewById(R.id.text_card_term);
        TextView definitionView = (TextView) frame.findViewById(R.id.text_card_definition);

        // Add term and definition
        termView.setText(term);
        definitionView.setText(definition);

        // Add click listeners to flip visibility
        View.OnClickListener cardListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (termScroll.getVisibility() == View.VISIBLE) {
                    termScroll.setVisibility(View.INVISIBLE);
                    definitionScroll.setVisibility(View.VISIBLE);
                } else {
                    termScroll.setVisibility(View.VISIBLE);
                    definitionScroll.setVisibility(View.INVISIBLE);
                }
            }
        };
        termView.setOnClickListener(cardListener);
        definitionView.setOnClickListener(cardListener);
        frame.setOnClickListener(cardListener);
        return card;
    }
}
