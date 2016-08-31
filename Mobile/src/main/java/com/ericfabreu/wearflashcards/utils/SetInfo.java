package com.ericfabreu.wearflashcards.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.ericfabreu.wearflashcards.data.FlashcardContract.CardSet;
import com.ericfabreu.wearflashcards.data.FlashcardContract.FolderEntry;
import com.ericfabreu.wearflashcards.data.FlashcardContract.FolderList;
import com.ericfabreu.wearflashcards.data.FlashcardContract.SetList;
import com.ericfabreu.wearflashcards.data.FlashcardProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A simple class that stores all of the information in a set.
 */
public class SetInfo {
    private boolean mFolder;
    private String mTableName;
    private List<String> mTerms = new ArrayList<>(), mDefinitions = new ArrayList<>();
    private List<Boolean> mStars = new ArrayList<>();
    private List<Long> mIds = new ArrayList<>(), mTableIds = new ArrayList<>();

    public SetInfo(Context context, long tableId, boolean folder) {
        FlashcardProvider provider = new FlashcardProvider(context);
        mFolder = folder;

        // Get information from the database
        mTableName = provider.getTableName(tableId, folder);
        final boolean starredOnly = PreferencesHelper.getStar(context, provider,
                mFolder ? FolderList.CONTENT_URI : SetList.CONTENT_URI, tableId,
                mFolder ? FolderList.STARRED_ONLY : SetList.STARRED_ONLY);

        // Check if it needs to load more than one set
        if (mFolder) {
            Cursor cursor = provider.query(Uri.withAppendedPath(FolderEntry.CONTENT_URI, mTableName),
                    new String[]{FolderEntry.SET_ID}, null, null, null);
            if (cursor != null) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    final long setId = cursor.getLong(cursor.getColumnIndex(FolderEntry.SET_ID));
                    final String setTable = provider.getTableName(setId, false);
                    Cursor set = provider.fetchAllCards(setTable, starredOnly);
                    for (set.moveToFirst(); !set.isAfterLast(); set.moveToNext()) {
                        mTerms.add(set.getString(set.getColumnIndex(CardSet.TERM)));
                        mDefinitions.add(set.getString(set.getColumnIndex(CardSet.DEFINITION)));
                        mStars.add(set.getInt(set.getColumnIndex(CardSet.STAR)) == 1);
                        mIds.add(set.getLong(set.getColumnIndex(CardSet._ID)));
                        mTableIds.add(setId);
                    }
                    set.close();
                }
                cursor.close();
            }
        } else {
            Cursor cursor = provider.fetchAllCards(mTableName, starredOnly);
            if (cursor != null) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    mTerms.add(cursor.getString(cursor.getColumnIndex(CardSet.TERM)));
                    mDefinitions.add(cursor.getString(cursor.getColumnIndex(CardSet.DEFINITION)));
                    mStars.add(cursor.getInt(cursor.getColumnIndex(CardSet.STAR)) == 1);
                    mIds.add(cursor.getLong(cursor.getColumnIndex(CardSet._ID)));
                }
                cursor.close();
            }
        }
    }

    public CardInfo getCardAt(int index) {
        return new CardInfo(mTableName, mTerms.get(index), mDefinitions.get(index),
                mTableIds.size() == 0 ? -1 : mTableIds.get(index),
                mIds.get(index), mStars.get(index), mTableIds.size() > 0);
    }

    /**
     * Uses the length of the term list to tell if the set is empty.
     */
    public boolean isEmpty() {
        return mTerms.isEmpty();
    }

    /**
     * Uses the length of the term list to return the set's length.
     */
    public int size() {
        return mTerms.size();
    }

    /**
     * Flips the terms and definitions.
     */
    public void flipCards() {
        List<String> temp = mTerms;
        mTerms = mDefinitions;
        mDefinitions = temp;
    }

    /**
     * Shuffles the terms, definitions, stars, and ids together if necessary.
     */
    public void shuffleCards() {
        int size = mTerms.size();
        int[] shuffleOrder = getShuffledArray(size);
        List<String> newTerms = new ArrayList<>(), newDefinitions = new ArrayList<>();
        List<Boolean> newStars = new ArrayList<>();
        List<Long> newIds = new ArrayList<>(), newTableIds = new ArrayList<>();

        // Use shuffled int array to ensure that the new terms, definitions, and stars match
        for (int i = 0; i < size; i++) {
            newTerms.add(i, mTerms.get(shuffleOrder[i]));
            newDefinitions.add(i, mDefinitions.get(shuffleOrder[i]));
            newStars.add(i, mStars.get(shuffleOrder[i]));
            newIds.add(i, mIds.get(shuffleOrder[i]));
            if (mFolder) {
                newTableIds.add(i, mTableIds.get(shuffleOrder[i]));
            }
        }

        mTerms = newTerms;
        mDefinitions = newDefinitions;
        mStars = newStars;
        mIds = newIds;
        mTableIds = newTableIds;
    }

    /**
     * Creates an int array of size 'size' in increasing order and shuffles it.
     */
    private int[] getShuffledArray(int size) {
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = i;
        }
        return shuffleArray(array);
    }

    /**
     * Shuffles an int array.
     * http://stackoverflow.com/a/18456998/3522216
     */
    private int[] shuffleArray(int[] array) {
        int index;
        Random random = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            index = random.nextInt(i + 1);
            if (index != i) {
                array[index] ^= array[i];
                array[i] ^= array[index];
                array[index] ^= array[i];
            }
        }
        return array;
    }

    /**
     * A simple container for cards.
     */
    public class CardInfo {
        private String mTable, mTerm, mDefinition;
        private long mTableId, mCardId;
        private boolean mStar, mFolderMode;

        public CardInfo(String table, String term, String definition, long tableId, long cardId,
                        boolean star, boolean folderMode) {
            mTable = table;
            mTerm = term;
            mDefinition = definition;
            mTableId = tableId;
            mCardId = cardId;
            mStar = star;
            mFolderMode = folderMode;
        }

        public String getTableName() {
            return mTable;
        }

        public String getTerm() {
            return mTerm;
        }

        public String getDefinition() {
            return mDefinition;
        }

        public long getTableId() {
            return mTableId;
        }

        public long getCardId() {
            return mCardId;
        }

        public boolean getStar() {
            return mStar;
        }

        public boolean getFolderMode() {
            return mFolderMode;
        }

        public void flipStar() {
            mStar ^= true;
        }
    }
}
