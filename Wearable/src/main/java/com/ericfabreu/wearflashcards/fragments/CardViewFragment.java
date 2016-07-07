package com.ericfabreu.wearflashcards.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
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
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        // Get term and definition
        final Bundle bundle = getArguments();
        final String term = bundle.getString(Constants.TERM);
        final String definition = bundle.getString(Constants.DEFINITION);
        final long id = bundle.getLong(Constants.ID);
        final boolean star = bundle.getBoolean(Constants.STAR);

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
        final int starDrawable = star ? R.drawable.ic_star_selected : R.drawable.ic_star_unselected;
        starView.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(),
                starDrawable));

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
                final int starDrawable = !star ? R.drawable.ic_star_selected
                        : R.drawable.ic_star_unselected;
                starView.setImageDrawable(ContextCompat.getDrawable(getActivity()
                        .getApplicationContext(), starDrawable));
                bundle.putBoolean(Constants.STAR, !star);
                Log.d("id", String.valueOf(id));
                onCreateView(inflater, container, savedInstanceState);
            }
        });
        return card;
    }
}
