package com.efa.wearflashcards.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for database.
 */
public final class FlashcardContract {

    // URI variables
    public static final String AUTHORITY = "com.efa.wearflashcards.app";
    public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    // Empty constructor to prevent someone from accidentally instantiating the contract class
    public FlashcardContract() {}

    // Table with all flashcard stacks
    public static abstract class StackList implements BaseColumns {
        // Columns in table
        public static final String TABLE_NAME = "stacks";
        public static final String COLUMN_NAME_STACK = "stack_name";

        // Content URI for table
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_URI, "stacks");

        // Mime type of a directory of stacks
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                AUTHORITY + "/" + TABLE_NAME;

        // Mime type of a single stack
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +
                AUTHORITY + "/" + TABLE_NAME;
    }

    // Table with a single stack of flashcards
    public static abstract class CardStack implements BaseColumns {
        public static final String COLUMN_NAME_TERM = "term";
        public static final String COLUMN_NAME_DEFINITION = "definition";
    }
}
