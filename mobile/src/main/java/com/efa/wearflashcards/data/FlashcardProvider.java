package com.efa.wearflashcards.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * Manages access to the flashcard database.
 * http://developer.android.com/guide/topics/providers/content-provider-basics.html
 * http://www.grokkingandroid.com/android-tutorial-writing-your-own-content-provider/
 * http://www.sitepoint.com/using-androids-content-providers-manage-app-data/
 */
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

    // Create a FlashcardDBHelper
    @Override
    public boolean onCreate() {
        mOpenHelper = new FlashcardDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] strings, String s, String[] strings2, String s2) {
        return null;
    }

    @Override
    public String getType(@NonNull Uri uri) {
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
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        // Validate URI
        if (URI_MATCHER.match(uri) == STACK_LIST ||
                URI_MATCHER.match(uri) == STACK_ITEM) {
            throw new IllegalArgumentException("Cannot insert new stacks directly into main table.");
        }
        if (URI_MATCHER.match(uri) == CARD_LIST) {
            throw new IllegalArgumentException("Use create() to create a new flashcard stack.");
        }
        if (URI_MATCHER.match(uri) != CARD_ITEM) {
            throw new IllegalArgumentException("Uri not supported for insertion: " + uri);
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        // Insert a new card into the stack
        long id = db.insert(contentValues.getAsString(FlashcardContract.StackList.STACK_TABLE_NAME), null, contentValues);
        if (id > 0) {
            return ContentUris.withAppendedId(uri, id);
        }

        // Failed to insert new card
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
