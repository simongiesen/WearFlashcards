package com.efa.wearflashcards.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.efa.wearflashcards.data.FlashcardContract.CardStack;
import com.efa.wearflashcards.data.FlashcardContract.StackList;

/**
 * Manages the flashcard databases.
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

        final String STACKLIST_TABLE_CREATE =
                "CREATE TABLE " + StackList.TABLE_NAME + " (" +
                        StackList._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        StackList.COLUMN_NAME_STACK + " TEXT UNIQUE NOT NULL);";

        db.execSQL(STACKLIST_TABLE_CREATE);
    }

    /**
     * Creates a new stack of flashcards.
     */
    public SQLiteDatabase newStack(String newTable) {

        SQLiteDatabase stack = this.getWritableDatabase();
        final String CARDSTACK_TABLE_CREATE =
                "CREATE TABLE " + newTable + " (" +
                        CardStack.COLUMN_NAME_TERM + " TEXT PRIMARY KEY UNIQUE NOT NULL," +
                        CardStack.COLUMN_NAME_DEFINITION + " TEXT NOT NULL);";
        stack.execSQL(CARDSTACK_TABLE_CREATE);

        // link stack to main database
        ContentValues values = new ContentValues();
        values.put(StackList.COLUMN_NAME_STACK, newTable);
        stack.insert(StackList.TABLE_NAME, null, values);
        return stack;
    }

    /**
     * Inserts a flashcard into a stack.
     */
    public void insert(Card newCard, SQLiteDatabase stack, String tableName) {
        ContentValues values = new ContentValues();
        values.put(CardStack.COLUMN_NAME_TERM, newCard.term);
        values.put(CardStack.COLUMN_NAME_DEFINITION, newCard.definition);
        stack.insert(tableName, null, values);
    }

    // card template
    public class Card {
        String term;
        String definition;
    }
}
