package com.ericfabreu.wearflashcards.sync;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ericfabreu.wearflashcards.data.FlashcardContract.CardSet;
import com.ericfabreu.wearflashcards.data.FlashcardContract.SetList;
import com.ericfabreu.wearflashcards.data.FlashcardProvider;
import com.ericfabreu.wearflashcards.utils.Constants;
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
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = event.getDataItem();
                DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                final String main = dataMap.getString(Constants.MAIN);
                // The wearable is asking for the list of sets
                if (main != null && main.equals(Constants.SET_LIST)) {
                    sendSetList();
                }
                // The wearable needs cards from a specific set
                else {
                    final String title = dataMap.getString(Constants.TITLE);
                    final String starredOption = dataMap.getString(Constants.STARRED_OPTION);
                    if (title != null && starredOption != null) {
                        sendSet(title, starredOption);
                    }
                }
            }
        }
    }

    /**
     * Sends a list of all sets to the wearable device.
     */
    private void sendSetList() {
        // Get titles from the database
        FlashcardProvider handle = new FlashcardProvider(getApplicationContext());
        Cursor cursor = handle.fetchAllTitles();
        if (cursor == null) {
            return;
        }

        // Put titles in a string array
        ArrayList<String> columnArray = new ArrayList<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            columnArray.add(cursor.getString(cursor
                    .getColumnIndex(SetList.SET_TITLE)));
        }
        String[] setList = columnArray.toArray(new String[columnArray.size()]);

        // Send data to the wearable
        GoogleApiClient mGoogleApiClient = wearConnect();
        final PutDataMapRequest putRequest = PutDataMapRequest.create(Constants.SET_LIST);
        final DataMap map = putRequest.getDataMap();
        map.putStringArray(Constants.SET_LIST, setList);
        map.putLong(Constants.TIME, new Date().getTime());
        Wearable.DataApi.putDataItem(mGoogleApiClient, putRequest.asPutDataRequest());
    }

    /**
     * Sends all the cards in a set to the wearable device.
     */
    private void sendSet(String title, String starredOption) {
        Log.d("star", starredOption);
        // Get terms and definitions from the database
        FlashcardProvider handle = new FlashcardProvider(getApplicationContext());
        final String tableName = handle.getTableName(title);
        final long tableId = handle.getTableId(title);
        final boolean starredOnly = handle.getFlag(SetList.CONTENT_URI,
                tableId, SetList.STARRED_ONLY);
        Cursor cursor = handle.fetchAllCards(tableName, starredOnly);

        // Put terms and definitions into string arrays
        ArrayList<Long> tempIds = new ArrayList<>();
        ArrayList<String> terms = new ArrayList<>();
        ArrayList<String> definitions = new ArrayList<>();
        ArrayList<Integer> stars = new ArrayList<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            tempIds.add(cursor.getLong(cursor.getColumnIndex(CardSet._ID)));
            terms.add(cursor.getString(cursor.getColumnIndex(CardSet.TERM)));
            definitions.add(cursor.getString(cursor.getColumnIndex(CardSet.DEFINITION)));
            stars.add(cursor.getInt(cursor.getColumnIndex(CardSet.STAR)));
        }

        // For some reason it is not possible to send long array lists to wearable devices
        long[] ids = new long[tempIds.size()];
        for (int i = 0; i < tempIds.size(); i++) {
            ids[i] = tempIds.get(i);
        }

        // Send data to the wearable
        GoogleApiClient mGoogleApiClient = wearConnect();
        final PutDataMapRequest putRequest = PutDataMapRequest.create("/" + tableName);
        final DataMap map = putRequest.getDataMap();
        map.putLongArray(Constants.ID, ids);
        map.putStringArrayList(Constants.TERMS, terms);
        map.putStringArrayList(Constants.DEFINITIONS, definitions);
        map.putIntegerArrayList(Constants.STAR, stars);
        map.putLong(Constants.TIME, new Date().getTime());
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