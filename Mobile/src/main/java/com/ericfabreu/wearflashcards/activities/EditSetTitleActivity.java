package com.ericfabreu.wearflashcards.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.data.FlashcardProvider;
import com.ericfabreu.wearflashcards.utils.Constants;

/**
 * Edits a set's title.
 */
public class EditSetTitleActivity extends AppCompatActivity {
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get set title from MainActivity
        Bundle bundle = getIntent().getExtras();
        title = bundle.getString(Constants.TAG_TITLE);

        // Create view
        setContentView(R.layout.activity_edit_set_title);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setElevation(Constants.TOOLBAR_ELEVATION);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Add title to EditText and move cursor to the end
        TextInputEditText text = (TextInputEditText) findViewById(R.id.edit_set_title);
        text.setText(title);
        text.setSelection(title.length());
    }

    /**
     * Edits a set's title and returns to MainActivity.
     */
    public void editSetTitle(View view) {
        TextInputEditText text = (TextInputEditText) findViewById(R.id.edit_set_title);
        String newTitle = text.getText().toString();

        // Check if title has changed
        if (title.equals(newTitle)) {
            returnMain();
            return;
        }

        // Check if title is empty
        if (newTitle.isEmpty() || newTitle.matches("[ ]+")) {
            text.setError(getString(R.string.error_empty_title));
            return;
        }

        // Check if new title is available
        FlashcardProvider handle = new FlashcardProvider(getApplicationContext());
        if (!handle.renameSet(title, newTitle)) {
            text.setError(getString(R.string.error_title_taken));
            return;
        }
        returnMain();
    }

    /**
     * Returns to MainActivity.
     */
    private void returnMain() {
        Intent main = new Intent(this, MainActivity.class);
        startActivity(main);
        finish();
    }
}
