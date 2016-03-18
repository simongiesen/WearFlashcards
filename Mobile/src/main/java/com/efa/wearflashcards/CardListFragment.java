package com.efa.wearflashcards;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.efa.wearflashcards.data.FlashcardContract.CardSet;
import com.efa.wearflashcards.data.FlashcardProvider;

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
    // Save table name from SetOverview
    private String table_name;
    // Store position of selected items
    private List<Integer> selections = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Get table name from SetOverview
        Bundle bundle = getArguments();
        table_name = bundle.getString(Constants.TABLE_NAME);
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
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // Respond to clicks on the actions in the CAB
                switch (item.getItemId()) {
                    case R.id.delete:
                        for (int i = 0, n = selections.size(); i < n; i++) {
                            // Get term and definition from the ListView item
                            LinearLayout card = (LinearLayout) getListView().getChildAt(selections.get(i));
                            TextView term = (TextView) card.findViewById(R.id.card_term);
                            TextView definition = (TextView) card.findViewById(R.id.card_definition);

                            // Build delete arguments
                            final String selection = CardSet.TERM + "=? AND " + CardSet.DEFINITION + "=?";
                            final String[] selectionArgs = {term.getText().toString(), definition.getText().toString()};

                            // Delete card from set
                            FlashcardProvider handle = new FlashcardProvider();
                            handle.delete(Uri.withAppendedPath(CardSet.CONTENT_URI, table_name), selection, selectionArgs);
                        }

                        // Reset selections list and hide the CAB
                        selections = new ArrayList<>();
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
        return new CursorLoader(getActivity(), Uri.withAppendedPath(CardSet.CONTENT_URI, table_name), SET_SUMMARY_PROJECTION, SET_SELECTION, null, null);
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