package com.ericfabreu.wearflashcards.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Group of functions that handle interactions with SharedPreferences.
 */
public class PreferencesHelper {
    private static String PREF_KEY_STARRED_ONLY = "starredOnly";

    /**
     * Returns the order in which the content in `column` should be displayed.
     */
    public static String getOrder(Context context, String column, String prefKey) {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        final int order = Integer.valueOf(settings.getString(prefKey, "0"));
        switch (order) {
            case 0:
                return column + " ASC";
            case 1:
                return column + " DESC";
            default:
                return null;
        }
    }

    /**
     * Returns the default star value for new cards.
     */
    public static String getStarMode(Context context) {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getBoolean(Constants.PREF_KEY_CREATE_STARRED, false) ? "1" : "0";
    }

    /**
     * Returns whether or not the global star setting should be used instead
     * of the set's or folder's own setting.
     */
    public static boolean getSharedStarSetting(Context context) {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getBoolean(Constants.PREF_KEY_SHARED_STAR, false);
    }

    /**
     * Returns the global starred only setting.
     */
    public static boolean getStarredOnly(Context context) {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getBoolean(PREF_KEY_STARRED_ONLY, false);
    }

    /**
     * Flips the global starred only setting.
     */
    public static void flipStarredOnly(Context context) {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        settings.edit().putBoolean(PREF_KEY_STARRED_ONLY, !getStarredOnly(context)).apply();
    }
}
