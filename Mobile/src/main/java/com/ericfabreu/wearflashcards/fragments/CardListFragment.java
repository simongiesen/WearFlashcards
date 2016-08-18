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

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.activities.ManageCardActivity;
import com.ericfabreu.wearflashcards.adapters.CardListAdapter;
import com.ericfabreu.wearflashcards.data.FlashcardContract.CardSet;
import com.ericfabreu.wearflashcards.data.FlashcardContract.FolderList;
import com.ericfabreu.wearflashcards.data.FlashcardContract.SetList;
import com.ericfabreu.wearflashcards.data.FlashcardProvider;
import com.ericfabreu.wearflashcards.utils.Constants;
import com.ericfabreu.wearflashcards.utils.PreferencesHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * Generates a list of cards to be displayed on screen.
 */
public class CardListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String[] SET_SUMMARY_PROJECTION =
            new String[]{CardSet._ID, CardSet.TERM, CardSet.DEFINITION, CardSet.STAR};
    private CardListAdapter mAdapter;
    private FlashcardProvider mProvider;
    private String tableName, folderTable;
    private long tableId, folderId;
    private List<Long> selections = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Get table name from SetOverviewActivity
        Bundle bundle = getArguments();
        tableName = bundle.getString(Constants.TAG_TABLE_NAME);
        folderTable = bundle.getString(Constants.TAG_FOLDER);
        tableId = bundle.getLong(Constants.TAG_ID);
        folderId = bundle.getLong(Constants.TAG_FOLDER_ID);
        mProvider = new FlashcardProvider(getActivity().getApplicationContext());
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Setup contextual action mode
        final ListView listView = getListView();
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setDividerHeight(0);
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
                        // Display alert message
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        if (selections.size() > 1) {
                            builder.setTitle(R.string.dialog_delete_cards);
                        } else {
                            builder.setTitle(R.string.dialog_delete_card);
                        }
                        builder.setMessage(R.string.dialog_card_cannot_undo);
                        builder.setCancelable(true);
                        builder.setPositiveButton(R.string.button_delete,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        for (int i = 0, n = selections.size(); i < n; i++) {
                                            // Delete card from set
                                            mProvider.delete(Uri.withAppendedPath
                                                            (CardSet.CONTENT_URI, tableName),
                                                    CardSet._ID + "=?",
                                                    new String[]{String
                                                            .valueOf(selections.get(i))});
                                        }
                                        mode.finish();
                                        getActivity().invalidateOptionsMenu();
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
                        cardEditListener(selections.get(0));
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

        // Use the CardListAdapter to display the list of cards
        mAdapter = new CardListAdapter(getActivity(),
                this,
                tableName,
                null,
                0);
        setListAdapter(mAdapter);

        // Show text if database is empty
        setEmptyText(getString(R.string.message_empty_stack));

        // Prepare the loader
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        cardEditListener(id);
    }

    @Override
    public void setEmptyText(CharSequence text) {
        final boolean starredOnly;
        if (folderTable == null) {
            starredOnly = PreferencesHelper.getStar(getActivity().getApplicationContext(),
                    mProvider, SetList.CONTENT_URI, tableId, SetList.STARRED_ONLY);
        } else {
            starredOnly = PreferencesHelper.getStar(getActivity().getApplicationContext(),
                    mProvider, FolderList.CONTENT_URI, folderId, FolderList.STARRED_ONLY);
        }
        final Uri tableUri = Uri.withAppendedPath(CardSet.CONTENT_URI, tableName);
        if (starredOnly && mProvider.getCardCount(tableUri, true) == 0 &&
                mProvider.getCardCount(tableUri, false) > 0) {
            super.setEmptyText(getText(R.string.message_all_cards_hidden));
        } else {
            super.setEmptyText(text);
        }
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final boolean starredOnly;
        if (folderTable == null) {
            starredOnly = PreferencesHelper.getStar(getActivity().getApplicationContext(),
                    mProvider, SetList.CONTENT_URI, tableId, SetList.STARRED_ONLY);
        } else {
            starredOnly = PreferencesHelper.getStar(getActivity().getApplicationContext(),
                    mProvider, FolderList.CONTENT_URI, folderId, FolderList.STARRED_ONLY);
        }
        final String selection = starredOnly ? CardSet.STAR + "=?" : null;
        final String[] selectionArgs = starredOnly ? new String[]{"1"} : null;
        return new CursorLoader(getActivity(),
                Uri.withAppendedPath(CardSet.CONTENT_URI, tableName),
                SET_SUMMARY_PROJECTION,
                selection,
                selectionArgs,
                PreferencesHelper.getOrder(getActivity().getApplicationContext(),
                        CardSet.TERM, Constants.PREF_KEY_CARD_ORDER));
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    /**
     * Finds the card's term and definition and send it to ManageCardActivity.
     */
    private void cardEditListener(long id) {
        Cursor cursor = mProvider.query(Uri.withAppendedPath(CardSet.CONTENT_URI, tableName),
                new String[]{CardSet.TERM, CardSet.DEFINITION},
                CardSet._ID + "=?",
                new String[]{String.valueOf(id)},
                null);
        if (cursor != null && cursor.moveToFirst()) {
            String term = cursor.getString(cursor.getColumnIndex(CardSet.TERM));
            String definition = cursor.getString(cursor.getColumnIndex(CardSet.DEFINITION));
            Intent intent = new Intent(getActivity(), ManageCardActivity.class);
            intent.putExtra(Constants.TAG_TERM, term);
            intent.putExtra(Constants.TAG_DEFINITION, definition);
            intent.putExtra(Constants.TAG_TABLE_NAME, tableName);
            intent.putExtra(Constants.TAG_TITLE, getActivity().getTitle());
            intent.putExtra(Constants.TAG_EDITING_MODE, true);
            startActivityForResult(intent, Constants.REQUEST_CODE_EDIT);
            cursor.close();
        }
    }
}