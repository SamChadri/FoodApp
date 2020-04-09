package com.example.foodapp.RegisterUI;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.foodapp.R;

import java.util.HashMap;

public class NameEntry extends AppCompatActivity {
    private static final String TAG = "NameEntryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_entry);

        final Button nextButton = findViewById(R.id.nextButton2);

        final EditText firstName = findViewById(R.id.firstName);

        final EditText lastName = findViewById(R.id.lastName);

        RegisterViewModel model = new RegisterViewModel();
        final HashMap buildUser = new HashMap();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        nextButton.setEnabled(false);
        TextWatcher afterTextChangedListner = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean enable = checkFields(firstName.getText().toString(), lastName.getText().toString());
                Log.d(TAG, "Enable value: " + enable);
                nextButton.setEnabled(enable);
            }
        };

        firstName.addTextChangedListener(afterTextChangedListner);
        lastName.addTextChangedListener(afterTextChangedListner);
        final Intent intent = new Intent(this, DisplayNameEntry.class);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildUser.put("firstName", firstName.getText().toString());
                buildUser.put("lastName", lastName.getText().toString());
                intent.putExtra(GetStarted.ARGKEY, buildUser);
                startActivity(intent);
                //NavDirections action = NameEntryDirections.actionNameEntryToDisplayNameEntry(buildUser);
                //Navigation.findNavController(v).navigate(action);
            }
        });
    }

    private boolean checkFields(String field1, String field2){
        if( field1 == null || field2 == null){
            return false;
        }
        return !field1.isEmpty() && !field2.isEmpty();
    }
}
