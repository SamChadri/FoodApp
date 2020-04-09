package com.example.foodapp.loginHelper;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.Image;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.example.authlibrary.AuthLib;
import com.example.foodapp.Home;
import com.example.foodapp.R;
import com.example.foodapp.RegisterUI.GetStarted;
import com.example.foodapp.forgotPasswordUI.sendReset;
import com.example.fooddata.FoodLibrary;
import com.example.fooddata.User;
import com.example.imagestorage.ImageStorage;
import com.example.result.ServerResult;


import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class SignIn extends AppCompatActivity {
    private static String TAG = "SignInActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        AuthLib.initAuth();
        FoodLibrary.initDatabase();
        ImageStorage.init();

        final LoginViewModel model = new LoginViewModel();

        final EditText email = findViewById(R.id.emailText);
        final EditText password = findViewById(R.id.passwordText);

        final Button signIn = findViewById(R.id.signInButton);
        final Button join = findViewById(R.id.joinButton);
        final ProgressBar loading = findViewById(R.id.loading);
        final TextView forgotPassword = findViewById(R.id.forgotPasswordView);
        final TextView loginError = findViewById(R.id.loginError);

        final User currUser = null;

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);


        /**
        try{
            FoodLibrary.importData(getApplicationContext(), "postBatch2.json", "Post");

        }catch(IOException e){
            Log.d(TAG, "Error occurred: " + e);
        }

        **/

        //TODO Implement loading widget.
        //TODO Change buton colors




        final Intent registerIntent = new Intent(this, GetStarted.class);
        final Intent resetIntent = new Intent(this, sendReset.class);

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(registerIntent);

            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(resetIntent);
            }
        });
        final Intent loginIntent = new Intent(this, Home.class);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading.setVisibility(View.VISIBLE);
                loginError.setVisibility(View.INVISIBLE);
                if(!isEmailValid(email.getText().toString())){
                    email.setError("Email format not valid");
                    loading.setVisibility(View.INVISIBLE);
                }else if(!isPasswordValid(password.getText().toString())){
                    password.setError("Password must be at least 5 characters");
                    loading.setVisibility(View.INVISIBLE);
                }else{
                    model.login(email.getText().toString(), password.getText().toString());

                }
                model.getLoginStatus().observe(SignIn.this, new Observer<HashMap>() {
                    @Override
                    public void onChanged(HashMap hashMap) {
                        if(hashMap.containsKey("authenticated")){
                            loading.setVisibility(View.INVISIBLE);
                            if((boolean)hashMap.get("authenticated") && hashMap.containsKey("foodStoreUser")){
                                startActivity(loginIntent);
                            }else{
                                if(hashMap.containsKey("loginError")){
                                    showLoginFailed((String)hashMap.get("loginError"), loginError);
                                }else{
                                    showLoginFailed("Error occurred please try again", loginError);
                                }
                            }
                        }
                    }
                });
            }
        });

    }
    private boolean isEmailValid(String email) {
        if (email == null) {
            return false;
        } else {
            return !email.trim().isEmpty() && email.contains("@");
        }
    }
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

    private void updateUiWithUser(User _user) {
        String welcome = "Welcome!" + _user.username;
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(String errorString, TextView loginError) {
        loginError.setTextColor(getResources().getColor(R.color.colorError));
        loginError.setText(errorString);
        loginError.setVisibility(View.VISIBLE);
    }



}
