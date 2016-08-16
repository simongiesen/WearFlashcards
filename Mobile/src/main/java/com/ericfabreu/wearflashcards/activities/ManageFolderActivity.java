package com.ericfabreu.wearflashcards.activities;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.data.FlashcardProvider;
import com.ericfabreu.wearflashcards.utils.Constants;

public class ManageFolderActivity extends AppCompatActivity {
    private String title;
    private boolean editing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        editing = bundle.getBoolean(Constants.TAG_EDITING_MODE);
        if (editing) {
            title = bundle.getString(Constants.TAG_TITLE);
            setTitle(getApplicationContext().getString(R.string.title_activity_edit_set));
        }

        setContentView(R.layout.activity_manage_folder);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setElevation(Constants.TOOLBAR_ELEVATION);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if activity needs to be opened in editing mode
        if (editing) {
            // Add title to EditText and move cursor to the end
            TextInputEditText text = (TextInputEditText) findViewById(R.id.edit_folder_title);
            text.setText(title);
            text.setSelection(title.length());

            // Rename the create button to save
            ((Button) findViewById(R.id.button_import_sets)).setText(getText(R.string.button_save));
        }
    }

    /**
     * Creates a new set and returns to MainActivity
     */
    public void manageFolder(View view) {
        // Get title and trim spaces from the start and end
        TextInputEditText text = (TextInputEditText) findViewById(R.id.edit_folder_title);
        final String newTitle = text.getText().toString().replaceAll("^\\s+|\\s+$", "");

        // Check if title has changed (if in editing mode)
        if (editing && newTitle.equals(title)) {
            finish();
            return;
        }

        // Check if title is empty
        if (newTitle.isEmpty() || newTitle.matches("[ ]+")) {
            text.setError(getString(R.string.error_empty_title));
            return;
        }

        // Check if title is available
        FlashcardProvider handle = new FlashcardProvider(getApplicationContext());
        if ((editing && !handle.renameMember(title, newTitle, true)) ||
                (!editing && !handle.newTable(newTitle, true))) {
            text.setError(getString(R.string.error_title_taken));
            return;
        }

        // Return to the main screen
        finish();
    }
}
