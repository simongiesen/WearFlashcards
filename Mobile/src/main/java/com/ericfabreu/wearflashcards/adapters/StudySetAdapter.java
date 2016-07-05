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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.ericfabreu.wearflashcards.fragments.CardViewFragment;
import com.ericfabreu.wearflashcards.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates CardViews for SetView.
 */
public class StudySetAdapter extends FragmentStatePagerAdapter {
    private List<Fragment> cards = new ArrayList<>();
    private String mTableName;

    public StudySetAdapter(FragmentManager fm, String tableName, List<String> terms,
                           List<String> definitions, List<Boolean> stars, List<Long> ids) {
        super(fm);
        mTableName = tableName;

        // Create all cards
        for (int i = 0, n = terms.size(); i < n; i++) {
            cards.add(newCard(terms.get(i), definitions.get(i), stars.get(i), ids.get(i)));
        }
    }

    /**
     * Sends term and definition to CardViewFragment and creates a new card.
     */
    private Fragment newCard(String term, String definition, boolean star, long id) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TABLE_NAME, mTableName);
        bundle.putString(Constants.TERM, term);
        bundle.putString(Constants.DEFINITION, definition);
        bundle.putBoolean(Constants.STAR, star);
        bundle.putLong(Constants.ID, id);
        CardViewFragment card = new CardViewFragment();
        card.setArguments(bundle);
        return card;
    }

    @Override
    public Fragment getItem(int position) {
        return cards.get(position);
    }

    @Override
    public int getCount() {
        return cards.size();
    }
}
