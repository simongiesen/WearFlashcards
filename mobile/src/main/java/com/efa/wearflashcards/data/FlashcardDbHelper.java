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
     * Returns a table name based on the given title.
     */
    public String getTableName(String title) {
        // Remove all non-alphabetic characters and convert the letters to lower-case
        String tableName = title.replaceAll("[^\\p{IsAlphabetic}]", "");
        tableName = tableName.toLowerCase();
        return tableName;
    }

    /**
     * Creates an empty stack of flashcards.
     */
    public boolean newStack(String title) {
        // Get the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Get table name from stack title
        String tableName = getTableName(title);

        // Build the CREATE command
        final String CARDSTACK_TABLE_CREATE =
                "CREATE TABLE " + tableName + " (" +
                        CardStack.TERM + " TEXT PRIMARY KEY NOT NULL," +
                        CardStack.DEFINITION + " TEXT NOT NULL);";
        db.execSQL(CARDSTACK_TABLE_CREATE);

        // Link new stack to main database
        ContentValues values = new ContentValues();
        values.put(StackList.STACK_TITLE, title);
        values.put(StackList.STACK_TABLE_NAME, tableName);
        db.insert(StackList.TABLE_NAME, null, values);
        return true;
    }

    /**
     * Do nothing on upgrade yet.
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
    }
}
