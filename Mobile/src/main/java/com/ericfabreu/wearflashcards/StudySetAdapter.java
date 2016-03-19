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

package com.ericfabreu.wearflashcards;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates CardViews for SetView.
 */
public class StudySetAdapter extends FragmentStatePagerAdapter {
    private List<Fragment> cards = new ArrayList<>();

    public StudySetAdapter(FragmentManager fm, String[] terms, String[] definitions) {
        super(fm);

        // Create all cards
        for (int i = 0, n = terms.length; i < n; i++) {
            cards.add(newCard(terms[i], definitions[i]));
        }
    }

    /**
     * Sends term and definition to CardView and creates a new card.
     */
    private Fragment newCard(String term, String definition) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TERM, term);
        bundle.putString(Constants.DEFINITION, definition);
        CardView card = new CardView();
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