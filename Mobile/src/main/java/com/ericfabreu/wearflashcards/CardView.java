package com.ericfabreu.wearflashcards;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Fragment used to display cards.
 */
public class CardView extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get term and definition
        Bundle bundle = getArguments();
        final String term = bundle.getString(Constants.TERM);
        final String definition = bundle.getString(Constants.DEFINITION);

        // Create card
        View card = inflater.inflate(R.layout.card_view, container, false);

        // Get necessary views
        FrameLayout frame = (FrameLayout) card.findViewById(R.id.card_frame);
        final ScrollView termScroll = (ScrollView) frame.findViewById(R.id.term_scroll);
        final ScrollView defScroll = (ScrollView) frame.findViewById(R.id.def_scroll);
        TextView termView = (TextView) frame.findViewById(R.id.card_term_text);
        TextView defView = (TextView) frame.findViewById(R.id.card_def_text);

        // Add term and definition
        termView.setText(term);
        defView.setText(definition);

        // Add click listeners
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
        defView.setOnClickListener(cardListener);
        frame.setOnClickListener(cardListener);
        return card;
    }
}