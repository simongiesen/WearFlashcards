package com.ericfabreu.wearflashcards.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ericfabreu.wearflashcards.data.FlashcardContract.CardSet;
import com.ericfabreu.wearflashcards.data.FlashcardContract.FolderList;
import com.ericfabreu.wearflashcards.data.FlashcardContract.SetList;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the flashcard database.
 * http://developer.android.com/training/basics/data-storage/databases.html
 */
public class FlashcardDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 5;
    public static final String DATABASE_NAME = "flashcards";

    public FlashcardDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + SetList.TABLE_NAME + " (" +
                SetList._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                SetList.SET_TITLE + " TEXT UNIQUE NOT NULL," +
                SetList.STARRED_ONLY + " INTEGER DEFAULT 0)");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + FolderList.TABLE_NAME + " (" +
                FolderList._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                FolderList.FOLDER_TITLE + " TEXT UNIQUE NOT NULL," +
                FolderList.SET_COUNT + " INTEGER DEFAULT 0," +
                FolderList.COLOR + " INTEGER DEFAULT 0," +
                FolderList.STARRED_ONLY + " INTEGER DEFAULT 0)");
    }

    /**
     * Do nothing on upgrade yet.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            // Use row IDs as table names
            case 1: {
                // Get current table names from the main table
                List<String> tables = new ArrayList<>();
                Cursor cursor = db.query(SetList.TABLE_NAME,
                        new String[]{SetList._ID, SetList.SET_TABLE_NAME},
                        null,
                        null,
                        null,
                        null,
                        SetList._ID + " ASC");
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
                db.execSQL("INSERT INTO " + SetList.TABLE_NAME + "(" + SetList.SET_TITLE + ")" +
                        " SELECT " + SetList.SET_TITLE + " FROM sets_backup");
                db.execSQL("DROP TABLE sets_backup");
                db.execSQL("COMMIT");

                // Rename all tables to use the row IDs from the new main table
                for (int i = 0; i < tables.size(); i++) {
                    db.execSQL("ALTER TABLE '" + tables.get(i) + "' RENAME TO 'w" + (i + 1) + "f'");
                }
            }

            // Add a star column to the set tables
            case 2: {
                // Get all table names
                List<String> tables = new ArrayList<>();
                Cursor cursor = db.query(SetList.TABLE_NAME,
                        new String[]{SetList._ID},
                        null,
                        null,
                        null,
                        null,
                        SetList._ID + " ASC");
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        final String tableName = "w" + cursor.getString(cursor
                                .getColumnIndex(SetList._ID)) + "f";
                        tables.add(tableName);
                    }
                    cursor.close();
                }

                // Recreate tables with a star column
                for (final String table : tables) {
                    db.execSQL("BEGIN TRANSACTION");
                    db.execSQL("CREATE TEMPORARY TABLE set_backup(" +
                            CardSet.TERM + "," + CardSet.DEFINITION + ")");
                    db.execSQL("INSERT INTO set_backup SELECT " +
                            CardSet.TERM + "," + CardSet.DEFINITION + " FROM " + table);
                    db.execSQL("DROP TABLE " + table + "");
                    db.execSQL("CREATE TABLE " + table + "(" +
                            CardSet._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                            CardSet.TERM + " TEXT NOT NULL," +
                            CardSet.DEFINITION + " TEXT NOT NULL," +
                            CardSet.STAR + " INTEGER DEFAULT 0)");
                    db.execSQL("INSERT INTO " + table + "(" + CardSet.TERM + "," +
                            CardSet.DEFINITION + ")" + " SELECT " +
                            CardSet.TERM + "," + CardSet.DEFINITION + " FROM set_backup");
                    db.execSQL("DROP TABLE set_backup");
                    db.execSQL("COMMIT");
                }
            }

            // Add a starred only column to the main table
            case 3: {
                db.execSQL("BEGIN TRANSACTION");
                db.execSQL("CREATE TEMPORARY TABLE sets_backup(" +
                        SetList._ID + "," + SetList.SET_TITLE + ")");
                db.execSQL("INSERT INTO sets_backup SELECT " + SetList._ID + "," +
                        SetList.SET_TITLE + " FROM " + SetList.TABLE_NAME);
                db.execSQL("DROP TABLE " + SetList.TABLE_NAME + "");
                db.execSQL("CREATE TABLE " + SetList.TABLE_NAME + "(" +
                        SetList._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        SetList.SET_TITLE + " TEXT UNIQUE NOT NULL," +
                        SetList.STARRED_ONLY + " INTEGER DEFAULT 0)");
                db.execSQL("INSERT INTO " + SetList.TABLE_NAME +
                        "(" + SetList._ID + "," + SetList.SET_TITLE + ")" +
                        " SELECT " + SetList._ID + "," + SetList.SET_TITLE + " FROM sets_backup");
                db.execSQL("DROP TABLE sets_backup");
                db.execSQL("COMMIT");
            }

            // Create the folder list table
            case 4: {
                db.execSQL("CREATE TABLE IF NOT EXISTS " + FolderList.TABLE_NAME + " (" +
                        FolderList._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        FolderList.FOLDER_TITLE + " TEXT UNIQUE NOT NULL," +
                        FolderList.SET_COUNT + " INTEGER DEFAULT 0," +
                        FolderList.COLOR + " INTEGER DEFAULT 0)");
            }

            default:
                break;
        }
    }
}
