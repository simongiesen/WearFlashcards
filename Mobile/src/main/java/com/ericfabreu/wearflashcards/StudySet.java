package com.ericfabreu.wearflashcards;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class StudySet extends AppCompatActivity {
    private String[] terms;
    private String[] definitions;
    private String title;
    private String tableName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.study_set);

        // Get set title, terms, and definitions from SetOverview
        Bundle bundle = getIntent().getExtras();
        terms = bundle.getStringArray(Constants.TERM);
        definitions = bundle.getStringArray(Constants.DEFINITION);
        title = bundle.getString(Constants.TITLE);
        tableName = bundle.getString(Constants.TABLE_NAME);
        setTitle(title);
        createCards();
    }

    // Adapted from the GridViewPager sample (https://goo.gl/ZGLbWH)
    protected void createCards() {
        final VerticalViewPager pager = (VerticalViewPager) findViewById(R.id.pager);
        pager.setAdapter(new StudySetAdapter(getSupportFragmentManager(), terms, definitions));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Pass table name back to SetOverview if the toolbar back button is clicked
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        // Pass table name back to SetOverview
        Intent intent = new Intent(this, SetOverview.class);
        intent.putExtra(Constants.TABLE_NAME, tableName);
        intent.putExtra(Constants.TITLE, title);
        startActivity(intent);
    }
}
