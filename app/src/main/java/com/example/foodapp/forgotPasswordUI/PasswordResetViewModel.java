package com.example.foodapp.forgotPasswordUI;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.authlibrary.AuthLib;
import com.example.foodapp.loginHelper.LoginUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.*;

public class PasswordResetViewModel extends ViewModel {
    private static String TAG = "PasswordResetViewModel";

    private HashMap rStatus = new HashMap();
    private String verificationCode;
    private MutableLiveData<HashMap> resetStatus = new MutableLiveData<>();

    public LiveData<HashMap> getResetStatus(){ return this.resetStatus;}
    public void setVerificationCode(String verificationCode){this.verificationCode = verificationCode;}


    public void sendReset(String email){
        // TODO: Add in action code settings for the dynamic link
        AuthLib.sendPasswordReset(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            rStatus.put("passwordResetSent", true);
                            resetStatus.setValue(rStatus);
                            Log.d(TAG, "sendReset success. Updating passwordResetSent to TRUE.");
                        }else{
                            rStatus.put("passwordResetSent", false);
                            resetStatus.setValue(rStatus);
                            Log.d(TAG, "sendReset failure. Updating passwordResetSent to FALSE." );
                        }
                    }
                });
    }

    public void resetPassword(String newPassword){
        AuthLib.verifyPasswordReset(verificationCode, newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            rStatus.put("passwordChanged", true);
                            resetStatus.setValue(rStatus);
                            Log.d(TAG, "resetPassword success. Updating passwordChanged key to TRUE.");
                        }else{
                            rStatus.put("passwordChanged", false);
                            resetStatus.setValue(rStatus);
                            Log.d(TAG, "resetPassword failure. Updating passwordChanged key to FALSE. ");
                        }
                    }
                });
    }

    public void confirmResetCode(String code){
        AuthLib.verifyPasswordResetCode(code)
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if(task.isSuccessful()){
                            rStatus.put("resetVerified", true);
                            resetStatus.setValue(rStatus);
                            Log.d(TAG, "confirmResetCode success. Updating resetVerified key to TRUE");
                        }else{
                            rStatus.put("resetVerified", false);
                            resetStatus.setValue(rStatus);
                            Log.d(TAG, "confirmResetCode failure with code. Updating resetVerified key to FALSE. " + task.getException());
                        }
                    }
                });
    }
}
