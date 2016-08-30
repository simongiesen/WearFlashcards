package com.ericfabreu.wearflashcards.activities;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.data.FlashcardContract.CardSet;
import com.ericfabreu.wearflashcards.data.FlashcardProvider;
import com.ericfabreu.wearflashcards.utils.Constants;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Imports a CSV file into a flashcard set.
 */
public class CSVImportActivity extends AppCompatActivity {
    private static final int CSV_REQUEST_CODE = 278, CSV_REQUEST_CODE_JB = 52;
    private static final String LOG_TAG = "CSV import";
    private String mTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the app needs to request permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    CSV_REQUEST_CODE);
        } else {
            // Ensure that a second intent is not called after rotating the device
            if (savedInstanceState == null) {
                loadFileBrowser();
            }
            // mTable becomes null after the rotation
            else {
                mTable = savedInstanceState.getString(Constants.TAG_TABLE_NAME);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        // Save the table name before the activity is recreated
        savedInstanceState.putString(Constants.TAG_TABLE_NAME, mTable);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == CSV_REQUEST_CODE) {
            // Permission to read files was granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadFileBrowser();
            } else {
                finish();
            }
        }
    }

    /**
     * Loads the file browser so that the user can pick a CSV file to import.
     */
    private void loadFileBrowser() {
        Bundle bundle = getIntent().getExtras();
        mTable = bundle.getString(Constants.TAG_TABLE_NAME);

        Intent fileIntent;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            fileIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        }
        fileIntent.setType("text/comma-separated-values");
        fileIntent.addCategory(Intent.CATEGORY_OPENABLE);

        // Use MaterialFilePicker if JellyBean device does not have a file manager
        PackageManager packageManager = getPackageManager();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT
                && fileIntent.resolveActivity(packageManager) == null) {
            new MaterialFilePicker()
                    .withActivity(this)
                    .withRequestCode(CSV_REQUEST_CODE_JB)
                    .withFilter(Pattern.compile(".*\\.csv$"))
                    .withFilterDirectories(false)
                    .withHiddenFiles(false)
                    .start();
        } else {
            startActivityForResult(fileIntent, CSV_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent resultData) {
        if (resultCode == Activity.RESULT_OK && resultData != null) {
            // Get file URI
            final Uri uri;
            if (requestCode == CSV_REQUEST_CODE) {
                uri = resultData.getData();
            } else if (requestCode == CSV_REQUEST_CODE_JB) {
                uri = Uri.fromFile(new File(resultData
                        .getStringExtra(FilePickerActivity.RESULT_FILE_PATH)));
            } else {
                uri = null;
            }

            // Load cards in a background thread
            if (uri != null) {
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        readFile(uri);
                    }
                };
                thread.start();
            }
        }

        // Return to the parent activity
        finish();
    }

    /**
     * Creates flashcards from a CSV file.
     */
    public void readFile(Uri uri) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            // Open the CSV file
            parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
            if (parcelFileDescriptor != null) {
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                FileReader fileReader = new FileReader(fileDescriptor);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String line;
                try {
                    FlashcardProvider provider = new FlashcardProvider(getApplicationContext());
                    int count = 0;
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] columns = line.split(",");
                        // Skip invalid rows
                        if (columns.length != 2 || (columns[0].trim().length() == 0
                                || columns[1].trim().length() == 0)) {
                            continue;
                        }

                        // Only import terms that are not already taken
                        Uri table = Uri.withAppendedPath(CardSet.CONTENT_URI, mTable);
                        if (provider.termAvailable(columns[0].trim(), table)) {
                            ContentValues cv = new ContentValues();
                            cv.put(CardSet.TERM, columns[0].trim());
                            cv.put(CardSet.DEFINITION, columns[1].trim());
                            provider.insert(table, cv);
                            count++;
                        }
                    }
                    final String insertCount = count == 0
                            ? getString(R.string.message_csv_import_zero)
                            : getResources()
                            .getQuantityString(R.plurals.message_csv_import, count, count);

                    // Toasts cannot be created in a background thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CSVImportActivity.this, insertCount,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load file.", e);
        } finally {
            try {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error closing ParcelFile Descriptor");
            }
        }
    }
}
