package com.ericfabreu.wearflashcards.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

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

    // Create an empty set and return to MainActivity
    public void newSet(View view) {
        EditText text = (EditText) findViewById(R.id.edit_new_set_title);
        String title = text.getText().toString();

        // Check if title is empty
        if (TextUtils.isEmpty(title)) {
            text.setError(getString(R.string.empty_title));
            return;
        }

        // Check if title is valid (must have at least one letter or number)
        if (!title.matches("[a-zA-Z0-9 ]+")) {
            text.setError(getString(R.string.invalid_title));
            return;
        }

        // Check if title is available
        FlashcardProvider handle = new FlashcardProvider(getApplicationContext());
        if (!handle.newSetTable(title)) {
            text.setError(getString(R.string.title_taken));
            return;
        }

        // Return to activity_main screen
        Intent main = new Intent(this, MainActivity.class);
        startActivity(main);
        finish();
    }
}
