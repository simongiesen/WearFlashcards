package com.efa.wearflashcards;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Date;

public class MainActivity extends Activity implements
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private String[] set_list = null;
    private String[] set_dummy = {"Vocabulary Part 1", "Vocabulary Part 2", "Vocabulary Part 3", "Vocabulary Part 4"};

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

        // Get set list from phone
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(Constants.SET_LIST);
        putDataMapReq.getDataMap().putStringArray(Constants.SET_LIST, null);
        putDataMapReq.getDataMap().putLong("time", new Date().getTime());
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        createList();
//        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
//        PendingResult<DataItemBuffer> results = Wearable.DataApi.getDataItems(mGoogleApiClient);
//        results.setResultCallback(new ResultCallback<DataItemBuffer>() {
//            @Override
//            public void onResult(DataItemBuffer dataItems) {
//                if (dataItems.getCount() != 0) {
//                    DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItems.get(0));
//
//                    // Put list into set list
//                    set_list = dataMapItem.getDataMap().getStringArray(Constants.SET_LIST);
//                    if (set_list != null) {
//                        Log.d("Title works", set_list[0]);
//                        createList();
//                    }
//                    return;
//                }
//
//                dataItems.release();
//            }
//        });
    }

    protected void createList() {
        // Get the list component from the layout of the activity
        setContentView(R.layout.activity_main);
        WearableListView listView =
                (WearableListView) findViewById(R.id.list);

        // Assign an adapter to the list
        listView.setAdapter(new Adapter(this, set_dummy));

        // Open SetView when an item is clicked
        listView.setClickListener(new WearableListView.ClickListener() {
            @Override
            public void onClick(WearableListView.ViewHolder view) {
                Intent intent = new Intent(MainActivity.this, SetView.class);
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
        Log.d("DataChangedWearable", "Great");
    }

    /**
     * http://developer.android.com/training/wearables/ui/lists.html
     */
    private static final class Adapter extends WearableListView.Adapter {
        private final Context mContext;
        private final LayoutInflater mInflater;
        private String[] mDataset;

        // Provide a suitable constructor (depends on the kind of dataset)
        public Adapter(Context context, String[] dataset) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
            mDataset = dataset;
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
            view.setText(mDataset[position]);
            // Replace list item's metadata
            holder.itemView.setTag(position);
        }

        // Return the size of your dataset
        // (invoked by the WearableListView's layout manager)
        @Override
        public int getItemCount() {
            return mDataset.length;
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
}