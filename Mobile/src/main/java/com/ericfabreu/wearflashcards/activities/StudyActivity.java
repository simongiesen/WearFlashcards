package com.ericfabreu.wearflashcards.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.adapters.StudyAdapter;
import com.ericfabreu.wearflashcards.data.FlashcardContract.FolderList;
import com.ericfabreu.wearflashcards.data.FlashcardContract.SetList;
import com.ericfabreu.wearflashcards.data.FlashcardProvider;
import com.ericfabreu.wearflashcards.utils.Constants;
import com.ericfabreu.wearflashcards.utils.PreferencesHelper;
import com.ericfabreu.wearflashcards.utils.SetInfo;
import com.ericfabreu.wearflashcards.views.VerticalViewPager;

public class StudyActivity extends AppCompatActivity {
    private static final int MENU_POS_STAR = 1;
    private long tableId;
    private boolean folder;
    private FlashcardProvider mProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_set);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setElevation(Constants.TOOLBAR_ELEVATION);
        }
        mProvider = new FlashcardProvider(getApplicationContext());

        // Get set or folder information from the parent activity
        Bundle bundle = getIntent().getExtras();
        tableId = bundle.getLong(Constants.TAG_ID);
        folder = bundle.getBoolean(Constants.TAG_FOLDER);
        setTitle(bundle.getString(Constants.TAG_TITLE));

        createCards();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Reset the adapter when on rotation in order to be able to remove cards from it
        final VerticalViewPager pager = (VerticalViewPager) findViewById(R.id.pager_study_set);
        final int currentItem = pager.getCurrentItem();
        SetInfo setInfo = new SetInfo(getApplicationContext(), tableId, folder);
        pager.setAdapter(new StudyAdapter(this, getSupportFragmentManager(), pager, setInfo));
        pager.setCurrentItem(currentItem);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.studying, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final boolean starredOnly = PreferencesHelper.getStar(getApplicationContext(), mProvider,
                folder ? FolderList.CONTENT_URI : SetList.CONTENT_URI, tableId,
                folder ? FolderList.STARRED_ONLY : SetList.STARRED_ONLY);
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
        SetInfo setInfo = new SetInfo(getApplicationContext(), tableId, folder);

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

        final VerticalViewPager pager = (VerticalViewPager) findViewById(R.id.pager_study_set);
        pager.setAdapter(new StudyAdapter(this, getSupportFragmentManager(), pager, setInfo));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
            case R.id.item_study_reload: {
                createCards();
                return true;
            }
            // Flip starred only flag and reload cards
            case R.id.item_starred_only: {
                final boolean flag = PreferencesHelper.getStar(getApplicationContext(), mProvider,
                        folder ? FolderList.CONTENT_URI : SetList.CONTENT_URI, tableId,
                        folder ? FolderList.STARRED_ONLY : SetList.STARRED_ONLY);
                final int icon = flag ? R.drawable.ic_star_unselected : R.drawable.ic_star_selected;
                item.setIcon(icon);
                PreferencesHelper.flipStar(getApplicationContext(), mProvider,
                        folder ? FolderList.CONTENT_URI : SetList.CONTENT_URI, tableId,
                        folder ? FolderList.STARRED_ONLY : SetList.STARRED_ONLY);
                createCards();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }
}
