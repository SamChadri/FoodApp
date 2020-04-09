package com.example.foodapp.ui.create;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.authlibrary.AuthLib;
import com.example.fooddata.FoodLibrary;
import com.example.fooddata.Post;
import com.example.fooddata.User;
import com.example.imagestorage.ImageStorage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Date;
import java.util.HashMap;

public class CreatePostViewModel extends ViewModel {

    private HashMap<Object, Object> libStatus = new HashMap<>();
    private MutableLiveData<HashMap> foodLibStatus = new MutableLiveData<>();
    private HashMap<String, Object> tagMap = new HashMap<>();



    private static final String TAG = "CreatePostViewModel";
    public Post newPost = new Post();


    private User foodUser;

    public void setTag(String key, Object value){
        tagMap.put(key, value);
    }

    public Object getTag(String key){
        return tagMap.get(key);
    }

    public LiveData<HashMap> getFoodLibStatus(){
        return foodLibStatus;
    }

    public User getFoodUser(){
        return foodUser;
    }

    public CreatePostViewModel(){
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
                            libStatus.put("errorMessage", "Failed to verify user");
                            foodLibStatus.setValue(libStatus);
                            Log.d(TAG, "Constructor failure. Setting userRetrieved KEY to FALSE");
                        }
                    }
                });
    }


    public void addPost(){
       if(newPost == null || !checkPost(newPost)){
           libStatus.put("postAdded", false);
           libStatus.put("errorMessage", "Not a valid post");
           foodLibStatus.setValue(libStatus);
       }
       setTag("newPost", newPost);
       FoodLibrary.addPost(newPost)
               .addOnCompleteListener(new OnCompleteListener<Void>() {
                   @Override
                   public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            libStatus.put("postAdded", true);
                            foodLibStatus.postValue(libStatus);
                            Log.d(TAG, "addPost() - Success. Setting postAdded KEY to TRUE");
                        }else{
                            libStatus.put("postAdded", false);
                            libStatus.put("errorMessage", "Failed to add post");
                            foodLibStatus.setValue(libStatus);
                            Log.d(TAG, "addPost() - Failure. Setting postAdded KEY to FALSE");
                        }
                   }
               });
    }

    public void addPostImage( File image){
        if(image == null || !image.exists()){
            libStatus.put("postImageUploaded", false);
            libStatus.put("errorMessage", "Invalid image");
            foodLibStatus.setValue(libStatus);
        }
        String filename = createFilename(foodUser.username);
        setTag("imageFilename", filename);
        ImageStorage.uploadPostImage(filename, Uri.fromFile(image))
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            libStatus.put("postImageUploaded",true);
                            foodLibStatus.postValue(libStatus);
                            Log.d(TAG, "addPostImage() - Success. Setting postImageUploaded KEY to TRUE");
                            getPostImageUrl(filename);
                        }else{
                            libStatus.put("postImageUploaded", false);
                            foodLibStatus.setValue(libStatus);
                            Log.d(TAG, "addPostImage() - Failure. Setting postImageUploaded KEY to FALSE");
                        }
                    }
                });

    }

    private void getPostImageUrl(String filename){
        if(filename == null || filename.isEmpty()){
            libStatus.put("imageUrlRetrieved", false);
            libStatus.put("errorMessage", "Not a valid filename");
            Log.d(TAG, "Invalid Filename = " + filename);
            foodLibStatus.setValue(libStatus);
        }

        ImageStorage.getPostUrl(filename)
                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            newPost.imageUrl = task.getResult().toString();
                            libStatus.put("imageUrlRetrieved", true);
                            foodLibStatus.postValue(libStatus);
                            Log.d(TAG, "getPostImage() - Success. Setting imageUrlRetrieved KEY to TRUE");
                        }else{
                            libStatus.put("imageUrlRetrieved", false);
                            libStatus.put("errorMessage", "Failed to retrieve image url");
                            foodLibStatus.setValue(libStatus);
                            Log.d(TAG, "getPostImage() - Failure. SEtting imageUrlRetrieved KEY to FALSE");
                        }
                    }
                });
    }

    private boolean checkPost(Post post){
        boolean nameCheck = post.username != null || !post.username.isEmpty();
        boolean captionCheck = post.caption != null || !post.caption.isEmpty();
        boolean imageUrlCheck = post.imageUrl != null || !post.imageUrl.isEmpty();

        if(nameCheck && captionCheck && imageUrlCheck ){
            return true;
        }
        return false;
    }


    private String createFilename(String username){
        Timestamp time = new Timestamp(new Date());
        return username + time.toString();

    }
}
