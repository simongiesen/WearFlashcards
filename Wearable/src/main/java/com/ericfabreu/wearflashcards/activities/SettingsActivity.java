package com.ericfabreu.wearflashcards.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.utils.Constants;

/**
 * Allows user to change the app's activity_settings.
 */
public class SettingsActivity extends Activity {
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Load and apply activity_settings
        settings = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        if (settings.getBoolean(Constants.SHUFFLE, false)) {
            ((TextView) findViewById(R.id.shuffle_status)).setText(getText(R.string.on));
        } else {
            ((TextView) findViewById(R.id.shuffle_status)).setText(getText(R.string.off));
        }
        if (settings.getBoolean(Constants.DEF_FIRST, false)) {
            ((TextView) findViewById(R.id.def_first_status)).setText(getText(R.string.on));
        } else {
            ((TextView) findViewById(R.id.def_first_status)).setText(getText(R.string.off));
        }
    }

    /**
     * Flips the shuffle status.
     */
    public void flipShuffle(View view) {
        // Update setting
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.SHUFFLE, !settings.getBoolean(Constants.SHUFFLE, false));
        editor.apply();

        // Update screen
        if (settings.getBoolean(Constants.SHUFFLE, false)) {
            ((TextView) findViewById(R.id.shuffle_status)).setText(getText(R.string.on));
        } else {
            ((TextView) findViewById(R.id.shuffle_status)).setText(getText(R.string.off));
        }
    }

    /**
     * Flips the definition first setting status.
     */
    public void flipDefFirst(View view) {
        // Update setting
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.DEF_FIRST, !settings.getBoolean(Constants.DEF_FIRST, false));
        editor.apply();

        // Update screen
        if (settings.getBoolean(Constants.DEF_FIRST, false)) {
            ((TextView) findViewById(R.id.def_first_status)).setText(getText(R.string.on));
        } else {
            ((TextView) findViewById(R.id.def_first_status)).setText(getText(R.string.off));
        }
    }
}
