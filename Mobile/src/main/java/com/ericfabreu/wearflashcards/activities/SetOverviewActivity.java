package com.ericfabreu.wearflashcards.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.data.FlashcardContract;
import com.ericfabreu.wearflashcards.data.FlashcardProvider;
import com.ericfabreu.wearflashcards.fragments.CardListFragment;
import com.ericfabreu.wearflashcards.utils.Constants;

import java.util.ArrayList;
import java.util.Random;

public class SetOverviewActivity extends AppCompatActivity {
    private String tableName, title;
    private String[] terms, definitions;
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_overview);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setElevation(Constants.TOOLBAR_ELEVATION);
        }

        // Load settings
        settings = PreferenceManager.getDefaultSharedPreferences(this);

        // Get table name from the caller activity and pass it to CardListFragment
        Bundle bundle = getIntent().getExtras();
        tableName = bundle.getString(Constants.TABLE_NAME);
        title = bundle.getString(Constants.TITLE);
        CardListFragment frag = new CardListFragment();
        frag.setArguments(bundle);

        setTitle(title);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_set_overview);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SetOverviewActivity.this, NewCardActivity.class);
                intent.putExtra(Constants.TABLE_NAME, tableName);
                intent.putExtra(Constants.TITLE, title);
                startActivity(intent);
            }
        });

        // Load flashcard sets
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.layout_set_overview, frag)
                    .commit();
        }

        // Get terms and definitions from the database and put them into arrays
        FlashcardProvider handle = new FlashcardProvider(getApplicationContext());
        String tableName = handle.getTableName(title);
        Cursor cursor = handle.fetchAllCards(tableName);
        ArrayList<String> columnArray1 = new ArrayList<>();
        ArrayList<String> columnArray2 = new ArrayList<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            columnArray1.add(cursor.getString(cursor
                    .getColumnIndex(FlashcardContract.CardSet.TERM)));
            columnArray2.add(cursor.getString(cursor
                    .getColumnIndex(FlashcardContract.CardSet.DEFINITION)));
        }
        terms = columnArray1.toArray(new String[columnArray1.size()]);
        definitions = columnArray2.toArray(new String[columnArray2.size()]);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.shared, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Load settings
        MenuItem shuffle = menu.getItem(Constants.SHUFFLE_POS);
        MenuItem termFirst = menu.getItem(Constants.DEF_FIRST_POS);
        shuffle.setChecked(settings.getBoolean(Constants.PREF_KEY_SHUFFLE, false));
        termFirst.setChecked(settings.getBoolean(Constants.PREF_KEY_DEFINITION_FIRST, false));

        // Hide set study button if there are no cards
        if (terms.length == 0) {
            menu.removeItem(R.id.item_study_set);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Flip the item's checked state and save settings
        if (id == R.id.item_shuffle) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(Constants.PREF_KEY_SHUFFLE, !item.isChecked());
            editor.apply();
            item.setChecked(!item.isChecked());
            return true;
        } else if (id == R.id.item_definition_first) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(Constants.PREF_KEY_DEFINITION_FIRST, !item.isChecked());
            editor.apply();
            return true;
        }

        // Load set study settings
        else if (id == R.id.item_study_set) {
            if (settings.getBoolean(Constants.PREF_KEY_DEFINITION_FIRST, false)) {
                String[] temp = terms;
                terms = definitions;
                definitions = temp;
            }
            if (settings.getBoolean(Constants.PREF_KEY_SHUFFLE, false)) {
                shuffleCards();
            }

            // Send terms and definitions to StudySetActivity
            Intent intent = new Intent(SetOverviewActivity.this, StudySetActivity.class);
            intent.putExtra(Constants.TERM, terms);
            intent.putExtra(Constants.DEFINITION, definitions);
            intent.putExtra(Constants.TITLE, title);
            intent.putExtra(Constants.TABLE_NAME, tableName);
            startActivity(intent);
            return true;
        } else if (id == R.id.item_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent, Constants.REQUEST_CODE_SETTINGS);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Return to MainActivity if back key is pressed instead of going to previous activity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Refresh activity with the proper sort order when SettingsActivity is closed
        if (requestCode == Constants.REQUEST_CODE_SETTINGS) {
            Intent refresh = new Intent(this, getClass());
            refresh.putExtra(Constants.TABLE_NAME, tableName);
            refresh.putExtra(Constants.TITLE, title);
            startActivity(refresh);
            this.finish();
        }
    }

    private void shuffleCards() {
        int size = terms.length;
        int[] shuffleOrder = getShuffledArray(size);
        String[] newTerms = new String[size];
        String[] newDefinitions = new String[size];

        // Use shuffled int array to ensure that the new terms and definitions match
        for (int i = 0; i < size; i++) {
            newTerms[i] = terms[shuffleOrder[i]];
            newDefinitions[i] = definitions[shuffleOrder[i]];
        }
        terms = newTerms;
        definitions = newDefinitions;
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
