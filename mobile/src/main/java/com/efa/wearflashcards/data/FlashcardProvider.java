package com.efa.wearflashcards.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class FlashcardProvider extends ContentProvider {
    // Helper constants for URI matcher
    private static final int STACK_LIST = 1;
    private static final int STACK_ITEM = 2;
    private static final int CARD_LIST = 3;
    private static final int CARD_ITEM = 4;
    private static final UriMatcher URI_MATCHER;

    // Prepare the UriMatcher
    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(FlashcardContract.AUTHORITY, FlashcardContract.StackList.TABLE_DIR, 1);
        URI_MATCHER.addURI(FlashcardContract.AUTHORITY, FlashcardContract.StackList.TABLE_DIR + "/#", 2);
        URI_MATCHER.addURI(FlashcardContract.AUTHORITY, FlashcardContract.CardStack.TABLE_DIR + "/*", 3);
        URI_MATCHER.addURI(FlashcardContract.AUTHORITY, FlashcardContract.CardStack.TABLE_DIR + "/*/#", 4);
    }

    // Handle to the database helper object
    private FlashcardDbHelper mOpenHelper;
    private SQLiteDatabase db;

    // Create a FlashcardDBHelper
    @Override
    public boolean onCreate() {
        mOpenHelper = new FlashcardDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings2, String s2) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case STACK_LIST:
                return FlashcardContract.StackList.CONTENT_TYPE;
            case STACK_ITEM:
                return FlashcardContract.StackList.CONTENT_ITEM_TYPE;
            case CARD_LIST:
                return FlashcardContract.CardStack.CONTENT_TYPE;
            case CARD_ITEM:
                return FlashcardContract.CardStack.CONTENT_ITEM_TYPE;
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
