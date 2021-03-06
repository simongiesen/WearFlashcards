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
        public static final String TABLE_NAME = "sets";

        // Mime type of a directory of sets
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                AUTHORITY + "/" + TABLE_DIR;

        // Mime type of a single set
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +
                AUTHORITY + "/" + TABLE_DIR;

        // Columns
        public static final String SET_TITLE = "set_title";
        public static final String STARRED_ONLY = "starred_only";

        // Deprecated columns
        public static final String SET_TABLE_NAME = "set_table_name";
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
        public static final String STAR = "star";

        // Creation command
        public static final String COLUMN_DEFINITIONS = "' (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                TERM + " TEXT NOT NULL," +
                DEFINITION + " TEXT NOT NULL," +
                STAR + " INTEGER DEFAULT 0)";
    }

    // Table with all folders
    public static abstract class FolderList implements BaseColumns {
        public static final String TABLE_DIR = "folder_list";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_URI, TABLE_DIR);
        public static final String TABLE_NAME = "folders";

        // Mime type of a directory of folders
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                AUTHORITY + "/" + TABLE_DIR;

        // Mime type of a single folder
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +
                AUTHORITY + "/" + TABLE_DIR;

        // Columns
        public static final String FOLDER_TITLE = "folder_title";
        public static final String COLOR = "color";
        public static final String STARRED_ONLY = "starred_only";
    }

    // Table with a single set of flashcards
    public static abstract class FolderEntry implements BaseColumns {
        public static final String TABLE_DIR = "folder";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_URI, TABLE_DIR);

        // Mime type of a set of cards
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                AUTHORITY + "/" + TABLE_DIR;

        // Mime type of a single card
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +
                AUTHORITY + "/" + TABLE_DIR;

        // Columns
        public static final String SET_ID = "set_id";

        // Creation command
        public static final String COLUMN_DEFINITIONS = "' (" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                SET_ID + " LONG NOT NULL)";
    }
}
