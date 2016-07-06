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

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.data.FlashcardContract.CardSet;
import com.ericfabreu.wearflashcards.data.FlashcardContract.SetList;
import com.ericfabreu.wearflashcards.data.FlashcardProvider;
import com.ericfabreu.wearflashcards.fragments.CardListFragment;
import com.ericfabreu.wearflashcards.utils.Constants;

public class SetOverviewActivity extends AppCompatActivity {
    private String tableName, title;
    private long tableId;
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_overview);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setElevation(Constants.TOOLBAR_ELEVATION);
        }

        // Load settings
        settings = PreferenceManager.getDefaultSharedPreferences(this);

        // Get table name from the caller activity and pass it to CardListFragment
        Bundle bundle = getIntent().getExtras();
        tableName = bundle.getString(Constants.TABLE_NAME);
        title = bundle.getString(Constants.TITLE);
        tableId = bundle.getLong(Constants.ID);

        setTitle(title);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_set_overview);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SetOverviewActivity.this, NewCardActivity.class);
                intent.putExtra(Constants.TABLE_NAME, tableName);
                intent.putExtra(Constants.TITLE, title);
                startActivityForResult(intent, Constants.REQUEST_CODE_CREATE);
            }
        });

        // Load flashcard sets
        if (savedInstanceState == null) {
            startCardListFragment(null);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Load the starred only setting
        FlashcardProvider handle = new FlashcardProvider(getApplicationContext());
        final Switch starredOnly = (Switch) findViewById(R.id.switch_starred_only);
        starredOnly.setChecked(handle.getFlag(SetList.CONTENT_URI, tableId, SetList.STARRED_ONLY));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.shared, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Load settings
        final MenuItem shuffle = menu.getItem(Constants.SHUFFLE_POS);
        final MenuItem termFirst = menu.getItem(Constants.DEF_FIRST_POS);
        shuffle.setChecked(settings.getBoolean(Constants.PREF_KEY_SHUFFLE, false));
        termFirst.setChecked(settings.getBoolean(Constants.PREF_KEY_DEFINITION_FIRST, false));

        // Hide set study button and the starred only bar if there are no cards
        FlashcardProvider handle = new FlashcardProvider(getApplicationContext());
        final Uri tableUri = Uri.withAppendedPath(CardSet.CONTENT_URI, tableName);
        Cursor cursor = handle.query(tableUri,
                new String[]{CardSet._ID},
                null,
                null,
                null);
        if (cursor == null || cursor.getCount() == 0) {
            menu.removeItem(R.id.item_study_set);
            (findViewById(R.id.layout_starred_only)).setVisibility(View.GONE);
        } else {
            (findViewById(R.id.layout_starred_only)).setVisibility(View.VISIBLE);
            cursor.close();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Flip the item's checked state and save settings
        if (id == R.id.item_shuffle) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(Constants.PREF_KEY_SHUFFLE, !item.isChecked());
            editor.apply();
            item.setChecked(!item.isChecked());
            return true;
        } else if (id == R.id.item_definition_first) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(Constants.PREF_KEY_DEFINITION_FIRST, !item.isChecked());
            editor.apply();
            return true;
        }

        // Launch StudySetActivity
        else if (id == R.id.item_study_set) {
            Intent intent = new Intent(this, StudySetActivity.class);
            intent.putExtra(Constants.TITLE, title);
            intent.putExtra(Constants.TABLE_NAME, tableName);
            intent.putExtra(Constants.ID, tableId);
            startActivityForResult(intent, Constants.REQUEST_CODE_STUDY);
            return true;
        } else if (id == R.id.item_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent, Constants.REQUEST_CODE_SETTINGS);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Return to MainActivity if back key is pressed instead of going to previous activity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Refresh activity with the proper sort order when SettingsActivity is closed
        if (requestCode == Constants.REQUEST_CODE_SETTINGS ||
                requestCode == Constants.REQUEST_CODE_EDIT ||
                requestCode == Constants.REQUEST_CODE_STUDY ||
                requestCode == Constants.REQUEST_CODE_CREATE) {
            Intent refresh = new Intent(this, getClass());
            refresh.putExtra(Constants.TABLE_NAME, tableName);
            refresh.putExtra(Constants.TITLE, title);
            refresh.putExtra(Constants.ID, tableId);
            startActivity(refresh);
            this.finish();
        }
    }

    /**
     * Refreshes the CardListFragment when the starred only setting changes.
     */
    public void startCardListFragment(View view) {
        // Check if the flag needs to be flipped
        if (view != null) {
            FlashcardProvider handle = new FlashcardProvider(getApplicationContext());
            handle.flipFlag(SetList.CONTENT_URI, tableId, SetList.STARRED_ONLY);
        }

        Bundle bundle = new Bundle();
        bundle.putString(Constants.TABLE_NAME, tableName);
        bundle.putLong(Constants.ID, tableId);
        CardListFragment cardListFragment = new CardListFragment();
        cardListFragment.setArguments(bundle);
        getFragmentManager().beginTransaction()
                .replace(R.id.layout_set_overview, cardListFragment, Constants.FRAG_TAG_CARD_LIST)
                .commit();
    }
}
