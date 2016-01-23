package com.efa.wearflashcards;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.wearable.view.CardFragment;
import android.util.Log;
import android.view.Gravity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class SetView extends Activity implements
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private String path = null;
    private String[] terms = null;
    private String[] definitions = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty_database);
        Bundle bundle = getIntent().getExtras();
        path = "/" + bundle.getString("title");
        Log.d("SetView", path);

        // Listen for data item events
        // http://developer.android.com/training/wearables/data-layer/data-items.html
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        sendMessage(path, Constants.BLANK_MESSAGE);
    }

    // https://www.binpress.com/tutorial/a-guide-to-the-android-wear-message-api/152
    // http://developer.android.com/training/wearables/data-layer/messages.html
    private void sendMessage(final String path, final String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                for (Node node : nodes.getNodes()) {
                    Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, node.getId(), path, message.getBytes()).await();
                    Log.d("sendMessage", "message sent");
                }
            }
        }).start();
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
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d("Wear", "dataChanged");
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                terms = dataMap.getStringArray(Constants.TERMS);
                definitions = dataMap.getStringArray(Constants.DEFINITIONS);
                if (terms != null && definitions != null) {
                    createCards();
                }
            }
        }
    }

    protected void createCards() {
        if (terms.length > 0 && definitions.length > 0) {
            Log.d("createCards", terms[0]);
            Log.d("createCards", definitions[0]);
        }

        setContentView(R.layout.activity_set_view);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        CardFragment cardFragment = CardFragment.create(getString(R.string.term),
                getString(R.string.definition));
        cardFragment.setCardGravity(Gravity.TOP);
        cardFragment.setExpansionDirection(CardFragment.EXPAND_DOWN);
        cardFragment.setExpansionEnabled(true);
        fragmentTransaction.add(R.id.frame_layout, cardFragment);
        fragmentTransaction.commit();
    }
}
