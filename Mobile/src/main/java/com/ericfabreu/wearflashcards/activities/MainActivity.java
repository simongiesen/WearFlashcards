package com.ericfabreu.wearflashcards.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.fragments.SetListFragment;
import com.ericfabreu.wearflashcards.utils.Constants;
import com.ericfabreu.wearflashcards.views.MainViewPager;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences settings;
    private MainViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setElevation(0);
        }

        // Load settings
        settings = PreferenceManager.getDefaultSharedPreferences(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_main);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ManageSetActivity.class);
                intent.putExtra(Constants.TAG_EDITING_MODE, false);
                startActivityForResult(intent, Constants.REQUEST_CODE_CREATE);
            }
        });

        // Create the adapter and pager used to display the tabbed fragments
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (MainViewPager) findViewById(R.id.layout_main);
        mViewPager.setAdapter(adapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_main);
        tabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.shared, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Get menu items
        MenuItem shuffle = menu.getItem(Constants.MENU_POS_SHUFFLE);
        MenuItem termFirst = menu.getItem(Constants.MENU_POS_DEFINITION);

        // Restore settings
        shuffle.setChecked(settings.getBoolean(Constants.PREF_KEY_SHUFFLE, false));
        termFirst.setChecked(settings.getBoolean(Constants.PREF_KEY_DEFINITION, false));

        // Remove study button since it is unnecessary in MainActivity
        menu.removeItem(R.id.item_study_set);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Flip the item's checked state and save settings
        if (id == R.id.item_shuffle) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(Constants.PREF_KEY_SHUFFLE, !item.isChecked());
            editor.apply();
            item.setChecked(!item.isChecked());
            return true;
        } else if (id == R.id.item_definition_first) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(Constants.PREF_KEY_DEFINITION, !item.isChecked());
            editor.apply();
            return true;
        } else if (id == R.id.item_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent, Constants.REQUEST_CODE_SETTINGS);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Refresh activity with the proper sort order when another activity is closed
        if (requestCode == Constants.REQUEST_CODE_SETTINGS ||
                requestCode == Constants.REQUEST_CODE_CREATE ||
                requestCode == Constants.REQUEST_CODE_EDIT) {
            Intent refresh = new Intent(this, getClass());
            startActivity(refresh);
            this.finish();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_card_list, container, false);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                SetListFragment setListFragment = new SetListFragment();
                setListFragment.setViewPager(mViewPager);
                return setListFragment;
            }
            return new PlaceholderFragment();
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SETS";
                case 1:
                    return "FOLDERS";
            }
            return null;
        }
    }
}
