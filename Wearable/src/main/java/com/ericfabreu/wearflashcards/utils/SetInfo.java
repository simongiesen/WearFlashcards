package com.ericfabreu.wearflashcards.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A simple class that stores all of the information in a set.
 */
public class SetInfo {
    private String mTitle;
    private boolean mStarredOnly, mFolderMode;
    private List<String> mTerms = new ArrayList<>(), mDefinitions = new ArrayList<>();
    private ArrayList<Integer> mStars = new ArrayList<>();
    private long[] mIds, mTableIds;

    public SetInfo(String title, boolean starredOnly, List<String> terms, List<String> definitions,
                   long[] ids, long[] tableIds, ArrayList<Integer> stars) {
        mTitle = title;
        mStarredOnly = starredOnly;
        mTerms = terms;
        mDefinitions = definitions;
        mIds = ids;
        mTableIds = tableIds;
        mStars = stars;
        mFolderMode = mTableIds != null;
    }

    public CardInfo getCardAt(int index) {
        return new CardInfo(mTitle, mTerms.get(index), mDefinitions.get(index),
                mFolderMode ? mTableIds[index] : -1,
                mIds[index], mStars.get(index) == 1);
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
        ArrayList<Integer> newStars = new ArrayList<>();
        long[] newIds = new long[mIds.length],
                newTableIds = mTableIds != null ? new long[mTableIds.length] : null;

        // Use shuffled int array to ensure that the new terms, definitions, and stars match
        for (int i = 0; i < size; i++) {
            newTerms.add(i, mTerms.get(shuffleOrder[i]));
            newDefinitions.add(i, mDefinitions.get(shuffleOrder[i]));
            newStars.add(i, mStars.get(shuffleOrder[i]));
            newIds[i] = mIds[shuffleOrder[i]];
            if (newTableIds != null) {
                newTableIds[i] = mTableIds[shuffleOrder[i]];
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
        private String mTitle, mTerm, mDefinition;
        private long mTableId, mCardId;
        private boolean mStar;

        public CardInfo(String title, String term, String definition, long tableId, long cardId,
                        boolean star) {
            mTitle = title;
            mTerm = term;
            mDefinition = definition;
            mTableId = tableId;
            mCardId = cardId;
            mStar = star;
        }

        public String getTitle() {
            return mTitle;
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

        public boolean getStarredOnly() {
            return mStarredOnly;
        }

        public boolean getFolderMode() {
            return mFolderMode;
        }

        public void flipStar() {
            mStar ^= true;
        }
    }
}
