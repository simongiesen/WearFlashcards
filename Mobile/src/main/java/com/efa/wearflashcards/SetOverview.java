package com.efa.wearflashcards;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class SetOverview extends AppCompatActivity {
    private String table_name;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_overview);

        // Get table name from SetListFragment or NewCard and pass it to CardListFragment
        Bundle bundle = getIntent().getExtras();
        table_name = bundle.getString("table_name");
        title = bundle.getString("title");
        CardListFragment frag = new CardListFragment();
        frag.setArguments(bundle);

        setTitle(title);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.set_overview_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SetOverview.this, NewCard.class);
                intent.putExtra("table_name", table_name);
                intent.putExtra("title", title);
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
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
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
