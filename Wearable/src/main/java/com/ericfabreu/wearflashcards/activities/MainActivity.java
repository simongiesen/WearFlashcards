package com.ericfabreu.wearflashcards.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.wearable.view.WearableListView;
import android.view.View;
import android.widget.TextView;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.adapters.ListViewAdapter;
import com.ericfabreu.wearflashcards.utils.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Date;

public class MainActivity extends Activity implements
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private String[] setList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status_empty_database);

        // Listen for data item events
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        // Check if watch is connected
        Thread checkThread = new Thread(new CheckConnection());
        checkThread.start();
    }

    protected void createList() {
        // Get the list component from the layout of the activity and assign an adapter to it
        setContentView(R.layout.activity_main);
        WearableListView listView = (WearableListView) findViewById(R.id.layout_list);
        listView.setAdapter(new ListViewAdapter(this, R.layout.item_set_list, setList));
        listView.setClickListener(new WearableListView.ClickListener() {
            @Override
            public void onClick(WearableListView.ViewHolder view) {
                // Get set title from list item and send it to SetViewActivity
                ListViewAdapter.ItemViewHolder itemHolder = (ListViewAdapter.ItemViewHolder) view;
                TextView tv = (TextView) itemHolder.getView();
                Intent intent = new Intent(MainActivity.this, SetViewActivity.class);
                intent.putExtra(Constants.TITLE, tv.getText().toString());
                startActivity(intent);
            }

            @Override
            public void onTopEmptyRegionClick() {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        sendMessage();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            // Create the list of sets upon receiving data from the mobile device
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = event.getDataItem();
                DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                setList = dataMap.getStringArray(Constants.SET_LIST);
                if (setList != null && setList.length > 0) {
                    createList();
                }
            }
        }
    }

    /**
     * Sends a data request to the mobile device asking for the list of sets.
     */
    private void sendMessage() {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(Constants.PATH);
        final DataMap dataMap = putDataMapReq.getDataMap();
        dataMap.putString(Constants.MAIN, Constants.SET_LIST);
        dataMap.putLong(Constants.TIME, new Date().getTime());
        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataMapReq.asPutDataRequest());
    }

    /**
     * Launches SettingsActivity.
     */
    public void openSettings(View view) {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Displays an offline status message if the watch is not connected to the mobile device.
     */
    private class CheckConnection implements Runnable {
        @Override
        public void run() {
            if (Wearable.NodeApi
                    .getConnectedNodes(mGoogleApiClient).await().getNodes().size() == 0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setContentView(R.layout.status_offline);
                    }
                });
            }
        }
    }
}