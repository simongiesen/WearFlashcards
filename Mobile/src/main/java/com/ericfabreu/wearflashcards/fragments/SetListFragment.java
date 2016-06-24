package com.ericfabreu.wearflashcards.fragments;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
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
import com.ericfabreu.wearflashcards.activities.EditSetTitleActivity;
import com.ericfabreu.wearflashcards.activities.SetOverviewActivity;
import com.ericfabreu.wearflashcards.data.FlashcardContract.SetList;
import com.ericfabreu.wearflashcards.data.FlashcardProvider;
import com.ericfabreu.wearflashcards.utils.Constants;

import java.util.ArrayList;
import java.util.List;


/**
 * Generates a list of sets to be displayed on screen.
 */
public class SetListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String[] SET_SUMMARY_PROJECTION =
            new String[]{SetList._ID, SetList.SET_TITLE};
    private SimpleCursorAdapter mAdapter;
    private List<Integer> selections = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Show text if database is empty
        setEmptyText(getString(R.string.empty_database));

        // Setup contextual action mode
        ListView listView = getListView();
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position,
                                                  long id, boolean checked) {
                // Store position of selected items
                if (checked) {
                    selections.add(position);
                } else {
                    selections.remove(Integer.valueOf(position));
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
                            builder.setTitle(R.string.delete_sets);
                        } else {
                            builder.setTitle(R.string.delete_set);
                        }
                        builder.setMessage(R.string.cannot_undo);
                        builder.setCancelable(true);
                        builder.setPositiveButton(R.string.delete,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        for (int i = 0, n = selections.size(); i < n; i++) {
                                            TextView set = (TextView) getListView()
                                                    .getChildAt(selections.get(i));
                                            String title = set.getText().toString();
                                            FlashcardProvider handle = new FlashcardProvider(
                                                    getActivity().getApplicationContext());
                                            handle.deleteSetTable(title);
                                        }
                                        mode.finish();
                                    }
                                });

                        builder.setNegativeButton(R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                        return true;

                    case R.id.item_edit:
                        // Get set title and send it to EditSetTitleActivity
                        TextView set = (TextView) getListView().getChildAt(selections.get(0));
                        String title = set.getText().toString();
                        Intent intent = new Intent(getActivity(), EditSetTitleActivity.class);
                        intent.putExtra(Constants.TITLE, title);
                        startActivity(intent);
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
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Reset selections when action mode is dismissed
                selections = new ArrayList<>();
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }
        });

        // Create an empty adapter to display the list of sets
        mAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.fragment_set_list,
                null,
                new String[]{SetList.SET_TITLE},
                new int[]{R.id.text_set_title},
                0);
        setListAdapter(mAdapter);

        // Prepare the loader
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        // Get stack title and use it to generate the table name
        TextView textView = (TextView) view.findViewById(R.id.text_set_title);
        String title = textView.getText().toString();
        FlashcardProvider handle = new FlashcardProvider(getActivity().getApplicationContext());
        String tableName = handle.getTableName(title);

        // Due to a bug present in 1.0.0,
        // if the title does not start with a letter, the table will not exist
        Cursor cursor = handle.query(SetList.CONTENT_URI,
                new String[]{SetList.SET_TITLE},
                SetList.SET_TITLE + "=?",
                new String[]{title},
                null);
        if (cursor != null && cursor.moveToFirst()) {
            final String setTitle = cursor.getString(cursor.getColumnIndex(SetList.SET_TITLE));
            if (!Character.isLetter((setTitle).charAt(0)) && handle.newSetTable(title)) {
                // Remove additional entry created by newSetTable
                handle.delete(SetList.CONTENT_URI,
                        SetList.SET_TABLE_NAME + "=?",
                        new String[]{tableName});
                // Update entry in the main table
                ContentValues contentValues = new ContentValues();
                contentValues.put(SetList.SET_TABLE_NAME, tableName);
                handle.update(SetList.CONTENT_URI,
                        contentValues,
                        SetList.SET_TITLE + "=\"" + title + "\"",
                        null);
                cursor.close();
            }
        }

        // Pass table name to SetOverviewActivity
        Intent intent = new Intent(getActivity(), SetOverviewActivity.class);
        intent.putExtra(Constants.TABLE_NAME, tableName);
        intent.putExtra(Constants.TITLE, title);
        startActivity(intent);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final String SET_SELECTION = "((" +
                SetList.SET_TITLE + " NOTNULL) AND (" +
                SetList.SET_TITLE + " != '' ))";
        return new CursorLoader(getActivity(),
                SetList.CONTENT_URI,
                SET_SUMMARY_PROJECTION,
                SET_SELECTION,
                null,
                null);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in
        mAdapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}