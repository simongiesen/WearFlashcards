package com.ericfabreu.wearflashcards.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.adapters.StudySetAdapter;
import com.ericfabreu.wearflashcards.data.FlashcardContract.CardSet;
import com.ericfabreu.wearflashcards.data.FlashcardProvider;
import com.ericfabreu.wearflashcards.utils.Constants;
import com.ericfabreu.wearflashcards.views.VerticalViewPager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StudySetActivity extends AppCompatActivity {
    private String title, tableName;
    private List<String> terms = new ArrayList<>(), definitions = new ArrayList<>();
    private List<Boolean> stars = new ArrayList<>();
    private List<Long> ids = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_set);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setElevation(Constants.TOOLBAR_ELEVATION);
        }

        // Get title and table name from SetOverviewActivity
        Bundle bundle = getIntent().getExtras();
        title = bundle.getString(Constants.TITLE);
        tableName = bundle.getString(Constants.TABLE_NAME);
        setTitle(title);

        createCards();
    }

    /**
     * Creates flashcards using a VerticalViewPager.
     */
    protected void createCards() {
        // Get information from the database
        FlashcardProvider handle = new FlashcardProvider(getApplicationContext());
        Cursor cursor = handle.fetchAllCards(tableName);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            terms.add(cursor.getString(cursor.getColumnIndex(CardSet.TERM)));
            definitions.add(cursor.getString(cursor.getColumnIndex(CardSet.DEFINITION)));
            stars.add(cursor.getInt(cursor.getColumnIndex(CardSet.STAR)) == 1);
            ids.add(cursor.getLong(cursor.getColumnIndex(CardSet._ID)));
        }

        // Load set study settings
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (settings.getBoolean(Constants.PREF_KEY_DEFINITION_FIRST, false)) {
            List<String> temp = terms;
            terms = definitions;
            definitions = temp;
        }
        if (settings.getBoolean(Constants.PREF_KEY_SHUFFLE, false)) {
            shuffleCards();
        }

        final VerticalViewPager pager = (VerticalViewPager) findViewById(R.id.pager_study_set);
        pager.setAdapter(new StudySetAdapter(getSupportFragmentManager(), tableName, terms, definitions, stars, ids));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Pass table name back to SetOverviewActivity if the main back button is clicked
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        // Pass table name back to SetOverviewActivity
        Intent intent = new Intent(this, SetOverviewActivity.class);
        intent.putExtra(Constants.TABLE_NAME, tableName);
        intent.putExtra(Constants.TITLE, title);
        startActivity(intent);
    }

    /**
     * Shuffles the terms, definitions, stars, and ids together if necessary.
     */
    private void shuffleCards() {
        int size = terms.size();
        int[] shuffleOrder = getShuffledArray(size);
        List<String> newTerms = new ArrayList<>(), newDefinitions = new ArrayList<>();
        List<Boolean> newStars = new ArrayList<>();
        List<Long> newIds = new ArrayList<>();

        // Use shuffled int array to ensure that the new terms, definitions, and stars match
        for (int i = 0; i < size; i++) {
            newTerms.add(i, terms.get(shuffleOrder[i]));
            newDefinitions.add(i, definitions.get(shuffleOrder[i]));
            newStars.add(i, stars.get(shuffleOrder[i]));
            newIds.add(i, ids.get(shuffleOrder[i]));
        }
        terms = newTerms;
        definitions = newDefinitions;
        stars = newStars;
        ids = newIds;
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
}
