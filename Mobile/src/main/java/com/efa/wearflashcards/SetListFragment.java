package com.efa.wearflashcards;

import android.app.ListFragment;
import android.app.LoaderManager;
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

import com.efa.wearflashcards.data.FlashcardContract.SetList;
import com.efa.wearflashcards.data.FlashcardProvider;

import java.util.ArrayList;
import java.util.List;


/**
 * Fragment that generates a list of sets to be displayed on screen.
 */
public class SetListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {
    // These are the set names that we will retrieve
    private static final String[] SET_SUMMARY_PROJECTION =
            new String[]{SetList._ID, SetList.SET_TITLE};
    // This is the Adapter being used to display the list's data
    private SimpleCursorAdapter mAdapter;
    // Store position of selected items
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

        // Contextual action mode code adapted from
        // http://developer.android.com/guide/topics/ui/menus.html#context-menu
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

                // Only show the edit button if there is only one item selected
                if (selections.size() == 1) {
                    mode.getMenu().findItem(R.id.edit).setVisible(true);
                } else {
                    mode.getMenu().findItem(R.id.edit).setVisible(false);
                }
            }

            @Override
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                // Respond to clicks on the actions in the CAB
                switch (item.getItemId()) {
                    case R.id.delete:
                        // Display alert message
                        // http://stackoverflow.com/a/13511580/3522216
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(R.string.delete_set);
                        builder.setMessage(R.string.cannot_undo);
                        builder.setCancelable(true);

                        builder.setPositiveButton(R.string.delete,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // Get table name from the set list and delete it from the database
                                        for (int i = 0, n = selections.size(); i < n; i++) {
                                            TextView set = (TextView) getListView().getChildAt(selections.get(i));
                                            String title = set.getText().toString();
                                            FlashcardProvider handle = new FlashcardProvider();
                                            handle.deleteSetTable(title);
                                        }

                                        // Reset selections list and hide the CAB
                                        selections = new ArrayList<>();
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

                    case R.id.edit:
                        // Get set title and send it to EditSetTitle
                        TextView set = (TextView) getListView().getChildAt(selections.get(0));
                        String title = set.getText().toString();
                        Intent intent = new Intent(getActivity(), EditSetTitle.class);
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
                // Inflate the menu for the CAB
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.context_menu, menu);
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }
        });

        // Create an empty adapter we will use to display the loaded data.
        mAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.main_list_item,
                null,
                new String[]{SetList.SET_TITLE},
                new int[]{R.id.main_set_title},
                0);
        setListAdapter(mAdapter);

        // Prepare the loader. Either re-connect with an existing one, or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }

    // Open the flashcard set when it is clicked
    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        // Get stack title
        TextView textView = (TextView) view.findViewById(R.id.main_set_title);
        String title = textView.getText().toString();

        // Pass table name to SetOverview
        FlashcardProvider handle = new FlashcardProvider();
        String table_name = handle.getTableName(title);
        Intent intent = new Intent(getActivity(), SetOverview.class);
        intent.putExtra(Constants.TABLE_NAME, table_name);
        intent.putExtra(Constants.TITLE, title);
        startActivity(intent);
    }

    // Called when a new Loader needs to be created
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Select criteria for flashcard sets
        final String SET_SELECTION = "((" +
                SetList.SET_TITLE + " NOTNULL) AND (" +
                SetList.SET_TITLE + " != '' ))";

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed
        return new CursorLoader(getActivity(), SetList.CONTENT_URI, SET_SUMMARY_PROJECTION, SET_SELECTION, null, null);
    }

    // Called when a previously created loader has finished loading
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(data);
    }

    // Called when a previously created loader is reset, making the data unavailable
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }
}