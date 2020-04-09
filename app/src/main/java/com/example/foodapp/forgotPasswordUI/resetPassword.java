package com.example.foodapp.forgotPasswordUI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.foodapp.R;
import com.example.foodapp.RegisterUI.verifyRegistration;
import com.example.foodapp.loginHelper.SignIn;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

import java.util.HashMap;

public class resetPassword extends AppCompatActivity {
    private static String TAG= "ResetPassword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        final SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();

        final HashMap cachePref = (HashMap)sharedPref.getAll();
        final EditText newPassword = findViewById(R.id.newPassword);
        final EditText confirmPassword = findViewById(R.id.confirmPassword);
        final Button confirmButton = findViewById(R.id.confirmButton);
        final Button resendButton = findViewById(R.id.resendButton);
        final TextView resetStatus = findViewById(R.id.resetStatus);


        final PasswordResetViewModel model = new PasswordResetViewModel();

        resendButton.setEnabled(false);
        confirmButton.setEnabled(false);
        final TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                confirmButton.setEnabled(checkEmpty(newPassword.getText().toString(), confirmPassword.getText().toString()));
            }
        };


        if(!cachePref.containsKey("passwordResetSent")){
            model.sendReset((String)cachePref.get("recoveryEmail"));
            model.getResetStatus().observe(this, new Observer<HashMap>() {
                @Override
                public void onChanged(HashMap hashMap) {
                    if(hashMap.containsKey("passwordResetSent")){
                        if((boolean)hashMap.get("passwordResetSent")){
                            editor.putBoolean("passwordResetSent", true);
                            editor.commit();
                        }else{
                            editor.putBoolean("passwordResetSent", false);
                            resetStatus.setText("Could not send reset code");
                            resetStatus.setTextColor(getResources().getColor(R.color.colorError));
                            resetStatus.requestFocus();
                            resetStatus.setError("Password reset email failed to send. Please try again");
                            resendButton.setEnabled(true);
                            Log.d(TAG, "Password reset failed to send");
                            //prompt user to try again or something like that
                        }
                    }
                }
            });
        }else{
            if(cachePref.containsKey("oobCode")){
                model.setVerificationCode((String)cachePref.get("oobCode"));
                model.confirmResetCode((String)cachePref.get("oobCode"));
            }
            model.getResetStatus().observe(this, new Observer<HashMap>() {
                @Override
                public void onChanged(HashMap hashMap) {
                    if(hashMap.containsKey("resetVerified")){
                        if((boolean)hashMap.get("resetVerified")){

                            resetStatus.setVisibility(View.VISIBLE);
                            newPassword.addTextChangedListener(textWatcher);
                            confirmPassword.addTextChangedListener(textWatcher);

                        }else{
                            resetStatus.setText("Invalid Code");
                            resetStatus.setTextColor(getResources().getColor(R.color.colorError));
                            resetStatus.requestFocus();
                            resetStatus.setError("An error occurred while verifying your reset code. Please try again");
                            resendButton.setEnabled(true);

                        }
                    }
                }
            });

        }


        final Intent intent = new Intent(this, SignIn.class);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkMatch(newPassword.getText().toString(), confirmPassword.getText().toString())){
                    //update password
                    model.resetPassword(confirmPassword.getText().toString());


                    model.getResetStatus().observe(resetPassword.this, new Observer<HashMap>() {
                        @Override
                        public void onChanged(HashMap hashMap) {
                            if(hashMap.containsKey("passwordChanged")){
                                if((boolean)hashMap.get("passwordChanged")){
                                    startActivity(intent);
                                }else{
                                    resetStatus.setText("Password Reset Failed");
                                    resetStatus.setTextColor(getResources().getColor(R.color.colorError));
                                    resetStatus.requestFocus();
                                    resetStatus.setError("An error occurred while resetting your password. Please try again");
                                    resendButton.setEnabled(true);                                }
                            }
                        }
                    });

                }else{
                    resetStatus.setText("Password Mismatch");
                    resetStatus.setTextColor(getResources().getColor(R.color.colorError));
                    resetStatus.requestFocus();
                    resetStatus.setError("The two password entries do not match.");

                }
            }
        });
        resendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putBoolean("verificationSent", false);
                editor.commit();
                resetPassword.this.recreate();
            }
        });


    }

    private boolean checkEmpty(String field1, String field2){
        return !field1.isEmpty() && !field2.isEmpty() &&
                field1 != null && field2 != null;
    }
    private boolean checkMatch(String field1, String field2){
        return field1.compareTo(field2) == 0;
    }

}
