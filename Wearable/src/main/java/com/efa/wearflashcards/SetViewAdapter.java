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
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.util.Log;

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
        Log.d("Row: ", String.valueOf(row));
        return CardView.create(terms[row], definitions[row]);
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
