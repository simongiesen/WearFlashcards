package com.ericfabreu.wearflashcards.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.fragments.SetListFragment;
import com.ericfabreu.wearflashcards.utils.Constants;

public class MainActivity extends AppCompatActivity {
    private static MainActivity instance;
    private SharedPreferences settings;

    // Allow other activities to get application context statically
    // http://stackoverflow.com/a/5114361
    public static Context getContext() {
        return instance.getApplicationContext();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Load settings
        settings = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
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
                    .add(R.id.main_layout, new SetListFragment())
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
        shuffle.setChecked(settings.getBoolean(Constants.SHUFFLE, false));
        termFirst.setChecked(settings.getBoolean(Constants.DEF_FIRST, false));

        // Set view button is unnecessary in MainActivity
        menu.removeItem(R.id.study_set_button);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Flip the item's checked state and save settings
        if (id == R.id.shuffle) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(Constants.SHUFFLE, !item.isChecked());
            editor.apply();
            item.setChecked(!item.isChecked());
            return true;
        } else if (id == R.id.definition_first) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(Constants.DEF_FIRST, !item.isChecked());
            editor.apply();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
