package com.efa.wearflashcards.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.efa.wearflashcards.data.FlashcardContract.CardStack;
import com.efa.wearflashcards.data.FlashcardContract.StackList;

/**
 * Manages the flashcard databases.
 * http://developer.android.com/training/basics/data-storage/databases.html
 */
public abstract class FlashcardDbHelper extends SQLiteOpenHelper{

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "flashcards.db";

    public FlashcardDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Build the CREATE command
        final String STACKLIST_TABLE_CREATE =
                "CREATE TABLE " + StackList.TABLE_NAME + " (" +
                        StackList._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        StackList.COLUMN_NAME_STACK + " TEXT UNIQUE NOT NULL);";

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
                        CardStack.COLUMN_NAME_TERM + " TEXT PRIMARY KEY UNIQUE NOT NULL," +
                        CardStack.COLUMN_NAME_DEFINITION + " TEXT NOT NULL);";
        stack.execSQL(CARDSTACK_TABLE_CREATE);

        // Link new stack to main database
        ContentValues values = new ContentValues();
        values.put(StackList.COLUMN_NAME_STACK, newTable);
        stack.insert(StackList.TABLE_NAME, null, values);
        return stack;
    }

    /**
     * Inserts a flashcard into a stack.
     */
    public void insertCard(Card newCard, String tableName) {
        // Get the data repository in write mode
        SQLiteDatabase db = getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(CardStack.COLUMN_NAME_TERM, newCard.term);
        values.put(CardStack.COLUMN_NAME_DEFINITION, newCard.definition);

        // Insert the new row
        db.insert(tableName, null, values);
    }

    /**
     * Deletes a flashcard into a stack.
     */
    public void deleteCard(Card card, String tableName) {
        // Get the data repository in write mode
        SQLiteDatabase db = getWritableDatabase();

        // Define 'where' part of query
        final String selection = CardStack.COLUMN_NAME_TERM + " = ? AND " +
                CardStack.COLUMN_NAME_DEFINITION + " = ?";

        // Specify arguments in placeholder order and issue SQL statement
        final String[] selectionArgs = {card.term, card.definition};
        db.delete(tableName, selection, selectionArgs);
    }

    /**
     * Deletes a stack of flashcards.
     */
    public void deleteStack(String tableName) {
        // Get the data repository in write mode
        SQLiteDatabase db = getWritableDatabase();

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
        SQLiteDatabase db = getReadableDatabase();

        // Updated information
        ContentValues values = new ContentValues();
        values.put(CardStack.COLUMN_NAME_TERM, card.term);
        values.put(CardStack.COLUMN_NAME_DEFINITION, card.definition);

        // Update row
        final String selection = CardStack.COLUMN_NAME_TERM + " = ? AND " +
                CardStack.COLUMN_NAME_DEFINITION + " = ?";
        final String[] selectionArgs = {card.term, card.definition};
        db.update(
                tableName,
                values,
                selection,
                selectionArgs);
    }

    // Card template
    public class Card {
        String term;
        String definition;
    }
}
