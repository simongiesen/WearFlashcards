package com.ericfabreu.wearflashcards.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.data.FlashcardContract.CardSet;
import com.ericfabreu.wearflashcards.data.FlashcardContract.FolderList;
import com.ericfabreu.wearflashcards.data.FlashcardContract.SetList;
import com.ericfabreu.wearflashcards.data.FlashcardProvider;
import com.ericfabreu.wearflashcards.fragments.CardListFragment;
import com.ericfabreu.wearflashcards.utils.Constants;
import com.ericfabreu.wearflashcards.utils.PreferencesHelper;
import com.melnykov.fab.FloatingActionButton;

public class SetOverviewActivity extends AppCompatActivity {
    private String tableName, title, folderTable;
    private long tableId, folderId;
    private SharedPreferences settings;
    private FlashcardProvider mProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_overview);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(Constants.TOOLBAR_ELEVATION);
        }

        // Load settings
        settings = PreferenceManager.getDefaultSharedPreferences(this);

        // Get table name from the caller activity and pass it to CardListFragment
        Bundle bundle = getIntent().getExtras();
        tableName = bundle.getString(Constants.TAG_TABLE_NAME);
        title = bundle.getString(Constants.TAG_TITLE);
        tableId = bundle.getLong(Constants.TAG_ID);
        folderTable = bundle.getString(Constants.TAG_FOLDER, null);
        folderId = bundle.getLong(Constants.TAG_FOLDER_ID, -1);

        setTitle(title);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_set_overview);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SetOverviewActivity.this, ManageCardActivity.class);
                intent.putExtra(Constants.TAG_TABLE_NAME, tableName);
                intent.putExtra(Constants.TAG_TITLE, title);
                intent.putExtra(Constants.TAG_EDITING_MODE, false);
                startActivityForResult(intent, Constants.REQUEST_CODE_CREATE);
            }
        });

        mProvider = new FlashcardProvider(getApplicationContext());

        // Load flashcard sets
        if (savedInstanceState == null) {
            startCardListFragment(null);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Load the starred only setting
        final Switch starredOnly = (Switch) findViewById(R.id.switch_starred_only);
        if (folderTable == null) {
            starredOnly.setChecked(PreferencesHelper.getStar(getApplicationContext(), mProvider,
                    SetList.CONTENT_URI, tableId, SetList.STARRED_ONLY));
        } else {
            starredOnly.setChecked(PreferencesHelper.getStar(getApplicationContext(), mProvider,
                    FolderList.CONTENT_URI, folderId, FolderList.STARRED_ONLY));
        }

        // Call the onClickListener if the user slides the switch or taps on it
        starredOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                startCardListFragment(starredOnly);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.shared, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Show CSV import and export buttons
        menu.getItem(Constants.MENU_POS_CSV_IMPORT).setVisible(true);
        menu.getItem(Constants.MENU_POS_CSV_EXPORT).setVisible(true);

        // Load settings
        final MenuItem shuffle = menu.getItem(Constants.MENU_POS_SHUFFLE);
        final MenuItem termFirst = menu.getItem(Constants.MENU_POS_DEFINITION);
        shuffle.setChecked(settings.getBoolean(Constants.PREF_KEY_SHUFFLE, false));
        termFirst.setChecked(settings.getBoolean(Constants.PREF_KEY_DEFINITION, false));

        // Hide set study button and the starred only bar if there are no cards to display
        final Uri tableUri = Uri.withAppendedPath(CardSet.CONTENT_URI, tableName);
        Cursor cursor = mProvider.query(tableUri,
                new String[]{CardSet._ID},
                null,
                null,
                null);
        if (cursor == null || cursor.getCount() == 0) {
            menu.removeItem(R.id.item_study_set);
            findViewById(R.id.layout_starred_only).setVisibility(View.GONE);
        } else {
            final boolean starredOnly;
            if (folderTable == null) {
                starredOnly = PreferencesHelper.getStar(getApplicationContext(),
                        mProvider, SetList.CONTENT_URI, tableId, SetList.STARRED_ONLY);
            } else {
                starredOnly = PreferencesHelper.getStar(getApplicationContext(), mProvider,
                        FolderList.CONTENT_URI, folderId, FolderList.STARRED_ONLY);
            }
            final int starredCount = mProvider.getCardCount(tableUri, true);
            if (starredOnly && starredCount == 0) {
                menu.removeItem(R.id.item_study_set);
            }
            findViewById(R.id.layout_starred_only).setVisibility(View.VISIBLE);
            cursor.close();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Flip the item's checked state and save settings
            case R.id.item_shuffle: {
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(Constants.PREF_KEY_SHUFFLE, !item.isChecked());
                editor.apply();
                item.setChecked(!item.isChecked());
                return true;
            }
            case R.id.item_definition_first: {
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(Constants.PREF_KEY_DEFINITION, !item.isChecked());
                editor.apply();
                return true;
            }
            // Launch StudyActivity
            case R.id.item_study_set: {
                Intent intent = new Intent(this, StudyActivity.class);
                intent.putExtra(Constants.TAG_TITLE, title);
                intent.putExtra(Constants.TAG_TABLE_NAME, tableName);
                intent.putExtra(Constants.TAG_ID, tableId);
                intent.putExtra(Constants.TAG_FOLDER, false);
                startActivityForResult(intent, Constants.REQUEST_CODE_STUDY);
                return true;
            }
            // Launch ManageFileActivity
            case R.id.item_csv_import: {
                Intent intent = new Intent(this, ManageFileActivity.class);
                intent.putExtra(Constants.TAG_TABLE_NAME, tableName);
                intent.putExtra(Constants.TAG_TITLE, title);
                intent.putExtra(Constants.TAG_READING_MODE, true);
                intent.putExtra(Constants.TAG_FOLDER, false);
                startActivityForResult(intent, Constants.REQUEST_CODE_CREATE);
                return true;
            }
            case R.id.item_csv_export: {
                Intent intent = new Intent(this, ManageFileActivity.class);
                intent.putExtra(Constants.TAG_TABLE_NAME, tableName);
                intent.putExtra(Constants.TAG_TITLE, title);
                intent.putExtra(Constants.TAG_READING_MODE, false);
                intent.putExtra(Constants.TAG_FOLDER, false);
                startActivityForResult(intent, Constants.REQUEST_CODE_CREATE);
                return true;
            }
            // Launch SettingsActivity
            case R.id.item_settings: {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, Constants.REQUEST_CODE_SETTINGS);
                return true;
            }
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Refresh activity with the proper sort order when a child activity is closed
        if (requestCode == Constants.REQUEST_CODE_SETTINGS ||
                requestCode == Constants.REQUEST_CODE_EDIT ||
                requestCode == Constants.REQUEST_CODE_STUDY ||
                requestCode == Constants.REQUEST_CODE_CREATE) {
            Intent refresh = new Intent(this, getClass());
            refresh.putExtra(Constants.TAG_TABLE_NAME, tableName);
            refresh.putExtra(Constants.TAG_TITLE, title);
            refresh.putExtra(Constants.TAG_ID, tableId);
            refresh.putExtra(Constants.TAG_FOLDER, folderTable);
            refresh.putExtra(Constants.TAG_FOLDER_ID, folderId);
            startActivity(refresh);
            this.finish();
        }
    }

    /**
     * Refreshes the CardListFragment when the starred only setting changes.
     */
    public void startCardListFragment(View view) {
        final String fragTagCardList = "cardListFragment";

        // Check if the flag needs to be flipped
        if (view != null) {
            if (folderTable == null) {
                PreferencesHelper.flipStar(getApplicationContext(), mProvider,
                        SetList.CONTENT_URI, tableId, SetList.STARRED_ONLY);
            } else {
                PreferencesHelper.flipStar(getApplicationContext(), mProvider,
                        FolderList.CONTENT_URI, folderId, FolderList.STARRED_ONLY);
            }
            invalidateOptionsMenu();
        }

        Bundle bundle = new Bundle();
        bundle.putString(Constants.TAG_TABLE_NAME, tableName);
        bundle.putString(Constants.TAG_FOLDER, folderTable);
        bundle.putLong(Constants.TAG_ID, tableId);
        bundle.putLong(Constants.TAG_FOLDER_ID, folderId);
        CardListFragment cardListFragment = new CardListFragment();
        cardListFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.layout_set_overview, cardListFragment, fragTagCardList)
                .commit();
    }
}
