package com.efa.wearflashcards;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class SetOverview extends AppCompatActivity {
    private String table_name;
    private String title;
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_overview);

        // Load settings
        settings = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);

        // Get table name from SetListFragment or NewCard and pass it to CardListFragment
        Bundle bundle = getIntent().getExtras();
        table_name = bundle.getString(Constants.TABLE_NAME);
        title = bundle.getString(Constants.TITLE);
        CardListFragment frag = new CardListFragment();
        frag.setArguments(bundle);

        // Use the set title as the activity title
        setTitle(title);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.set_overview_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SetOverview.this, NewCard.class);
                intent.putExtra(Constants.TABLE_NAME, table_name);
                intent.putExtra(Constants.TITLE, title);
                startActivity(intent);
            }
        });

        // Load flashcard sets
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.set_overview_layout, frag)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Get menu items
        MenuItem shuffle = menu.getItem(Constants.SHUFFLE_POS);
        MenuItem termFirst = menu.getItem(Constants.DEF_FIRST_POS);

        // Restore settings
        shuffle.setChecked(settings.getBoolean(Constants.SHUFFLE, false));
        termFirst.setChecked(settings.getBoolean(Constants.DEF_FIRST, false));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Flip the item's checked state and save settings
        if (id == R.id.shuffle) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(Constants.SHUFFLE, !item.isChecked());
            editor.apply();
            item.setChecked(!item.isChecked());
            return true;
        } else if (id == R.id.definition_first) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(Constants.DEF_FIRST, !item.isChecked());
            editor.apply();
            return true;
        } else if (id == R.id.study_set_button) {
            Intent intent = new Intent(SetOverview.this, StudySet.class);
            intent.putExtra(Constants.TERM, new String[]{"Term one", "Term two"});
            intent.putExtra(Constants.DEFINITION, new String[]{"Def one", "Def two"});
            intent.putExtra(Constants.TITLE, title);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Return to Main if back key is pressed instead of going to previous activity
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
    }
}
