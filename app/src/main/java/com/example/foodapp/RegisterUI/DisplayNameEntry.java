package com.example.foodapp.RegisterUI;

import androidx.annotation.NonNull;
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
import android.widget.TextView;

import com.example.foodapp.R;
import com.example.foodapp.utils.MyIdlingResource;
import com.example.fooddata.FoodLibrary;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

public class DisplayNameEntry extends AppCompatActivity {

    private HashMap<Object, Object> buildUser;
    private static String TAG= "DisplayNameEntry";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_name_entry);

        if(getIntent().getExtras() != null){
            Log.d(TAG, "Getting Intent Extra");
            buildUser = (HashMap)getIntent().getExtras().getSerializable(GetStarted.ARGKEY);
        }else{
            Log.d(TAG, "Getting buildUser from sharedPreferences");
            buildUser = (HashMap)getPreferences(Context.MODE_PRIVATE).getAll();
        }

        final EditText displayName = findViewById(R.id.displayNameInput);
        final TextView errorText = findViewById(R.id.DisplayNameError);
        final ProgressBar progressBar = findViewById(R.id.DisplayNameProgress);

        final Button nextButton  = findViewById(R.id.nextButton3);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        nextButton.setEnabled(false);
        TextWatcher textChanged = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean enable = checkField(displayName.getText().toString());
                nextButton.setEnabled(enable);
            }
        };

        displayName.addTextChangedListener(textChanged);
        final Intent intent = new Intent(this, EmailEntry.class);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildUser.put("username", displayName.getText().toString());
                intent.putExtra(GetStarted.ARGKEY, buildUser);
                progressBar.setVisibility(View.VISIBLE);
                MyIdlingResource.increment();
                FoodLibrary.validateUser(displayName.getText().toString()).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        MyIdlingResource.decrement();
                        if(task.isSuccessful()){
                            if(task.getResult().getDocuments().size() == 0){
                                if(errorText.getVisibility() == View.VISIBLE){
                                    errorText.setVisibility(View.INVISIBLE);
                                }
                                progressBar.setVisibility(View.INVISIBLE);
                                startActivity(intent);
                            }else{
                                progressBar.setVisibility(View.INVISIBLE);
                                errorText.setTextColor(getResources().getColor(R.color.colorError));
                                errorText.setVisibility(View.VISIBLE);
                            }
                        }else{
                            progressBar.setVisibility(View.INVISIBLE);
                            errorText.setText("Error occurred. Please try again");
                            errorText.setTextColor(getResources().getColor(R.color.colorError));
                            errorText.setVisibility(View.VISIBLE);
                            //Display Error
                        }
                    }
                });
                //NavDirections action = DisplayNameEntryDirections.actionDisplayNameEntryToEmailEntry(buildUser);
                //Navigation.findNavController(v).navigate(action);
            }
        });
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.d(TAG, "Calling onPause()");
        getIntent().putExtra(GetStarted.ARGKEY,buildUser );
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "Calling onSavedInstanceState");
        outState.putSerializable(GetStarted.ARGKEY, buildUser);
        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
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
