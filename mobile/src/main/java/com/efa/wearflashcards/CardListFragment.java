package com.efa.wearflashcards;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SimpleCursorAdapter;

import com.efa.wearflashcards.data.FlashcardContract.CardSet;


/**
 * Fragment that generates a list of cards to be displayed on screen.
 */
public class CardListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {
    // These are the set names that we will retrieve
    static final String[] SET_SUMMARY_PROJECTION = new String[]{CardSet._ID, CardSet.TERM, CardSet.DEFINITION};
    // Save table name from SetOverview
    public String table_name;
    // This is the Adapter being used to display the list's data
    SimpleCursorAdapter mAdapter;

    // Empty constructor
    public CardListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Get table name from SetOverview
        Bundle bundle = getArguments();
        table_name = bundle.getString("table_name");
        Log.d("cardList_onCreate: ", table_name);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        registerForContextMenu(getListView());

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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return true;
//        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
//        switch (item.getItemId()) {
//            case R.id.delete:
//                // Get title from textView item
//                TextView textView = (TextView) info.targetView.findViewById(R.id.main_set_title);
//                String title = textView.getText().toString();
//
//                // Delete set from database
//                FlashcardProvider handle = new FlashcardProvider();
//                handle.deleteSetTable(title);
//
//                // Refresh the loader with new data
//                getLoaderManager().initLoader(0, null, this);
//                return true;
//            default:
//                return super.onContextItemSelected(item);
//        }
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