package com.example.foodapp.ui.create;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.authlibrary.AuthLib;
import com.example.fooddata.FoodLibrary;
import com.example.fooddata.Recipe;
import com.example.fooddata.User;
import com.example.imagestorage.ImageStorage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.File;
import java.util.Date;
import java.util.HashMap;

public class CreateRecipeViewModel extends ViewModel {

    private static String TAG = "CreateRecipeViewModel";
    public Recipe newRecipe = new Recipe();
    private HashMap<Object, Object> libStatus = new HashMap<>();

    private MutableLiveData<HashMap> foodLibStatus = new MutableLiveData<>();
    private User foodUser;
    private HashMap<String, Object> tagMap = new HashMap<>();

    public void setTag(String key, Object value){
        tagMap.put(key, value);
    }
    public Object getTag(String key){
        return tagMap.get(key);
    }
    public LiveData<HashMap> getFoodLibStatus() {
        return foodLibStatus;
    }

    public User getFoodUser(){return foodUser;}

    public CreateRecipeViewModel(){
        FoodLibrary.getUser(AuthLib.getAcitveUser().getUid())
               .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                   @Override
                   public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                       if(task.isSuccessful()){
                           foodUser = task.getResult().toObject(User.class);
                           libStatus.put("userRetrieved", true);
                           foodLibStatus.postValue(libStatus);
                           Log.d(TAG, "Constructor success. Setting userRetrieved KEY to TRUE");
                       }else{
                           libStatus.put("userRetrieved", false);
                           libStatus.put("errorMessage", "Error occurred.");
                           foodLibStatus.postValue(libStatus);
                           Log.d(TAG, "CreateRecipeViewModel() - Constructor error. Setting userRetrieved KEY to FALSE");
                       }
                   }
               });

    }

    public void addRecipe(){
        if(newRecipe == null  || !checkRecipe(newRecipe)){
            libStatus.put("recipeAdded", false);
            libStatus.put("errorMessage", "Not a valid recipe");
            foodLibStatus.postValue(libStatus);
        }
        setTag("newRecipe", newRecipe);
        FoodLibrary.addRecipe(newRecipe)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            libStatus.put("recipeAdded", true);
                            foodLibStatus.postValue(libStatus);
                            Log.d(TAG, "addRecipe() - Successful. Setting recipeAdded KEY to TRUE");
                        }else{
                            libStatus.put("recipeAdded", false);
                            foodLibStatus.postValue(libStatus);
                            Log.d(TAG, "addRecipe() - Failed. Setting recipeAdded KEY to FALSE");

                        }
                    }
                });
    }

    public void addRecipePicture(File image){
        if(image == null || !image.exists()){
            libStatus.put("recipeImageUploaded", false);
            libStatus.put("errorMessage", "Invalid image");
            foodLibStatus.postValue(libStatus);
        }
        String filename = createFilename(foodUser.username);
        setTag("recipeFilename", filename);
        ImageStorage.uploadRecipeImage(filename, Uri.fromFile(image))
                .addOnCompleteListener(new OnCompleteListener<com.google.firebase.storage.UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<com.google.firebase.storage.UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            libStatus.put("recipeImageUploaded", true);
                            foodLibStatus.postValue(libStatus);
                            Log.d(TAG, "addRecipePicture() - Successful. Changing recipeImageAdded KEY to TRUE");
                            getRecipeImageUrl(filename);

                        }else{
                            libStatus.put("recipeImageAdded", false);
                            libStatus.put("errorMessage", "Error occurred");
                            foodLibStatus.postValue(libStatus);
                            Log.d(TAG, "addRecipePicture() - Failure. Changing recipeImageAdded KEY to FALSE");
                        }
                    }
                });
    }

    private void getRecipeImageUrl(String filename){
        if(filename == null || filename.isEmpty()){
            libStatus.put("imageUrlRetrieved", false);
            libStatus.put("errorMessage", "Not a valid filename");
            foodLibStatus.postValue(libStatus);
        }

        ImageStorage.getRecipeUrl(filename)
                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            newRecipe.images.add( task.getResult().toString());
                            libStatus.put("imageUrlRetrieved", true);
                            foodLibStatus.postValue(libStatus);
                            Log.d(TAG, "getRecipeImage() - Success. Setting imageUrlRetrieved KEY to TRUE");
                        }else{
                            libStatus.put("imageUrlRetrieved", false);
                            libStatus.put("errorMessage", "Failed to retrieve image url");
                            foodLibStatus.postValue(libStatus);
                            Log.d(TAG, "getRecipeImage() - Failure. Setting imageUrlRetrieved KEY to FALSE");
                        }
                    }
                });
    }


    private boolean checkRecipe(Recipe recipe){
        boolean userCheck = recipe.username != null || !recipe.username.isEmpty();
        boolean titleCheck = recipe.title != null || !recipe.title.isEmpty();
        boolean descriptionCheck = recipe.description != null || !recipe.description.isEmpty();
        boolean imagesCheck = recipe.images != null || !recipe.images.isEmpty();
        boolean ingredientsCheck = recipe.ingredients != null || !recipe.ingredients.isEmpty();
        boolean directionsCheck = recipe.directions != null || !recipe.directions.isEmpty();
        boolean ratingCheck = recipe.rating != null;
        boolean difficultyCheck = recipe.difficulty != null || !recipe.difficulty.isEmpty();

        if(userCheck && titleCheck && descriptionCheck && imagesCheck && ingredientsCheck && directionsCheck && ratingCheck && difficultyCheck
            && recipe.servings != 0 && recipe.totalTime != 0 ){return true;}

        return false;
    }

    private String createFilename(String username){
        Timestamp time = new Timestamp(new Date());
        return username + time.toString();

    }



}
