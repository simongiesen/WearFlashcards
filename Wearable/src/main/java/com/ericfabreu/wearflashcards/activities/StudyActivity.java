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
import com.ericfabreu.wearflashcards.adapters.StudyAdapter;
import com.ericfabreu.wearflashcards.utils.Constants;
import com.ericfabreu.wearflashcards.utils.PreferencesHelper;
import com.ericfabreu.wearflashcards.utils.SetInfo;
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

public class StudyActivity extends Activity implements
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private final static String TAG_TERMS = "terms", TAG_DEFINITIONS = "definitions",
            TAG_STARRED_OPTION = "starred_option", TAG_FOLDER_ID = "folder_id";
    private GoogleApiClient mGoogleApiClient;
    private boolean setMode;
    private String title, starValue;
    private boolean starredOnly;
    private ArrayList<String> terms, definitions;
    private long[] ids, tableIds;
    private ArrayList<Integer> stars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status_empty_database);
        Bundle bundle = getIntent().getExtras();
        setMode = bundle.getBoolean(Constants.TAG_MODE);
        title = bundle.getString(Constants.TAG_TITLE);
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
                ids = dataMap.getLongArray(Constants.TAG_ID);
                if (!setMode) {
                    tableIds = dataMap.getLongArray(TAG_FOLDER_ID);
                }
                terms = dataMap.getStringArrayList(TAG_TERMS);
                definitions = dataMap.getStringArrayList(TAG_DEFINITIONS);
                stars = dataMap.getIntegerArrayList(Constants.TAG_STAR);
                starredOnly = dataMap.getBoolean(Constants.TAG_STARRED_ONLY);
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
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(Constants.REQUEST_PATH);
        final DataMap dataMap = putDataMapReq.getDataMap();
        dataMap.putInt(Constants.TAG_MODE, setMode ? 2 : 3);
        dataMap.putString(Constants.TAG_TITLE, tableName);
        dataMap.putString(TAG_STARRED_OPTION, starredOption);
        dataMap.putLong(Constants.TAG_TIME, new Date().getTime());
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
        SetInfo setInfo = new SetInfo(title, starredOnly, terms, definitions, ids, tableIds, stars);

        // Apply settings and open cards
        SharedPreferences settings =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (settings.getBoolean(Constants.PREF_KEY_DEFINITION, false)) {
            setInfo.flipCards();
        }
        if (settings.getBoolean(Constants.PREF_KEY_SHUFFLE, false)) {
            setInfo.shuffleCards();
        }
        pager.setAdapter(new StudyAdapter(getFragmentManager(), this, pager, mGoogleApiClient,
                setInfo));
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
