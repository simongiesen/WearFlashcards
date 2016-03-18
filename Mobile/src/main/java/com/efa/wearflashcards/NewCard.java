package com.efa.wearflashcards;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.efa.wearflashcards.data.FlashcardContract.CardSet;
import com.efa.wearflashcards.data.FlashcardProvider;

public class NewCard extends AppCompatActivity {
    private String table_name;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get table name and title from CardListFragment
        Bundle bundle = getIntent().getExtras();
        table_name = bundle.getString(Constants.TABLE_NAME);
        title = bundle.getString(Constants.TITLE);
        setContentView(R.layout.new_card);

        // Put the set title in the toolbar
        setTitle(getString(R.string.create_card));
    }

    // Create a card and either return to Main or reset view
    public void newCard(View view) {
        EditText text1 = (EditText) findViewById(R.id.new_term_text);
        EditText text2 = (EditText) findViewById(R.id.new_definition_text);
        String term = text1.getText().toString();
        String definition = text2.getText().toString();

        // Return to SetOverview if the view is empty and 'done' was selected
        if (view.getId() == R.id.done &&
                TextUtils.isEmpty(term) &&
                TextUtils.isEmpty(definition)) {
            onBackPressed();
            finish();
            return;
        }

        // Check if term is blank
        if (TextUtils.isEmpty(term)) {
            text1.setError(getString(R.string.empty_term));
            return;
        }

        // Check if definition is blank
        if (TextUtils.isEmpty(definition)) {
            text2.setError(getString(R.string.empty_definition));
            return;
        }

        // Check if the term already exists in the stack
        FlashcardProvider handle = new FlashcardProvider();
        Uri tableUri = Uri.withAppendedPath(CardSet.CONTENT_URI, table_name);
        if (handle.termExists(term, tableUri)) {
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
        if (view.getId() == R.id.next) {
            text1.setText(null);
            text2.setText(null);
            text1.requestFocus();
            return;
        }

        // Pass table name back to SetOverview and return
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
        // Pass table name back to SetOverview
        Intent intent = new Intent(this, SetOverview.class);
        intent.putExtra(Constants.TABLE_NAME, table_name);
        intent.putExtra(Constants.TITLE, title);
        startActivity(intent);
    }
}
