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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import com.ericfabreu.wearflashcards.fragments.CardViewFragment;
import com.ericfabreu.wearflashcards.utils.SetInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates CardViews for SetView.
 */
public class StudyAdapter extends FragmentStatePagerAdapter {
    private List<CardViewFragment> cards = new ArrayList<>();
    private Activity mActivity;
    private ViewPager mPager;

    public StudyAdapter(Activity activity, FragmentManager fragmentManager, ViewPager viewPager,
                        SetInfo setInfo) {
        super(fragmentManager);
        mActivity = activity;
        mPager = viewPager;

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
        card.setCardInfo(cardInfo, position);
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

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    public void deleteItem(int position) {
        // Go back to the parent activity if this is the last starred card
        if (cards.size() <= 1) {
            mActivity.finish();
        }

        cards.remove(position);

        // Update the position in the cards after the one being removed
        for (int i = position; i < cards.size(); i++) {
            cards.get(i).setPosition(i);
        }
        notifyDataSetChanged();
        mPager.setCurrentItem(position);
    }
}
