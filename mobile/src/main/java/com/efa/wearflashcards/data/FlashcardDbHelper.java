package com.efa.wearflashcards.data;

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
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String STACKLIST_TABLE_CREATE =
                "CREATE TABLE " + StackList.TABLE_NAME + " (" +
                        StackList._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        StackList.COLUMN_NAME_STACK + " TEXT UNIQUE NOT NULL);";

        final String CARDSTACK_TABLE_CREATE =
                "CREATE TABLE " + CardStack.TABLE_NAME + " (" +
                        CardStack.COLUMN_NAME_TERM + " TEXT PRIMARY KEY UNIQUE NOT NULL," +
                        CardStack.COLUMN_NAME_DEFINITION + " TEXT NOT NULL);";

        sqLiteDatabase.execSQL(STACKLIST_TABLE_CREATE);
        sqLiteDatabase.execSQL(CARDSTACK_TABLE_CREATE);
    }
}
