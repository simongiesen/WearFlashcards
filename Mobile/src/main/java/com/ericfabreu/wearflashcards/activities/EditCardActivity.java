package com.ericfabreu.wearflashcards.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.data.FlashcardContract;
import com.ericfabreu.wearflashcards.data.FlashcardProvider;
import com.ericfabreu.wearflashcards.utils.Constants;

/**
 * Edits a card.
 */
public class EditCardActivity extends AppCompatActivity {
    private String term;
    private String definition;
    private String tableName;
    private String setTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get term and definition from SetOverviewActivity
        Bundle bundle = getIntent().getExtras();
        term = bundle.getString(Constants.TERM);
        definition = bundle.getString(Constants.DEFINITION);
        tableName = bundle.getString(Constants.TABLE_NAME);
        setTitle = bundle.getString(Constants.TITLE);

        // Create view
        setContentView(R.layout.activity_edit_card);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setElevation(Constants.TOOLBAR_ELEVATION);
        }
        setTitle(getString(R.string.edit_card));
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Add term and definition to EditText and move cursor to the end of the term
        EditText tView = (EditText) findViewById(R.id.edit_term);
        EditText defView = (EditText) findViewById(R.id.edit_definition);
        tView.setText(term);
        tView.setSelection(term.length());
        defView.setText(definition);
    }

    /**
     * Edits a card and returns to SetOverviewActivity.
     */
    public void editCard(View view) {
        EditText tView = (EditText) findViewById(R.id.edit_term);
        EditText defView = (EditText) findViewById(R.id.edit_definition);
        String newTerm = tView.getText().toString();
        String newDef = defView.getText().toString();

        // Check if term and definition have changed
        if (term.equals(newTerm) && definition.equals(newDef)) {
            onBackPressed();
            return;
        }

        // Check if term is blank
        if (TextUtils.isEmpty(newTerm)) {
            tView.setError(getString(R.string.empty_term));
            return;
        }

        // Check if definition is blank
        if (TextUtils.isEmpty(newDef)) {
            defView.setError(getString(R.string.empty_definition));
            return;
        }

        // Edit card
        FlashcardProvider handle = new FlashcardProvider(getApplicationContext());
        Uri tableUri = Uri.withAppendedPath(FlashcardContract.CardSet.CONTENT_URI, tableName);
        if (!handle.editCard(tableUri, term, newTerm, newDef)) {
            tView.setError(getString(R.string.term_taken));
            return;
        }


        // Pass set title and table name back to SetOverviewActivity and return
        onBackPressed();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Pass table name back to SetOverviewActivity if the toolbar back button is clicked
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        // Pass set title and table name back to SetOverviewActivity
        Intent intent = new Intent(this, SetOverviewActivity.class);
        intent.putExtra(Constants.TABLE_NAME, tableName);
        intent.putExtra(Constants.TITLE, setTitle);
        startActivity(intent);
    }
}