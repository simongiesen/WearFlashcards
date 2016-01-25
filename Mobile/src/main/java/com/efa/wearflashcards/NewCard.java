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
        table_name = bundle.getString("table_name");
        title = bundle.getString("title");
        setContentView(R.layout.activity_new_card);

        // Put the set title in the toolbar
        setTitle(getString(R.string.create_card));
    }

    // Create an empty set and return to MainActivity
    public void newCard(View view) {
        EditText text1 = (EditText) findViewById(R.id.new_term_text);
        EditText text2 = (EditText) findViewById(R.id.new_definition_text);
        String term = text1.getText().toString();
        String definition = text2.getText().toString();

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

        // Insert card into stack
        FlashcardProvider handle = new FlashcardProvider();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CardSet.TERM, term);
        contentValues.put(CardSet.DEFINITION, definition);
        handle.insert(Uri.withAppendedPath(CardSet.CONTENT_URI, table_name), contentValues);

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
        intent.putExtra("table_name", table_name);
        intent.putExtra("title", title);
        startActivity(intent);
    }
}