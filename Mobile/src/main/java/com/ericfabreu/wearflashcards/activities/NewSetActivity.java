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

public class NewSetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_set);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setElevation(Constants.TOOLBAR_ELEVATION);
        }
        setTitle(getString(R.string.create_set));
    }

    /**
     * Creates a new set and returns to MainActivity
     */
    public void newSet(View view) {
        // Get title and trim spaces from the start and end
        TextInputEditText text = (TextInputEditText) findViewById(R.id.edit_new_set_title);
        String title = text.getText().toString().replaceAll("^\\s+|\\s+$", "");

        // Check if title is empty
        if (title.isEmpty() || title.matches("[ ]+")) {
            text.setError(getString(R.string.empty_title));
            return;
        }

        // Check if title is available
        FlashcardProvider handle = new FlashcardProvider(getApplicationContext());
        if (!handle.newSetTable(title)) {
            text.setError(getString(R.string.title_taken));
            return;
        }

        // Return to the main screen
        Intent main = new Intent(this, MainActivity.class);
        startActivity(main);
        finish();
    }
}
