package com.ericfabreu.wearflashcards.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.ericfabreu.wearflashcards.data.FlashcardContract.CardSet;
import com.ericfabreu.wearflashcards.data.FlashcardContract.FolderEntry;
import com.ericfabreu.wearflashcards.data.FlashcardContract.FolderList;
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
    private static final int FOLDER_LIST = 5;
    private static final int FOLDER_LIST_ITEM = 6;
    private static final int FOLDER = 7;
    private static final int FOLDER_ITEM = 8;
    private static final UriMatcher URI_MATCHER;

    // Prepare the UriMatcher
    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(FlashcardContract.AUTHORITY, SetList.TABLE_DIR, 1);
        URI_MATCHER.addURI(FlashcardContract.AUTHORITY, SetList.TABLE_DIR + "/#", 2);
        URI_MATCHER.addURI(FlashcardContract.AUTHORITY, CardSet.TABLE_DIR + "/*", 3);
        URI_MATCHER.addURI(FlashcardContract.AUTHORITY, CardSet.TABLE_DIR + "/*/#", 4);
        URI_MATCHER.addURI(FlashcardContract.AUTHORITY, FolderList.TABLE_DIR, 5);
        URI_MATCHER.addURI(FlashcardContract.AUTHORITY, FolderList.TABLE_DIR + "/#", 6);
        URI_MATCHER.addURI(FlashcardContract.AUTHORITY, FolderEntry.TABLE_DIR + "/*", 7);
        URI_MATCHER.addURI(FlashcardContract.AUTHORITY, FolderEntry.TABLE_DIR + "/*/#", 8);
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
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

        // Handle table name and order differently depending on the uri
        switch (URI_MATCHER.match(uri)) {
            case SET_LIST: {
                builder.setTables(SetList.TABLE_NAME);
                break;
            }
            case SET_ITEM: {
                builder.setTables(SetList.TABLE_NAME);
                builder.appendWhere(SetList._ID + "=" + uri.getLastPathSegment());
                break;
            }
            case CARD_LIST: {
                builder.setTables("'" + uri.getLastPathSegment() + "'");
                break;
            }
            case CARD_ITEM: {
                // Get table name from uri
                List<String> segments = uri.getPathSegments();
                final String table = "'" + segments.get(segments.size() - 1) + "'";
                builder.setTables(table);
                builder.appendWhere(CardSet._ID + "=" + uri.getLastPathSegment());
                break;
            }
            case FOLDER_LIST: {
                builder.setTables(FolderList.TABLE_NAME);
                break;
            }
            case FOLDER_LIST_ITEM: {
                builder.setTables(FolderList.TABLE_NAME);
                builder.appendWhere(FolderList._ID + "=" + uri.getLastPathSegment());
                break;
            }
            case FOLDER: {
                builder.setTables("'" + uri.getLastPathSegment() + "'");
                break;
            }
            case FOLDER_ITEM: {
                // Get table name from uri
                List<String> segments = uri.getPathSegments();
                final String table = "'" + segments.get(segments.size() - 1) + "'";
                builder.setTables(table);
                builder.appendWhere(FolderEntry._ID + "=" + uri.getLastPathSegment());
                break;
            }
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
     * Queries the database for all the set or folder titles in the main table.
     */
    public Cursor fetchAllTitles(boolean folder) {
        return query(folder ? FolderList.CONTENT_URI : SetList.CONTENT_URI,
                new String[]{folder ? FolderList.FOLDER_TITLE : SetList.SET_TITLE},
                null,
                null,
                PreferencesHelper.getOrder(context, folder ? FolderList.FOLDER_TITLE
                        : SetList.SET_TITLE, Constants.PREF_KEY_SET_ORDER));
    }

    /**
     * Queries the database for all the terms and definitions in a given table.
     */
    public Cursor fetchAllCards(String tableName, boolean starredOnly) {
        final String selection = starredOnly ? CardSet.STAR + "=?" : null;
        final String[] selectionArgs = starredOnly ? new String[]{"1"} : null;
        return query(Uri.withAppendedPath(CardSet.CONTENT_URI, tableName),
                new String[]{CardSet._ID, CardSet.TERM, CardSet.DEFINITION, CardSet.STAR},
                selection,
                selectionArgs,
                PreferencesHelper.getOrder(context, CardSet.TERM,
                        Constants.PREF_KEY_CARD_ORDER));
    }

    /**
     * Queries the database for all sets in a given folder.
     */
    public Cursor fetchFolderSets(String tableName) {
        return query(Uri.withAppendedPath(FolderEntry.CONTENT_URI, tableName),
                new String[]{FolderEntry.SET_ID},
                null,
                null,
                null);
    }

    /**
     * Queries the database for all sets not inside a given folder.
     */
    public Cursor fetchComplementSets(String tableName) {
        final String where = SetList._ID + " NOT IN (SELECT " +
                FolderEntry.SET_ID + " FROM " + tableName + ")";
        return query(SetList.CONTENT_URI,
                new String[]{SetList.SET_TITLE, SetList._ID},
                where,
                null,
                PreferencesHelper.getOrder(context, SetList.SET_TITLE,
                        Constants.PREF_KEY_SET_ORDER));
    }

    /**
     * Checks if the folder has any cards in its sets.
     */
    public boolean isFolderStudiable(String table, boolean starredOnly) {
        Cursor cursor = query(Uri.withAppendedPath(FolderEntry.CONTENT_URI, table),
                new String[]{FolderEntry.SET_ID}, null, null, null);
        // Go through all the sets in the folder until it finds a valid card
        if (cursor != null) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                final long setId = cursor.getLong(cursor.getColumnIndex(FolderEntry.SET_ID));
                final String setTable = getTableName(setId, false);
                Cursor set = fetchAllCards(setTable, starredOnly);
                if (set != null) {
                    if (set.getCount() > 0) {
                        set.close();
                        return true;
                    }
                }
            }
            cursor.close();
        }
        return false;
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
            case FOLDER_LIST:
                return FolderList.CONTENT_TYPE;
            case FOLDER_LIST_ITEM:
                return FolderList.CONTENT_ITEM_TYPE;
            case FOLDER:
                return FolderEntry.CONTENT_TYPE;
            case FOLDER_ITEM:
                return FolderEntry.CONTENT_ITEM_TYPE;
            // Unsupported type
            default:
                return null;
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        // Validate URI
        if (URI_MATCHER.match(uri) == SET_LIST ||
                URI_MATCHER.match(uri) == SET_ITEM ||
                URI_MATCHER.match(uri) == FOLDER_LIST ||
                URI_MATCHER.match(uri) == FOLDER_LIST_ITEM) {
            throw new IllegalArgumentException("Cannot insert this directly into main table.");
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
            case FOLDER_LIST: {
                deleteCount = db.delete(FolderList.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            }
            // Used for updating the folder name
            case FOLDER_LIST_ITEM: {
                // Build where statement
                final String id = uri.getLastPathSegment();
                String where = FolderList._ID + "=" + id;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                deleteCount = db.delete(FolderList.TABLE_NAME,
                        where,
                        selectionArgs
                );
                break;
            }
            case FOLDER: {
                final String table = uri.getLastPathSegment();
                deleteCount = db.delete(
                        table,
                        selection,
                        selectionArgs
                );
                break;
            }
            // Used for updating individual cards
            case FOLDER_ITEM: {
                final String id = uri.getLastPathSegment();
                String where = FolderEntry._ID + "=" + id;
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
    public int update(@NonNull Uri uri, ContentValues contentValues,
                      String selection, String[] selectionArgs) {
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
            case FOLDER_LIST: {
                updateCount = db.update(FolderList.TABLE_NAME,
                        contentValues,
                        selection,
                        selectionArgs
                );
                break;
            }
            // Used for updating the folder name
            case FOLDER_LIST_ITEM: {
                final String id = uri.getLastPathSegment();
                String where = FolderList._ID + "=" + id;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                updateCount = db.update(FolderList.TABLE_NAME,
                        contentValues,
                        where,
                        selectionArgs
                );
                break;
            }
            case FOLDER: {
                final String table = uri.getLastPathSegment();
                updateCount = db.update(table,
                        contentValues,
                        selection,
                        selectionArgs
                );
                break;
            }
            case FOLDER_ITEM: {
                final String id = uri.getLastPathSegment();
                String where = FolderEntry._ID + "=" + id;
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
     * Returns the table's row id in the main table.
     */
    public long getTableId(String title, boolean folder) {
        final String table = folder ? FolderList.TABLE_NAME : SetList.TABLE_NAME;
        final String column = folder ? FolderList.FOLDER_TITLE : SetList.SET_TITLE;
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor = db.query(table,
                new String[]{"_id"},
                column + "=?",
                new String[]{title},
                null,
                null,
                null);
        if (cursor != null && cursor.moveToFirst()) {
            final long tableId = cursor.getLong(cursor.getColumnIndex("_id"));
            cursor.close();
            return tableId;
        }
        return 0;
    }

    /**
     * Returns a table name given the table's title.
     */
    public String getTableName(String title, boolean folder) {
        final String prefix = folder ? "f" : "w";
        final long id = getTableId(title, folder);
        return id == 0 ? null : prefix + id + "f";
    }

    /**
     * Returns the title of a given set or folder.
     */
    public String getTitle(long tableId, boolean folder) {
        final Uri uri = folder ? FolderList.CONTENT_URI : SetList.CONTENT_URI;
        final String[] column = new String[]{folder ? FolderList.FOLDER_TITLE : SetList.SET_TITLE};
        final Cursor cursor = query(uri,
                column,
                "_id=?",
                new String[]{String.valueOf(tableId)},
                null);
        if (cursor != null && cursor.moveToFirst()) {
            final String title = cursor.getString(cursor.getColumnIndex(column[0]));
            cursor.close();
            return title;
        }
        return null;
    }

    /**
     * Returns a table name given the table's id.
     */
    public String getTableName(long tableId, boolean folder) {
        return folder ? "f" + tableId + "f" : "w" + tableId + "f";
    }

    /**
     * Checks if the given title is available.
     */
    private boolean titleAvailable(String title, boolean folder) {
        final Uri uri = folder ? FolderList.CONTENT_URI : SetList.CONTENT_URI;
        final String column = folder ? FolderList.FOLDER_TITLE : SetList.SET_TITLE;
        Cursor cursor = query(uri,
                new String[]{column},
                column + "=?",
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
    private String nextTableName(boolean folder) {
        final String table = folder ? FolderList.TABLE_NAME : SetList.TABLE_NAME;
        final String query = "SELECT seq FROM sqlite_sequence WHERE name = '" + table + "'";
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        final String prefix = folder ? "f" : "w";
        final long middle = cursor.moveToFirst() ?
                (cursor.getLong(cursor.getColumnIndex("seq")) + 1) : 1;
        cursor.close();
        return prefix + middle + "f";
    }

    /**
     * Creates an empty table and links it to the proper main table.
     */
    public Boolean newTable(String title, boolean folder) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        if (titleAvailable(title, folder)) {
            final String columnDefinitions = folder ? FolderEntry.COLUMN_DEFINITIONS
                    : CardSet.COLUMN_DEFINITIONS;
            final String tableCommand = "CREATE TABLE '" + nextTableName(folder) +
                    columnDefinitions;
            db.execSQL(tableCommand);

            // Link new set to main database
            final String table = folder ? FolderList.TABLE_NAME : SetList.TABLE_NAME;
            final String column = folder ? FolderList.FOLDER_TITLE : SetList.SET_TITLE;
            ContentValues values = new ContentValues();
            values.put(column, title);
            db.insert(table, null, values);
            return true;
        }
        return false;
    }

    /**
     * Deletes a table (set or folder) from the database.
     */
    public void deleteTable(long tableRowId, boolean folder) {
        final Uri uri = folder ? FolderList.CONTENT_URI : SetList.CONTENT_URI;
        final String column = folder ? FolderList.FOLDER_TITLE : SetList.SET_TITLE;
        // Get the table's title
        Cursor cursor = query(uri,
                new String[]{column},
                "_id=?",
                new String[]{String.valueOf(tableRowId)},
                null);
        if (cursor != null && cursor.moveToFirst()) {
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            final String title = cursor.getString(cursor.getColumnIndex(column));

            // Remove set or folder from main database
            db.execSQL("DROP TABLE IF EXISTS '" + getTableName(title, folder) + "'");
            delete(uri, column + "=?", new String[]{title});
            cursor.close();
        }

        // Remove set from folders
        if (!folder) {
            cursor = query(FolderList.CONTENT_URI, new String[]{FolderList._ID}, null, null, null);
            if (cursor != null) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    final String table = getTableName(cursor
                            .getLong(cursor.getColumnIndex(FolderList._ID)), true);
                    delete(Uri.withAppendedPath(FolderEntry.CONTENT_URI, table),
                            FolderEntry.SET_ID + "=?", new String[]{String.valueOf(tableRowId)});
                }
                cursor.close();
            }
        }
    }

    /**
     * Removes a set from a folder.
     */
    public void removeSet(String folderTable, long setId) {
        final Uri uri = Uri.withAppendedPath(FolderEntry.CONTENT_URI, folderTable);
        delete(uri, FolderEntry.SET_ID + "=?", new String[]{String.valueOf(setId)});
    }

    /**
     * Renames a set or folder.
     */
    public boolean renameMember(String oldTitle, String newTitle, boolean folder) {
        final String table = folder ? FolderList.TABLE_NAME : SetList.TABLE_NAME;
        final String column = folder ? FolderList.FOLDER_TITLE : SetList.SET_TITLE;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        // Update set or title in main table
        if (titleAvailable(newTitle, folder)) {
            ContentValues values = new ContentValues();
            values.put(column, newTitle);
            db.update(table, values, column + "=?", new String[]{oldTitle});
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
     * Flips an integer flag in a given table.
     */
    public void flipFlag(Uri uri, long id, String flagColumn) {
        // Get the current flag value
        Cursor cursor = query(uri,
                new String[]{"_id", flagColumn},
                "_id=?",
                new String[]{String.valueOf(id)},
                null);

        // Update the star flag
        if (cursor != null && cursor.moveToFirst()) {
            final int flippedValue = Math.abs(cursor.getInt(cursor.getColumnIndex(flagColumn)) - 1);
            ContentValues values = new ContentValues();
            values.put(flagColumn, flippedValue);
            update(uri, values, "_id=?", new String[]{String.valueOf(id)});
            cursor.close();
        }
    }

    /**
     * Returns the current value of a flag.
     */
    public boolean getFlag(Uri uri, long id, String flagColumn) {
        Cursor cursor = query(uri,
                new String[]{"_id", flagColumn},
                "_id=?",
                new String[]{String.valueOf(id)},
                null);
        if (cursor != null && cursor.moveToFirst()) {
            final boolean value = cursor.getInt(cursor.getColumnIndex(flagColumn)) == 1;
            cursor.close();
            return value;
        }
        return false;
    }

    /**
     * Returns the number of starred cards.
     */
    public int getCardCount(Uri uri, boolean star) {
        Cursor cursor = query(uri,
                new String[]{CardSet.STAR},
                CardSet.STAR + "=?",
                new String[]{star ? "1" : "0"},
                null);
        if (cursor != null && cursor.moveToFirst()) {
            final int size = cursor.getCount();
            cursor.close();
            return size;
        }
        return 0;
    }

    /**
     * Returns the number of rows in {@param table}.
     */
    public long getRowCount(String table) {
        return DatabaseUtils.queryNumEntries(mOpenHelper.getReadableDatabase(), table);
    }
}
