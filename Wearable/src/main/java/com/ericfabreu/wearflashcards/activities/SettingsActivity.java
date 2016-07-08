package com.ericfabreu.wearflashcards.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.wearable.view.WearableListView;
import android.widget.TextView;

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
        settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        final String[] titles = getTitles();
        final String[] options = getOptions();
        final Drawable[] icons = getIcons();

        WearableListView listView = (WearableListView) findViewById(R.id.layout_list_settings);
        listView.setAdapter(new ListViewAdapter(this, R.layout.item_settings_list,
                titles, options, icons));
        listView.setClickListener(new WearableListView.ClickListener() {
            @Override
            public void onClick(WearableListView.ViewHolder viewHolder) {
                TextView description = (TextView) viewHolder.itemView
                        .findViewById(R.id.text_list_description_settings);
                flipSetting(description, viewHolder.getLayoutPosition());
            }

            @Override
            public void onTopEmptyRegionClick() {
            }
        });
    }

    /**
     * Changes the status of a given setting.
     */
    private void flipSetting(TextView description, int position) {
        SharedPreferences.Editor editor = settings.edit();
        switch (position) {
            // Flip the shuffle status
            case 0: {
                final boolean shuffleValue = !settings
                        .getBoolean(Constants.PREF_KEY_SHUFFLE, false);
                final String shuffle = shuffleValue ? getString(R.string.pref_description_on)
                        : getString(R.string.pref_description_off);
                description.setText(shuffle);
                editor.putBoolean(Constants.PREF_KEY_SHUFFLE, shuffleValue);
                break;
            }
            // Flip the definition first status
            case 1: {
                final boolean definitionValue = !settings
                        .getBoolean(Constants.PREF_KEY_DEFINITION, false);
                final String definition = definitionValue ? getString(R.string.pref_description_on)
                        : getString(R.string.pref_description_off);
                description.setText(definition);
                editor.putBoolean(Constants.PREF_KEY_DEFINITION, definitionValue);
                break;
            }
            // Switch the starred only status to the next option
            case 2: {
                final int starValue = (settings.getInt(Constants.PREF_KEY_STARRED, 0) + 1) % 3;
                final String[] starredOptions = getResources()
                        .getStringArray(R.array.pref_description_starred);
                final String star = starredOptions[starValue];
                description.setText(star);
                editor.putInt(Constants.PREF_KEY_STARRED, starValue);
                break;
            }
            default:
                break;
        }
        editor.apply();
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
