package com.ericfabreu.wearflashcards.activities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.data.FlashcardContract.FolderEntry;
import com.ericfabreu.wearflashcards.data.FlashcardContract.SetList;
import com.ericfabreu.wearflashcards.data.FlashcardProvider;
import com.ericfabreu.wearflashcards.utils.Constants;

public class ImportSetsActivity extends AppCompatActivity {
    private String mTableName;
    private FlashcardProvider mProvider;
    private Cursor mCursor;
    private ListView mListView;
    private boolean[] mImports;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_sets);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setElevation(Constants.TOOLBAR_ELEVATION);
        }

        Bundle bundle = getIntent().getExtras();
        mTableName = bundle.getString(Constants.TAG_TABLE_NAME);
        mProvider = new FlashcardProvider(getApplicationContext());
        mCursor = mProvider.fetchAllSets(mTableName);
        mImports = new boolean[mCursor.getCount()];

        // Create the import list and add the import button at the bottom
        SetsAdapter adapter = new SetsAdapter(this, mCursor, 0);
        mListView = (ListView) findViewById(R.id.list_import_data);
        mListView.setAdapter(adapter);
        FrameLayout importButton = (FrameLayout) getLayoutInflater()
                .inflate(R.layout.item_import_button, mListView, false);
        mListView.addFooterView(importButton);
    }

    /**
     * Adds to or removes the set index from the list to be imported.
     */
    public void flipImportState(View view) {
        mImports[mListView.getPositionForView(view)] ^= true;
    }

    /**
     * Imports the selected sets and returns to FolderOverviewActivity.
     */
    public void importSets(View view) {
        int index = 0;
        for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext(), index++) {
            if (mImports[index]) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(FolderEntry.SET_ID,
                        mCursor.getLong(mCursor.getColumnIndex(SetList._ID)));
                Uri uri = Uri.withAppendedPath(FolderEntry.CONTENT_URI, mTableName);
                mProvider.insert(uri, contentValues);
            }
        }
        mCursor.close();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Quit activity
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * A simple cursor adapter that allows remembering the checkbox checked state and
     * to link the textView's click listener to the correct box.
     */
    private class SetsAdapter extends CursorAdapter {
        public SetsAdapter(Context context, Cursor cursor, int flags) {
            super(context, cursor, flags);
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            final CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox_import_item);
            checkBox.setChecked(mImports[position]);

            // Flip the box checked state when the user taps on the title
            TextView title = (TextView) view.findViewById(R.id.text_import_title);
            title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    flipImportState(view);
                    checkBox.setChecked(mImports[position]);
                }
            });
            return view;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.item_import_sets, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView title = (TextView) view.findViewById(R.id.text_import_title);
            title.setText(cursor.getString(cursor.getColumnIndex(SetList.SET_TITLE)));
        }
    }
}
