package com.efa.wearflashcards;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridViewPager;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;

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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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
                if (terms.length > 0 && definitions.length > 0) {
                    createCards();
                }
            }
        }
    }

    // Adapted from the GridViewPager sample (https://goo.gl/ZGLbWH)
    protected void createCards() {
        Log.d("createCards", terms[0]);
        Log.d("createCards", definitions[0]);
        setContentView(R.layout.set_view);
        final Resources res = getResources();
        final GridViewPager pager = (GridViewPager) findViewById(R.id.pager);
        pager.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                // Adjust page margins:
                //   A little extra horizontal spacing between pages looks a bit
                //   less crowded on a round display.
                final boolean round = insets.isRound();
                int rowMargin = res.getDimensionPixelOffset(R.dimen.page_row_margin);
                int colMargin = res.getDimensionPixelOffset(round ?
                        R.dimen.page_column_margin_round : R.dimen.page_column_margin);
                pager.setPageMargins(rowMargin, colMargin);

                // GridViewPager relies on insets to properly handle
                // layout for round displays. They must be explicitly
                // applied since this listener has taken them over.
                pager.onApplyWindowInsets(insets);
                return insets;
            }
        });
        pager.setAdapter(new SetViewAdapter(getFragmentManager(), terms, definitions));
        DotsPageIndicator dotsPageIndicator = (DotsPageIndicator) findViewById(R.id.page_indicator);
        dotsPageIndicator.setPager(pager);
    }
}
