package com.example.foodapp.RegisterUI;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.authlibrary.AuthLib;
import com.example.foodapp.loginHelper.LoginUtils;
import com.example.fooddata.FoodLibrary;
import com.example.fooddata.User;
import com.example.result.ServerResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.auth.UserInfo;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import io.grpc.Server;

import static com.example.foodapp.loginHelper.LoginUtils.Register;

public class RegisterViewModel extends ViewModel {
    private static String TAG = "RegisterViewModel";

    private HashMap userFields = new HashMap();

    private HashMap vStatus = new HashMap();
    private MutableLiveData<HashMap> verificationStatus = new MutableLiveData<>();

    private MutableLiveData<ServerResult> registerResult = new MutableLiveData<>();

    private FirebaseUser firebaseUser;

    public RegisterViewModel(){
        this.verificationStatus.setValue(new HashMap());
    }

    public LiveData<HashMap> getVerificationStatus() {return this.verificationStatus;}
    public void setUserFields(HashMap userFields){this.userFields = userFields;}

    public void createUser(){
        if(userFields.containsKey("username") && userFields.containsKey("email")
                && userFields.containsKey("firstName") && userFields.containsKey("lastName")
                && userFields.containsKey("uid") && userFields.containsKey("month") && userFields.containsKey("year") && userFields.containsKey("day")){

            User newUser = new User((String)userFields.get("username"), (String)userFields.get("firstName"),
                     (String)userFields.get("lastName"),(String)userFields.get("email"));
            newUser.id = (String)userFields.get("uid");
            Date newDate = new GregorianCalendar((int)userFields.get("year"),(int)userFields.get("month"),(int)userFields.get("day")).getTime();
            newUser.dateOfBirth = new Timestamp(newDate);
            newUser.dateJoined = new Timestamp(new Date());




            Task<Void> result = FoodLibrary.addUser(newUser)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                vStatus.put("userCreated", true);
                                verificationStatus.setValue(vStatus);
                                Log.d(TAG, "createUser()- User created and added to database. Setting userCreated KEY to TRUE.");
                            }else{
                                vStatus.put("userCreated", false);
                                verificationStatus.setValue(vStatus);
                                Log.d(TAG, "createUser()- Failed to add user. Setting userCreated KEY to FALSE");
                            }
                        }
                    });
/**
            if(!result.getResult()){
                //TODO: Give more detailed responses based on the error.
                registerResult.setValue(new ServerResult<>("Registration failed. Please Try again", false));
            }
            registerResult.setValue(new ServerResult<>("Registration successful! Login using your email and password."));
 */
        }
    }

    public void createCredentials(final SharedPreferences sharedPref){
        if(userFields.containsKey("email") && userFields.containsKey("password")) {


            final Task<ServerResult> serverResultTask = LoginUtils.Register((String) userFields.get("email"), (String) userFields.get("password"));

            serverResultTask.addOnCompleteListener(new OnCompleteListener<ServerResult>() {
                @Override
                public void onComplete(@NonNull Task<ServerResult> task) {
                    if (task.isSuccessful() && task.getResult().getResult()) {
                        firebaseUser = LoginUtils.get_user();
                        registerResult.setValue(task.getResult());
                        sharedPref.edit().putString("uid", firebaseUser.getUid()).apply();
                        Log.d(TAG, "createCredentials()- User registered and email verification sent. Firebase user has been set.");


                    } else {
                        if(task.isSuccessful()){
                            Log.d(TAG, "createCredentials()- Error occurred: " + task.getResult().getError());
                        }else{
                            Log.d(TAG, "createCredentials()- Error occurred: " + task.getException());
                        }
                    }
                }
            });
        }
    }

    public void checkUser(String email){
        if(email == null || email.isEmpty()){
            vStatus.put("emailEmpty", true);
            verificationStatus.setValue(vStatus);
            return;
        }
        AuthLib.alreadyRegistered(email)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        if(task.isSuccessful()){
                            vStatus.put("emailValid", task.getResult().getSignInMethods().isEmpty());
                            verificationStatus.setValue(vStatus);
                            Log.d(TAG, "CheckUser completion success");
                        }else{
                            vStatus.put("emailValid", false);
                            verificationStatus.setValue(vStatus);
                            Log.d(TAG, "CheckUserError occurred: " + task.getException());
                        }
                    }
                });
    }

    public void verifyUser(String code){

        AuthLib.checkEmailVerification(code).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    vStatus.put("verified", true);
                    verificationStatus.setValue(vStatus);
                    Log.d(TAG, "VerifyUser Email successfully verified");
                }else{
                    vStatus.put("verified", false);
                    verificationStatus.setValue(vStatus);
                    Log.d(TAG, "VerifyUserError occurred: " + task.getException());
                }
            }
        });

    }


}
