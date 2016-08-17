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

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.ericfabreu.wearflashcards.data.FlashcardProvider;
import com.ericfabreu.wearflashcards.fragments.CardViewFragment;
import com.ericfabreu.wearflashcards.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates CardViews for SetView.
 */
public class StudySetAdapter extends FragmentStatePagerAdapter {
    private List<CardViewFragment> cards = new ArrayList<>();
    private String mTableName;
    private long mTableId;
    private boolean mFolderMode;
    private Activity mActivity;
    private ViewPager mPager;

    public StudySetAdapter(Activity activity, FragmentManager fragmentManager, ViewPager viewPager,
                           String tableName, List<String> terms, List<String> definitions,
                           List<Boolean> stars, List<Long> ids, List<Long> tableIds, long tableId) {
        super(fragmentManager);
        mActivity = activity;
        mPager = viewPager;
        mTableName = tableName;
        mFolderMode = tableIds.size() > 0;
        mTableId = tableId;

        // Create all cards
        for (int i = 0, n = terms.size(); i < n; i++) {
            cards.add(newCard(terms.get(i), definitions.get(i), stars.get(i), ids.get(i),
                    tableIds.size() == 0 ? -1 : tableIds.get(i), i));
        }
    }

    /**
     * Sends term and definition to CardViewFragment and creates a new card.
     */
    private CardViewFragment newCard(String term, String definition, boolean star,
                                     long id, long tableId, int position) {
        Bundle bundle = new Bundle();
        FlashcardProvider provider = new FlashcardProvider(mActivity.getApplicationContext());

        // Use the tableId to get the table name if this was started inside a folder
        bundle.putString(Constants.TAG_TABLE_NAME, tableId > 0 ?
                provider.getTableName(tableId, false) : mTableName);
        bundle.putString(Constants.TAG_FOLDER, mTableName);
        bundle.putString(Constants.TAG_TERM, term);
        bundle.putString(Constants.TAG_DEFINITION, definition);
        bundle.putBoolean(Constants.TAG_STAR, star);
        bundle.putBoolean(Constants.TAG_FOLDER_MODE, mFolderMode);
        bundle.putLong(Constants.TAG_ID, id);
        bundle.putLong(Constants.TAG_FOLDER_ID, mTableId);
        CardViewFragment card = new CardViewFragment();
        card.setArguments(bundle);
        card.setPosition(position);
        card.setAdapter(this);
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

    public void deleteItem(int position) {
        // Go back to the parent activity if this is the last starred card
        if (cards.size() <= 1) {
            mActivity.finish();
        }
        mPager.setAdapter(null);
        cards.remove(position);

        // Update the position in the cards after the one being removed
        for (int i = position; i < cards.size(); i++) {
            cards.get(i).setPosition(i);
        }

        // Reset the adapter and go to the next card
        mPager.setAdapter(this);
        mPager.setCurrentItem(position);
    }
}
