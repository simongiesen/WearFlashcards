package com.efa.wearflashcards;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        // Create card and add term and definition
        View card = inflater.inflate(R.layout.card_view, container, false);
        TextView termView = (TextView) card.findViewById(R.id.term);
        termView.setText(term);
        TextView defView = (TextView) card.findViewById(R.id.definition);
        defView.setText(definition);

        // Add click listeners
        final ScrollView termScroll = (ScrollView) card.findViewById(R.id.term_scroll);
        final ScrollView defScroll = (ScrollView) card.findViewById(R.id.def_scroll);
        View.OnClickListener cardListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        return card;
    }
}
