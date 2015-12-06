package com.efa.wearflashcards;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Provides data to the wearable app.
 */
public class WearableService extends WearableListenerService {
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        String event = messageEvent.getPath();
        Log.d("ListClicked", event);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(Constants.SET_LIST);
        putDataMapReq.getDataMap().putStringArray(Constants.SET_LIST, new String[]{"table 14", "table 27", "table 12323"});
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
        Log.d("DataChanged", "Great");
    }
}