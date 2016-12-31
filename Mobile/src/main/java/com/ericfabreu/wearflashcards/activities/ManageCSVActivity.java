package com.ericfabreu.wearflashcards.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.regex.Pattern;

/**
 * Imports a CSV file into a flashcard set.
 */
public class ManageCSVActivity extends FragmentActivity {
    private static final int CSV_REQUEST_CODE = 278, CSV_REQUEST_CODE_JB = 52;
    private static final String LOG_TAG = "CSV manager";
    private String mTable, mTitle;
    private Uri mUri;
    private boolean mReadMode, mFolderMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the app needs to request permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    CSV_REQUEST_CODE);
        } else {
            // Ensure that a second intent is not called after rotating the device
            if (savedInstanceState == null) {
                loadFileBrowser();
            }
            // Non-static variables become null after the rotation
            else {
                mTable = savedInstanceState.getString(Constants.TAG_TABLE_NAME);
                mTitle = savedInstanceState.getString(Constants.TAG_TITLE);
                mReadMode = savedInstanceState.getBoolean(Constants.TAG_READING_MODE);
                mFolderMode = savedInstanceState.getBoolean(Constants.TAG_FOLDER);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        // Save non-static variables before the activity is recreated
        savedInstanceState.putString(Constants.TAG_TABLE_NAME, mTable);
        savedInstanceState.putString(Constants.TAG_TITLE, mTitle);
        savedInstanceState.putBoolean(Constants.TAG_READING_MODE, mReadMode);
        savedInstanceState.putBoolean(Constants.TAG_FOLDER, mFolderMode);
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
     * Checks if external storage is available for read and write.
     */
    public boolean isExternalStorageWritable() {
        final String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }


    /**
     * Loads the file browser so that the user can pick a CSV file to import.
     */
    private void loadFileBrowser() {
        // Check that we can read/write files
        if (!isExternalStorageWritable()) {
            Toast.makeText(ManageCSVActivity.this, R.string.message_csv_unavailable,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        final Bundle bundle = getIntent().getExtras();
        mTable = bundle.getString(Constants.TAG_TABLE_NAME);
        mTitle = bundle.getString(Constants.TAG_TITLE);
        mReadMode = bundle.getBoolean(Constants.TAG_READING_MODE);
        mFolderMode = bundle.getBoolean(Constants.TAG_FOLDER);

        // Remove non-alphanumeric characters from the title
        if (mTitle != null) {
            mTitle = mTitle.replaceAll("[^A-Za-z0-9 .-]", "");
        }

        final Intent fileIntent;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        } else if (mReadMode) {
            fileIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        } else {
            fileIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        }
        fileIntent.putExtra(Intent.EXTRA_TITLE, mTitle);
        fileIntent.setType("text/comma-separated-values");
        fileIntent.addCategory(Intent.CATEGORY_OPENABLE);

        // Use MaterialFilePicker if JellyBean device does not have a file manager
        final PackageManager packageManager = getPackageManager();
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
                new ManageFile().execute(mReadMode);
            }
        } else {
            finish();
        }
    }

    /**
     * Creates flashcards from a CSV file in a background thread.
     */
    private class ManageFile extends AsyncTask<Boolean, Void, Void> {
        private final ProgressDialog mDialog = new ProgressDialog(ManageCSVActivity.this);

        @Override
        protected void onPreExecute() {
            // Display loading message while the file is being processed
            mDialog.setMessage(getString(R.string.message_csv_reading));
            mDialog.setIndeterminate(true);
            mDialog.setCancelable(false);
            mDialog.show();
        }

        /**
         * Generates a set from a CSV file.
         */
        private ParcelFileDescriptor loadCSV() {
            ParcelFileDescriptor parcelFileDescriptor = null;
            try {
                // Open the CSV file
                parcelFileDescriptor = getContentResolver().openFileDescriptor(mUri, "r");
                if (parcelFileDescriptor != null) {
                    final FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                    final FileReader fileReader = new FileReader(fileDescriptor);
                    final BufferedReader bufferedReader = new BufferedReader(fileReader);
                    String line;
                    try {
                        final FlashcardProvider provider =
                                new FlashcardProvider(getApplicationContext());
                        final Uri table = Uri.withAppendedPath(CardSet.CONTENT_URI, mTable);
                        final String star = PreferencesHelper
                                .getDefaultStar(getApplicationContext());
                        final String sep = PreferencesHelper.getSeparator(getApplicationContext());
                        int count = 0;
                        while ((line = bufferedReader.readLine()) != null) {
                            String[] columns = line.split(sep);
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
                                Toast.makeText(ManageCSVActivity.this, insertCount,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Failed to load file.", e);
            }
            return parcelFileDescriptor;
        }

        /**
         * Generates a CSV file from a set.
         */
        private ParcelFileDescriptor writeCSV() {
            ParcelFileDescriptor parcelFileDescriptor = null;
            try {
                // Open the CSV file
                parcelFileDescriptor = getContentResolver().openFileDescriptor(mUri, "w");
                if (parcelFileDescriptor != null) {
                    final FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                    final FileWriter fileWriter = new FileWriter(fileDescriptor);
                    final BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                    try {
                        final FlashcardProvider handle =
                                new FlashcardProvider(getApplicationContext());
                        final String sep = PreferencesHelper.getSeparator(getApplicationContext());
                        final MessageFormat form = new MessageFormat("{0}" + sep + "{1}");
                        int count = 0;

                        // Save folder to CSV
                        if (mFolderMode) {
                            final Cursor sets = handle.fetchFolderSets(mTable);
                            while (sets.moveToNext()) {
                                final String table = handle.getTableName(sets.getLong(0), false);
                                final Cursor cards = handle.fetchAllCards(table, false);
                                while (cards.moveToNext()) {
                                    final Object[] line = {cards.getString(1), cards.getString(2)};
                                    bufferedWriter.write(form.format(line));
                                    bufferedWriter.newLine();
                                    count++;
                                }
                                cards.close();
                            }
                            bufferedWriter.close();
                            sets.close();
                        }

                        // Save set to CSV
                        else {
                            final Cursor cards = handle.fetchAllCards(mTable, false);
                            while (cards.moveToNext()) {
                                final Object[] line = {cards.getString(1), cards.getString(2)};
                                bufferedWriter.write(form.format(line));
                                bufferedWriter.newLine();
                                count++;
                            }
                            bufferedWriter.close();
                            cards.close();
                        }

                        final String insertCount = count == 0
                                ? getString(R.string.message_csv_export_zero)
                                : getResources()
                                .getQuantityString(R.plurals.message_csv_export, count, count);

                        // Toasts cannot be created in a background thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ManageCSVActivity.this, insertCount,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Failed to load file.", e);
            }
            return parcelFileDescriptor;
        }

        protected Void doInBackground(Boolean... readMode) {
            ParcelFileDescriptor parcelFileDescriptor = null;

            // Read CSV or export set depending on the mode
            try {
                parcelFileDescriptor = readMode[0] ? loadCSV() : writeCSV();
            }

            // Handle IO errors
            catch (Exception e) {
                Log.e(LOG_TAG, "Failed to " + (readMode[0] ? "load" : "write") + " file.", e);
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
