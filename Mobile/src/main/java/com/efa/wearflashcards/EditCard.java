package com.efa.wearflashcards;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.efa.wearflashcards.data.FlashcardContract;
import com.efa.wearflashcards.data.FlashcardProvider;

/**
 * Edits a card.
 */
public class EditCard extends AppCompatActivity {
    private String term;
    private String definition;
    private String tableName;
    private String setTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get term and definition from SetOverview
        Bundle bundle = getIntent().getExtras();
        term = bundle.getString(Constants.TERM);
        definition = bundle.getString(Constants.DEFINITION);
        tableName = bundle.getString(Constants.TABLE_NAME);
        setTitle = bundle.getString(Constants.TITLE);

        // Create view
        setContentView(R.layout.edit_card);
        setTitle(getString(R.string.edit_card));
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Add term and definition to EditText and move cursor to the end of the term
        EditText tView = (EditText) findViewById(R.id.edit_term_text);
        EditText defView = (EditText) findViewById(R.id.edit_definition_text);
        tView.setText(term);
        tView.setSelection(term.length());
        defView.setText(definition);
    }

    /**
     * Edits a card and returns to SetOverview.
     */
    public void editCard(View view) {
        EditText tView = (EditText) findViewById(R.id.edit_term_text);
        EditText defView = (EditText) findViewById(R.id.edit_definition_text);
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
        FlashcardProvider handle = new FlashcardProvider();
        Uri tableUri = Uri.withAppendedPath(FlashcardContract.CardSet.CONTENT_URI, tableName);
        if (!handle.editCard(tableUri, term, newTerm, newDef)) {
            tView.setError(getString(R.string.term_taken));
            return;
        }


        // Pass set title and table name back to SetOverview and return
        onBackPressed();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Pass table name back to SetOverview if the toolbar back button is clicked
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        // Pass set title and table name back to SetOverview
        Intent intent = new Intent(this, SetOverview.class);
        intent.putExtra(Constants.TABLE_NAME, tableName);
        intent.putExtra(Constants.TITLE, setTitle);
        startActivity(intent);
    }
}
