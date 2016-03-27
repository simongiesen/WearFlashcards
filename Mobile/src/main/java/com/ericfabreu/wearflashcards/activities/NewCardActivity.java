package com.ericfabreu.wearflashcards.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.data.FlashcardContract.CardSet;
import com.ericfabreu.wearflashcards.data.FlashcardProvider;
import com.ericfabreu.wearflashcards.utils.Constants;

public class NewCardActivity extends AppCompatActivity {
    private String table_name;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get table name and title from CardListFragment
        Bundle bundle = getIntent().getExtras();
        table_name = bundle.getString(Constants.TABLE_NAME);
        title = bundle.getString(Constants.TITLE);
        setContentView(R.layout.activity_new_card);

        // Put the set title in the toolbar_main
        setTitle(getString(R.string.create_card));
    }

    // Create a card and either return to MainActivity or reset view
    public void newCard(View view) {
        EditText text1 = (EditText) findViewById(R.id.edit_new_term);
        EditText text2 = (EditText) findViewById(R.id.edit_new_definition);
        String term = text1.getText().toString();
        String definition = text2.getText().toString();

        // Return to SetOverviewActivity if the view is empty and 'done' was selected
        if (view.getId() == R.id.button_done &&
                TextUtils.isEmpty(term) &&
                TextUtils.isEmpty(definition)) {
            onBackPressed();
            finish();
            return;
        }

        // Check if term is blank
        if (TextUtils.isEmpty(term)) {
            text1.setError(getString(R.string.empty_term));
            text1.requestFocus();
            return;
        }

        // Check if definition is blank
        if (TextUtils.isEmpty(definition)) {
            text2.setError(getString(R.string.empty_definition));
            text2.requestFocus();
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
        if (view.getId() == R.id.button_next) {
            text1.setText(null);
            text2.setText(null);
            text1.requestFocus();
            return;
        }

        // Pass table name back to SetOverviewActivity and return
        onBackPressed();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Pass table name back to SetOverviewActivity if the toolbar_main back button is clicked
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
        intent.putExtra(Constants.TABLE_NAME, table_name);
        intent.putExtra(Constants.TITLE, title);
        startActivity(intent);
    }
}
