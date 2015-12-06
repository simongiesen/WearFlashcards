package com.efa.wearflashcards;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends AppCompatActivity {
    private static MainActivity instance;
    private GoogleApiClient mGoogleApiClient;

    // Allow other activities to get application context statically
    // http://stackoverflow.com/a/5114361
    public static Context getContext() {
        return instance.getApplicationContext();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String[] set_list = {"table 1", "table 2"};
                mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                        .addApi(Wearable.API)
                        .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle connectionHint) {
                                Log.d("wow", "onConnected: " + connectionHint);
                            }

                            @Override
                            public void onConnectionSuspended(int cause) {
                                Log.d("wow", "onConnectionSuspended: " + cause);
                            }
                        })
                        .build();
                PutDataMapRequest putDataMapReq = PutDataMapRequest.create(Constants.SET_LIST);
                putDataMapReq.getDataMap().putStringArray(Constants.SET_LIST, set_list);
                PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
                Log.d("wow", " sending");
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
                Intent intent = new Intent(MainActivity.this, NewSet.class);
                startActivity(intent);
            }
        });

        // Load flashcard sets
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.main_layout, new SetListFragment())
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
