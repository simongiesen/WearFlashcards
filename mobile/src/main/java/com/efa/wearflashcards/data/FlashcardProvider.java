package com.efa.wearflashcards.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.List;

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
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        switch (URI_MATCHER.match(uri)) {
            case STACK_LIST:
                builder.setTables(FlashcardContract.StackList.TABLE_NAME);
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = FlashcardContract.StackList.SORT_ORDER_DEFAULT;
                }
                break;
            case STACK_ITEM:
                builder.setTables(FlashcardContract.StackList.TABLE_NAME);
                builder.appendWhere(FlashcardContract.StackList._ID + "=" + uri.getLastPathSegment());
                break;
            case CARD_LIST:
                builder.setTables(uri.getLastPathSegment());
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = FlashcardContract.CardStack.SORT_ORDER_DEFAULT;
                }
                break;
            case CARD_ITEM:
                List<String> segments = uri.getPathSegments();
                final String table = segments.get(segments.size() - 1);
                builder.setTables(table);
                builder.appendWhere(FlashcardContract.CardStack._ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        return builder.query(db,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
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
        final String tableName = contentValues.getAsString(FlashcardContract.StackList.STACK_TABLE_NAME);
        contentValues.remove(FlashcardContract.StackList.STACK_TABLE_NAME);
        long id = db.insert(tableName, null, contentValues);
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
