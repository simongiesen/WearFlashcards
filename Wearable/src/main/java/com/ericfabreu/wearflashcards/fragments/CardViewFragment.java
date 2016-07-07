package com.ericfabreu.wearflashcards.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.utils.Constants;
import com.thinkincode.utils.views.AutoResizeTextView;

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
        final AutoResizeTextView termView =
                (AutoResizeTextView) frame.findViewById(R.id.text_term);
        final AutoResizeTextView definitionView =
                (AutoResizeTextView) frame.findViewById(R.id.text_card_definition);
        termView.setMinTextSize(14f);
        definitionView.setMinTextSize(12f);
        final ImageView starView = (ImageView) frame.findViewById(R.id.image_star_card);
        starView.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(),
                R.drawable.ic_star_selected));

        // Add text and click listeners
        termView.setText(term);
        definitionView.setText(definition);
        View.OnClickListener cardListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Flip term/definition visibility
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

        final FrameLayout starFrame = (FrameLayout) frame.findViewById(R.id.layout_star_card);
        starFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                starView.setImageDrawable(ContextCompat.getDrawable(getActivity()
                        .getApplicationContext(), R.drawable.ic_star_unselected));
            }
        });
        return card;
    }
}
