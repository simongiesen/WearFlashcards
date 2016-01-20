package com.efa.wearflashcards;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

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
        map.putStringArray(Constants.SET_LIST, new String[]{"Android Wear", "Google Apps", "Android Releases", "Windows Updates", "Math 21b Review"});
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