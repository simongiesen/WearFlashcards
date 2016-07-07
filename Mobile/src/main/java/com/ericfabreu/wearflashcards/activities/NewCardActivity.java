package com.ericfabreu.wearflashcards.activities;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.data.FlashcardContract.CardSet;
import com.ericfabreu.wearflashcards.data.FlashcardProvider;
import com.ericfabreu.wearflashcards.utils.Constants;

import static com.ericfabreu.wearflashcards.utils.PreferencesHelper.getStarMode;

public class NewCardActivity extends AppCompatActivity {
    private String tableName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get table name and title from CardListFragment
        Bundle bundle = getIntent().getExtras();
        tableName = bundle.getString(Constants.TABLE_NAME);
        setContentView(R.layout.activity_new_card);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setElevation(Constants.TOOLBAR_ELEVATION);
        }
    }

    /**
     * Creates a card and either resets the view or return to SetOverviewActivity
     */
    public void newCard(View view) {
        // Get term and definition from view and trim spaces from start and end
        TextInputEditText text1 = (TextInputEditText) findViewById(R.id.edit_new_term);
        TextInputEditText text2 = (TextInputEditText) findViewById(R.id.edit_new_definition);
        String term = text1.getText().toString().replaceAll("^\\s+|\\s+$", "");
        String definition = text2.getText().toString().replaceAll("^\\s+|\\s+$", "");

        // Return to SetOverviewActivity if the view is empty and 'DONE' was selected
        if (view.getId() == R.id.button_done && term.isEmpty() && definition.isEmpty()) {
            finish();
        }

        // Validate input
        else if (term.isEmpty() || term.matches("[ ]+")) {
            text1.setError(getString(R.string.error_empty_term));
            text1.requestFocus();
        } else if (definition.isEmpty() || definition.matches("[ ]+")) {
            text2.setError(getString(R.string.error_empty_definition));
            text2.requestFocus();
        }

        // Create card
        else {
            FlashcardProvider handle = new FlashcardProvider(getApplicationContext());
            Uri tableUri = Uri.withAppendedPath(CardSet.CONTENT_URI, tableName);
            if (!handle.termAvailable(term, tableUri)) {
                text1.setError(getString(R.string.error_term_taken));
                text1.requestFocus();
                return;
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put(CardSet.TERM, term);
            contentValues.put(CardSet.DEFINITION, definition);
            if (getStarMode(getApplicationContext())) {
                contentValues.put(CardSet.STAR, "1");
            }
            handle.insert(tableUri, contentValues);

            // Check if it should reset view
            if (view.getId() == R.id.button_next) {
                text1.setText(null);
                text2.setText(null);
                text1.requestFocus();
                return;
            }
        }
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Quit activity
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
