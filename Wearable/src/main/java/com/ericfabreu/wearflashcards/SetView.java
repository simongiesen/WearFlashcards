package com.ericfabreu.wearflashcards;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.wearable.view.GridViewPager;
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

import java.util.Random;

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
        path = "/" + bundle.getString(Constants.TITLE);

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
                terms = dataMap.getStringArray(Constants.TERMS);
                definitions = dataMap.getStringArray(Constants.DEFINITIONS);
                if (terms != null && definitions != null &&
                        terms.length > 0 && definitions.length > 0) {
                    createCards();
                }
            }
        }
    }

    // Adapted from the GridViewPager sample (https://goo.gl/ZGLbWH)
    protected void createCards() {
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

        // Apply settings and open cards
        SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        if (settings.getBoolean(Constants.DEF_FIRST, false)) {
            String[] temp = terms;
            terms = definitions;
            definitions = temp;
        }
        if (settings.getBoolean(Constants.SHUFFLE, false)) {
            shuffleCards();
        }
        pager.setAdapter(new SetViewAdapter(getFragmentManager(), terms, definitions));
    }

    private void shuffleCards() {
        int size = terms.length;
        int[] shuffleOrder = getShuffledArray(size);
        String[] newTerms = new String[size];
        String[] newDefs = new String[size];

        // Use shuffled int array to ensure that the new terms and definitions match
        for (int i = 0; i < size; i++) {
            newTerms[i] = terms[shuffleOrder[i]];
            newDefs[i] = definitions[shuffleOrder[i]];
        }

        terms = newTerms;
        definitions = newDefs;
    }

    /**
     * Creates an int array of size 'size' in increasing order and shuffles it.
     */
    private int[] getShuffledArray(int size) {
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = i;
        }
        return shuffleArray(array);
    }

    /**
     * Shuffles an int array.
     * http://stackoverflow.com/a/18456998/3522216
     */
    private int[] shuffleArray(int[] array) {
        int index;
        Random random = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            index = random.nextInt(i + 1);
            if (index != i) {
                array[index] ^= array[i];
                array[i] ^= array[index];
                array[index] ^= array[i];
            }
        }
        return array;
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
