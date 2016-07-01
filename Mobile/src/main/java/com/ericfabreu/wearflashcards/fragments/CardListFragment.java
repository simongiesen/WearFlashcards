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
import android.view.View;
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
 * Generates a list of cards to be displayed on screen.
 */
public class CardListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String[] SET_SUMMARY_PROJECTION =
            new String[]{CardSet._ID, CardSet.TERM, CardSet.DEFINITION};
    private SimpleCursorAdapter mAdapter;
    private String tableName;
    private List<View> selections = new ArrayList<>();

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

        // Setup contextual action mode
        final ListView listView = getListView();
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position,
                                                  long id, boolean checked) {
                // Store position of selected items
                if (checked) {
                    selections.add(listView.getChildAt(position));
                } else {
                    selections.remove(listView.getChildAt(position));
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
                        // Display alert message
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
                                            LinearLayout card = (LinearLayout) selections.get(i);
                                            TextView term = (TextView) card
                                                    .getChildAt(Constants.TERM_POS);
                                            TextView definition = (TextView) card
                                                    .getChildAt(Constants.DEF_POS);

                                            // Build delete arguments
                                            final String selection = CardSet.TERM + "=? AND " +
                                                    CardSet.DEFINITION + "=?";
                                            final String[] selectionArgs = {
                                                    term.getText().toString(),
                                                    definition.getText().toString()};

                                            // Delete card from set
                                            FlashcardProvider handle = new FlashcardProvider(
                                                    getActivity().getApplicationContext());
                                            handle.delete(Uri.withAppendedPath
                                                            (CardSet.CONTENT_URI, tableName),
                                                    selection,
                                                    selectionArgs);
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
                        // Get term and definition and send them to EditCardActivity
                        LinearLayout card = (LinearLayout) selections.get(0);
                        TextView termView = (TextView) card.getChildAt(Constants.TERM_POS);
                        TextView definitionView = (TextView) card.getChildAt(Constants.DEF_POS);
                        String term = termView.getText().toString();
                        String definition = definitionView.getText().toString();
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

        // Create an empty adapter to display the list of cards
        mAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.fragment_card_list,
                null,
                new String[]{CardSet.TERM, CardSet.DEFINITION},
                new int[]{R.id.text_term, R.id.text_definition},
                0);
        setListAdapter(mAdapter);

        // Show text if database is empty
        setEmptyText(getString(R.string.empty_stack));

        // Prepare the loader
        getLoaderManager().initLoader(0, null, this);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final String SET_SELECTION = "((" +
                CardSet.TERM + " NOTNULL) AND (" +
                CardSet.TERM + " != '' ))";
        return new CursorLoader(getActivity(),
                Uri.withAppendedPath(CardSet.CONTENT_URI, tableName),
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