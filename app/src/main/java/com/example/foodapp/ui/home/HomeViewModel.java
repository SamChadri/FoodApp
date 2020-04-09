package com.example.foodapp.ui.home;

import android.app.Activity;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.RecyclerView;

import com.example.authlibrary.AuthLib;
import com.example.foodapp.utils.MyIdlingResource;
import com.example.fooddata.FoodLibrary;
import com.example.fooddata.Post;
import com.example.fooddata.Recipe;
import com.example.fooddata.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomeViewModel extends ViewModel {
    private static String TAG = "HomeViewModel";

    //TODO: Later on, make a class that can encompass the status and error message maybe?

    private MutableLiveData<HashMap> foodLibStatus;
    private HashMap<Object,Object> libStatus;

    public static int FEED_TAB = 0;
    public static int RECIPE_TAB = 1;

    private ArrayList<Post> _posts;
    private ArrayList<Recipe> _recipes;
    private FirebaseUser firebaseUser;
    private User foodUser;

    public Activity currentActivity;

    public LiveData<HashMap> getLibStatus(){
        return foodLibStatus;
    }

    public ArrayList<Post> getPosts(){
        return _posts;
    }

    public ArrayList<Recipe> getRecipes(){return _recipes;}

    public User getFoodUser(){return foodUser;}

    private MutableLiveData<User> userData = new MutableLiveData<>();

    private HashMap<String, Object> tagMap = new HashMap<>();

    private TextView navUsername;

    private TextView navEmail;

    public void setTag(String key, Object value){
        tagMap.put(key, value);
    }

    public Object getTag(String key){
        return tagMap.get(key);
    }

    public LiveData<User> getUserStatus() {
        return userData;
    }

    public RecyclerView currRecyclerView;

    public void setNavUsername(TextView navUsername) {
        this.navUsername = navUsername;
    }

    public void setNavEmail(TextView navEmail){
        Log.d(TAG, "navEmail TextView is null: " + (navEmail == null));
        this.navEmail = navEmail;
    }

    private TabLayout currTabLayout;
    public void setTabLayout(TabLayout tabLayout){

        currTabLayout = tabLayout;
    }

    public void selectTab(int index){
        currTabLayout.getTabAt(index).select();
    }


    public boolean clearUpdates(){
        boolean cleared = false;
        if(libStatus.containsKey("userUpdated")){
            libStatus.remove("userUpdated");
            cleared = true;
        }
        if(libStatus.containsKey("postUpdated")){
            libStatus.remove("postUpdated");
            cleared = true;
        }
        if(libStatus.containsKey("recipeUpdated")){
            libStatus.remove("recipeUpdated");
            cleared = true;
        }
        if(libStatus.containsKey("recipeCommentCreated")){
            libStatus.remove("recipeCommentCreated");
            cleared = true;
        }
        return cleared;

    }
    public HomeViewModel() {
        MyIdlingResource.increment();
        foodLibStatus = new MutableLiveData<>();
        libStatus = new HashMap();
        firebaseUser = AuthLib.getAcitveUser();
        getlibData();
    }

    public void createPostComment(int postId, Post.Comment comment){

        FoodLibrary.addPostComment(postId, comment)
                .continueWithTask(new Continuation<Void, Task<Void>>() {
                    @Override
                    public Task<Void> then(@NonNull Task<Void> task) throws Exception {
                        if(task.isSuccessful()){
                            User.UserComment userComment = new User.UserComment(foodUser.username, true, postId);
                            setTag("userPostComment", userComment);
                            return FoodLibrary.addUserComment(foodUser, userComment);
                        }else{
                            libStatus.put("postCommentCreated", false);
                            foodLibStatus.postValue(libStatus);
                            Log.d(TAG, "createPostComment() - Error occurred. Setting postCommentCreated KEY to FALSE");
                            return null;
                        }

                    }
                }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    updatePost(postId);
                    updateUserFields();
                    libStatus.put("postCommentCreated", true);
                    foodLibStatus.postValue(libStatus);
                    Log.d(TAG, "PostComment created successfully. Setting postCommentCreated KEY to TRUE");
                }else{
                    libStatus.put("postCommentCreated", false);
                    foodLibStatus.postValue(libStatus);
                    Log.d(TAG, "createPostComment() - Error occurred. Setting postCommentCreated KEY to FALSE");

                }

            }
        });


    }


    public void createRecipeComment(int recipeId, Post.Comment comment){
        FoodLibrary.addRecipeComment(recipeId, comment)
                .continueWithTask(new Continuation<Void, Task<Void>>() {
                    @Override
                    public Task<Void> then(@NonNull Task<Void> task) throws Exception {
                        if(task.isSuccessful()){
                            User.UserComment userComment = new User.UserComment(foodUser.username, false, recipeId);
                            setTag("userRecipeComment", userComment);
                            return FoodLibrary.addUserComment(foodUser, userComment);
                        }else{
                            libStatus.put("recipeCommentCreated", false);
                            foodLibStatus.postValue(libStatus);
                            Log.d(TAG, "recipePostComment() - Error occurred. Setting postCommentCreated KEY to FALSE");
                            return null;
                        }

                    }
                }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    updateRecipe(recipeId);
                    updateUserFields();
                    libStatus.put("recipeCommentCreated", true);
                    foodLibStatus.postValue(libStatus);
                    Log.d(TAG, "createRecipeComment() - successful. Setting postCommentCreated KEY to TRUE");
                }else{
                    libStatus.put("recipeCommentCreated", false);
                    foodLibStatus.postValue(libStatus);
                    Log.d(TAG, "createRecipeComment() - Error occurred. Setting postCommentCreated KEY to FALSE");

                }

            }
        });

    }

    public void updateUserFields(){
        FoodLibrary.getUser(foodUser.id)
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            foodUser  = task.getResult().toObject(User.class);
                            libStatus.put("userUpdated", true);
                            foodLibStatus.postValue(libStatus);
                            Log.d(TAG, "updateUserFields() - HomeViewModel foodUser up to date. Setting userUpdated KEY to TRUE");
                        }else{
                            libStatus.put("userUpdated", false);
                            foodLibStatus.postValue(libStatus);
                            Log.d(TAG, "updateUserFields() - Error occurred. Setting userUpdated KEY to FALSE");
                        }

                    }
                });
    }

    public void updatePost(Post post){
        FoodLibrary.getPost(post.id)
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            Post newPost = task.getResult().toObject(Post.class);
                            int index = 0;
                            for(int i = 0; i < _posts.size(); i++){
                                if(_posts.get(i).id == post.id){
                                    index = i;
                                }
                            }
                            _posts.set(index, newPost);
                            libStatus.put("postUpdated", true);
                            foodLibStatus.postValue(libStatus);
                            Log.d(TAG, "updatePost() - Post has been updated in HomeViewModel. Setting postUpdated KEY to TRUE");
                        }else{
                            libStatus.put("postUpdated", false);
                            foodLibStatus.postValue(libStatus);
                            Log.d(TAG, "updatePost() - Error occurred. Setting postUpdated KEY to FALSE");
                        }

                    }
                });
    }

    public void updatePost(int postId){
        FoodLibrary.getPost(postId)
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            Post newPost = task.getResult().toObject(Post.class);
                            int index = 0;
                            for(int i = 0; i < _posts.size(); i++){
                                if(_posts.get(i).id == postId){
                                    index = i;
                                }
                            }
                            _posts.set(index, newPost);
                            libStatus.put("postUpdated", true);
                            foodLibStatus.postValue(libStatus);
                            Log.d(TAG, "updatePost() - Post has been updated in HomeViewModel. Setting postUpdated KEY to TRUE");
                        }else{
                            libStatus.put("postUpdated", false);
                            foodLibStatus.postValue(libStatus);
                            Log.d(TAG, "updatePost() - Error occurred. Setting postUpdated KEY to FALSE");
                        }

                    }
                });
    }

    public void updateRecipe(Recipe recipe){
        FoodLibrary.getRecipe(recipe.id)
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            Recipe newRecipe = task.getResult().toObject(Recipe.class);
                            int index = 0;
                            for(int i  = 0; i < _recipes.size(); i++){
                                if(_recipes.get(i).id == recipe.id){
                                    index = i;
                                }
                            }
                            libStatus.put("recipeUpdated", true);
                            foodLibStatus.postValue(libStatus);
                            Log.d(TAG, "updateRecipe() - Recipe updated in HomeViewModel repo. Setting recipeUpdated KEY to TRUE");
                        }else{
                            libStatus.put("recipeUpdated", false);
                            foodLibStatus.postValue(libStatus);
                            Log.d(TAG, "updateRecipe() - Error occurred. Setting recipeUpdated KEY to FALSE ");
                        }

                    }
                });
    }

    public void updateRecipe(int recipeId){
        FoodLibrary.getRecipe(recipeId)
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            Recipe newRecipe = task.getResult().toObject(Recipe.class);
                            int index = 0;
                            for(int i  = 0; i < _recipes.size(); i++){
                                if(_recipes.get(i).id == recipeId){
                                    index = i;
                                }
                            }
                            _recipes.set(index, newRecipe);
                            libStatus.put("recipeUpdated", true);
                            foodLibStatus.postValue(libStatus);
                            Log.d(TAG, "updateRecipe() - Recipe updated in HomeViewModel repo. Setting recipeUpdated KEY to TRUE");
                        }else{
                            libStatus.put("recipeUpdated", false);
                            foodLibStatus.postValue(libStatus);
                            Log.d(TAG, "updateRecipe() - Error occurred. Setting recipeUpdated KEY to FALSE ");
                        }

                    }
                });
    }

    public void getlibData(){
        FoodLibrary.getUser(firebaseUser.getUid()).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){

                    foodUser = task.getResult().toObject(User.class);
                    //navEmail.setText(foodUser.email);
                    //navUsername.setText(foodUser.username);
                    userData.postValue(foodUser);
                    libStatus.put("userRetrieved",true);
                    foodLibStatus.postValue(libStatus);
                    requestPosts();
                    requestRecipes();
                    Log.d(TAG, "FoodUser retrieval successful. Setting userRetrieved KEY to TRUE");
                }else{
                    libStatus.put("userRetrieved", false);
                    libStatus.put("errorMessage", task.getException().getMessage());
                    foodLibStatus.postValue(libStatus);
                    Log.d(TAG, "FoodUser retrieval failure. Setting userRetrieved KEY to FALSE. Error message set.");
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
        FoodLibrary.getRecipeBatch(foodUser.username).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    _recipes = (ArrayList<Recipe>)task.getResult().toObjects(Recipe.class);
                    libStatus.put("recipesRetrieved", true);
                    foodLibStatus.postValue(libStatus);
                    MyIdlingResource.decrement();
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

    public void requestPosts(){
        if(foodUser == null){
            libStatus.put("errorMessage", "User not available.");
            foodLibStatus.setValue(libStatus);
            return;
        }
        FoodLibrary.getPostBatch(foodUser.username).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){

                    _posts = (ArrayList<Post>)task.getResult().toObjects(Post.class);
                    libStatus.put("postsRetrieved", true);
                    foodLibStatus.postValue(libStatus);
                    Log.d(TAG, "requestPosts() - successful. Setting postsRetrieved KEY to TRUE");
                }else{
                    libStatus.put("postsRetrieved", false);
                    libStatus.put("errorMessage", task.getException().getMessage());
                    foodLibStatus.postValue((libStatus));
                    Log.d(TAG, "requestPosts() - failure. Setting postsRetrieved KEY to FALSE");

                }
            }
        });
    }



}