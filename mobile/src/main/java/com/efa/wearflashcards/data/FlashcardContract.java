package com.efa.wearflashcards.data;

import android.provider.BaseColumns;

/**
 * Defines table and column names for database.
 */
public final class FlashcardContract {
    // empty constructor to prevent someone from accidentally instantiating the contract class
    public FlashcardContract() {}

    // list of all flashcard stacks
    public static abstract class StackList implements BaseColumns {
        public static final String TABLE_NAME = "stacks";
        public static final String COLUMN_NAME_STACK = "stack_name";
    }

    // stack of flashcards
    public static abstract class CardStack implements BaseColumns {
        public static final String TABLE_NAME = "stack";
        public static final String COLUMN_NAME_TERM = "term";
        public static final String COLUMN_NAME_DEFINITION = "definition";
    }
}
