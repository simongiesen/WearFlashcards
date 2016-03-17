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

package com.efa.wearflashcards;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.wearable.view.FragmentGridPagerAdapter;

/**
 * Generates CardViews for SetView.
 */
public class SetViewAdapter extends FragmentGridPagerAdapter {
    private String[] terms;
    private String[] definitions;

    public SetViewAdapter(FragmentManager fm, String[] tms, String[] defs) {
        super(fm);

        // Save terms and definitions
        terms = tms;
        definitions = defs;
    }

    @Override
    public Fragment getFragment(int row, int col) {
        // Send term and definition to CardView and create a new card
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TERM, terms[row]);
        bundle.putString(Constants.DEFINITION, definitions[row]);
        CardView card = new CardView();
        card.setArguments(bundle);
        return card;
    }

    @Override
    public int getRowCount() {
        return terms.length;
    }

    @Override
    public int getColumnCount(int rowNum) {
        return Constants.COLUMN_COUNT;
    }
}
