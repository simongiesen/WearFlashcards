package com.ericfabreu.wearflashcards.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.data.FlashcardProvider;
import com.ericfabreu.wearflashcards.utils.Constants;

public class ManageSetFolderActivity extends AppCompatActivity {
    private String mTitle;
    private boolean mFolder, mEditing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_set_folder);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setElevation(Constants.TOOLBAR_ELEVATION);
        }

        Bundle bundle = getIntent().getExtras();
        mFolder = bundle.getBoolean(Constants.TAG_FOLDER);
        mEditing = bundle.getBoolean(Constants.TAG_EDITING_MODE);

        // Use the folder and editing settings to load the proper strings
        TextInputLayout titleText = (TextInputLayout) findViewById(R.id.layout_manage_title);
        if (mFolder) {
            titleText.setHint(getString(R.string.hint_folder_title));
        } else {
            titleText.setHint(getString(R.string.hint_set_title));
        }
        if (mEditing) {
            mTitle = bundle.getString(Constants.TAG_TITLE);
            setTitle(getString(R.string.title_activity_edit_title));
        } else {
            setTitle(getString(mFolder ? R.string.title_activity_create_folder
                    : R.string.title_activity_create_set));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if activity needs to be opened in editing mode
        if (mEditing) {
            // Add title to EditText and move cursor to the end
            TextInputEditText text = (TextInputEditText) findViewById(R.id.edit_manage_title);
            text.setText(mTitle);
            text.setSelection(mTitle.length());

            // Rename the create button to save
            ((Button) findViewById(R.id.button_manage_set_folder))
                    .setText(getText(R.string.button_save));
        }

        // Remove CSV button if it is in editing or folder mode
        if (mEditing || mFolder) {
            findViewById(R.id.button_import_csv).setVisibility(View.GONE);
        }
    }

    /**
     * Creates a new set or folder and returns to MainActivity.
     */
    public void manageSetFolder(View view) {
        // Get title and trim spaces from the start and end
        TextInputEditText text = (TextInputEditText) findViewById(R.id.edit_manage_title);
        final String newTitle = text.getText().toString().replaceAll("^\\s+|\\s+$", "");

        // Check if title has changed (if in editing mode)
        if (mEditing && newTitle.equals(mTitle)) {
            finish();
            return;
        }

        // Check if title is empty
        if (newTitle.isEmpty() || newTitle.matches("[ ]+")) {
            text.setError(getString(R.string.error_empty_title));
            return;
        }

        // Check if title is available
        FlashcardProvider provider = new FlashcardProvider(getApplicationContext());
        if ((mEditing && !provider.renameMember(mTitle, newTitle, mFolder)) ||
                (!mEditing && !provider.newTable(newTitle, mFolder))) {
            text.setError(getString(R.string.error_title_taken));
            return;
        }

        // Check if it should import a CSV file
        if (view.getId() == R.id.button_import_csv) {
            Intent intent = new Intent(this, CSVImportActivity.class);
            intent.putExtra(Constants.TAG_TABLE_NAME, provider.getTableName(newTitle, false));
            startActivityForResult(intent, Constants.REQUEST_CODE_CREATE);
            return;
        }

        // Return to the main screen
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Return to MainActivity after CSVImportActivity closes
        if (requestCode == Constants.REQUEST_CODE_CREATE) {
            this.finish();
        }
    }
}
