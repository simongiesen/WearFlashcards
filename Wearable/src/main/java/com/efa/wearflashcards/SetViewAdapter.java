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
import android.content.Context;
import android.content.res.Resources;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.view.Gravity;

import java.util.ArrayList;
import java.util.List;

/**
 * Constructs fragments as requested by the GridViewPager. For each row a different background is
 * provided. Taken from the GridViewPager sample (https://goo.gl/ZGLbWH)
 */
public class SetViewAdapter extends FragmentGridPagerAdapter {
    private final Context mContext;
    private List<Row> mRows;

    public SetViewAdapter(Context ctx, FragmentManager fm, String[] terms, String[] definitions) {
        super(fm);
        mContext = ctx;
        mRows = new ArrayList<>();

        // Create cards with given data
        for (int i = 0, n = terms.length; i < n; i++) {
            mRows.add(new Row(cardFragment(terms[i], definitions[i])));
        }
    }

    private Fragment cardFragment(String title, String text) {
        Resources res = mContext.getResources();
        CardFragment fragment = CardFragment.create(title, text);

        // Add some extra bottom margin to leave room for the page indicator
        fragment.setCardMarginBottom(
                res.getDimensionPixelSize(R.dimen.card_margin_bottom));
        fragment.setCardGravity(Gravity.TOP);
        return fragment;
    }

    @Override
    public Fragment getFragment(int row, int col) {
        Row adapterRow = mRows.get(row);
        return adapterRow.getColumn(col);
    }

    @Override
    public int getRowCount() {
        return mRows.size();
    }

    @Override
    public int getColumnCount(int rowNum) {
        return mRows.get(rowNum).getColumnCount();
    }

    /**
     * A convenient container for a row of fragments.
     */
    private class Row {
        final List<Fragment> columns = new ArrayList<>();

        public Row(Fragment... fragments) {
            for (Fragment f : fragments) {
                add(f);
            }
        }

        public void add(Fragment f) {
            columns.add(f);
        }

        Fragment getColumn(int i) {
            return columns.get(i);
        }

        public int getColumnCount() {
            return columns.size();
        }
    }
}
