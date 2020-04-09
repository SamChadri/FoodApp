package com.example.foodapp.forgotPasswordUI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import com.example.authlibrary.AuthLib;
import com.example.foodapp.MainActivity;
import com.example.foodapp.R;
import com.example.foodapp.loginHelper.LoginUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.SignInMethodQueryResult;

public class sendReset extends AppCompatActivity {
    final private static String TAG = "SendReset";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_reset);

        final SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove("passwordResetSent").commit();
        editor.clear().commit();


        final EditText recoveryEmail = findViewById(R.id.recoveryEmail);
        final Button sendButton = findViewById(R.id.sendButton);
        final ProgressBar progressBar = findViewById(R.id.checkEmailProgress);
        final TextView errorText = findViewById(R.id.sendResetError);

        sendButton.setEnabled(false);
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                sendButton.setEnabled(checkFeild(recoveryEmail.getText().toString()));
            }
        };
        recoveryEmail.addTextChangedListener(textWatcher);

        final Intent intent = new Intent(this, resetPassword.class);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                editor.putString("recoveryEmail", recoveryEmail.getText().toString());
                editor.commit();
                intent.putExtra("recoveryEmail", recoveryEmail.getText().toString());
                boolean notNull  =recoveryEmail.getText().toString()!=null;
                Log.d(TAG, "recoveryEmail status: " + notNull );
                Log.d(TAG, "recoveryEmail: " + recoveryEmail.getText().toString());
                AuthLib.alreadyRegistered(recoveryEmail.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                            @Override
                            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                if(task.isSuccessful()){
                                    if(!task.getResult().getSignInMethods().isEmpty()){
                                        Log.d(TAG, "Valid email provided");
                                        startActivity(intent);
                                    }else{
                                        errorText.setTextColor(getResources().getColor(R.color.colorError));
                                        errorText.setVisibility(View.VISIBLE);
                                        errorText.setError("Email not found. Please provide a valid email address.");
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                }
                            }
                        });

            }
        });


    }

    @Override
    protected void onPause(){
        super.onPause();
        ((ProgressBar)findViewById(R.id.checkEmailProgress)).setVisibility(View.INVISIBLE);

    }
    private boolean checkFeild(String field){
        return !field.isEmpty() && field != null;
    }
}
