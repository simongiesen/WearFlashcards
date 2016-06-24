package com.ericfabreu.wearflashcards.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.utils.Constants;
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
        WearableListView listView =
                (WearableListView) findViewById(R.id.layout_list);
        listView.setAdapter(new Adapter(this, setList));

        listView.setClickListener(new WearableListView.ClickListener() {
            @Override
            public void onClick(WearableListView.ViewHolder view) {
                // Get set title from list item and send it to SetViewActivity
                Adapter.ItemViewHolder itemHolder = (Adapter.ItemViewHolder) view;
                TextView tv = itemHolder.textView;
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
        sendMessage(Constants.SET_LIST);
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
                if (setList.length > 0) {
                    createList();
                }
            }
        }
    }

    /**
     * Sends a message to the mobile device with the selected set title.
     * https://www.binpress.com/tutorial/a-guide-to-the-android-wear-message-api/152
     */
    private void sendMessage(final String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes =
                        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                for (Node node : nodes.getNodes()) {
                    Wearable.MessageApi.sendMessage(mGoogleApiClient,
                            node.getId(),
                            Constants.PATH,
                            message.getBytes()).await();
                }
            }
        }).start();
    }

    /**
     * Launches SettingsActivity.
     */
    public void openSettings(View view) {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Provides the ListView adapter used to display the list of sets.
     */
    private static final class Adapter extends WearableListView.Adapter {
        private final Context mContext;
        private final LayoutInflater mInflater;
        private String[] mDataSet;

        public Adapter(Context context, String[] dataSet) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
            mDataSet = dataSet;
        }

        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // Inflate custom layout for list items
            return new ItemViewHolder(mInflater.inflate(R.layout.item_set_list, parent, false));
        }

        // Replace the contents of a list item
        @Override
        public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
            // Retrieve the text view and replace its contents
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            TextView view = itemHolder.textView;
            view.setText(mDataSet[position]);
            holder.itemView.setTag(position);
        }

        @Override
        public int getItemCount() {
            return mDataSet.length;
        }

        // Provide a reference to the views used in the ListView
        public static class ItemViewHolder extends WearableListView.ViewHolder {
            private TextView textView;

            public ItemViewHolder(View itemView) {
                super(itemView);
                textView = (TextView) itemView.findViewById(R.id.text_name);
            }
        }
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