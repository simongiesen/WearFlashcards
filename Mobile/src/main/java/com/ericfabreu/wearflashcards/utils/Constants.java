package com.ericfabreu.wearflashcards.utils;

/**
 * Constants used throughout the app.
 */
public class Constants {
    // Preferences
    public final static String PREF_KEY_SHUFFLE = "shuffle";
    public final static String PREF_KEY_DEFINITION = "definitionFirst";
    public final static String PREF_KEY_SET_ORDER = "setOrder";
    public final static String PREF_KEY_CARD_ORDER = "cardOrder";
    public final static String PREF_KEY_CREATE_STARRED = "createCardStarred";
    public final static String PREF_KEY_SHARED_STAR = "sharedStar";

    // Bundle tags
    public final static String TAG_TERM = "term";
    public final static String TAG_DEFINITION = "definition";
    public final static String TAG_TABLE_NAME = "table_name";
    public final static String TAG_TITLE = "title";
    public final static String TAG_STAR = "star";
    public final static String TAG_ID = "id";
    public final static String TAG_EDITING_MODE = "editing";
    public final static String TAG_FOLDER = "folder";
    public final static String TAG_FOLDER_ID = "folder_id";
    public final static String TAG_FOLDER_MODE = "folder_mode";

    // Fragments and activities
    public final static int REQUEST_CODE_SETTINGS = 1;
    public final static int REQUEST_CODE_EDIT = 2;
    public final static int REQUEST_CODE_STUDY = 3;
    public final static int REQUEST_CODE_CREATE = 4;

    // Menus and toolbars
    public final static int MENU_POS_SHUFFLE = 0;
    public final static int MENU_POS_DEFINITION = 1;
    public final static float TOOLBAR_ELEVATION = 10;
}
