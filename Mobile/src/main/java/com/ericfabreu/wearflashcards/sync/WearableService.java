package com.ericfabreu.wearflashcards.sync;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.ericfabreu.wearflashcards.data.FlashcardContract;
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
        String message = messageEvent.getPath();

        // Determine if the wearable is asking for the set list or for the list of cards in a set
        if (message.equals(Constants.SET_LIST)) {
            sendData();
        } else {
            sendSet(message);
        }
    }

    // http://stackoverflow.com/a/25244496/3522216
    private void sendData() {
        // Get titles from the database
        FlashcardProvider handle = new FlashcardProvider(getApplicationContext());
        Cursor cursor = handle.fetchAllTitles();
        if (cursor == null) {
            return;
        }

        // Put titles in a string array
        // http://stackoverflow.com/a/8939324/3522216
        ArrayList<String> columnArray = new ArrayList<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            columnArray.add(cursor.getString(cursor.getColumnIndex(FlashcardContract.SetList.SET_TITLE)));
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

    private void sendSet(String title) {
        // Get terms and definitions from the database
        FlashcardProvider handle = new FlashcardProvider(getApplicationContext());
        String tableName = handle.getTableName(title);
        Cursor cursor = handle.fetchAllCards(tableName);

        // Put terms and definitions into string arrays
        ArrayList<String> columnArray1 = new ArrayList<>();
        ArrayList<String> columnArray2 = new ArrayList<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            columnArray1.add(cursor.getString(cursor.getColumnIndex(FlashcardContract.CardSet.TERM)));
            columnArray2.add(cursor.getString(cursor.getColumnIndex(FlashcardContract.CardSet.DEFINITION)));
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