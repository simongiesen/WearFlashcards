package com.ericfabreu.wearflashcards.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.data.FlashcardContract.CardSet;
import com.ericfabreu.wearflashcards.data.FlashcardProvider;
import com.ericfabreu.wearflashcards.utils.Constants;

public class NewCardActivity extends AppCompatActivity {
    private String tableName, title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get table name and title from CardListFragment
        Bundle bundle = getIntent().getExtras();
        tableName = bundle.getString(Constants.TABLE_NAME);
        title = bundle.getString(Constants.TITLE);
        setContentView(R.layout.activity_new_card);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setElevation(Constants.TOOLBAR_ELEVATION);
        }

        // Put the set title in the toolbar
        setTitle(getString(R.string.create_card));
    }

    /**
     * Creates a card and either resets the view or return to SetOverviewActivity
     */
    public void newCard(View view) {
        // Get term and definition from view and trim spaces from start and end
        EditText text1 = (EditText) findViewById(R.id.edit_new_term);
        EditText text2 = (EditText) findViewById(R.id.edit_new_definition);
        String term = text1.getText().toString().replaceAll("^\\s+|\\s+$", "");
        String definition = text2.getText().toString().replaceAll("^\\s+|\\s+$", "");

        // Return to SetOverviewActivity if the view is empty and 'DONE' was selected
        if (view.getId() == R.id.button_done && term.isEmpty() && definition.isEmpty()) {
            onBackPressed();
            finish();
            return;
        }

        // Check if term is blank
        if (term.isEmpty() || term.matches("[ ]+")) {
            text1.setError(getString(R.string.empty_term));
            text1.requestFocus();
            return;
        }

        // Check if definition is blank
        if (definition.isEmpty() || definition.matches("[ ]+")) {
            text2.setError(getString(R.string.empty_definition));
            text2.requestFocus();
            return;
        }

        // Check if the term already exists in the stack
        FlashcardProvider handle = new FlashcardProvider(getApplicationContext());
        Uri tableUri = Uri.withAppendedPath(CardSet.CONTENT_URI, tableName);
        if (!handle.termAvailable(term, tableUri)) {
            text1.setError(getString(R.string.term_taken));
            text1.requestFocus();
            return;
        }

        // Insert card into stack
        ContentValues contentValues = new ContentValues();
        contentValues.put(CardSet.TERM, term);
        contentValues.put(CardSet.DEFINITION, definition);
        handle.insert(tableUri, contentValues);

        // Check if it should reset view
        if (view.getId() == R.id.button_next) {
            text1.setText(null);
            text2.setText(null);
            text1.requestFocus();
            return;
        }
        onBackPressed();
        finish();
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
}
