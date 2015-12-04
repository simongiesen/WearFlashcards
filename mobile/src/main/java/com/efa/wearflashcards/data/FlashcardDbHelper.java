package com.efa.wearflashcards.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
                "CREATE TABLE IF NOT EXISTS " + SetList.TABLE_NAME + " (" +
                        SetList._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        SetList.SET_TABLE_NAME + " TEXT UNIQUE NOT NULL," +
                        SetList.SET_TITLE + " TEXT NOT NULL);";

        // Create main table
        db.execSQL(SETLIST_TABLE_CREATE);
    }

    /**
     * Do nothing on upgrade yet.
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
    }
}
