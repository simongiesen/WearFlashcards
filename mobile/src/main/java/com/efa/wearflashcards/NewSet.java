package com.efa.wearflashcards;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.efa.wearflashcards.data.FlashcardProvider;

public class NewSet extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_set);
    }

    // Create an empty set and return to MainActivity
    public void newSet(View view) {
        EditText text = (EditText) findViewById(R.id.new_set_title);
        String title = text.getText().toString();
        FlashcardProvider handle = new FlashcardProvider();
        handle.newSetTable(title);

        // Return to main screen
        Intent main = new Intent(NewSet.this, MainActivity.class);
        startActivity(main);
    }
}
