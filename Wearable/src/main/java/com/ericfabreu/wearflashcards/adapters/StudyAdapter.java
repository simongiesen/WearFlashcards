/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ericfabreu.wearflashcards.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.widget.TextView;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.activities.StudyActivity;
import com.ericfabreu.wearflashcards.fragments.CardViewFragment;
import com.ericfabreu.wearflashcards.utils.SetInfo;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates CardViews for StudyActivity.
 */
public class StudyAdapter extends FragmentGridPagerAdapter {
    private final static int COLUMN_COUNT = 1;
    private List<CardViewFragment> cards = new ArrayList<>();
    private GoogleApiClient mGoogleApiClient;
    private StudyActivity mActivity;
    private GridViewPager mPager;

    public StudyAdapter(FragmentManager fragmentManager, StudyActivity activity,
                        GridViewPager pager, GoogleApiClient googleApiClient, SetInfo setInfo) {
        super(fragmentManager);
        mGoogleApiClient = googleApiClient;
        mActivity = activity;
        mPager = pager;

        // Create all cards
        for (int i = 0, n = setInfo.size(); i < n; i++) {
            cards.add(newCard(setInfo.getCardAt(i), i));
        }
    }

    /**
     * Sends term and definition to CardViewFragment and creates a new card.
     */
    private CardViewFragment newCard(SetInfo.CardInfo cardInfo, int position) {
        CardViewFragment card = new CardViewFragment();
        card.setGoogleApiClient(mGoogleApiClient);
        card.setCardInfo(cardInfo, this, position);
        return card;
    }

    @Override
    public Fragment getFragment(int row, int col) {
        return cards.get(row);
    }

    @Override
    public int getRowCount() {
        return cards.size();
    }

    @Override
    public int getColumnCount(int rowNum) {
        return COLUMN_COUNT;
    }

    public void deleteItem(int position) {
        // Display the all cards hidden message if this is the last card
        if (cards.size() <= 1) {
            mPager.removeAllViews();
            mPager.setAdapter(null);
            mActivity.setContentView(R.layout.status_empty_database);
            final TextView empty = (TextView) mActivity.findViewById(R.id.text_empty_status);
            empty.setText(R.string.message_all_cards_hidden);
        }

        cards.remove(position);
        notifyDataSetChanged();

        // Update the position in the cards after the one being removed
        for (int i = position; i < cards.size(); i++) {
            cards.get(i).setPosition(i);
        }
    }
}
