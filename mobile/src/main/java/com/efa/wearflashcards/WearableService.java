package com.efa.wearflashcards;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import com.efa.wearflashcards.data.FlashcardContract;
import com.efa.wearflashcards.data.FlashcardProvider;
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
        String path = messageEvent.getPath();
        Log.d("messageReceived:", path);
        sendData();
    }

    // http://stackoverflow.com/a/25244496/3522216
    private void sendData() {
        GoogleApiClient mGoogleApiClient = wearConnect();
        final PutDataMapRequest putRequest = PutDataMapRequest.create(Constants.SET_LIST);
        final DataMap map = putRequest.getDataMap();
        map.putLong("time", new Date().getTime());

        // Get titles from the database
        // http://stackoverflow.com/a/8939324/3522216
        FlashcardProvider handle = new FlashcardProvider();
        Cursor cursor = handle.fetchAllTitles();
        ArrayList<String> columnArray = new ArrayList<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            columnArray.add(cursor.getString(cursor.getColumnIndex(FlashcardContract.SetList.SET_TITLE)));
        }
        String[] setList = columnArray.toArray(new String[columnArray.size()]);

        map.putStringArray(Constants.SET_LIST, setList);
        Wearable.DataApi.putDataItem(mGoogleApiClient, putRequest.asPutDataRequest());
        Log.d("WearableService", "Message sent");
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
                    public void onConnectionFailed(ConnectionResult result) {
                    }
                })
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
        return mGoogleApiClient;
    }
}