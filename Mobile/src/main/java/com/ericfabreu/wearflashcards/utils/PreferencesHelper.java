package com.ericfabreu.wearflashcards.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Group of functions that handle interactions with SharedPreferences.
 */
public class PreferencesHelper {
    /**
     * Returns the order in which the content in `column` should be displayed.
     */
    public static String getOrder(Context context, String column, String prefKey) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        int order = Integer.valueOf(settings.getString(prefKey, "0"));
        switch (order) {
            case 0:
                return column + " ASC";
            case 1:
                return column + " DESC";
            default:
                return null;
        }
    }
}
