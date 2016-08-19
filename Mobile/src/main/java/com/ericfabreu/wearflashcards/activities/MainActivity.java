package com.ericfabreu.wearflashcards.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.fragments.SetFolderListFragment;
import com.ericfabreu.wearflashcards.utils.Constants;
import com.ericfabreu.wearflashcards.views.MainViewPager;
import com.melnykov.fab.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences settings;
    private MainViewPager mViewPager;
    private SectionsPagerAdapter mPagerAdapter;

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
                // Determine if the fab should open the set or the folder activity
                final Class activityClass = mViewPager.getCurrentItem() == 0 ?
                        ManageSetActivity.class : ManageFolderActivity.class;
                Intent intent = new Intent(MainActivity.this, activityClass);
                intent.putExtra(Constants.TAG_EDITING_MODE, false);
                startActivityForResult(intent, Constants.REQUEST_CODE_CREATE);
            }
        });

        // Create the adapter and pager used to display the tabbed fragments
        mPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (MainViewPager) findViewById(R.id.layout_main);
        mViewPager.setAdapter(mPagerAdapter);
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

        // Refresh the fragments with the proper sort order when another activity is closed
        if (requestCode == Constants.REQUEST_CODE_SETTINGS ||
                requestCode == Constants.REQUEST_CODE_CREATE ||
                requestCode == Constants.REQUEST_CODE_EDIT ||
                requestCode == Constants.REQUEST_CODE_STUDY) {
            mPagerAdapter.refreshFragments();
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private final FragmentManager mFragmentManager;
        private ViewGroup mContainer;
        private SparseArray<SetFolderListFragment> mFragments;
        private FragmentTransaction mCurTransaction;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragmentManager = fm;
            mFragments = new SparseArray<>();
        }

        public void refreshFragments() {
            for (int i = 0; i < mFragments.size(); i++) {
                if (mFragments.get(i) != null) {
                    mFragments.get(i).refresh();
                }
            }
        }

        @Override
        @SuppressLint("CommitTransaction")
        public Object instantiateItem(ViewGroup container, int position) {
            SetFolderListFragment fragment = (SetFolderListFragment) getItem(position);
            mContainer = container;
            if (mCurTransaction == null) {
                mCurTransaction = mFragmentManager.beginTransaction();
            }
            mCurTransaction.add(mContainer.getId(), fragment);
            mFragments.append(position, fragment);
            return fragment;
        }

        @Override
        @SuppressLint("CommitTransaction")
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (mCurTransaction == null) {
                mCurTransaction = mFragmentManager.beginTransaction();
            }
            mCurTransaction.detach(mFragments.get(position));
            mFragments.remove(position);
        }

        @Override
        public Fragment getItem(int position) {
            SetFolderListFragment setFolderListFragment = new SetFolderListFragment();
            setFolderListFragment.setViewPager(mViewPager);
            setFolderListFragment.setMode(null, 0, R.id.fab_main, position);
            return setFolderListFragment;
        }

        @Override
        public boolean isViewFromObject(View view, Object fragment) {
            return ((Fragment) fragment).getView() == view;
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

        @Override
        public void finishUpdate(ViewGroup container) {
            if (mCurTransaction != null) {
                mCurTransaction.commitAllowingStateLoss();
                mCurTransaction = null;
                mFragmentManager.executePendingTransactions();
            }
        }
    }
}
