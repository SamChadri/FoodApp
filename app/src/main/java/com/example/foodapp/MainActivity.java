package com.example.foodapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.Navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.authlibrary.AuthLib;
import com.example.foodapp.RegisterUI.verifyRegistration;
import com.example.foodapp.forgotPasswordUI.resetPassword;
import com.example.foodapp.loginHelper.SignIn;
import com.example.fooddata.FoodLibrary;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

public class MainActivity extends AppCompatActivity {
public static String TAG="MainActivity";
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        // TODO: Remember to implement noWIFI fail safe.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        final Intent registerIntent = new Intent(this, verifyRegistration.class);
        final Intent resetIntent = new Intent(this, resetPassword.class);
        final ProgressBar loadingBar = findViewById(R.id.mainProgressBar);
        final TextView errorMessage = findViewById(R.id.errorMessage);

        //oodLibrary.importRecipes(sampleData.json);


        loadingBar.setVisibility(View.VISIBLE);

        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                        }
                        Log.d(TAG, "Received dynamic link!");
                        String mode = null;
                        String code = null;
                        try{
                            mode = deepLink.getQueryParameter("mode");
                            code = deepLink.getQueryParameter("oobCode");

                            if(mode != null && code != null){
                                sharedPref.edit().putString("oobCode", code).commit();
                                if(mode.compareTo("verifyEmail") == 0){
                                    loadingBar.setVisibility(View.INVISIBLE);
                                    setResult(Activity.RESULT_OK);
                                    startActivity(registerIntent);
                                    finish();
                                }
                                if(mode.compareTo("resetPassword") == 0){
                                    loadingBar.setVisibility(View.INVISIBLE);
                                    setResult(Activity.RESULT_OK);
                                    startActivity(resetIntent);
                                    finish();
                                }
                            }else{
                                loadingBar.setVisibility(View.INVISIBLE);
                                errorMessage.setTextColor(getResources().getColor(R.color.colorError));
                                errorMessage.setVisibility(View.VISIBLE);
                                //create an error handler activity.
                            }


                        }catch (UnsupportedOperationException | NullPointerException e){
                            loadingBar.setVisibility(View.INVISIBLE);
                            errorMessage.setTextColor(getResources().getColor(R.color.colorError));
                            errorMessage.setVisibility(View.VISIBLE);

                        }

                        // Handle the deep link. For example, open the linked
                        // content, or apply promotional credit to the user's
                        // account.
                        // ...

                        // ...
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "getDynamicLink:onFailure", e);
                        loadingBar.setVisibility(View.INVISIBLE);
                        errorMessage.setTextColor(getResources().getColor(R.color.colorError));
                        errorMessage.setVisibility(View.VISIBLE);
                    }
                });

        //Intent intent = new Intent(this, SignIn.class);
        //startActivity(intent);
    }
}
