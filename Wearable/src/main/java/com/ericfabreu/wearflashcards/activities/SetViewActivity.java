package com.ericfabreu.wearflashcards.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.wearable.view.GridViewPager;
import android.view.View;
import android.view.WindowInsets;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.adapters.SetViewAdapter;
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

import java.util.Random;

public class SetViewActivity extends Activity implements
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private String title = null;
    private String[] terms = null;
    private String[] definitions = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status_empty_database);
        Bundle bundle = getIntent().getExtras();
        title = bundle.getString(Constants.TITLE);

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

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        sendMessage(title);
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
                // Create the cards upon receiving data from the mobile device
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
     * Uses a GridViewPager to display the flashcards.
     * Adapted from the GridViewPager sample (https://goo.gl/ZGLbWH).
     */
    protected void createCards() {
        setContentView(R.layout.activity_set_view);
        final Resources resources = getResources();
        final GridViewPager pager = (GridViewPager) findViewById(R.id.pager_set_view);
        pager.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                // Add extra horizontal spacing on round displays
                final boolean round = insets.isRound();
                int rowMargin = resources.getDimensionPixelOffset(R.dimen.page_row_margin);
                int colMargin = resources.getDimensionPixelOffset(round ?
                        R.dimen.page_column_margin_round : R.dimen.page_column_margin);
                pager.setPageMargins(rowMargin, colMargin);
                pager.onApplyWindowInsets(insets);
                return insets;
            }
        });

        // Apply settings and open cards
        SharedPreferences settings =
                getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
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

    /**
     * Shuffles the terms and definitions together.
     */
    private void shuffleCards() {
        int size = terms.length;
        int[] shuffleOrder = getShuffledArray(size);
        String[] newTerms = new String[size];
        String[] newDefinitions = new String[size];

        // Use shuffled int array to ensure that the new terms and definitions match
        for (int i = 0; i < size; i++) {
            newTerms[i] = terms[shuffleOrder[i]];
            newDefinitions[i] = definitions[shuffleOrder[i]];
        }
        terms = newTerms;
        definitions = newDefinitions;
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
