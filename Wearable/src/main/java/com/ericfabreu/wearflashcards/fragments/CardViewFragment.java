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
import com.ericfabreu.wearflashcards.adapters.SetViewAdapter;
import com.ericfabreu.wearflashcards.utils.Constants;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.thinkincode.utils.views.AutoResizeTextView;

import java.util.Date;

/**
 * Fragment used to display cards.
 */
public class CardViewFragment extends Fragment {
    private final static String TAG_CARD_ID = "card_id";
    private GoogleApiClient mGoogleApiClient;
    private int mPosition;
    private SetViewAdapter mAdapter;

    public void setGoogleApiClient(GoogleApiClient googleApiClient) {
        mGoogleApiClient = googleApiClient;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    public void setAdapter(SetViewAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        // Get term and definition
        final Bundle bundle = getArguments();
        final String title = bundle.getString(Constants.TAG_TITLE);
        final String term = bundle.getString(Constants.TAG_TERM);
        final String definition = bundle.getString(Constants.TAG_DEFINITION);
        final long id = bundle.getLong(Constants.TAG_ID);
        final boolean star = bundle.getBoolean(Constants.TAG_STAR);
        final boolean starredOnly = bundle.getBoolean(Constants.TAG_STARRED_ONLY);

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
                final boolean star = !bundle.getBoolean(Constants.TAG_STAR);
                final int starDrawable = star ? R.drawable.ic_star_selected
                        : R.drawable.ic_star_unselected;
                starView.setImageDrawable(ContextCompat.getDrawable(getActivity()
                        .getApplicationContext(), starDrawable));
                bundle.putBoolean(Constants.TAG_STAR, star);
                flipStar(title, id);

                // Delete card if star is removed and starred only mode is on
                if (!star && starredOnly) {
                    mAdapter.deleteItem(mPosition);
                }
            }
        });
        return card;
    }

    /**
     * Sends a data request to the phone in order to flip the star value in the database.
     */
    private void flipStar(final String title, final long id) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(Constants.REQUEST_PATH);
        final DataMap dataMap = putDataMapReq.getDataMap();
        dataMap.putString(Constants.TAG_TITLE, title);
        dataMap.putLong(TAG_CARD_ID, id);
        dataMap.putLong(Constants.TAG_TIME, new Date().getTime());
        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataMapReq.asPutDataRequest());
    }
}
