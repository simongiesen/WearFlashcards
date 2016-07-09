package com.ericfabreu.wearflashcards.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.wearable.view.GridViewPager;
import android.view.View;
import android.view.WindowInsets;
import android.widget.TextView;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.adapters.SetViewAdapter;
import com.ericfabreu.wearflashcards.utils.Constants;
import com.ericfabreu.wearflashcards.utils.PreferencesHelper;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class SetViewActivity extends Activity implements
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private String title, starValue;
    private ArrayList<String> terms = new ArrayList<>(), definitions = new ArrayList<>();
    private long[] ids;
    private ArrayList<Integer> stars = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status_empty_database);
        Bundle bundle = getIntent().getExtras();
        title = bundle.getString(Constants.TITLE);
        starValue = PreferencesHelper.getStarredOption(getApplicationContext());

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
        sendMessage(title, starValue);
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
                ids = dataMap.getLongArray(Constants.ID);
                terms = dataMap.getStringArrayList(Constants.TERMS);
                definitions = dataMap.getStringArrayList(Constants.DEFINITIONS);
                stars = dataMap.getIntegerArrayList(Constants.STAR);
                if (terms != null && definitions != null && stars != null && ids != null) {
                    if (terms.size() == 0) {
                        final TextView empty = (TextView) findViewById(R.id.text_empty_status);
                        empty.setText(R.string.message_all_cards_hidden);
                    } else {
                        createCards();
                    }
                }
            }
        }
    }

    /**
     * Sends a data request to the mobile device asking for the cards in a given set.
     */
    private void sendMessage(final String tableName, final String starredOption) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(Constants.PATH);
        final DataMap dataMap = putDataMapReq.getDataMap();
        dataMap.putString(Constants.TITLE, tableName);
        dataMap.putString(Constants.STARRED_OPTION, starredOption);
        dataMap.putLong(Constants.TIME, new Date().getTime());
        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataMapReq.asPutDataRequest());
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
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (settings.getBoolean(Constants.PREF_KEY_DEFINITION, false)) {
            ArrayList<String> temp = terms;
            terms = definitions;
            definitions = temp;
        }
        if (settings.getBoolean(Constants.PREF_KEY_SHUFFLE, false)) {
            shuffleCards();
        }
        pager.setAdapter(new SetViewAdapter(getFragmentManager(), mGoogleApiClient, title,
                terms, definitions, ids, stars));
    }

    /**
     * Shuffles the terms and definitions together.
     */
    private void shuffleCards() {
        int size = terms.size();
        int[] shuffleOrder = getShuffledArray(size);
        ArrayList<String> newTerms = new ArrayList<>(), newDefinitions = new ArrayList<>();
        long[] newIds = new long[size];
        ArrayList<Integer> newStars = new ArrayList<>();

        // Use shuffled int array to ensure that the new terms and definitions match
        for (int i = 0; i < size; i++) {
            newTerms.add(i, terms.get(shuffleOrder[i]));
            newDefinitions.add(i, definitions.get(shuffleOrder[i]));
            newIds[i] = ids[shuffleOrder[i]];
            newStars.add(i, stars.get(shuffleOrder[i]));
        }
        terms = newTerms;
        definitions = newDefinitions;
        ids = newIds;
        stars = newStars;
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
