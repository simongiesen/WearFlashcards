package com.ericfabreu.wearflashcards.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.adapters.StudyAdapter;
import com.ericfabreu.wearflashcards.data.FlashcardContract.CardSet;
import com.ericfabreu.wearflashcards.data.FlashcardContract.FolderList;
import com.ericfabreu.wearflashcards.data.FlashcardContract.SetList;
import com.ericfabreu.wearflashcards.data.FlashcardProvider;
import com.ericfabreu.wearflashcards.utils.PreferencesHelper;
import com.ericfabreu.wearflashcards.utils.SetInfo;
import com.thinkincode.utils.views.AutoResizeTextView;

/**
 * Displays cards on the screen.
 */
public class CardViewFragment extends Fragment {
    private StudyAdapter mAdapter;
    private SetInfo.CardInfo mCardInfo;
    private int mPosition;

    public CardViewFragment() {
        super();
    }

    public void setCardInfo(SetInfo.CardInfo cardInfo, int position) {
        mCardInfo = cardInfo;
        mPosition = position;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    public void setAdapter(StudyAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        // Ensure that its objects are not destroyed on rotation
        setRetainInstance(true);

        // Create card and get necessary views
        View card = inflater.inflate(R.layout.item_card_view, container, false);
        FrameLayout frame = (FrameLayout) card.findViewById(R.id.layout_card_view);
        final AutoResizeTextView termView =
                (AutoResizeTextView) frame.findViewById(R.id.text_card_term);
        termView.setMinTextSize(14f);
        final AutoResizeTextView definitionView =
                (AutoResizeTextView) frame.findViewById(R.id.text_card_definition);
        definitionView.setMinTextSize(10f);
        final ImageView starView = (ImageView) frame.findViewById(R.id.image_star_card);
        final int starDrawable = mCardInfo.getStar() ? R.drawable.ic_star_selected
                : R.drawable.ic_star_unselected;
        starView.setImageDrawable(ContextCompat.getDrawable(getContext(), starDrawable));

        // Add term and definition
        termView.setText(mCardInfo.getTerm());
        definitionView.setText(mCardInfo.getDefinition());

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

        starView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FlashcardProvider handle = new FlashcardProvider(getContext());
                final Uri uri = Uri.withAppendedPath(CardSet.CONTENT_URI, mCardInfo.getTableName());
                PreferencesHelper.flipStar(getContext(), handle, uri,
                        mCardInfo.getCardId(), CardSet.STAR);

                // Delete card if star is removed and starred only mode is on
                if (mCardInfo.getTableName() != null) {
                    final boolean starredOnly;
                    if (!mCardInfo.getFolderMode()) {
                        final long tableId = Long.valueOf(mCardInfo.getTableName()
                                .substring(1, mCardInfo.getTableName().length() - 1));
                        starredOnly = PreferencesHelper.getStar(getContext(), handle,
                                SetList.CONTENT_URI, tableId, SetList.STARRED_ONLY);
                    } else {
                        starredOnly = PreferencesHelper.getStar(getContext(), handle,
                                FolderList.CONTENT_URI, mCardInfo.getTableId(),
                                FolderList.STARRED_ONLY);
                    }
                    if (starredOnly) {
                        mAdapter.deleteItem(mPosition);
                        return;
                    }
                }

                // Switch star drawable
                final boolean flag = PreferencesHelper.getStar(getContext(), handle,
                        uri, mCardInfo.getCardId(), CardSet.STAR);
                final int star = flag ? R.drawable.ic_star_selected : R.drawable.ic_star_unselected;
                starView.setImageDrawable(ContextCompat.getDrawable(getContext(), star));
                mCardInfo.flipStar();
                onCreateView(inflater, container, savedInstanceState);
            }
        });
        return card;
    }
}
