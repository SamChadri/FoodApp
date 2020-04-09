package com.example.foodapp.loginHelper;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.util.Log;
import android.util.Patterns;
import android.view.View;

import com.example.authlibrary.*;
import com.example.fooddata.*;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.example.result.ServerResult;
import com.example.foodapp.R;

import java.util.HashMap;

import io.grpc.Server;

public class LoginViewModel extends ViewModel {
    private static String TAG = "LoginViewModel";

    private HashMap lStatus = new HashMap();
    private MutableLiveData<HashMap> loginStatus = new MutableLiveData<>();
    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();


    public LiveData<HashMap> getLoginStatus() {return loginStatus;}

    public LiveData<LoginFormState> getLoginFormState() {return loginFormState;}


    public void login(String email, String password){

        Task<ServerResult> result = LoginUtils.SignIn(email, password);

        result.addOnCompleteListener(new OnCompleteListener<ServerResult>() {
            @Override
            public void onComplete(@NonNull Task<ServerResult> task) {
                if(task.getResult().getResult()){
                    lStatus.put("authenticated", true);
                    lStatus.put("foodStoreUser" , (User)task.getResult().getData());
                    loginStatus.setValue(lStatus);
                    Log.d(TAG, "login()- Completed successfully. Setting authenticated KEY to TRUE. Setting currUser to foodStoreUser.");
                }else{
                    lStatus.put("authenticated", false);
                    lStatus.put("loginError", task.getResult().getData());
                    loginStatus.setValue(lStatus);
                    Log.d(TAG, "login()- Error occurred. Setting authenticated KEY to FALSE");
                }
            }
        });

    }

    public void checkFields(String email, String password){

        if(!isEmailValid(email)){
            loginFormState.setValue(new LoginFormState("Email format not valid",null));
        }
        if(!isPasswordValid(password)){
            loginFormState.setValue(new LoginFormState(null,"Password must be at least 5 characters"));

        }else{
            loginFormState.setValue(new LoginFormState(true));
        }
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


}
