package com.efa.wearflashcards.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.efa.wearflashcards.data.FlashcardContract.CardSet;
import com.efa.wearflashcards.data.FlashcardContract.SetList;

/**
 * Manages the flashcard database.
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
        final String SETLIST_TABLE_CREATE =
                "CREATE TABLE " + SetList.TABLE_NAME + " (" +
                        SetList._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        SetList.SET_TABLE_NAME + " TEXT UNIQUE NOT NULL," +
                        SetList.SET_TITLE + " TEXT NOT NULL);";

        // Create main table
        db.execSQL(SETLIST_TABLE_CREATE);
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
     * Creates an empty set of flashcards.
     */
    public boolean newSet(String title) {
        // Get the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Get table name from set title
        String tableName = getTableName(title);

        // Build the CREATE command
        final String CARDSET_TABLE_CREATE =
                "CREATE TABLE " + tableName + " (" +
                        CardSet.TERM + " TEXT PRIMARY KEY NOT NULL," +
                        CardSet.DEFINITION + " TEXT NOT NULL);";
        db.execSQL(CARDSET_TABLE_CREATE);

        // Link new set to main database
        ContentValues values = new ContentValues();
        values.put(SetList.SET_TITLE, title);
        values.put(SetList.SET_TABLE_NAME, tableName);
        db.insert(SetList.TABLE_NAME, null, values);
        return true;
    }

    /**
     * Do nothing on upgrade yet.
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
    }
}
