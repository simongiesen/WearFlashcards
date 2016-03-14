package com.efa.wearflashcards.data;

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

import com.efa.wearflashcards.Main;
import com.efa.wearflashcards.data.FlashcardContract.CardSet;
import com.efa.wearflashcards.data.FlashcardContract.SetList;

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

        // Get context from Main
        return Main.getContext();
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
            throw new IllegalArgumentException("Cannot insert new sets directly into main table.");
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
        Cursor cursor;
        try {
            cursor = db.rawQuery("SELECT * FROM '" + tableName + "'", null);
        } catch (Exception e) {
            // Build the CREATE command
            final String CARDSET_TABLE_CREATE;
            CARDSET_TABLE_CREATE = "CREATE TABLE '" + tableName + "' (" +
                    CardSet._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    CardSet.TERM + " TEXT NOT NULL," +
                    CardSet.DEFINITION + " TEXT NOT NULL);";
            db.execSQL(CARDSET_TABLE_CREATE);

            // Link new set to main database
            ContentValues values = new ContentValues();
            values.put(SetList.SET_TITLE, title);
            values.put(SetList.SET_TABLE_NAME, tableName);
            db.insert(SetList.TABLE_NAME, null, values);
            return true;
        }

        cursor.close();
        return false;
    }

    public void deleteSetTable(String title) {
        // Get the data repository in write mode
        mOpenHelper = new FlashcardDbHelper(providerContext());
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        // Get table name from set title
        String tableName = getTableName(title);

        // Remove set from main database
        db.execSQL("DROP TABLE IF EXISTS '" + tableName + "'");
        this.delete(SetList.CONTENT_URI, SetList.SET_TITLE + " = ?", new String[]{title});
    }
}
