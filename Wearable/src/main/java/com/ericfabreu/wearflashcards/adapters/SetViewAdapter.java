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
import android.os.Bundle;
import android.support.wearable.view.FragmentGridPagerAdapter;

import com.ericfabreu.wearflashcards.fragments.CardViewFragment;
import com.ericfabreu.wearflashcards.utils.Constants;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates CardViews for SetViewActivity.
 */
public class SetViewAdapter extends FragmentGridPagerAdapter {
    private List<Fragment> cards = new ArrayList<>();
    private GoogleApiClient mGoogleApiClient;

    public SetViewAdapter(FragmentManager fragmentManager, GoogleApiClient googleApiClient,
                          String title, ArrayList<String> terms, ArrayList<String> definitions,
                          long[] ids, ArrayList<Integer> stars) {
        super(fragmentManager);
        mGoogleApiClient = googleApiClient;

        // Create all cards
        for (int i = 0, n = terms.size(); i < n; i++) {
            cards.add(newCard(title, terms.get(i), definitions.get(i), ids[i], stars.get(i) == 1));
        }
    }

    /**
     * Sends term and definition to CardViewFragment and creates a new card.
     */
    private CardViewFragment newCard(String title, String term, String definition,
                                     long id, boolean star) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TITLE, title);
        bundle.putString(Constants.TERM, term);
        bundle.putString(Constants.DEFINITION, definition);
        bundle.putLong(Constants.ID, id);
        bundle.putBoolean(Constants.STAR, star);
        CardViewFragment card = new CardViewFragment();
        card.setArguments(bundle);
        card.setGoogleApiClient(mGoogleApiClient);
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
        return Constants.COLUMN_COUNT;
    }
}
