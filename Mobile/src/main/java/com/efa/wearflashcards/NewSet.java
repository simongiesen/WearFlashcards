package com.efa.wearflashcards;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.efa.wearflashcards.data.FlashcardProvider;

public class NewSet extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_set);
        setTitle(getString(R.string.create_set));
    }

    // Create an empty set and return to Main
    public void newSet(View view) {
        EditText text = (EditText) findViewById(R.id.new_set_title);
        String title = text.getText().toString();

        // Check if title is empty
        if (TextUtils.isEmpty(title)) {
            text.setError(getString(R.string.empty_title));
            return;
        }

        // Check if title is available
        FlashcardProvider handle = new FlashcardProvider();
        if (!handle.newSetTable(title)) {
            text.setError(getString(R.string.title_taken));
            return;
        }

        // Return to main screen
        Intent main = new Intent(this, Main.class);
        startActivity(main);
        finish();
    }
}
