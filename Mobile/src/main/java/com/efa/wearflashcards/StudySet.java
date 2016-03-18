package com.efa.wearflashcards;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

public class StudySet extends AppCompatActivity {
    private String[] terms = null;
    private String[] definitions = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.study_set);

        // Get set title, terms, and definitions from SetOverview
        Bundle bundle = getIntent().getExtras();
        terms = bundle.getStringArray(Constants.TERM);
        definitions = bundle.getStringArray(Constants.DEFINITION);
        setTitle(bundle.getString(Constants.TITLE));
        createCards();
    }

    // Adapted from the GridViewPager sample (https://goo.gl/ZGLbWH)
    protected void createCards() {
        final ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new StudySetAdapter(getSupportFragmentManager(), terms, definitions));
    }
}
