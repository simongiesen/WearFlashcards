package com.efa.wearflashcards;

import android.app.ActionBar;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;

import com.efa.wearflashcards.data.FlashcardContract;


/**
 * Fragment that generates a list of stacks to be displayed on screen.
 */
public class ListViewLoader extends ListActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    // These are the stack names that we will retrieve
    static final String[] STACKS = new String[]{FlashcardContract.StackList._ID,
            FlashcardContract.StackList.TABLE_NAME};
    // This is the select criteria for stacks
    static final String STACK_SELECTION = "((" +
            FlashcardContract.StackList.TABLE_NAME + " NOTNULL) AND (" +
            FlashcardContract.StackList.TABLE_NAME + " != '' ))";
    // This is the Adapter being used to display the list's data
    SimpleCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a progress bar to display while the list loads
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        progressBar.setIndeterminate(true);
        getListView().setEmptyView(progressBar);

        // Must add the progress bar to the root of the layout
        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        root.addView(progressBar);

        // For the cursor adapter, specify which columns go into which view
        String[] fromColumns = {FlashcardContract.StackList.TABLE_NAME};
        int[] toViews = {android.R.id.text1}; // The TextView in simple_list_item_1

        // Create an empty adapter that we will use to display the loaded data.
        // We pass null for the cursor, then update it in onLoadFinished()
        mAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1, null, fromColumns, toViews, 0);
        setListAdapter(mAdapter);

        // Prepare the loader. Either re-connect with an existing one, or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }

    // Called when a new Loader needs to be created
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed
        // TODO create a content provider
        return new CursorLoader(this, null, STACKS, STACK_SELECTION, null, null) {
        };
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

    }
}