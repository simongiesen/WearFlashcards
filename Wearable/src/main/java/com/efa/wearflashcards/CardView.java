package com.efa.wearflashcards;

import android.app.Fragment;
import android.os.Bundle;
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
    private static String term;
    private static String definition;

    /**
     * Initializes term and definition for onCreateView.
     */
    public static Fragment create(String tm, String def) {
        term = tm;
        definition = def;
        return new CardView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Create card
        View card = inflater.inflate(R.layout.card_view, container, false);

        // Get necessary views
        FrameLayout frame = (FrameLayout) card.findViewById(R.id.frame);
        final ScrollView termScroll = (ScrollView) frame.findViewById(R.id.term_scroll);
        final ScrollView defScroll = (ScrollView) frame.findViewById(R.id.def_scroll);
        TextView termView = (TextView) termScroll.findViewById(R.id.term);
        TextView defView = (TextView) termScroll.findViewById(R.id.definition);

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
