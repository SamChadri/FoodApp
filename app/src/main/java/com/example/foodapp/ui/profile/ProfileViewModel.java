package com.example.foodapp.ui.profile;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.authlibrary.AuthLib;
import com.example.fooddata.FoodLibrary;
import com.example.fooddata.Post;
import com.example.fooddata.Recipe;
import com.example.fooddata.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class ProfileViewModel extends ViewModel {

    private static String TAG = "ProfileViewModel";
    private MutableLiveData<String> mText;
    private FirebaseUser firebaseUser;
    private User foodUser;
    private HashMap<Object,Object> libStatus = new HashMap();
    private DocumentSnapshot postCursor;
    private DocumentSnapshot recipeCursor;
    private ArrayList<Post> _posts;
    private ArrayList<Post> _likedPosts;
    private ArrayList<Recipe> _recipes;
    private MutableLiveData<HashMap> foodLibStatus = new MutableLiveData<>();


    public User getUser(){return foodUser;}

    public LiveData<HashMap> getfoodLibStatus() {return foodLibStatus;}

    public ArrayList<Post> getPosts(){return _posts;}
    public ArrayList<Recipe> getRecipes(){return _recipes;}



    public ProfileViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is slideshow fragment");
        firebaseUser = AuthLib.getAcitveUser();
        FoodLibrary.getUser(firebaseUser.getUid()).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    foodUser = task.getResult().toObject(User.class);
                    libStatus.put("userRetrieved", true);
                    foodLibStatus.setValue(libStatus);
                    requestRecipes();
                    requestPosts();
                    Log.d(TAG, "Current user retrieved successfully. Setting userRetrieved KEY to TRUE");
                }else{
                    libStatus.put("userRetrieved", false);
                    libStatus.put("errorMessage", "Error occurred getting user data");
                    Log.d(TAG, "Error occurred retrieving current user. Setting userRetrieved KEY to FALSE");
                }
            }
        });
    }


    public void requestPosts(){
        if(foodUser == null){
            libStatus.put("errorMessage", "User not available");
            foodLibStatus.setValue(libStatus);
            return;
        }
        Task<QuerySnapshot> request = null;
        if(postCursor == null){
            request = FoodLibrary.getPostBatch(foodUser.username);
        }else{
            request = FoodLibrary.getPostBatch(foodUser.username, postCursor);
        }

        request.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    _posts = (ArrayList<Post>)task.getResult().toObjects(Post.class);
                    libStatus.put("postsRetrieved", true);
                    postCursor = task.getResult().getDocuments().get(task.getResult().size() -1);
                    foodLibStatus.setValue(libStatus);
                    Log.d(TAG, "requestPosts() - successful. Setting postsRetrieved KEY to TRUE");
                }else{
                    libStatus.put("postsRetrieved", false);
                    libStatus.put("errorMessage", task.getException().getMessage());
                    foodLibStatus.setValue(libStatus);
                    Log.d(TAG, "requestPosts() - failure. Setting postsRetrieved KEY to FALSE");

                }
            }
        });
    }

    public void requestRecipes(){
        if(foodUser == null){
            libStatus.put("errorMessage", "User not available");
            foodLibStatus.setValue(libStatus);
            return;
        }

        Task<QuerySnapshot> request = null;
        if(recipeCursor == null){
            request = FoodLibrary.getRecipeBatch(foodUser.username);
        }else{
            request = FoodLibrary.getRecipeBatch(foodUser.username, recipeCursor);
        }


        request.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    _recipes = (ArrayList<Recipe>)task.getResult().toObjects(Recipe.class);
                    libStatus.put("recipesRetrieved", true);
                    foodLibStatus.setValue(libStatus);
                    recipeCursor = task.getResult().getDocuments().get(task.getResult().size() -1);
                    Log.d(TAG, "requestRecipes() - successful. Setting recipesRetrieved to TRUE");
                }else{
                    libStatus.put("recipesRetrieved", false);
                    libStatus.put("errorMessage", task.getException().getMessage());
                    foodLibStatus.setValue(libStatus);
                    Log.d(TAG, "requestRecipes() - failure. Setting recipesRetrieved to FALSE");
                }
            }
        });

    }

    public LiveData<String> getText() {
        return mText;
    }
}