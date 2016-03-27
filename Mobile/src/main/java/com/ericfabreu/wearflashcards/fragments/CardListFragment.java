package com.ericfabreu.wearflashcards.fragments;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.activities.EditCardActivity;
import com.ericfabreu.wearflashcards.data.FlashcardContract.CardSet;
import com.ericfabreu.wearflashcards.data.FlashcardProvider;
import com.ericfabreu.wearflashcards.utils.Constants;

import java.util.ArrayList;
import java.util.List;


/**
 * Fragment that generates a list of cards to be displayed on screen.
 */
public class CardListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {
    // These are the set names that we will retrieve
    static final String[] SET_SUMMARY_PROJECTION = new String[]{CardSet._ID, CardSet.TERM, CardSet.DEFINITION};
    // This is the Adapter being used to display the list's data
    SimpleCursorAdapter mAdapter;
    // Save table name from SetOverviewActivity
    private String tableName;
    // Store position of selected items
    private List<Integer> selections = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Get table name from SetOverviewActivity
        Bundle bundle = getArguments();
        tableName = bundle.getString(Constants.TABLE_NAME);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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
                        if (selections.size() > 1) {
                            builder.setTitle(R.string.delete_cards);
                        } else {
                            builder.setTitle(R.string.delete_card);
                        }
                        builder.setMessage(R.string.cannot_undo);
                        builder.setCancelable(true);

                        builder.setPositiveButton(R.string.delete,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        for (int i = 0, n = selections.size(); i < n; i++) {
                                            // Get term and definition from the ListView item
                                            LinearLayout card = (LinearLayout) getListView().getChildAt(selections.get(i));
                                            TextView term = (TextView) card.getChildAt(Constants.TERM_POS);
                                            TextView definition = (TextView) card.getChildAt(Constants.DEF_POS);

                                            // Build delete arguments
                                            final String selection = CardSet.TERM + "=? AND " + CardSet.DEFINITION + "=?";
                                            final String[] selectionArgs = {term.getText().toString(), definition.getText().toString()};

                                            // Delete card from set
                                            FlashcardProvider handle = new FlashcardProvider();
                                            handle.delete(Uri.withAppendedPath(CardSet.CONTENT_URI, tableName), selection, selectionArgs);
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
                        // Get term and definition and send them to EditCardActivity
                        LinearLayout card = (LinearLayout) getListView().getChildAt(selections.get(0));
                        TextView tView = (TextView) card.getChildAt(Constants.TERM_POS);
                        TextView defView = (TextView) card.getChildAt(Constants.DEF_POS);
                        String term = tView.getText().toString();
                        String definition = defView.getText().toString();
                        Intent intent = new Intent(getActivity(), EditCardActivity.class);
                        intent.putExtra(Constants.TERM, term);
                        intent.putExtra(Constants.DEFINITION, definition);
                        intent.putExtra(Constants.TABLE_NAME, tableName);
                        intent.putExtra(Constants.TITLE, getActivity().getTitle());
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
                R.layout.card_list,
                null,
                new String[]{CardSet.TERM, CardSet.DEFINITION},
                new int[]{R.id.card_term, R.id.card_definition},
                0);
        setListAdapter(mAdapter);

        // Show text if database is empty
        setEmptyText(getString(R.string.empty_stack));

        // Prepare the loader. Either re-connect with an existing one, or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }

    // Called when a new Loader needs to be created
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Select criteria for flashcard sets
        final String SET_SELECTION = "((" +
                CardSet.TERM + " NOTNULL) AND (" +
                CardSet.TERM + " != '' ))";

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed
        return new CursorLoader(getActivity(), Uri.withAppendedPath(CardSet.CONTENT_URI, tableName), SET_SUMMARY_PROJECTION, SET_SELECTION, null, null);
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