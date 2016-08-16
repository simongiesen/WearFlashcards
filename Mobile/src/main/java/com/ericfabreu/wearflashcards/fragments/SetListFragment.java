package com.ericfabreu.wearflashcards.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.activities.ManageSetActivity;
import com.ericfabreu.wearflashcards.activities.SetOverviewActivity;
import com.ericfabreu.wearflashcards.data.FlashcardContract.FolderEntry;
import com.ericfabreu.wearflashcards.data.FlashcardContract.SetList;
import com.ericfabreu.wearflashcards.data.FlashcardProvider;
import com.ericfabreu.wearflashcards.utils.Constants;
import com.ericfabreu.wearflashcards.utils.PreferencesHelper;
import com.ericfabreu.wearflashcards.views.MainViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates a list of sets to be displayed on screen.
 */
public class SetListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private SimpleCursorAdapter mAdapter;
    private MainViewPager mViewPager;
    private boolean mFolder = false;
    private String mTable = null;
    private List<Long> selections = new ArrayList<>();

    public void setViewPager(MainViewPager viewPager) {
        mViewPager = viewPager;
    }

    public void setFolderMode(String table) {
        mFolder = true;
        mTable = table;
    }

    public void refresh() {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Show text if database is empty
        setEmptyText(mFolder ? getString(R.string.message_empty_folder)
                : getString(R.string.message_empty_database));

        // Setup contextual action mode
        final ListView listView = getListView();
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position,
                                                  long id, boolean checked) {
                // Store position of selected items
                if (checked) {
                    selections.add(id);
                } else {
                    selections.remove(selections.indexOf(id));
                }

                // Show the edit button only if there is exactly one item selected
                if (selections.size() == 1) {
                    mode.getMenu().findItem(R.id.item_edit).setVisible(true);
                } else {
                    mode.getMenu().findItem(R.id.item_edit).setVisible(false);
                }
            }

            @Override
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item_delete:
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        if (selections.size() > 1) {
                            builder.setTitle(R.string.dialog_delete_sets);
                        } else {
                            builder.setTitle(R.string.dialog_delete_set);
                        }
                        builder.setMessage(R.string.dialog_cannot_undo);
                        builder.setCancelable(true);
                        builder.setPositiveButton(R.string.button_delete,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        for (int i = 0, n = selections.size(); i < n; i++) {
                                            FlashcardProvider handle = new FlashcardProvider(
                                                    getActivity().getApplicationContext());
                                            handle.deleteTable(selections.get(i), mFolder);
                                        }
                                        mode.finish();
                                    }
                                });

                        builder.setNegativeButton(R.string.button_cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                        return true;

                    case R.id.item_edit:
                        // Get set title and send it to ManageSetActivity
                        FlashcardProvider handle = new FlashcardProvider(getActivity()
                                .getApplicationContext());
                        Cursor cursor = handle.query(SetList.CONTENT_URI,
                                mFolder ? new String[]{FolderEntry.SET_TITLE}
                                        : new String[]{SetList.SET_TITLE},
                                (mFolder ? FolderEntry._ID : SetList._ID) + "=?",
                                new String[]{String.valueOf(selections.get(0))},
                                null);
                        if (cursor != null && cursor.moveToFirst()) {
                            String title = cursor.getString(cursor
                                    .getColumnIndex(mFolder ? FolderEntry.SET_TITLE
                                            : SetList.SET_TITLE));
                            Intent intent = new Intent(getActivity(), ManageSetActivity.class);
                            intent.putExtra(Constants.TAG_EDITING_MODE, true);
                            intent.putExtra(Constants.TAG_TITLE, title);
                            getActivity().startActivityForResult(intent,
                                    Constants.REQUEST_CODE_EDIT);
                            cursor.close();
                        }
                        mode.finish();
                        return true;

                    default:
                        return false;
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.contextual, menu);
                getActivity().findViewById(R.id.tab_main).setVisibility(View.GONE);
                if (mViewPager != null) {
                    mViewPager.setScrollable(false);
                }
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Reset selections when action mode is dismissed
                selections = new ArrayList<>();
                getActivity().findViewById(R.id.tab_main).setVisibility(View.VISIBLE);
                if (mViewPager != null) {
                    mViewPager.setScrollable(true);
                }
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }
        });

        // Create an empty adapter to display the list of sets
        mAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.item_set_list,
                null,
                mFolder ? new String[]{FolderEntry.SET_TITLE} : new String[]{SetList.SET_TITLE},
                new int[]{R.id.text_set_title},
                0);
        setListAdapter(mAdapter);

        // Prepare the loader
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        // Use title to find the table name and pass it to SetOverviewActivity
        TextView textView = (TextView) view.findViewById(R.id.text_set_title);
        String title = textView.getText().toString();
        FlashcardProvider handle = new FlashcardProvider(getActivity().getApplicationContext());
        String tableName = handle.getTableName(title, mFolder);
        Intent intent = new Intent(getActivity(), SetOverviewActivity.class);
        intent.putExtra(Constants.TAG_TABLE_NAME, tableName);
        intent.putExtra(Constants.TAG_TITLE, title);
        intent.putExtra(Constants.TAG_ID, id);
        startActivityForResult(intent, Constants.REQUEST_CODE_STUDY);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final Uri uri = mFolder ? Uri.withAppendedPath(FolderEntry.CONTENT_URI, mTable)
                : SetList.CONTENT_URI;
        final String[] columns = mFolder ? new String[]{FolderEntry._ID, FolderEntry.SET_TITLE}
                : new String[]{SetList._ID, SetList.SET_TITLE};
        return new CursorLoader(getActivity(),
                uri,
                columns,
                null,
                null,
                PreferencesHelper.getOrder(getActivity().getApplicationContext(),
                        mFolder ? FolderEntry.SET_TITLE : SetList.SET_TITLE,
                        Constants.PREF_KEY_SET_ORDER));
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}