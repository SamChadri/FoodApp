package com.example.foodapp.RegisterUI;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.foodapp.R;

import java.util.HashMap;

public class EmailEntry extends AppCompatActivity {

    private static String TAG= "EmailEntry";

    private HashMap<Object, Object> buildUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_entry);

        if(getIntent().getSerializableExtra(GetStarted.ARGKEY) != null){
            Log.d(TAG, "Getting Intent Extra");
            buildUser = (HashMap)getIntent().getSerializableExtra(GetStarted.ARGKEY);
        }else{
            Log.d(TAG, "Getting buildUser from sharedPreferences");
            buildUser = (HashMap)getPreferences(Context.MODE_PRIVATE).getAll();
        }

        final EditText emailInput = findViewById(R.id.emailInput);

        final Button nextButton = findViewById(R.id.emailNextButton);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);


        nextButton.setEnabled(false);
        TextWatcher textChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean enable = checkField(emailInput.getText().toString());
                nextButton.setEnabled(enable);
            }
        };

        emailInput.addTextChangedListener(textChangedListener);

        final Intent intent = new Intent(this, DateOfBirth.class);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildUser.put("email", emailInput.getText().toString());
                intent.putExtra(GetStarted.ARGKEY, buildUser);

                startActivity(intent);
                //NavDirections action = EmailEntryDirections.actionEmailEntryToCreatePassword(buildUser);
                //Navigation.findNavController(v).navigate(action);
            }
        });


    }

    @Override
    protected void onStop(){
        SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();

        for(HashMap.Entry<Object, Object> item: buildUser.entrySet()){
            if(item.getValue() instanceof Integer){
                editor.putInt((String)item.getKey(), (int)item.getValue());
            }else{
                editor.putString((String)item.getKey(), (String)item.getValue());
            }
        }

        editor.commit();
        Log.d(TAG, "Put values into sharedPreferences");

        super.onStop();

    }



    private boolean checkField(String field){
        return !field.isEmpty() && field != null;
    }
}
