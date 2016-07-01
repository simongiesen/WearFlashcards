package com.ericfabreu.wearflashcards.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.ericfabreu.wearflashcards.R;
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
        final String term = bundle.getString(Constants.TERM);
        final String definition = bundle.getString(Constants.DEFINITION);

        // Create card and get necessary views
        View card = inflater.inflate(R.layout.fragment_card_view, container, false);
        FrameLayout frame = (FrameLayout) card.findViewById(R.id.layout_card_view);
        final AutoResizeTextView termView =
                (AutoResizeTextView) frame.findViewById(R.id.text_card_term);
        final AutoResizeTextView definitionView =
                (AutoResizeTextView) frame.findViewById(R.id.text_card_definition);
        termView.setMinTextSize(14f);
        termView.setEllipsize(TextUtils.TruncateAt.END);
        definitionView.setMinTextSize(10f);
        definitionView.setEllipsize(TextUtils.TruncateAt.END);

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
        return card;
    }
}
