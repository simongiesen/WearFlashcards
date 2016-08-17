package com.ericfabreu.wearflashcards.activities;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.adapters.StudySetAdapter;
import com.ericfabreu.wearflashcards.data.FlashcardContract.CardSet;
import com.ericfabreu.wearflashcards.data.FlashcardContract.FolderEntry;
import com.ericfabreu.wearflashcards.data.FlashcardContract.FolderList;
import com.ericfabreu.wearflashcards.data.FlashcardContract.SetList;
import com.ericfabreu.wearflashcards.data.FlashcardProvider;
import com.ericfabreu.wearflashcards.utils.Constants;
import com.ericfabreu.wearflashcards.utils.PreferencesHelper;
import com.ericfabreu.wearflashcards.views.VerticalViewPager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StudySetActivity extends AppCompatActivity {
    private static final int MENU_POS_STAR = 1;
    private String tableName, title;
    private long tableId;
    private boolean folder;
    private List<String> terms = new ArrayList<>(), definitions = new ArrayList<>();
    private List<Boolean> stars = new ArrayList<>();
    private List<Long> ids = new ArrayList<>(), tableIds = new ArrayList<>();
    private FlashcardProvider mProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_set);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setElevation(Constants.TOOLBAR_ELEVATION);
        }
        mProvider = new FlashcardProvider(getApplicationContext());

        // Get set or folder information from the parent activity
        Bundle bundle = getIntent().getExtras();
        tableName = bundle.getString(Constants.TAG_TABLE_NAME);
        tableId = bundle.getLong(Constants.TAG_ID);
        title = bundle.getString(Constants.TAG_TITLE);
        folder = bundle.getBoolean(Constants.TAG_FOLDER);
        setTitle(title);

        createCards();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.studying, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final boolean starredOnly = PreferencesHelper.getStar(getApplicationContext(), mProvider,
                folder ? FolderList.CONTENT_URI : SetList.CONTENT_URI, tableId,
                folder ? FolderList.STARRED_ONLY : SetList.STARRED_ONLY);
        if (starredOnly) {
            MenuItem star = menu.getItem(MENU_POS_STAR);
            star.setIcon(R.drawable.ic_star_selected);
        }
        return true;
    }

    /**
     * Creates flashcards using a VerticalViewPager.
     */
    protected void createCards() {
        // Get information from the database
        final boolean starredOnly = PreferencesHelper.getStar(getApplicationContext(), mProvider,
                folder ? FolderList.CONTENT_URI : SetList.CONTENT_URI, tableId,
                folder ? FolderList.STARRED_ONLY : SetList.STARRED_ONLY);
        // Check if it needs to load more than one set
        if (folder) {
            Cursor cursor = mProvider.query(Uri.withAppendedPath(FolderEntry.CONTENT_URI, tableName),
                    new String[]{FolderEntry.SET_ID}, null, null, null);
            if (cursor != null) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    final long setId = cursor.getLong(cursor.getColumnIndex(FolderEntry.SET_ID));
                    final String setTable = mProvider.getTableName(setId, false);
                    Cursor set = mProvider.fetchAllCards(setTable, starredOnly);
                    for (set.moveToFirst(); !set.isAfterLast(); set.moveToNext()) {
                        terms.add(set.getString(set.getColumnIndex(CardSet.TERM)));
                        definitions.add(set.getString(set.getColumnIndex(CardSet.DEFINITION)));
                        stars.add(set.getInt(set.getColumnIndex(CardSet.STAR)) == 1);
                        ids.add(set.getLong(set.getColumnIndex(CardSet._ID)));
                        tableIds.add(setId);
                    }
                    set.close();
                }
                cursor.close();
            }
        } else {
            Cursor cursor = mProvider.fetchAllCards(tableName, starredOnly);
            if (cursor != null) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    terms.add(cursor.getString(cursor.getColumnIndex(CardSet.TERM)));
                    definitions.add(cursor.getString(cursor.getColumnIndex(CardSet.DEFINITION)));
                    stars.add(cursor.getInt(cursor.getColumnIndex(CardSet.STAR)) == 1);
                    ids.add(cursor.getLong(cursor.getColumnIndex(CardSet._ID)));
                }
                cursor.close();
            }
        }

        // Return to the parent activity if all cards have been hidden
        if (terms.isEmpty()) {
            finish();
        }

        // Load set study settings
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (settings.getBoolean(Constants.PREF_KEY_DEFINITION, false)) {
            List<String> temp = terms;
            terms = definitions;
            definitions = temp;
        }
        if (settings.getBoolean(Constants.PREF_KEY_SHUFFLE, false)) {
            shuffleCards();
        }

        final VerticalViewPager pager = (VerticalViewPager) findViewById(R.id.pager_study_set);
        pager.setAdapter(new StudySetAdapter(this, getSupportFragmentManager(), pager, tableName,
                terms, definitions, stars, ids, tableIds, tableId));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
            case R.id.item_study_reload: {
                recreateCards();
                return true;
            }
            // Flip starred only flag and reload cards
            case R.id.item_starred_only: {
                final boolean flag = PreferencesHelper.getStar(getApplicationContext(), mProvider,
                        folder ? FolderList.CONTENT_URI : SetList.CONTENT_URI, tableId,
                        folder ? FolderList.STARRED_ONLY : SetList.STARRED_ONLY);
                final int icon = flag ? R.drawable.ic_star_unselected : R.drawable.ic_star_selected;
                item.setIcon(icon);
                PreferencesHelper.flipStar(getApplicationContext(), mProvider,
                        folder ? FolderList.CONTENT_URI : SetList.CONTENT_URI, tableId,
                        folder ? FolderList.STARRED_ONLY : SetList.STARRED_ONLY);
                recreateCards();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    /**
     * Shuffles the terms, definitions, stars, and ids together if necessary.
     */
    private void shuffleCards() {
        int size = terms.size();
        int[] shuffleOrder = getShuffledArray(size);
        List<String> newTerms = new ArrayList<>(), newDefinitions = new ArrayList<>();
        List<Boolean> newStars = new ArrayList<>();
        List<Long> newIds = new ArrayList<>(), newTableIds = new ArrayList<>();

        // Use shuffled int array to ensure that the new terms, definitions, and stars match
        for (int i = 0; i < size; i++) {
            newTerms.add(i, terms.get(shuffleOrder[i]));
            newDefinitions.add(i, definitions.get(shuffleOrder[i]));
            newStars.add(i, stars.get(shuffleOrder[i]));
            newIds.add(i, ids.get(shuffleOrder[i]));
            if (folder) {
                newTableIds.add(i, tableIds.get(shuffleOrder[i]));
            }
        }

        terms = newTerms;
        definitions = newDefinitions;
        stars = newStars;
        ids = newIds;
        tableIds = newTableIds;
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
     * Discards all current set information and recreates all cards.
     */
    private void recreateCards() {
        terms.clear();
        definitions.clear();
        stars.clear();
        ids.clear();
        tableIds.clear();
        createCards();
    }
}
