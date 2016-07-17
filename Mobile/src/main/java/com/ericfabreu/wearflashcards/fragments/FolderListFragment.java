package com.ericfabreu.wearflashcards.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.util.Log;
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
import com.ericfabreu.wearflashcards.activities.ManageFolderActivity;
import com.ericfabreu.wearflashcards.data.FlashcardContract.FolderList;
import com.ericfabreu.wearflashcards.data.FlashcardProvider;
import com.ericfabreu.wearflashcards.utils.Constants;
import com.ericfabreu.wearflashcards.utils.PreferencesHelper;
import com.ericfabreu.wearflashcards.views.MainViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates a list of folders to be displayed on screen.
 */
public class FolderListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private SimpleCursorAdapter mAdapter;
    private MainViewPager mViewPager;
    private List<Long> selections = new ArrayList<>();

    public void setViewPager(MainViewPager viewPager) {
        mViewPager = viewPager;
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
        setEmptyText(getString(R.string.message_no_folders));

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
                            builder.setTitle(R.string.dialog_delete_folders);
                        } else {
                            builder.setTitle(R.string.dialog_delete_folder);
                        }
                        builder.setMessage(R.string.dialog_cannot_undo);
                        builder.setCancelable(true);
                        builder.setPositiveButton(R.string.button_delete,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        for (int i = 0, n = selections.size(); i < n; i++) {
                                            FlashcardProvider handle = new FlashcardProvider(
                                                    getActivity().getApplicationContext());
                                            handle.deleteTable(selections.get(i), true);
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
                        // Get folder title and send it to ManageFolderActivity
                        FlashcardProvider handle = new FlashcardProvider(getActivity()
                                .getApplicationContext());
                        Cursor cursor = handle.query(FolderList.CONTENT_URI,
                                new String[]{FolderList.FOLDER_TITLE},
                                FolderList._ID + "=?",
                                new String[]{String.valueOf(selections.get(0))},
                                null);
                        if (cursor != null && cursor.moveToFirst()) {
                            String title = cursor.getString(cursor
                                    .getColumnIndex(FolderList.FOLDER_TITLE));
                            Intent intent = new Intent(getActivity(), ManageFolderActivity.class);
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
                mViewPager.setScrollable(false);
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Reset selections when action mode is dismissed
                selections = new ArrayList<>();
                getActivity().findViewById(R.id.tab_main).setVisibility(View.VISIBLE);
                mViewPager.setScrollable(true);
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }
        });

        // Create an empty adapter to display the list of folders
        mAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.fragment_folder_list,
                null,
                new String[]{FolderList.FOLDER_TITLE},
                new int[]{R.id.text_folder_title},
                0);
        setListAdapter(mAdapter);

        // Prepare the loader
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        // TODO FolderOverviewActivity
        TextView textView = (TextView) view.findViewById(R.id.text_folder_title);
        String title = textView.getText().toString();
        Log.d("title", title);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final String SET_SELECTION = "((" +
                FolderList.FOLDER_TITLE + " NOTNULL) AND (" +
                FolderList.FOLDER_TITLE + " != '' ))";
        return new CursorLoader(getActivity(),
                FolderList.CONTENT_URI,
                new String[]{FolderList._ID, FolderList.FOLDER_TITLE},
                SET_SELECTION,
                null,
                PreferencesHelper.getOrder(getActivity().getApplicationContext(),
                        FolderList.FOLDER_TITLE, Constants.PREF_KEY_SET_ORDER));
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}