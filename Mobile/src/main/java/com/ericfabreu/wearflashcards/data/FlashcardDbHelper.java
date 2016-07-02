package com.ericfabreu.wearflashcards.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ericfabreu.wearflashcards.data.FlashcardContract.SetList;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the flashcard database.
 * http://developer.android.com/training/basics/data-storage/databases.html
 */
public class FlashcardDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "flashcards";

    public FlashcardDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Build the CREATE command
        final String SETLIST_TABLE_CREATE =
                "CREATE TABLE IF NOT EXISTS " + SetList.TABLE_NAME + " (" +
                        SetList._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        SetList.SET_TITLE + " TEXT UNIQUE NOT NULL)";

        // Create main table
        db.execSQL(SETLIST_TABLE_CREATE);
    }

    /**
     * Do nothing on upgrade yet.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1: {
                // Get current table names from the main table
                List<String> tables = new ArrayList<>();
                Cursor cursor = db.query(SetList.TABLE_NAME,
                        new String[]{SetList._ID, SetList.SET_TABLE_NAME},
                        null,
                        null,
                        null,
                        null,
                        null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        tables.add(cursor.getString(cursor.getColumnIndex(SetList.SET_TABLE_NAME)));
                    }
                    cursor.close();
                }

                // Drop set table name column from the main table
                db.execSQL("BEGIN TRANSACTION");
                db.execSQL("CREATE TEMPORARY TABLE sets_backup(" + SetList.SET_TITLE + ")");
                db.execSQL("INSERT INTO sets_backup SELECT " + SetList.SET_TITLE +
                        " FROM " + SetList.TABLE_NAME);
                db.execSQL("DROP TABLE " + SetList.TABLE_NAME + "");
                db.execSQL("CREATE TABLE " + SetList.TABLE_NAME + "(" +
                        SetList._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        SetList.SET_TITLE + " TEXT UNIQUE NOT NULL)");
                db.execSQL("INSERT INTO " + SetList.TABLE_NAME + " SELECT " + SetList.SET_TITLE +
                        " FROM sets_backup");
                db.execSQL("DROP TABLE sets_backup");
                db.execSQL("COMMIT");

                // Rename all tables to use the row IDs from the new main table
                for (int i = 0; i < tables.size(); i++) {
                    db.execSQL("ALTER TABLE '" + tables.get(i) + "' RENAME TO 'w" + (i + 1) + "f'");
                }
            }
            default:
                break;
        }
    }
}
