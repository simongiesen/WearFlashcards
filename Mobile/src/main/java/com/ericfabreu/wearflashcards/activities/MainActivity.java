package com.ericfabreu.wearflashcards.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.fragments.SetListFragment;
import com.ericfabreu.wearflashcards.utils.Constants;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setElevation(Constants.TOOLBAR_ELEVATION);
        }

        // Load settings
        settings = PreferenceManager.getDefaultSharedPreferences(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_main);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NewSetActivity.class);
                startActivity(intent);
            }
        });

        // Load flashcard sets
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.layout_main, new SetListFragment())
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.shared, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Get menu items
        MenuItem shuffle = menu.getItem(Constants.SHUFFLE_POS);
        MenuItem termFirst = menu.getItem(Constants.DEF_FIRST_POS);

        // Restore settings
        shuffle.setChecked(settings.getBoolean(Constants.PREF_KEY_SHUFFLE, false));
        termFirst.setChecked(settings.getBoolean(Constants.PREF_KEY_DEFINITION, false));

        // Set study button is unnecessary in MainActivity
        menu.removeItem(R.id.item_study_set);
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
            editor.putBoolean(Constants.PREF_KEY_DEFINITION, !item.isChecked());
            editor.apply();
            return true;
        } else if (id == R.id.item_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent, Constants.REQUEST_CODE_SETTINGS);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Refresh activity with the proper sort order when SettingsActivity is closed
        if (requestCode == Constants.REQUEST_CODE_SETTINGS) {
            Intent refresh = new Intent(this, getClass());
            startActivity(refresh);
            this.finish();
        }
    }
}
