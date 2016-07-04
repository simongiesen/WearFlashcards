package com.ericfabreu.wearflashcards.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.ericfabreu.wearflashcards.data.FlashcardContract.CardSet;
import com.ericfabreu.wearflashcards.data.FlashcardContract.SetList;
import com.ericfabreu.wearflashcards.utils.Constants;
import com.ericfabreu.wearflashcards.utils.PreferencesHelper;

import java.util.List;

/**
 * Manages access to the flashcard database.
 * http://developer.android.com/guide/topics/providers/content-provider-basics.html
 * http://www.grokkingandroid.com/android-tutorial-writing-your-own-content-provider/
 */
public class FlashcardProvider extends ContentProvider {
    // Helper constants for URI matcher
    private static final int SET_LIST = 1;
    private static final int SET_ITEM = 2;
    private static final int CARD_LIST = 3;
    private static final int CARD_ITEM = 4;
    private static final UriMatcher URI_MATCHER;

    // Prepare the UriMatcher
    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(FlashcardContract.AUTHORITY, SetList.TABLE_DIR, 1);
        URI_MATCHER.addURI(FlashcardContract.AUTHORITY, SetList.TABLE_DIR + "/#", 2);
        URI_MATCHER.addURI(FlashcardContract.AUTHORITY, CardSet.TABLE_DIR + "/*", 3);
        URI_MATCHER.addURI(FlashcardContract.AUTHORITY, CardSet.TABLE_DIR + "/*/#", 4);
    }

    Context context;
    private FlashcardDbHelper mOpenHelper;

    public FlashcardProvider(Context c) {
        context = c;
        mOpenHelper = new FlashcardDbHelper(context);
    }

    @SuppressWarnings("unused")
    public FlashcardProvider() {
    }

    @Override
    public boolean onCreate() {
        context = getContext();
        mOpenHelper = new FlashcardDbHelper(context);
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

        // Handle table name and order differently depending on the uri
        switch (URI_MATCHER.match(uri)) {
            case SET_LIST:
                builder.setTables(SetList.TABLE_NAME);
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = SetList.SORT_ORDER_DEFAULT;
                }
                break;
            case SET_ITEM:
                builder.setTables(SetList.TABLE_NAME);
                builder.appendWhere(SetList._ID + "=" + uri.getLastPathSegment());
                break;
            case CARD_LIST:
                builder.setTables("'" + uri.getLastPathSegment() + "'");
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = CardSet.SORT_ORDER_DEFAULT;
                }
                break;
            case CARD_ITEM:
                // Get table name from uri
                List<String> segments = uri.getPathSegments();
                final String table = "'" + segments.get(segments.size() - 1) + "'";
                builder.setTables(table);
                builder.appendWhere(CardSet._ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Cursor cursor = builder.query(db,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);

        // Notify all listeners of changes
        cursor.setNotificationUri(context.getContentResolver(), uri);
        return cursor;
    }

    /**
     * Queries the database for all the titles in the main table.
     */
    public Cursor fetchAllTitles() {
        return query(SetList.CONTENT_URI,
                new String[]{SetList.SET_TITLE},
                null,
                null,
                PreferencesHelper.getOrder(context, SetList.SET_TITLE,
                        Constants.PREF_KEY_SET_ORDER));
    }

    /**
     * Queries the database for all the terms and definitions in a given table.
     */
    public Cursor fetchAllCards(String tableName) {
        return query(Uri.withAppendedPath(CardSet.CONTENT_URI, tableName),
                new String[]{CardSet._ID, CardSet.TERM, CardSet.DEFINITION, CardSet.STAR},
                null,
                null,
                PreferencesHelper.getOrder(context, CardSet.TERM,
                        Constants.PREF_KEY_CARD_ORDER));
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case SET_LIST:
                return SetList.CONTENT_TYPE;
            case SET_ITEM:
                return SetList.CONTENT_ITEM_TYPE;
            case CARD_LIST:
                return CardSet.CONTENT_TYPE;
            case CARD_ITEM:
                return CardSet.CONTENT_ITEM_TYPE;
            // Unsupported type
            default:
                return null;
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        // Validate URI
        if (URI_MATCHER.match(uri) == SET_LIST ||
                URI_MATCHER.match(uri) == SET_ITEM) {
            throw new IllegalArgumentException("Cannot insert new sets directly into main table.");
        }
        if (URI_MATCHER.match(uri) == CARD_ITEM) {
            throw new IllegalArgumentException("Use update() to edit a flashcard.");
        }
        if (URI_MATCHER.match(uri) != CARD_LIST) {
            throw new IllegalArgumentException("Uri not supported for insertion: " + uri);
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        // Insert a new card into the set
        final String table_name = uri.getLastPathSegment();
        long id = db.insert(table_name, null, contentValues);

        // Notify all listeners of changes
        if (id > 0) {
            Uri returnUri = ContentUris.withAppendedId(uri, id);
            context.getContentResolver().notifyChange(returnUri, null);
            return returnUri;
        }

        // Failed to insert new card
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int deleteCount;

        // Handle update differently depending on the uri
        switch (URI_MATCHER.match(uri)) {
            case SET_LIST: {
                deleteCount = db.delete(SetList.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            }
            // Used for updating the set name
            case SET_ITEM: {
                // Build where statement
                final String id = uri.getLastPathSegment();
                String where = SetList._ID + "=" + id;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                deleteCount = db.delete(SetList.TABLE_NAME,
                        where,
                        selectionArgs
                );
                break;
            }
            case CARD_LIST: {
                final String table = uri.getLastPathSegment();
                deleteCount = db.delete(
                        table,
                        selection,
                        selectionArgs
                );
                break;
            }
            // Used for updating individual cards
            case CARD_ITEM: {
                final String id = uri.getLastPathSegment();
                String where = SetList._ID + "=" + id;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                List<String> segments = uri.getPathSegments();
                final String table = segments.get(segments.size() - 1);
                deleteCount = db.delete(
                        table,
                        where,
                        selectionArgs
                );
                break;
            }
            default: {
                throw new IllegalArgumentException("Unsupported URI: " + uri);
            }
        }
        if (deleteCount > 0) {
            context.getContentResolver().notifyChange(uri, null);
        }
        return deleteCount;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int updateCount;

        // Handle update differently depending on the uri
        switch (URI_MATCHER.match(uri)) {
            case SET_LIST: {
                updateCount = db.update(SetList.TABLE_NAME,
                        contentValues,
                        selection,
                        selectionArgs
                );
                break;
            }
            // Used for updating the set name
            case SET_ITEM: {
                final String id = uri.getLastPathSegment();
                String where = SetList._ID + "=" + id;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                updateCount = db.update(SetList.TABLE_NAME,
                        contentValues,
                        where,
                        selectionArgs
                );
                break;
            }
            case CARD_LIST: {
                final String table = uri.getLastPathSegment();
                updateCount = db.update(table,
                        contentValues,
                        selection,
                        selectionArgs
                );
                break;
            }
            // Used for updating individual cards
            case CARD_ITEM: {
                final String id = uri.getLastPathSegment();
                String where = SetList._ID + "=" + id;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                List<String> segments = uri.getPathSegments();
                final String table = segments.get(segments.size() - 1);
                updateCount = db.update(table,
                        contentValues,
                        where,
                        selectionArgs
                );
                break;
            }
            default: {
                throw new IllegalArgumentException("Unsupported URI: " + uri);
            }
        }
        if (updateCount > 0) {
            context.getContentResolver().notifyChange(uri, null);
        }
        return updateCount;
    }

    /**
     * Returns a table name given the table's title.
     */
    public String getTableName(String title) {
        // Find the table's row ID in the main table
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor = db.query(SetList.TABLE_NAME,
                new String[]{SetList._ID},
                SetList.SET_TITLE + "=?",
                new String[]{title},
                null,
                null,
                null);
        String tableName = null;
        if (cursor != null && cursor.moveToFirst()) {
            tableName = "w" + cursor.getString(cursor.getColumnIndex(SetList._ID)) + "f";
            cursor.close();
        }
        return tableName;
    }

    /**
     * Checks if the given title is available.
     */
    private boolean titleAvailable(String title) {
        Cursor cursor = query(SetList.CONTENT_URI,
                new String[]{SetList.SET_TITLE},
                SetList.SET_TITLE + "=?",
                new String[]{title},
                null);
        if (cursor != null && cursor.getCount() == 0) {
            cursor.close();
            return true;
        }
        return false;
    }

    /**
     * Queries the main table to create a table name from the next row ID.
     */
    private String nextTableName() {
        final String query =
                "SELECT seq FROM sqlite_sequence WHERE name = '" + SetList.TABLE_NAME + "'";
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        final String nextTableName = cursor.moveToFirst() ?
                "w" + (cursor.getLong(cursor.getColumnIndex("seq")) + 1) + "f" : null;
        cursor.close();
        return nextTableName;
    }

    /**
     * Creates an empty set of flashcards.
     */
    public Boolean newSetTable(String title) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        if (titleAvailable(title)) {
            // Get the next available table name
            final String CARDSET_TABLE_CREATE;
            CARDSET_TABLE_CREATE = "CREATE TABLE '" + nextTableName() + "' (" +
                    CardSet._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    CardSet.TERM + " TEXT NOT NULL," +
                    CardSet.DEFINITION + " TEXT NOT NULL," +
                    CardSet.STAR + " INTEGER DEFAULT 0)";
            db.execSQL(CARDSET_TABLE_CREATE);

            // Link new set to main database
            ContentValues values = new ContentValues();
            values.put(SetList.SET_TITLE, title);
            db.insert(SetList.TABLE_NAME, null, values);
            return true;
        }
        return false;
    }

    /**
     * Deletes a stack of flashcards from the database.
     */
    public void deleteSetTable(long tableRowId) {
        // Get the table's title
        Cursor cursor = this.query(SetList.CONTENT_URI,
                new String[]{SetList.SET_TITLE},
                SetList._ID + "=?",
                new String[]{String.valueOf(tableRowId)},
                null);
        if (cursor != null && cursor.moveToFirst()) {
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            final String title = cursor.getString(cursor.getColumnIndex(SetList.SET_TITLE));

            // Remove set from main database
            db.execSQL("DROP TABLE IF EXISTS '" + getTableName(title) + "'");
            delete(SetList.CONTENT_URI, SetList.SET_TITLE + " = ?", new String[]{title});
            cursor.close();
        }
    }

    /**
     * Renames a set of flashcards.
     */
    public boolean renameSet(String oldTitle, String newTitle) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        // Update set name in main table
        if (titleAvailable(newTitle)) {
            ContentValues values = new ContentValues();
            values.put(SetList.SET_TITLE, newTitle);
            db.update(SetList.TABLE_NAME, values, SetList.SET_TITLE + "=?", new String[]{oldTitle});
            return true;
        }
        return false;
    }

    /**
     * Check if a term already exists in a stack.
     */
    public boolean termAvailable(String term, Uri uri) {
        Cursor cursor = query(uri,
                new String[]{CardSet.TERM},
                CardSet.TERM + "=?",
                new String[]{term},
                null);

        // Term does not exist yet
        if (cursor != null && cursor.getCount() == 0) {
            cursor.close();
            return true;
        }
        return false;
    }

    /**
     * Edits a card.
     */
    public boolean editCard(Uri uri, String oldTerm, String newTerm, String newDef) {
        // Check if new term already exists
        if (!oldTerm.equals(newTerm) && !termAvailable(newTerm, uri)) {
            return false;
        }

        // Get row id in the set table
        Cursor cursor = query(uri,
                new String[]{CardSet.TERM, CardSet._ID},
                CardSet.TERM + "=?",
                new String[]{oldTerm},
                null);
        if (cursor != null && cursor.moveToFirst()) {
            String rowId = String.valueOf(cursor.getLong(cursor.getColumnIndex(CardSet._ID)));
            cursor.close();

            // Update term and definition in the set table
            ContentValues values = new ContentValues();
            values.put(CardSet.TERM, newTerm);
            values.put(CardSet.DEFINITION, newDef);
            update(uri, values, CardSet._ID + "=?", new String[]{rowId});
            cursor.close();
            return true;
        }
        return false;
    }

    /**
     * Flips a card's star flag.
     */
    public void flipStar(Uri uri, long id) {
        // Get the current flag value
        Cursor cursor = query(uri,
                new String[]{CardSet._ID, CardSet.STAR},
                CardSet._ID + "=?",
                new String[]{String.valueOf(id)},
                null);

        // Update the star flag
        if (cursor != null && cursor.moveToFirst()) {
            final int flippedValue = Math.abs(cursor.getInt(cursor.getColumnIndex(CardSet.STAR)) - 1);
            ContentValues values = new ContentValues();
            values.put(CardSet.STAR, flippedValue);
            update(uri, values, CardSet._ID + "=?", new String[]{String.valueOf(id)});
            cursor.close();
        }
    }
}
