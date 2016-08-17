package com.ericfabreu.wearflashcards.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.data.FlashcardContract.CardSet;
import com.ericfabreu.wearflashcards.data.FlashcardContract.FolderList;
import com.ericfabreu.wearflashcards.data.FlashcardContract.SetList;
import com.ericfabreu.wearflashcards.data.FlashcardProvider;
import com.ericfabreu.wearflashcards.fragments.SetListFragment;
import com.ericfabreu.wearflashcards.utils.Constants;
import com.ericfabreu.wearflashcards.utils.PreferencesHelper;

public class FolderOverviewActivity extends AppCompatActivity {
    private String tableName, title;
    private long tableId;
    private SharedPreferences settings;
    private FlashcardProvider mProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_overview);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setElevation(Constants.TOOLBAR_ELEVATION);
        }

        // Load settings
        settings = PreferenceManager.getDefaultSharedPreferences(this);

        // Get table name from the caller activity and pass it to CardListFragment
        Bundle bundle = getIntent().getExtras();
        tableName = bundle.getString(Constants.TAG_TABLE_NAME);
        title = bundle.getString(Constants.TAG_TITLE);
        tableId = bundle.getLong(Constants.TAG_ID);

        setTitle(title);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_folder_overview);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Do not launch ImportSetsActivity if there are no sets to import
                FlashcardProvider handle = new FlashcardProvider(getApplicationContext());
                if (handle.getRowCount(SetList.TABLE_NAME) == handle.getRowCount(tableName)) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            R.string.message_import_empty, Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    Intent intent = new Intent(FolderOverviewActivity.this,
                            ImportSetsActivity.class);
                    intent.putExtra(Constants.TAG_TABLE_NAME, tableName);
                    startActivityForResult(intent, Constants.REQUEST_CODE_EDIT);
                }
            }
        });

        mProvider = new FlashcardProvider(getApplicationContext());

        // Load flashcard sets
        if (savedInstanceState == null) {
            startSetListFragment();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Load the starred only setting
        final Switch starredOnly = (Switch) findViewById(R.id.switch_folder_starred_only);
        starredOnly.setChecked(PreferencesHelper.getStar(getApplicationContext(), mProvider,
                FolderList.CONTENT_URI, tableId, FolderList.STARRED_ONLY));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.shared, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
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
        } else {
            findViewById(R.id.layout_folder_starred_only).setVisibility(View.VISIBLE);
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
            // Launch StudySetActivity
            case R.id.item_study_set: {
                Intent intent = new Intent(this, StudySetActivity.class);
                intent.putExtra(Constants.TAG_TITLE, title);
                intent.putExtra(Constants.TAG_TABLE_NAME, tableName);
                intent.putExtra(Constants.TAG_ID, tableId);
                intent.putExtra(Constants.TAG_FOLDER, true);
                startActivityForResult(intent, Constants.REQUEST_CODE_STUDY);
                return true;
            }
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
                requestCode == Constants.REQUEST_CODE_STUDY) {
            Intent refresh = new Intent(this, getClass());
            refresh.putExtra(Constants.TAG_TABLE_NAME, tableName);
            refresh.putExtra(Constants.TAG_TITLE, title);
            refresh.putExtra(Constants.TAG_ID, tableId);
            startActivity(refresh);
            this.finish();
        }
    }

    /**
     * Refreshes the SetListFragment when the starred only setting changes.
     */
    public void startSetListFragment() {
        final String fragTagSetList = "setListFragment";
        SetListFragment setListFragment = new SetListFragment();
        setListFragment.setFolderMode(tableName, tableId);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.layout_folder_overview, setListFragment, fragTagSetList)
                .commit();
    }

    public void flipStarredOnly(View view) {
        PreferencesHelper.flipStar(getApplicationContext(), mProvider,
                FolderList.CONTENT_URI, tableId, FolderList.STARRED_ONLY);
    }
}