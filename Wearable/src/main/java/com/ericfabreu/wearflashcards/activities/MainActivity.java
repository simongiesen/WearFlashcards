package com.ericfabreu.wearflashcards.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.wearable.view.WearableListView;
import android.view.View;
import android.widget.TextView;

import com.ericfabreu.wearflashcards.R;
import com.ericfabreu.wearflashcards.adapters.ListViewAdapter;
import com.ericfabreu.wearflashcards.layouts.WearableListItemLayout;
import com.ericfabreu.wearflashcards.utils.Constants;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the list component from the layout of the activity and assign an adapter to it
        setContentView(R.layout.activity_main);
        WearableListView listView = (WearableListView) findViewById(R.id.layout_list);
        listView.setAdapter(new ListViewAdapter(this,
                R.layout.item_set_list, getTitles(), null, getIcons(), 0));
        listView.setClickListener(new WearableListView.ClickListener() {
            @Override
            public void onClick(WearableListView.ViewHolder holder) {
                ListViewAdapter.ItemViewHolder itemHolder = (ListViewAdapter.ItemViewHolder) holder;
                WearableListItemLayout layout = (WearableListItemLayout) itemHolder.getView();
                TextView textView = (TextView) layout.findViewById(R.id.text_list_item);
                Intent intent = new Intent(MainActivity.this, ListActivity.class);
                intent.putExtra(Constants.TAG_MODE, textView.getText());
                startActivity(intent);
            }

            @Override
            public void onTopEmptyRegionClick() {
            }
        });
    }

    private String[] getTitles() {
        return new String[]{getString(R.string.text_option_sets),
                getString(R.string.text_option_folders)};
    }

    private Drawable[] getIcons() {
        final Drawable shuffle = ContextCompat.getDrawable(getApplicationContext(),
                R.drawable.ic_list_set);
        final Drawable definition = ContextCompat.getDrawable(getApplicationContext(),
                R.drawable.ic_list_folder);
        return new Drawable[]{shuffle, definition};
    }

    /**
     * Launches SettingsActivity.
     */
    public void openSettings(View view) {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }
}