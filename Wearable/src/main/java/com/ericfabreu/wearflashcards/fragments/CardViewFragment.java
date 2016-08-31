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
import com.ericfabreu.wearflashcards.adapters.StudyAdapter;
import com.ericfabreu.wearflashcards.utils.Constants;
import com.ericfabreu.wearflashcards.utils.SetInfo;
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
    private StudyAdapter mAdapter;
    private SetInfo.CardInfo mCardInfo;

    public void setGoogleApiClient(GoogleApiClient googleApiClient) {
        mGoogleApiClient = googleApiClient;
    }

    public void setCardInfo(SetInfo.CardInfo cardInfo, StudyAdapter adapter, int position) {
        mCardInfo = cardInfo;
        mAdapter = adapter;
        mPosition = position;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
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
        final int starDrawable = mCardInfo.getStar() ? R.drawable.ic_star_selected
                : R.drawable.ic_star_unselected;
        starView.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(),
                starDrawable));

        // Add text and click listeners
        termView.setText(mCardInfo.getTerm());
        definitionView.setText(mCardInfo.getDefinition());
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
                final boolean star = !mCardInfo.getStar();
                final int starDrawable = star ? R.drawable.ic_star_selected
                        : R.drawable.ic_star_unselected;
                starView.setImageDrawable(ContextCompat.getDrawable(getActivity()
                        .getApplicationContext(), starDrawable));
                mCardInfo.flipStar();
                if (mCardInfo.getFolderMode()) {
                    flipStar(mCardInfo.getTableId(), mCardInfo.getCardId());
                } else {
                    flipStar(mCardInfo.getTitle(), mCardInfo.getCardId());
                }

                // Delete card if star is removed and starred only mode is on
                if (!star && mCardInfo.getStarredOnly()) {
                    mAdapter.deleteItem(mPosition);
                }
            }
        });
        return card;
    }

    /**
     * Sends a data request to the phone in order to flip the star value in the database.
     */
    private void flipStar(final String tableName, final long id) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(Constants.REQUEST_PATH);
        final DataMap dataMap = putDataMapReq.getDataMap();
        dataMap.putInt(Constants.TAG_MODE, 4);
        dataMap.putString(Constants.TAG_TITLE, tableName);
        dataMap.putLong(TAG_CARD_ID, id);
        dataMap.putLong(Constants.TAG_TIME, new Date().getTime());
        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataMapReq.asPutDataRequest());
    }

    private void flipStar(long tableId, final long id) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(Constants.REQUEST_PATH);
        final DataMap dataMap = putDataMapReq.getDataMap();
        dataMap.putInt(Constants.TAG_MODE, 5);
        dataMap.putLong(Constants.TAG_TABLE_ID, tableId);
        dataMap.putLong(TAG_CARD_ID, id);
        dataMap.putLong(Constants.TAG_TIME, new Date().getTime());
        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataMapReq.asPutDataRequest());
    }
}
