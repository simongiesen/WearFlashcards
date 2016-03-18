package com.efa.wearflashcards;

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

public class Main extends Activity implements
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private String[] setList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty_database);

        // Listen for data item events
        // http://developer.android.com/training/wearables/data-layer/data-items.html
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

    public void openSettings(View view) {
        Intent intent = new Intent(Main.this, Settings.class);
        startActivity(intent);
    }

    protected void createList() {
        // Get the list component from the layout of the activity
        setContentView(R.layout.main);
        WearableListView listView =
                (WearableListView) findViewById(R.id.list);

        // Assign an adapter to the list
        listView.setAdapter(new Adapter(this, setList));

        // Open SetView when an item is clicked
        listView.setClickListener(new WearableListView.ClickListener() {
            @Override
            public void onClick(WearableListView.ViewHolder view) {
                // Get set title from list item and send it to SetView
                Adapter.ItemViewHolder itemHolder = (Adapter.ItemViewHolder) view;
                TextView tv = itemHolder.textView;
                Intent intent = new Intent(Main.this, SetView.class);
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
        sendMessage(Constants.SET_LIST, Constants.BLANK_MESSAGE);
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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
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
     * http://developer.android.com/training/wearables/ui/lists.html
     */
    private static final class Adapter extends WearableListView.Adapter {
        private final Context mContext;
        private final LayoutInflater mInflater;
        private String[] mDataSet;

        // Provide a suitable constructor (depends on the kind of data set)
        public Adapter(Context context, String[] dataSet) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
            mDataSet = dataSet;
        }

        // Create new views for list items
        // (invoked by the WearableListView's layout manager)
        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                              int viewType) {
            // Inflate our custom layout for list items
            return new ItemViewHolder(mInflater.inflate(R.layout.set_list_item, parent, false));
        }

        // Replace the contents of a list item
        // Instead of creating new views, the list tries to recycle existing ones
        // (invoked by the WearableListView's layout manager)
        @Override
        public void onBindViewHolder(WearableListView.ViewHolder holder,
                                     int position) {
            // Retrieve the text view
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            TextView view = itemHolder.textView;
            // Replace text contents
            view.setText(mDataSet[position]);
            // Replace list item's metadata
            holder.itemView.setTag(position);
        }

        // Return the size of your data set
        // (invoked by the WearableListView's layout manager)
        @Override
        public int getItemCount() {
            return mDataSet.length;
        }

        // Provide a reference to the type of views you're using
        public static class ItemViewHolder extends WearableListView.ViewHolder {
            private TextView textView;

            public ItemViewHolder(View itemView) {
                super(itemView);
                // Find the text view within the custom item's layout
                textView = (TextView) itemView.findViewById(R.id.name);
            }
        }
    }

    private class CheckConnection implements Runnable {
        @Override
        public void run() {
            if (Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await().getNodes().size() == 0) {
                // Display offline message
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setContentView(R.layout.offline);
                    }
                });
            }
        }
    }
}