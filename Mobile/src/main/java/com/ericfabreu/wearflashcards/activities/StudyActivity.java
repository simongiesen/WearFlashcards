package com.ericfabreu.wearflashcards.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.adapters.StudyAdapter;
import com.ericfabreu.wearflashcards.data.FlashcardContract.FolderList;
import com.ericfabreu.wearflashcards.data.FlashcardContract.SetList;
import com.ericfabreu.wearflashcards.data.FlashcardProvider;
import com.ericfabreu.wearflashcards.utils.Constants;
import com.ericfabreu.wearflashcards.utils.PreferencesHelper;
import com.ericfabreu.wearflashcards.utils.SetInfo;

import java.util.ArrayList;

public class StudyActivity extends AppCompatActivity {
    private static final int MENU_POS_STAR = 1;
    private static final String CARD_POSITION = "position", SHUFFLE_ORDER = "shuffle";
    private ArrayList<Integer> mOrder = null;
    private int mPosition;
    private long mTableId;
    private boolean mFolder;
    private FlashcardProvider mProvider;
    private ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_set);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(Constants.TOOLBAR_ELEVATION);
        }
        mProvider = new FlashcardProvider(getApplicationContext());

        // Get set or folder information from the parent activity
        Bundle bundle = getIntent().getExtras();
        mTableId = bundle.getLong(Constants.TAG_ID);
        mFolder = bundle.getBoolean(Constants.TAG_FOLDER);
        setTitle(bundle.getString(Constants.TAG_TITLE));

        // Restore list position
        mPosition = savedInstanceState != null ? savedInstanceState.getInt(CARD_POSITION) : 0;
        mOrder = savedInstanceState != null ?
                savedInstanceState.getIntegerArrayList(SHUFFLE_ORDER) : null;

        createCards();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.studying, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final boolean starredOnly = PreferencesHelper.getStar(getApplicationContext(), mProvider,
                mFolder ? FolderList.CONTENT_URI : SetList.CONTENT_URI, mTableId,
                mFolder ? FolderList.STARRED_ONLY : SetList.STARRED_ONLY);
        if (starredOnly) {
            MenuItem star = menu.getItem(MENU_POS_STAR);
            star.setIcon(R.drawable.ic_star_selected);
        }
        return true;
    }

    /**
     * Creates flashcards using a VerticalViewPager.
     */
    protected void createCards() {
        // Get information from the database
        final SetInfo setInfo = new SetInfo(getApplicationContext(), mTableId, mFolder);

        // Return to the parent activity if all cards have been hidden
        if (setInfo.isEmpty()) {
            finish();
        }

        // Load set study settings
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (settings.getBoolean(Constants.PREF_KEY_DEFINITION, false)) {
            setInfo.flipCards();
        }
        if (settings.getBoolean(Constants.PREF_KEY_SHUFFLE, false)) {
            setInfo.shuffleCards();
        }
        if (settings.getBoolean(Constants.PREF_KEY_HORIZONTAL_PAGER, false)) {
            findViewById(R.id.pager_study_vertical).setVisibility(View.GONE);
            mPager = (ViewPager) findViewById(R.id.pager_study_horizontal);
            mPager.setVisibility(View.VISIBLE);
        } else {
            mPager = (ViewPager) findViewById(R.id.pager_study_vertical);
        }

        // Save or restore the shuffle order
        if (mOrder == null) {
            mOrder = setInfo.getOrder();
        } else {
            setInfo.setOrder(mOrder);
        }

        mPager.setAdapter(new StudyAdapter(this, getSupportFragmentManager(), mPager, setInfo));
        mPager.setCurrentItem(mPosition);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
            case R.id.item_study_reload: {
                mOrder = null;
                createCards();
                return true;
            }
            // Flip starred only flag and reload cards
            case R.id.item_starred_only: {
                final boolean flag = PreferencesHelper.getStar(getApplicationContext(), mProvider,
                        mFolder ? FolderList.CONTENT_URI : SetList.CONTENT_URI, mTableId,
                        mFolder ? FolderList.STARRED_ONLY : SetList.STARRED_ONLY);
                final int icon = flag ? R.drawable.ic_star_unselected : R.drawable.ic_star_selected;
                item.setIcon(icon);
                PreferencesHelper.flipStar(getApplicationContext(), mProvider,
                        mFolder ? FolderList.CONTENT_URI : SetList.CONTENT_URI, mTableId,
                        mFolder ? FolderList.STARRED_ONLY : SetList.STARRED_ONLY);
                mOrder = null;
                createCards();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    // Save current position before the activity is recreated
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CARD_POSITION, mPager.getCurrentItem());
        outState.putIntegerArrayList(SHUFFLE_ORDER, mOrder);
    }

    @Override
    protected void onResume() {
        super.onResume();
        createCards();
    }
}
