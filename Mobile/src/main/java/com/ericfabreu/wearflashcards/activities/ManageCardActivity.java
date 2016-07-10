package com.ericfabreu.wearflashcards.activities;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.data.FlashcardContract.CardSet;
import com.ericfabreu.wearflashcards.data.FlashcardProvider;
import com.ericfabreu.wearflashcards.utils.Constants;
import com.ericfabreu.wearflashcards.utils.PreferencesHelper;

public class ManageCardActivity extends AppCompatActivity {
    private String tableName, term, definition;
    private boolean editing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_card);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setElevation(Constants.TOOLBAR_ELEVATION);
        }

        // Get card info from CardListFragment
        Bundle bundle = getIntent().getExtras();
        tableName = bundle.getString(Constants.TAG_TABLE_NAME);
        editing = bundle.getBoolean(Constants.TAG_EDITING_MODE);
        if (editing) {
            term = bundle.getString(Constants.TAG_TERM);
            definition = bundle.getString(Constants.TAG_DEFINITION);
            setTitle(getString(R.string.title_activity_edit_card));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Fill in the text fields and change the buttons if in editing mode
        if (editing) {
            TextInputEditText termView = (TextInputEditText) findViewById(R.id.edit_manage_term);
            TextInputEditText definitionView =
                    (TextInputEditText) findViewById(R.id.edit_manage_definition);
            termView.setText(term);
            termView.setSelection(term.length());
            definitionView.setText(definition);
            findViewById(R.id.button_manage_next).setVisibility(View.GONE);
            ((Button) findViewById(R.id.button_manage_done)).setText(getText(R.string.button_save));
        }
    }

    /**
     * Creates a card and either resets the view or return to SetOverviewActivity
     */
    public void manageCard(View view) {
        // Get term and definition from view and trim spaces from start and end
        TextInputEditText termView = (TextInputEditText) findViewById(R.id.edit_manage_term);
        TextInputEditText definitionView = (TextInputEditText)
                findViewById(R.id.edit_manage_definition);
        final String newTerm = termView.getText().toString().replaceAll("^\\s+|\\s+$", "");
        final String newDefinition = definitionView.getText().toString()
                .replaceAll("^\\s+|\\s+$", "");

        // Return to SetOverviewActivity if the view is empty and 'DONE' was selected
        if (!editing && view.getId() == R.id.button_manage_done &&
                newTerm.isEmpty() && newDefinition.isEmpty()) {
            finish();
        }

        // Validate input
        else if (editing && (term.equals(newTerm) && definition.equals(newDefinition))) {
            finish();
        } else if (newTerm.isEmpty() || newTerm.matches("[ ]+")) {
            termView.setError(getString(R.string.error_empty_term));
            termView.requestFocus();
        } else if (newDefinition.isEmpty() || newDefinition.matches("[ ]+")) {
            definitionView.setError(getString(R.string.error_empty_definition));
            definitionView.requestFocus();
        }

        // Create or edit card
        else {
            FlashcardProvider handle = new FlashcardProvider(getApplicationContext());
            Uri tableUri = Uri.withAppendedPath(CardSet.CONTENT_URI, tableName);

            if (editing && !handle.editCard(tableUri, term, newTerm, newDefinition)) {
                termView.setError(getString(R.string.error_term_taken));
            } else if (!editing) {
                if (!handle.termAvailable(newTerm, tableUri)) {
                    termView.setError(getString(R.string.error_term_taken));
                    termView.requestFocus();
                    return;
                }
                ContentValues contentValues = new ContentValues();
                contentValues.put(CardSet.TERM, newTerm);
                contentValues.put(CardSet.DEFINITION, newDefinition);
                contentValues.put(CardSet.STAR,
                        PreferencesHelper.getStarMode(getApplicationContext()));
                handle.insert(tableUri, contentValues);

                // Check if it should reset view
                if (view.getId() == R.id.button_manage_next) {
                    termView.setText(null);
                    definitionView.setText(null);
                    termView.requestFocus();
                    return;
                }
            }
            finish();
        }
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
