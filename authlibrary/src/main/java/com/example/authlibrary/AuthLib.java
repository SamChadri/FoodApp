package com.example.authlibrary;

import com.google.android.gms.tasks.*;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.*;

import com.google.firebase.firestore.auth.User;
import com.google.firestore.v1.*;

import java.util.*;
import java.util.concurrent.ExecutionException;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.result.ServerResult;




public class AuthLib {
    private static String TAG = "AuthLibrary";


    private static FirebaseAuth mAuth;

    public static void initAuth(){
        mAuth = FirebaseAuth.getInstance();
    }


    private static final MutableLiveData<ServerResult> serverResult = new MutableLiveData<>();


    public static LiveData<ServerResult> getServerResult(){return serverResult;}


    public static Task<AuthResult> registerUser(String email, String password){
        return mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "User created with id: " + task.getResult().getUser().getUid());

                        }else{
                            Log.d(TAG, "Error occurred: " + task.getException());
                        }
                    }
                });
        

    }

    public static Task<Void> sendUserVerification(FirebaseUser user){
        String url = "https://foodstore-563e8.firebaseapp.com";
        String dynamicLink = "foodstore.page.link";
        ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                .setUrl(url)
                // The default for this is populated with the current android package name.
                .setAndroidPackageName("com.example.foodapp", false, null)
                .setDynamicLinkDomain(dynamicLink)
                .setHandleCodeInApp(true)
                .build();
        Task<Void> result = user.sendEmailVerification(actionCodeSettings)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "sendUserVerification()- Email sent");
                        }else{
                            Log.d(TAG, "sendUserVerification()- Error occurred: " + task.getException());
                        }
                    }
                });
        return result;
    }

    public static Task<Void> checkEmailVerification( String code) {
        return mAuth.applyActionCode(code);
    }
    public static Task<Void> verifyPasswordReset(String code, String newPassword){

        return mAuth.confirmPasswordReset(code, newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "passwordReset()- successful" );
                        }else{
                            Log.d(TAG, "passwordReset()- Error occurred: " + task.getException());
                        }
                    }
                });
    }

    public static Task<SignInMethodQueryResult> alreadyRegistered(String email){
        return mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "alreadyRegistered()- completed successfully ");
                        }else{
                            Log.d(TAG, "alreadyRegistered()- Error occured: " + task.getException());
                        }
                    }
                });

    }



    public static Task<Void> sendPasswordReset(String email){
        String url = "https://foodstore-563e8.web.app";
        String dynamicLink = "foodstore.page.link";
        ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                .setUrl(url)
                // The default for this is populated with the current android package name.
                .setAndroidPackageName("com.example.foodapp", false, null)
                .setDynamicLinkDomain(dynamicLink)
                .setHandleCodeInApp(true)
                .build();
        return mAuth.sendPasswordResetEmail(email, actionCodeSettings)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "sendPasswordReset()- Password reset email sent");
                        }else{
                            Log.d(TAG, "sendPasswordReset()- Error occurred: " + task.getException());
                        }
                    }
                });

    }

    public static Task<String> verifyPasswordResetCode(String code){
        return mAuth.verifyPasswordResetCode(code)
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "verifyPasswordResetCode()- Password Reset verified " );
                        }else{
                            Log.d(TAG, "verifyPasswordResetCode()- Error occurred: " + task.getException());
                        }
                    }
                });
    }



    public static Task<AuthResult> userLogin(String email, String password){

        return mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "userLogin()- User sign in successful with id: " + task.getResult().getUser().getUid());
                        }else{
                            Log.d(TAG, "userLogin() - Error occurred: " + task.getException());
                        }
                    }
                });

    }

    public static void userLogout(){
        mAuth.signOut();
    }

    public static FirebaseUser getAcitveUser(){
        return mAuth.getCurrentUser();
    }

    public static Task<Void> updatePassword(FirebaseUser user, String password){
        return user.updatePassword(password)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "updatePassword()- Password updated");
                        }else{
                            Log.d(TAG, "updatePassword()- Error occurred: " + task.getException());
                        }
                    }
                });
    }

    public static Task<Void> updateEmail(FirebaseUser user, String email){
        return user.updateEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "updateEmail()-Email updated");
                        }else{
                            Log.d(TAG, "updateEmail()- Error occurred: " + task.getException());
                        }
                    }
                });
    }

    public static Task<Void> deleteUser(FirebaseUser user){
        return  user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "deleteUser()- User deleted");
                        }else{
                            Log.d(TAG, "deleteUser()- Error occurred: " + task.getException());

                        }
                    }
                });
    }


    public static Task<Void> reauthenticate(AuthCredential credential, FirebaseUser user){
        return user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "Authenticated successfully ");
                        }else{
                            Log.d(TAG, "Error occurred: " + task.getException());
                        }
                    }
                });

    }

    public static AuthCredential getCredentials(String email, String password){
        return EmailAuthProvider.getCredential(email, password);
    }


}
