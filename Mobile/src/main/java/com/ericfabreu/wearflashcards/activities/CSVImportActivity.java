package com.ericfabreu.wearflashcards.activities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.data.FlashcardContract.CardSet;
import com.ericfabreu.wearflashcards.data.FlashcardProvider;
import com.ericfabreu.wearflashcards.utils.Constants;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileReader;
import java.io.IOException;

/**
 * Imports a CSV file into a flashcard set.
 */
public class CSVImportActivity extends AppCompatActivity {
    private static final int CSV_REQUEST_CODE = 278;
    private static final String LOG_TAG = "CSV import";
    private String mTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ensure that a second intent is not called after rotating the device
        if (savedInstanceState == null) {
            Bundle bundle = getIntent().getExtras();
            mTable = bundle.getString(Constants.TAG_TABLE_NAME);

            Intent fileIntent;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                fileIntent = new Intent();
            } else {
                fileIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            }
            fileIntent.setType("text/comma-separated-values");
            fileIntent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(fileIntent, CSV_REQUEST_CODE);
        } else {
            // mTable becomes null after the rotation
            mTable = savedInstanceState.getString(Constants.TAG_TABLE_NAME);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        // Save the table name before the activity is recreated
        savedInstanceState.putString(Constants.TAG_TABLE_NAME, mTable);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == CSV_REQUEST_CODE && resultCode == Activity.RESULT_OK
                && resultData != null) {
            readFile(resultData.getData());
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
                    Toast.makeText(CSVImportActivity.this, insertCount, Toast.LENGTH_SHORT).show();
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
