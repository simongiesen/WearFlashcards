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
        // Table directory
        public static final String TABLE_DIR = "stack_list";

        // Content URI for table
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_URI, TABLE_DIR);

        // Mime type of a directory of stacks
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                AUTHORITY + "/" + TABLE_DIR;

        // Mime type of a single stack
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +
                AUTHORITY + "/" + TABLE_DIR;

        // Columns in table
        public static final String TABLE_NAME = "stacks";
        public static final String STACK_TABLE_NAME = "stack_table_name";
        public static final String STACK_TITLE = "stack_title";

        // Default sort order
        public static final String SORT_ORDER_DEFAULT = "_ID DESC";
    }

    // Table with a single stack of flashcards
    public static abstract class CardStack implements BaseColumns {
        // Table directory
        public static final String TABLE_DIR = "card_stacks";

        // Content URI for table
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_URI, TABLE_DIR);

        // Mime type of a stack of cards
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                AUTHORITY + "/" + TABLE_DIR;

        // Mime type of a single card
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +
                AUTHORITY + "/" + TABLE_DIR;

        // Columns in table
        public static final String TERM = "term";
        public static final String DEFINITION = "definition";

        // Default sort order
        public static final String SORT_ORDER_DEFAULT = "_ID DESC";
    }
}
