package com.ericfabreu.wearflashcards.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.data.FlashcardContract.CardSet;
import com.ericfabreu.wearflashcards.data.FlashcardProvider;
import com.ericfabreu.wearflashcards.utils.Constants;
import com.ericfabreu.wearflashcards.utils.PreferencesHelper;
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
public class CSVImportActivity extends FragmentActivity {
    private static final int CSV_REQUEST_CODE = 278, CSV_REQUEST_CODE_JB = 52;
    private static final String LOG_TAG = "CSV import";
    private String mTable;
    private Uri mUri;

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
            // mTable and mTableId become null after the rotation
            else {
                mTable = savedInstanceState.getString(Constants.TAG_TABLE_NAME);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        // Save the table name and id before the activity is recreated
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
            if (requestCode == CSV_REQUEST_CODE) {
                mUri = resultData.getData();
            } else if (requestCode == CSV_REQUEST_CODE_JB) {
                mUri = Uri.fromFile(new File(resultData
                        .getStringExtra(FilePickerActivity.RESULT_FILE_PATH)));
            } else {
                mUri = null;
            }
            if (mUri != null) {
                new ReadFile().execute();
            }
        } else {
            finish();
        }
    }

    /**
     * Creates flashcards from a CSV file in a background thread.
     */
    private class ReadFile extends AsyncTask<Void, Void, Void> {
        private final ProgressDialog mDialog = new ProgressDialog(CSVImportActivity.this);

        @Override
        protected void onPreExecute() {
            // Display loading message while the file is being processed
            mDialog.setMessage(getString(R.string.message_csv_loading));
            mDialog.setIndeterminate(true);
            mDialog.setCancelable(false);
            mDialog.show();
        }

        protected Void doInBackground(Void... values) {
            ParcelFileDescriptor parcelFileDescriptor = null;
            try {
                // Open the CSV file
                parcelFileDescriptor = getContentResolver().openFileDescriptor(mUri, "r");
                if (parcelFileDescriptor != null) {
                    FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                    FileReader fileReader = new FileReader(fileDescriptor);
                    BufferedReader bufferedReader = new BufferedReader(fileReader);
                    String line;
                    try {
                        FlashcardProvider provider = new FlashcardProvider(getApplicationContext());
                        final Uri table = Uri.withAppendedPath(CardSet.CONTENT_URI, mTable);
                        final String star = PreferencesHelper
                                .getDefaultStar(getApplicationContext());
                        int count = 0;
                        while ((line = bufferedReader.readLine()) != null) {
                            String[] columns = line.split(",");
                            // Skip invalid rows
                            if (columns.length != 2 || (columns[0].trim().length() == 0
                                    || columns[1].trim().length() == 0)) {
                                continue;
                            }

                            // Only import terms that are not already taken
                            if (provider.termAvailable(columns[0].trim(), table)) {
                                ContentValues cv = new ContentValues();
                                cv.put(CardSet.TERM, columns[0].trim());
                                cv.put(CardSet.DEFINITION, columns[1].trim());
                                cv.put(CardSet.STAR, star);
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
            return null;
        }

        @Override
        protected void onPostExecute(Void value) {
            if (mDialog.isShowing()) {
                mDialog.dismiss();
                finish();
            }
        }
    }
}
