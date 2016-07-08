package com.ericfabreu.wearflashcards.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.wearable.view.WearableListView;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.adapters.ListViewAdapter;
import com.ericfabreu.wearflashcards.utils.Constants;

/**
 * Allows the user to change the app's settings.
 */
public class SettingsActivity extends Activity {
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        settings = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);

        final String[] titles = getTitles();
        final String[] options = getOptions();
        final Drawable[] icons = getIcons();

        WearableListView listView =
                (WearableListView) findViewById(R.id.layout_list_settings);
        listView.setAdapter(new ListViewAdapter(this, R.layout.item_settings_list,
                titles, options, icons));
    }

    /**
     * Generates an array with the titles needed for the list adapter.
     */
    private String[] getTitles() {
        return new String[]{getString(R.string.pref_title_shuffle),
                getString(R.string.pref_title_definition_first),
                getString(R.string.pref_title_starred_only)};
    }

    /**
     * Generates an array with the options needed for the list adapter.
     */
    private String[] getOptions() {
        final boolean shuffleValue = settings.getBoolean(Constants.PREF_KEY_SHUFFLE, false);
        final boolean definitionValue = settings.getBoolean(Constants.PREF_KEY_DEFINITION, false);
        final int starredValue = settings.getInt(Constants.PREF_KEY_STARRED, 0);
        final String shuffle = shuffleValue ?
                getString(R.string.pref_description_on) : getString(R.string.pref_description_off);
        final String definition = definitionValue ?
                getString(R.string.pref_description_on) : getString(R.string.pref_description_off);
        final String[] starredOptions = getResources()
                .getStringArray(R.array.pref_description_starred);
        final String starred = starredOptions[starredValue];
        return new String[]{shuffle, definition, starred};
    }

    /**
     * Generates an array with the icons needed for the list adapter.
     */
    private Drawable[] getIcons() {
        final Drawable shuffle = ContextCompat.getDrawable(getApplicationContext(),
                R.drawable.ic_settings_shuffle);
        final Drawable definition = ContextCompat.getDrawable(getApplicationContext(),
                R.drawable.ic_settings_switch_terms);
        final Drawable starred = ContextCompat.getDrawable(getApplicationContext(),
                R.drawable.ic_settings_star);
        return new Drawable[]{shuffle, definition, starred};
    }
}
