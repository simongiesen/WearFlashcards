package com.efa.wearflashcards.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.efa.wearflashcards.data.FlashcardContract.CardStack;
import com.efa.wearflashcards.data.FlashcardContract.StackList;

/**
 * Manages the flashcard databases.
 * http://developer.android.com/training/basics/data-storage/databases.html
 */
public class FlashcardDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FLASHCARD_DB";

    public FlashcardDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Build the CREATE command
        final String STACKLIST_TABLE_CREATE =
                "CREATE TABLE " + StackList.TABLE_NAME + " (" +
                        StackList._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        StackList.STACK_TABLE_NAME + " TEXT UNIQUE NOT NULL," +
                        StackList.STACK_TITLE + " TEXT NOT NULL);";

        // Create main table
        db.execSQL(STACKLIST_TABLE_CREATE);
    }

    /**
     * Creates a new stack of flashcards.
     */
    public SQLiteDatabase newStack(String newTable) {
        // Get the data repository in write mode
        SQLiteDatabase stack = this.getWritableDatabase();

        // Build the CREATE command
        final String CARDSTACK_TABLE_CREATE =
                "CREATE TABLE " + newTable + " (" +
                        CardStack.TERM + " TEXT PRIMARY KEY UNIQUE NOT NULL," +
                        CardStack.DEFINITION + " TEXT NOT NULL);";
        stack.execSQL(CARDSTACK_TABLE_CREATE);

        // Content URI for new table
        final Uri CONTENT_URI = Uri.withAppendedPath(FlashcardContract.BASE_URI, "stacks");

        // Mime type of a directory of cards
        final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                FlashcardContract.AUTHORITY + "/" + newTable;

        // Mime type of a single card
        final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +
                FlashcardContract.AUTHORITY + "/" + newTable;

        // Link new stack to main database
        ContentValues values = new ContentValues();
        values.put(StackList.STACK_TABLE_NAME, newTable);
        stack.insert(StackList.TABLE_NAME, null, values);
        return stack;
    }

    /**
     * Inserts a flashcard into a stack.
     */
    public void insertCard(Card newCard, String tableName) {
        // Get the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(CardStack.TERM, newCard.term);
        values.put(CardStack.DEFINITION, newCard.definition);

        // Insert the new row
        db.insert(tableName, null, values);
    }

    /**
     * Deletes a flashcard into a stack.
     */
    public void deleteCard(Card card, String tableName) {
        // Get the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Define 'where' part of query
        final String selection = CardStack.TERM + " = ? AND " +
                CardStack.DEFINITION + " = ?";

        // Specify arguments in placeholder order and issue SQL statement
        final String[] selectionArgs = {card.term, card.definition};
        db.delete(tableName, selection, selectionArgs);
    }

    /**
     * Deletes a stack of flashcards.
     */
    public void deleteStack(String tableName) {
        // Get the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Build DELETE statement and execute it
        final String CARDSTACK_TABLE_DELETE =
                "DROP TABLE IF EXISTS " + tableName;
        db.execSQL(CARDSTACK_TABLE_DELETE);
    }

    /**
     * Updates a flashcard.
     */
    public void updateCard(Card card, String tableName) {
        // Get the data repository in read mode
        SQLiteDatabase db = this.getReadableDatabase();

        // Updated information
        ContentValues values = new ContentValues();
        values.put(CardStack.TERM, card.term);
        values.put(CardStack.DEFINITION, card.definition);

        // Update row
        final String selection = CardStack.TERM + " = ? AND " +
                CardStack.DEFINITION + " = ?";
        final String[] selectionArgs = {card.term, card.definition};
        db.update(
                tableName,
                values,
                selection,
                selectionArgs);
    }

    /**
     * Do nothing on upgrade yet.
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
    }

    // Card template
    public class Card {
        String term;
        String definition;
    }
}
