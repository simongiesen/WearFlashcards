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
 * Allows the user to change the app's settings.
 */
public class SettingsActivity extends Activity {
    private SharedPreferences settings;
    private TextView textShuffle, textDefinitionFirst;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Load and apply settings
        settings = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        textShuffle = (TextView) findViewById(R.id.text_shuffle_status);
        textDefinitionFirst = (TextView) findViewById(R.id.text_definition_first_status);
        if (settings.getBoolean(Constants.SHUFFLE, false)) {
            textShuffle.setText(getText(R.string.preference_on));
        } else {
            textShuffle.setText(getText(R.string.preference_off));
        }
        if (settings.getBoolean(Constants.DEF_FIRST, false)) {
            textDefinitionFirst.setText(getText(R.string.preference_on));
        } else {
            textDefinitionFirst.setText(getText(R.string.preference_off));
        }
    }

    /**
     * Flips either the shuffle or the definition first setting status.
     */
    public void flipStatus(View view) {
        // Get the proper view and setting field
        final TextView textView = view.equals(findViewById(R.id.layout_shuffle)) ?
                textShuffle : textDefinitionFirst;
        final String settingField = view.equals(findViewById(R.id.layout_shuffle)) ?
                Constants.SHUFFLE : Constants.DEF_FIRST;

        // Update setting
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(settingField, !settings.getBoolean(settingField, false));
        editor.apply();

        // Update screen
        if (settings.getBoolean(settingField, false)) {
            textView.setText(getText(R.string.preference_on));
        } else {
            textView.setText(getText(R.string.preference_off));
        }
    }
}
