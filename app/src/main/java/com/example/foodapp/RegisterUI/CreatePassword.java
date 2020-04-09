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
import android.widget.ProgressBar;

import com.example.foodapp.R;
import com.google.firebase.Timestamp;

import java.util.HashMap;

public class CreatePassword extends AppCompatActivity {
    private static final String TAG = "CreatePasswordActivity";
    private HashMap<Object, Object> buildUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_password);

        if(getIntent().getSerializableExtra(GetStarted.ARGKEY) != null){
            Log.d(TAG, "Getting Intent Extra");
            buildUser = (HashMap)getIntent().getSerializableExtra(GetStarted.ARGKEY);
        }else{
            Log.d(TAG, "Getting buildUser from sharedPreferences");
            buildUser = (HashMap)getPreferences(Context.MODE_PRIVATE).getAll();
        }


        final EditText password =  findViewById(R.id.passwordInput);
        final RegisterViewModel model = new RegisterViewModel();
        final Button finishButton = findViewById(R.id.finishRegisterButton);
        final ProgressBar progressBar = findViewById(R.id.progressBar);

        final SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key),Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        progressBar.setVisibility(View.INVISIBLE);
        finishButton.setEnabled(false);
        TextWatcher textChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                finishButton.setEnabled(checkField(password.getText().toString()));
            }
        };

        password.addTextChangedListener(textChangedListener);
        final Intent intent = new Intent(this, verifyRegistration.class);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressBar.setVisibility(View.VISIBLE);
                buildUser.put("password", password.getText().toString());
                for(HashMap.Entry<Object, Object> item: buildUser.entrySet()){
                    if(item.getValue() instanceof Integer){
                        editor.putInt((String)item.getKey(), (int)item.getValue());
                    }else{
                        editor.putString((String)item.getKey(), (String)item.getValue());

                    }
                }
                editor.putBoolean("verificationSent", false);
                editor.commit();

                Log.d(TAG,buildUser.get("username").toString() + buildUser.get("password").toString());
                //Navigate to Verify Page
                startActivity(intent);
                //NavDirections action = CreatePasswordDirections.actionCreatePasswordToVerifyRegistration(buildUser);
                //Navigation.findNavController(v).navigate(action);
            }
        });
    }
    @Override
    protected void onPause(){
        super.onPause();

        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
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
        //TODO: Add more parameters for a valid secure password.
        //TODO: EX: Must contain: Numeric, and capital letter
        return !field.isEmpty() && field != null && field.trim().length() > 5 ;
    }
}
