package com.ericfabreu.wearflashcards.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for database.
 */
public final class FlashcardContract {
    public static final String AUTHORITY = "com.ericfabreu.wearflashcards.app";
    public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    // Prevent class from being instantiated accidentally
    public FlashcardContract() {}

    // Table with all flashcard sets
    public static abstract class SetList implements BaseColumns {
        public static final String TABLE_DIR = "set_list";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_URI, TABLE_DIR);

        // Mime type of a directory of sets
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                AUTHORITY + "/" + TABLE_DIR;

        // Mime type of a single set
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +
                AUTHORITY + "/" + TABLE_DIR;

        // Columns
        public static final String TABLE_NAME = "sets";
        public static final String SET_TABLE_NAME = "set_table_name";
        public static final String SET_TITLE = "set_title";

        // Default sort order
        public static final String SORT_ORDER_DEFAULT = "_ID ASC";
    }

    // Table with a single set of flashcards
    public static abstract class CardSet implements BaseColumns {
        public static final String TABLE_DIR = "card_sets";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_URI, TABLE_DIR);

        // Mime type of a set of cards
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                AUTHORITY + "/" + TABLE_DIR;

        // Mime type of a single card
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +
                AUTHORITY + "/" + TABLE_DIR;

        // Columns
        public static final String TERM = "term";
        public static final String DEFINITION = "definition";

        // Default sort order
        public static final String SORT_ORDER_DEFAULT = "_ID ASC";
    }
}
