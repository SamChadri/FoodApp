package com.example.foodapp.RegisterUI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.foodapp.R;
import com.example.foodapp.RegisterUI.RegisterViewModel;
import com.example.foodapp.loginHelper.SignIn;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

import java.util.HashMap;
import java.util.Map;

public class verifyRegistration extends AppCompatActivity {
    private static String TAG = "VerifyRegistration";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //TODO: Add option to resend verification email.

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_registration);

        final SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key),Context.MODE_PRIVATE);
        final SharedPreferences.Editor edit = sharedPref.edit();
        final HashMap buildUser = (HashMap)sharedPref.getAll();

        final TextView verificationStatus = findViewById(R.id.verificationStatus);
        final Button finishButton = findViewById(R.id.finishButton);
        final RegisterViewModel model = new RegisterViewModel();
        final Button resendButton  = findViewById(R.id.verifyResendButton);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);



        finishButton.setEnabled(false);
        resendButton.setEnabled(false);

        if(buildUser.containsKey("verificationSent") && !(boolean)buildUser.get("verificationSent") ){
            model.setUserFields(buildUser);
            model.checkUser((String)buildUser.get("email"));
            model.getVerificationStatus().observe(this, new Observer<HashMap>() {
                @Override
                public void onChanged(HashMap hashMap) {
                    Log.d(TAG, "Contains emailValid key: " + model.getVerificationStatus().getValue().entrySet());
                    if(hashMap.containsKey("emailValid")){

                        if((boolean)hashMap.get("emailValid")){
                            model.createCredentials(sharedPref);
                            edit.putBoolean("verificationSent",true);
                            edit.commit();
                        }else{
                            resendButton.setEnabled(true);
                            Log.d(TAG,"Email provided already in use.");
                            verificationStatus.setVisibility(View.VISIBLE);
                            verificationStatus.requestFocus();
                            verificationStatus.setError("This email is already in use. Please choose a different one.");
                            verificationStatus.setText("Invalid Email");
                            verificationStatus.setTextColor(getResources().getColor(R.color.colorError));
                        }

                    }

                }
            });
        }else{

            if(buildUser.containsKey("oobCode")){
                model.setUserFields(buildUser);
                model.verifyUser((String)buildUser.get("oobCode"));
            }

            model.getVerificationStatus().observe(this, new Observer<HashMap>() {
                @Override
                public void onChanged(HashMap hashMap) {
                    Log.d(TAG, "Registered verification change");
                    if(hashMap.containsKey("verified") && (boolean)hashMap.get("verified")){

                        if((boolean)hashMap.get("verified")){
                            verificationStatus.setVisibility(View.VISIBLE);
                            finishButton.setEnabled(true);
                            Log.d(TAG,"Verification successful");
                        }else{
                            resendButton.setEnabled(true);
                            verificationStatus.setTextColor(getResources().getColor(R.color.colorError));
                            verificationStatus.setText("Verification Error");
                            verificationStatus.setVisibility(View.VISIBLE);
                            verificationStatus.requestFocus();
                            verificationStatus.setError("An error occurred during the verification process. Please try again.");
                        }
                    }
                }
            });


        }




        final Intent intent = new Intent(this, SignIn.class);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model.createUser();
                model.getVerificationStatus().observe(verifyRegistration.this, new Observer<HashMap>() {
                    @Override
                    public void onChanged(HashMap hashMap) {
                        if(hashMap.containsKey("userCreated")){
                            if((boolean)hashMap.get("userCreated")){
                                startActivity(intent);
                            }else{
                                resendButton.setEnabled(true);
                                verificationStatus.setText("Registration Error");
                                verificationStatus.setTextColor(getResources().getColor(R.color.colorError));
                                verificationStatus.setVisibility(View.VISIBLE);
                                verificationStatus.requestFocus();
                                verificationStatus.setError("An error occurred during the registration process. Please try again");
                            }
                        }
                    }
                });

            }
        });

        resendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit.putBoolean("verificationSent", false);
                edit.commit();
                verifyRegistration.this.recreate();
            }
        });
    }

    private boolean checkFields(String field){
        return !field.isEmpty() && field != null;
    }
}
