package com.example.foodapp.loginHelper;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.authlibrary.*;
import com.example.fooddata.*;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.example.result.ServerResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Observable;

import io.grpc.Server;

/**
 * A wrapper class for multiple calls to the backend for managing user credentials.
 *
 */
public class LoginUtils {

    private static final String TAG = "LoginUtils";

    private MutableLiveData<ServerResult> utilResult = new MutableLiveData<>();

    private static FirebaseUser _user;

    public static FirebaseUser get_user(){return _user;}
    public LiveData<ServerResult> getUtilResult() {return utilResult; }

    /**
     *
     * @param email: user email
     * @param password: user password
     * @return: ServerResult that contains a User object.
     */
    public static Task<ServerResult> SignIn(String email, String password){
        Task<AuthResult> result = AuthLib.userLogin(email, password);
        
        Task<ServerResult> serverResultTask= result.continueWithTask(new Continuation<AuthResult, Task<DocumentSnapshot>>() {
            @Override
            public Task<DocumentSnapshot> then(Task<AuthResult> t) throws Exception{
                 FirebaseUser user = t.getResult().getUser();
                 return FoodLibrary.getUser(user.getUid());
            }
        }).continueWith(new Continuation<DocumentSnapshot, ServerResult>() {
            @Override
            public ServerResult then(Task<DocumentSnapshot> task) throws Exception {
                if(task.isSuccessful()){
                    Log.d(TAG, "SignIn()- Task is successful");
                    return new ServerResult(task.getResult().toObject(User.class));
                }else{
                    Log.d(TAG, "SignIn() - Task failed");
                    if(task.getException().getClass() == FirebaseAuthInvalidUserException.class){
                        return new ServerResult("Email is not valid", false);
                    }else{
                        return new ServerResult("Wrong Password", false);
                    }
                }
            }
        });

        return serverResultTask;
    }

    /**
     *
     * @param email provided from the user
     * @param password password provided from the user
     * @return Server result which contains an AuthResult Object. If there is an error the Server Result contains the exception that was thrown.
     */

    public static Task<ServerResult> Register(String email, String password){

        ServerResult retval;
        Task<AuthResult> result = AuthLib.registerUser(email, password);

        Task<ServerResult> serverResultTask = result.continueWithTask(new Continuation<AuthResult, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull Task<AuthResult> task) throws Exception {
                // Take the result from the first task and start the second one

                AuthResult result = task.getResult();
                _user = result.getUser();
                return AuthLib.sendUserVerification(result.getUser());
            }
        }).continueWith(new Continuation<Void, ServerResult>() {
            @Override
            public ServerResult then(Task<Void> task) throws Exception {
                if(task.isSuccessful()){
                   ServerResult retval =  new ServerResult<>("Email verification sent");
                    return retval;
                }else{
                    return new ServerResult<>(task.getException());
                }
            }
        });
        return serverResultTask;
    }

    public static Task<ServerResult> ResetPassword(final String email, String password, FirebaseUser user){
        AuthCredential credential = AuthLib.getCredentials(email, password);
        Task<Void> result = AuthLib.reauthenticate(credential, user);
        Task<ServerResult> serverResultTask = result.continueWithTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull Task<Void> task) throws Exception {
                return AuthLib.sendPasswordReset(email);
            }
        }).continueWith(new Continuation<Void, ServerResult>() {
            @Override
            public ServerResult then(@NonNull Task<Void> task) throws Exception {
                if(task.isSuccessful()){
                    return new ServerResult("Password reset sent");
                }else{
                    return new ServerResult(task.getException());
                }
            }
        });
        return serverResultTask;
    }

    public static Task<ServerResult> updatePassword(final FirebaseUser user, final String password, String code){
       Task<String> result = AuthLib.verifyPasswordResetCode(code);
       Task<ServerResult> serverResultTask = result.continueWithTask(new Continuation<String, Task<Void>>() {
           @Override
           public Task<Void> then(@NonNull Task<String> task) throws Exception {
               return AuthLib.updatePassword(user, password);
           }
       }).continueWith(new Continuation<Void, ServerResult>() {
           @Override
           public ServerResult then(@NonNull Task<Void> task) throws Exception {
               if(task.isSuccessful()){
                   return new ServerResult("Password updated");
               }else{
                   return new ServerResult(task.getException());
               }
           }
       });

       return serverResultTask;
    }

    public static Task<ServerResult> updateEmail(final FirebaseUser user, final String email, String password){
        AuthCredential credential = AuthLib.getCredentials(email, password);
        Task<Void> result = AuthLib.reauthenticate(credential, user);

        Task<ServerResult> serverResultTask = result.continueWithTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull Task<Void> task) throws Exception {
                return AuthLib.updateEmail(user, email);
            }
        }).continueWith(new Continuation<Void, ServerResult>() {
            @Override
            public ServerResult then(@NonNull Task<Void> task) throws Exception {
                if(task.isSuccessful()){
                    return new ServerResult("Email updated");
                }else{
                    return new ServerResult(task.getException());
                }
            }
        });

        return serverResultTask;

    }

    public static Task<ServerResult> deleteUser(final FirebaseUser user, String email, String password){
        AuthCredential credential = AuthLib.getCredentials(email, password);
        Task<Void> result = AuthLib.reauthenticate(credential, user);

        Task<ServerResult> serverResultTask = result.continueWithTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull Task<Void> task) throws Exception {
                return AuthLib.deleteUser(user);
            }
        }).continueWith(new Continuation<Void, ServerResult>() {
            @Override
            public ServerResult then(@NonNull Task<Void> task) throws Exception {
                if(task.isSuccessful()){
                    return new ServerResult("User deleted");
                }else{
                    return new ServerResult(task.getException());
                }
            }
        });
        return serverResultTask;
    }

}
