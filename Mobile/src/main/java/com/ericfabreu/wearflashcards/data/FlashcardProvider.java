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

import com.ericfabreu.wearflashcards.activities.MainActivity;
import com.ericfabreu.wearflashcards.data.FlashcardContract.CardSet;
import com.ericfabreu.wearflashcards.data.FlashcardContract.SetList;

import java.util.List;

/**
 * Manages access to the flashcard database.
 * http://developer.android.com/guide/topics/providers/content-provider-basics.html
 * http://www.grokkingandroid.com/android-tutorial-writing-your-own-content-provider/
 * http://www.sitepoint.com/using-androids-content-providers-manage-app-data/
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

    // Get caller context
    Context context = null;
    // Handle to the database helper object
    private FlashcardDbHelper mOpenHelper;

    public FlashcardProvider(Context c) {
        context = c;
    }

    // Empty constructor
    public FlashcardProvider() {
    }

    // Create a FlashcardDBHelper
    @Override
    public boolean onCreate() {
        mOpenHelper = new FlashcardDbHelper(getContext());
        return true;
    }

    private Context providerContext() {
        // Use given context if there is one
        if (context != null) {
            return context;
        }

        // Get context from MainActivity
        return MainActivity.getContext();
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        mOpenHelper = new FlashcardDbHelper(providerContext());
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

        // Issue query statement
        Cursor cursor = builder.query(db,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);

        // Notify all listeners of changes
        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return cursor;
    }

    public Cursor fetchAllTitles() {
        return query(SetList.CONTENT_URI,
                new String[]{SetList.SET_TITLE},
                null,
                null,
                null);
    }

    public Cursor fetchAllCards(String tableName) {
        return query(Uri.withAppendedPath(CardSet.CONTENT_URI, tableName),
                new String[]{CardSet.TERM, CardSet.DEFINITION},
                null,
                null,
                null);
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
            throw new IllegalArgumentException("Cannot insert new sets directly into activity_main table.");
        }
        if (URI_MATCHER.match(uri) == CARD_ITEM) {
            throw new IllegalArgumentException("Use update() to edit a flashcard.");
        }
        if (URI_MATCHER.match(uri) != CARD_LIST) {
            throw new IllegalArgumentException("Uri not supported for insertion: " + uri);
        }

        mOpenHelper = new FlashcardDbHelper(providerContext());
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        // Insert a new card into the set
        final String table_name = uri.getLastPathSegment();
        long id = db.insert(table_name, null, contentValues);

        // Notify all listeners of changes and return
        if (id > 0) {
            Uri returnUri = ContentUris.withAppendedId(uri, id);
            providerContext().getContentResolver().notifyChange(returnUri, null);
            return returnUri;
        }

        // Failed to insert new card
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        mOpenHelper = new FlashcardDbHelper(providerContext());
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int deleteCount;

        // Handle update differently depending on the uri
        switch (URI_MATCHER.match(uri)) {
            case SET_LIST: {
                deleteCount = db.delete(
                        SetList.TABLE_NAME,
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

                // Issue update statement
                deleteCount = db.delete(
                        SetList.TABLE_NAME,
                        where,
                        selectionArgs
                );
                break;
            }

            case CARD_LIST: {
                // Get table name from uri
                final String table = uri.getLastPathSegment();

                // Issue update statement
                deleteCount = db.delete(
                        table,
                        selection,
                        selectionArgs
                );
                break;
            }

            // Used for updating individual cards
            case CARD_ITEM: {
                // Build where statement
                final String id = uri.getLastPathSegment();
                String where = SetList._ID + "=" + id;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }

                // Get table name from uri
                List<String> segments = uri.getPathSegments();
                final String table = segments.get(segments.size() - 1);

                // Issue update statement
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

        // Notify all listeners of changes
        if (deleteCount > 0) {
            providerContext().getContentResolver().notifyChange(uri, null);
        }

        return deleteCount;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        mOpenHelper = new FlashcardDbHelper(providerContext());
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int updateCount;

        // Handle update differently depending on the uri
        switch (URI_MATCHER.match(uri)) {
            case SET_LIST: {
                updateCount = db.update(
                        SetList.TABLE_NAME,
                        contentValues,
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

                // Issue update statement
                updateCount = db.update(
                        SetList.TABLE_NAME,
                        contentValues,
                        where,
                        selectionArgs
                );
                break;
            }

            case CARD_LIST: {
                // Get table name from uri
                final String table = uri.getLastPathSegment();

                // Issue update statement
                updateCount = db.update(
                        table,
                        contentValues,
                        selection,
                        selectionArgs
                );
                break;
            }

            // Used for updating individual cards
            case CARD_ITEM: {
                // Build where statement
                final String id = uri.getLastPathSegment();
                String where = SetList._ID + "=" + id;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }

                // Get table name from uri
                List<String> segments = uri.getPathSegments();
                final String table = segments.get(segments.size() - 1);

                // Issue update statement
                updateCount = db.update(
                        table,
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

        // Notify all listeners of changes
        if (updateCount > 0) {
            providerContext().getContentResolver().notifyChange(uri, null);
        }

        return updateCount;
    }

    /**
     * Returns a table name based on the given title.
     */
    public String getTableName(String title) {
        // Remove all non-alphabetic characters and convert the letters to lower-case
        String tableName = title.replaceAll("[\\W\\s]", "");
        tableName = tableName.toLowerCase();

        // Table name cannot start with a number
        if (Character.isDigit(tableName.charAt(0))) {
            tableName = "n" + tableName;
        }

        // Add two characters to the end of the table name to ensure that the name is not an SQLite command
        // No SQLite command ends with wf (the app's initials), so this should be safe
        tableName += "wf";
        return tableName;
    }

    /**
     * Creates an empty set of flashcards.
     */
    public Boolean newSetTable(String title) {
        // Get the data repository in write mode
        mOpenHelper = new FlashcardDbHelper(providerContext());
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        // Get table name from set title
        String tableName = getTableName(title);

        // Check if the table already exists
        Cursor cursor = query(SetList.CONTENT_URI,
                new String[]{SetList.SET_TABLE_NAME},
                SetList.SET_TABLE_NAME + "=?",
                new String[]{tableName},
                null);

        // Table does not exist
        if (cursor != null && cursor.getCount() == 0) {
            final String CARDSET_TABLE_CREATE;
            CARDSET_TABLE_CREATE = "CREATE TABLE '" + tableName + "' (" +
                    CardSet._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    CardSet.TERM + " TEXT NOT NULL," +
                    CardSet.DEFINITION + " TEXT NOT NULL);";
            db.execSQL(CARDSET_TABLE_CREATE);

            // Link new set to activity_main database
            ContentValues values = new ContentValues();
            values.put(SetList.SET_TITLE, title);
            values.put(SetList.SET_TABLE_NAME, tableName);
            db.insert(SetList.TABLE_NAME, null, values);
            cursor.close();
            return true;
        }

        return false;
    }

    /**
     * Deletes a stack of flashcards from the database.
     */
    public void deleteSetTable(String title) {
        // Get the data repository in write mode
        mOpenHelper = new FlashcardDbHelper(providerContext());
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        // Get table name from set title
        String tableName = getTableName(title);

        // Remove set from activity_main database
        db.execSQL("DROP TABLE IF EXISTS '" + tableName + "'");
        this.delete(SetList.CONTENT_URI, SetList.SET_TITLE + " = ?", new String[]{title});
    }

    /**
     * Renames a set of flashcards.
     */
    public boolean renameSet(String oldTitle, String newTitle) {
        // Get the data repository in write mode
        mOpenHelper = new FlashcardDbHelper(providerContext());
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        // Get table names from set titles
        String oldName = getTableName(oldTitle);
        String newName = getTableName(newTitle);

        // Check if the new title is taken
        Cursor cursor = query(SetList.CONTENT_URI,
                new String[]{SetList.SET_TABLE_NAME},
                SetList.SET_TABLE_NAME + "=?",
                new String[]{newName},
                null);

        // New title is not taken
        if (cursor != null && cursor.getCount() == 0) {
            // Rename flashcard table
            db.execSQL("ALTER TABLE '" + oldName + "' RENAME TO '" + newName + "'");

            // Get row id in activity_main table
            Cursor oldCursor = query(SetList.CONTENT_URI,
                    new String[]{SetList.SET_TABLE_NAME, SetList._ID},
                    SetList.SET_TABLE_NAME + "=?",
                    new String[]{oldName},
                    null);
            String rowId;
            if (oldCursor != null && oldCursor.moveToFirst()) {
                rowId = String.valueOf(oldCursor.getLong(oldCursor.getColumnIndex(SetList._ID)));
                oldCursor.close();

                // Update set name in activity_main table
                ContentValues values = new ContentValues();
                values.put(SetList.SET_TITLE, newTitle);
                values.put(SetList.SET_TABLE_NAME, newName);
                db.update(SetList.TABLE_NAME, values, SetList._ID + "=?", new String[]{rowId});
            }

            cursor.close();
            return true;
        }

        return false;
    }

    /**
     * Check if a term already exists in a stack.
     */
    public boolean termExists(String term, Uri uri) {
        Cursor cursor = query(uri,
                new String[]{CardSet.TERM},
                CardSet.TERM + "=?",
                new String[]{term},
                null);

        // Term does not exist yet
        if (cursor != null && cursor.getCount() == 0) {
            cursor.close();
            return false;
        }

        return true;
    }

    /**
     * Edits a card.
     */
    public boolean editCard(Uri uri, String oldTerm, String newTerm, String newDef) {
        // Check if new term already exists
        if (!oldTerm.equals(newTerm) && termExists(newTerm, uri)) {
            return false;
        }

        // Get row id in the set table
        Cursor cursor = query(uri,
                new String[]{CardSet.TERM, CardSet._ID},
                CardSet.TERM + "=?",
                new String[]{oldTerm},
                null);
        String rowId;
        if (cursor != null && cursor.moveToFirst()) {
            rowId = String.valueOf(cursor.getLong(cursor.getColumnIndex(CardSet._ID)));
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
}
