package com.ericfabreu.wearflashcards.sync;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.ericfabreu.wearflashcards.data.FlashcardContract.CardSet;
import com.ericfabreu.wearflashcards.data.FlashcardContract.FolderEntry;
import com.ericfabreu.wearflashcards.data.FlashcardContract.FolderList;
import com.ericfabreu.wearflashcards.data.FlashcardContract.SetList;
import com.ericfabreu.wearflashcards.data.FlashcardProvider;
import com.ericfabreu.wearflashcards.utils.Constants;
import com.ericfabreu.wearflashcards.utils.PreferencesHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;
import java.util.Date;

/**
 * Provides data to the wearable app.
 */
public class WearableService extends WearableListenerService {
    private static final String PATH = "/WearFlashcardsP", TAG_TIME = "time", TAG_MODE = "mode",
            TAG_TITLE_LIST = "title_list", TAG_CARD_ID = "card_id", TAG_TERMS = "terms",
            TAG_DEFINITIONS = "definitions", TAG_STARRED_ONLY = "starred_only",
            TAG_STARRED_OPTION = "starred_option", TAG_TABLE_ID = "table_id";

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = event.getDataItem();
                DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                final int mode = dataMap.getInt(TAG_MODE, -1);

                // The wearable is asking for the list of sets
                if (mode == 0 || mode == 1) {
                    setTitles(mode == 1);
                }
                // The wearable needs cards from a specific set
                else if (mode >= 2 && mode <= 5) {
                    final String title = dataMap.getString(Constants.TAG_TITLE);
                    final String starredOption = dataMap.getString(TAG_STARRED_OPTION);
                    if (title != null && starredOption != null && mode < 4) {
                        sendCards(title, starredOption, mode == 3);
                    } else {
                        final long cardId = dataMap.getLong(TAG_CARD_ID);
                        FlashcardProvider handle = new FlashcardProvider(getApplicationContext());
                        // The wearable needs to flip a card's star value
                        if (title != null && cardId > 0 && mode == 4) {
                            final String tableName = handle.getTableName(title, false);
                            final Uri uri = Uri.withAppendedPath(CardSet.CONTENT_URI, tableName);
                            PreferencesHelper.flipStar(getApplicationContext(), handle, uri,
                                    cardId, CardSet.STAR);
                        } else {
                            final long tableId = dataMap.getLong(TAG_TABLE_ID);
                            final String tableName = handle.getTableName(tableId, false);
                            final Uri uri = Uri.withAppendedPath(CardSet.CONTENT_URI, tableName);
                            PreferencesHelper.flipStar(getApplicationContext(), handle, uri,
                                    cardId, CardSet.STAR);
                        }
                    }
                }
            }
        }
    }

    /**
     * Sends a list of all sets to the wearable device.
     */
    private void setTitles(boolean folder) {
        // Get titles from the database
        FlashcardProvider handle = new FlashcardProvider(getApplicationContext());
        Cursor cursor = handle.fetchAllTitles(folder);
        if (cursor == null) {
            return;
        }

        // Put titles in a string array
        ArrayList<String> columnArray = new ArrayList<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            columnArray.add(cursor.getString(cursor
                    .getColumnIndex(folder ? FolderList.FOLDER_TITLE : SetList.SET_TITLE)));
        }
        String[] setList = columnArray.toArray(new String[columnArray.size()]);

        // Send data to the wearable
        GoogleApiClient mGoogleApiClient = wearConnect();
        final PutDataMapRequest putRequest = PutDataMapRequest.create(PATH);
        final DataMap map = putRequest.getDataMap();
        map.putStringArray(TAG_TITLE_LIST, setList);
        map.putLong(TAG_TIME, new Date().getTime());
        Wearable.DataApi.putDataItem(mGoogleApiClient, putRequest.asPutDataRequest());
    }

    /**
     * Sends all the cards in a set to the wearable device.
     */
    private void sendCards(String title, String starredOption, boolean folder) {
        // Get terms and definitions from the database
        FlashcardProvider handle = new FlashcardProvider(getApplicationContext());
        final String tableName = handle.getTableName(title, folder);
        final long tableId = handle.getTableId(title, folder);
        final boolean starredOnly;

        // Respect the wearable's starred only setting
        switch (starredOption) {
            case "0": {
                starredOnly = PreferencesHelper.getStar(getApplicationContext(), handle,
                        folder ? FolderList.CONTENT_URI : SetList.CONTENT_URI, tableId,
                        folder ? FolderList.STARRED_ONLY : SetList.STARRED_ONLY);
                break;
            }
            case "1": {
                starredOnly = false;
                break;
            }
            default: {
                starredOnly = true;
                break;
            }
        }

        ArrayList<Long> tempIds = new ArrayList<>(), tempTableIds = new ArrayList<>();
        ArrayList<String> terms = new ArrayList<>(), definitions = new ArrayList<>();
        ArrayList<Integer> stars = new ArrayList<>();

        // Check if it needs to load more than one set
        if (folder) {
            Cursor cursor = handle.query(Uri.withAppendedPath(FolderEntry.CONTENT_URI, tableName),
                    new String[]{FolderEntry.SET_ID}, null, null, null);
            if (cursor != null) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    final long setId = cursor.getLong(cursor.getColumnIndex(FolderEntry.SET_ID));
                    final String setTable = handle.getTableName(setId, false);
                    Cursor set = handle.fetchAllCards(setTable, starredOnly);
                    for (set.moveToFirst(); !set.isAfterLast(); set.moveToNext()) {
                        terms.add(set.getString(set.getColumnIndex(CardSet.TERM)));
                        definitions.add(set.getString(set.getColumnIndex(CardSet.DEFINITION)));
                        stars.add(set.getInt(set.getColumnIndex(CardSet.STAR)));
                        tempIds.add(set.getLong(set.getColumnIndex(CardSet._ID)));
                        tempTableIds.add(setId);
                    }
                    set.close();
                }
                cursor.close();
            }
        } else {
            Cursor cursor = handle.fetchAllCards(tableName, starredOnly);
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                tempIds.add(cursor.getLong(cursor.getColumnIndex(CardSet._ID)));
                terms.add(cursor.getString(cursor.getColumnIndex(CardSet.TERM)));
                definitions.add(cursor.getString(cursor.getColumnIndex(CardSet.DEFINITION)));
                stars.add(cursor.getInt(cursor.getColumnIndex(CardSet.STAR)));
            }
            cursor.close();
        }

        // For some reason it is not possible to send long array lists to wearable devices
        long[] ids = new long[tempIds.size()], tableIds = new long[tempTableIds.size()];
        for (int i = 0; i < tempIds.size(); i++) {
            ids[i] = tempIds.get(i);
            if (folder) {
                tableIds[i] = tempTableIds.get(i);
            }
        }

        // Send data to the wearable
        GoogleApiClient mGoogleApiClient = wearConnect();
        final PutDataMapRequest putRequest = PutDataMapRequest.create(PATH);
        final DataMap map = putRequest.getDataMap();
        map.putLongArray(Constants.TAG_ID, ids);
        if (folder) {
            map.putLongArray(Constants.TAG_FOLDER_ID, tableIds);
        }
        map.putStringArrayList(TAG_TERMS, terms);
        map.putStringArrayList(TAG_DEFINITIONS, definitions);
        map.putIntegerArrayList(Constants.TAG_STAR, stars);
        map.putBoolean(TAG_STARRED_ONLY, starredOnly);
        map.putLong(TAG_TIME, new Date().getTime());
        Wearable.DataApi.putDataItem(mGoogleApiClient, putRequest.asPutDataRequest());
    }

    private GoogleApiClient wearConnect() {
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult result) {
                    }
                })
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
        return mGoogleApiClient;
    }
}