package com.ericfabreu.wearflashcards.sync;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.ericfabreu.wearflashcards.data.FlashcardContract.CardSet;
import com.ericfabreu.wearflashcards.data.FlashcardContract.SetList;
import com.ericfabreu.wearflashcards.data.FlashcardProvider;
import com.ericfabreu.wearflashcards.utils.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
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
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        String message = new String(messageEvent.getData());

        // Determine if the wearable is asking for the set list or for the list of cards in a set
        if (message.equals(Constants.SET_LIST)) {
            sendSetList();
        } else {
            sendSet(message);
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
    private void sendSet(String title) {
        // Get terms and definitions from the database
        FlashcardProvider handle = new FlashcardProvider(getApplicationContext());
        final String tableName = handle.getTableName(title);
        final long tableId = handle.getTableId(title);
        final boolean starredOnly = handle.getFlag(SetList.CONTENT_URI, tableId, SetList.STARRED_ONLY);
        Cursor cursor = handle.fetchAllCards(tableName, starredOnly);

        // Put terms and definitions into string arrays
        ArrayList<String> columnArray1 = new ArrayList<>();
        ArrayList<String> columnArray2 = new ArrayList<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            columnArray1.add(cursor.getString(cursor
                    .getColumnIndex(CardSet.TERM)));
            columnArray2.add(cursor.getString(cursor
                    .getColumnIndex(CardSet.DEFINITION)));
        }
        String[] terms = columnArray1.toArray(new String[columnArray1.size()]);
        String[] definitions = columnArray2.toArray(new String[columnArray2.size()]);

        // Send data to the wearable
        GoogleApiClient mGoogleApiClient = wearConnect();
        final PutDataMapRequest putRequest = PutDataMapRequest.create("/" + tableName);
        final DataMap map = putRequest.getDataMap();
        map.putStringArray(Constants.TERMS, terms);
        map.putStringArray(Constants.DEFINITIONS, definitions);
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