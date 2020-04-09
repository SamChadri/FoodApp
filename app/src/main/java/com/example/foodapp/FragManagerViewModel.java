package com.example.foodapp;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.authlibrary.AuthLib;
import com.example.fooddata.FoodLibrary;
import com.example.fooddata.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;

public class FragManagerViewModel extends ViewModel {
    private static String TAG = "FragManagerViewModel";
    public int currFragment;
    private HashMap<Object, Object> libStatus = new HashMap<>();
    private MutableLiveData<HashMap> foodLibStatus = new MutableLiveData<>();
    private User currUser;
    private Bundle args = new Bundle();

    private MutableLiveData<Integer> fragStatus = new MutableLiveData<>();

    public void setFragStatus(int id){
        currFragment = id;
        fragStatus.setValue(currFragment);

    }

    public Bundle getArgs(){
        return args;
    }

    public User getCurrUser(){
        return currUser;
    }

    public LiveData<HashMap> getFoodLibStatus(){
        return foodLibStatus;
    }

    public LiveData<Integer> currFragmentStatus(){
        return fragStatus;
    }


    public FragManagerViewModel(){

        FoodLibrary.getUser(AuthLib.getAcitveUser().getUid())
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            currUser = task.getResult().toObject(User.class);
                            libStatus.put("userRetrieved", true);
                            //foodLibStatus.setValue(libStatus);
                            Log.d(TAG, "userRetrieved KEY set to TRUE");
                        }else{
                            libStatus.put("userRetrieved", false);
                            libStatus.put("errorMessage", "Could not retrieve user");
                            foodLibStatus.setValue(libStatus);
                            Log.d(TAG, "userRetrieved KEY to set to FALSE");
                        }
                    }
                });
    }
}
