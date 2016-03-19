package com.ericfabreu.wearflashcards;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ericfabreu.wearflashcards.data.FlashcardContract;
import com.ericfabreu.wearflashcards.data.FlashcardProvider;

import java.util.ArrayList;
import java.util.Random;

public class SetOverview extends AppCompatActivity {
    private String[] terms;
    private String[] definitions;
    private String tableName;
    private String title;
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_overview);

        // Load settings
        settings = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);

        // Get table name from SetListFragment or NewCard and pass it to CardListFragment
        Bundle bundle = getIntent().getExtras();
        tableName = bundle.getString(Constants.TABLE_NAME);
        title = bundle.getString(Constants.TITLE);
        CardListFragment frag = new CardListFragment();
        frag.setArguments(bundle);

        // Use the set title as the activity title
        setTitle(title);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.set_overview_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SetOverview.this, NewCard.class);
                intent.putExtra(Constants.TABLE_NAME, tableName);
                intent.putExtra(Constants.TITLE, title);
                startActivity(intent);
            }
        });

        // Load flashcard sets
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.set_overview_layout, frag)
                    .commit();
        }

        // Get terms and definitions from the database
        FlashcardProvider handle = new FlashcardProvider(getApplicationContext());
        String tableName = handle.getTableName(title);
        Cursor cursor = handle.fetchAllCards(tableName);

        // Put terms and definitions into string arrays
        ArrayList<String> columnArray1 = new ArrayList<>();
        ArrayList<String> columnArray2 = new ArrayList<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            columnArray1.add(cursor.getString(cursor.getColumnIndex(FlashcardContract.CardSet.TERM)));
            columnArray2.add(cursor.getString(cursor.getColumnIndex(FlashcardContract.CardSet.DEFINITION)));
        }
        terms = columnArray1.toArray(new String[columnArray1.size()]);
        definitions = columnArray2.toArray(new String[columnArray2.size()]);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Get menu items
        MenuItem shuffle = menu.getItem(Constants.SHUFFLE_POS);
        MenuItem termFirst = menu.getItem(Constants.DEF_FIRST_POS);

        // Restore settings
        shuffle.setChecked(settings.getBoolean(Constants.SHUFFLE, false));
        termFirst.setChecked(settings.getBoolean(Constants.DEF_FIRST, false));

        // Hide StudySet button if there are no cards
        if (terms.length == 0) {
            menu.removeItem(R.id.study_set_button);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Flip the item's checked state and save settings
        if (id == R.id.shuffle) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(Constants.SHUFFLE, !item.isChecked());
            editor.apply();
            item.setChecked(!item.isChecked());
            return true;
        } else if (id == R.id.definition_first) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(Constants.DEF_FIRST, !item.isChecked());
            editor.apply();
            return true;
        } else if (id == R.id.study_set_button) {
            // Apply settings
            if (settings.getBoolean(Constants.DEF_FIRST, false)) {
                String[] temp = terms;
                terms = definitions;
                definitions = temp;
            }
            if (settings.getBoolean(Constants.SHUFFLE, false)) {
                shuffleCards();
            }

            // Send terms and definitions to StudySet
            Intent intent = new Intent(SetOverview.this, StudySet.class);
            intent.putExtra(Constants.TERM, terms);
            intent.putExtra(Constants.DEFINITION, definitions);
            intent.putExtra(Constants.TITLE, title);
            intent.putExtra(Constants.TABLE_NAME, tableName);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Return to Main if back key is pressed instead of going to previous activity
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
    }

    private void shuffleCards() {
        int size = terms.length;
        int[] shuffleOrder = getShuffledArray(size);
        String[] newTerms = new String[size];
        String[] newDefs = new String[size];

        // Use shuffled int array to ensure that the new terms and definitions match
        for (int i = 0; i < size; i++) {
            newTerms[i] = terms[shuffleOrder[i]];
            newDefs[i] = definitions[shuffleOrder[i]];
        }

        terms = newTerms;
        definitions = newDefs;
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