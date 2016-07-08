package com.ericfabreu.wearflashcards.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Group of functions that handle interactions with SharedPreferences.
 */
public class PreferencesHelper {
    public static String getStarredOption(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return String.valueOf(settings.getInt(Constants.PREF_KEY_STARRED, 0));
    }
}
