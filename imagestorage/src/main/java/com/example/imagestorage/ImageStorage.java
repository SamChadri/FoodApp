package com.example.imagestorage;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class ImageStorage {

    private static String TAG = "ImageStorage";

    private static  FirebaseStorage storage;
    private static StorageReference storageRef;
    private static StorageReference postRef;
    private static StorageReference recipeRef;
    private static StorageReference userRef;


    public static void init(){
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        postRef = storageRef.child("posts");
        recipeRef = storageRef.child("recipes");
        userRef = storageRef.child("users");
    }

    public static Task<UploadTask.TaskSnapshot> uploadPostImage(String filename, Uri image){
        StorageReference imageRef = postRef.child(filename);
        return imageRef.putFile(image)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "uploadPostImage()- Successfully uploaded post image with path: " + task.getResult().getMetadata().getPath());
                        }else {
                            Log.d(TAG, "uploadPostImage() - Upload failed with error: " + task.getException());
                        }
                    }
                });

    }

    public static Task<UploadTask.TaskSnapshot> uploadRecipeImage(String filename, Uri image){
        StorageReference imageRef = recipeRef.child(filename);
        return imageRef.putFile(image)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "uploadRecipeImage() - Successfully uploaded recipe image with path: " + task.getResult().getMetadata().getPath());
                        } else{
                            Log.d(TAG, "uploadRecipeImage() - Upload failed with error: " + task.getException());
                        }
                    }
                });

    }

    public static Task<UploadTask.TaskSnapshot> uploadUserImage(String username ,Uri image){
        StorageReference imageRef = userRef.child(username);
        return imageRef.putFile(image)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "uploadUserImage() - Successfully uploaded user image with path: " + task.getResult().getMetadata().getPath());
                        }else{
                            Log.d(TAG, "uploadUserImage() - Upload failed with error: " + task.getException());
                        }
                    }
                });
    }

    public static Task<Uri> getPostUrl(String filename){
        StorageReference imageRef = postRef.child(filename);
        return imageRef.getDownloadUrl()
                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "getPostUrl()- Successfully retrieved postImage with url: " + task.getResult().toString());
                        }else{
                            Log.d(TAG, "getPostUrl() - Failed to retrieve image with error: " + task.getException());
                        }
                    }
                });

    }

    public static Task<Uri> getRecipeUrl(String filename){
        StorageReference imageRef = recipeRef.child(filename);
        return imageRef.getDownloadUrl()
                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "getRecipeUrl() - Successfully retrieved recipeImage with url: " + task.getResult().toString());
                        }else{
                            Log.d(TAG, "getRecipeUrl() - Failed to retrieve image with error: " + task.getException());
                        }
                    }
                });
    }

    public static Task<Uri> getUserUrl(String filename){
        StorageReference imageRef = userRef.child(filename);
        return imageRef.getDownloadUrl()
                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "getUserUrl() - Successfully retrieved userImage with url: " + task.getResult().toString());
                        }else{
                            Log.d(TAG, "getUserUrl() - Failed to retrieve image with error: " + task.getException());
                        }
                    }
                });
    }

    public static Task<Void> deletePostImage(String filename){
        StorageReference imageRef = postRef.child(filename);
        return imageRef.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "deletePostImage() - Successfully deleted post image.");
                        }else{
                            Log.d(TAG, "deletePostImage() - Failed to delete image with error: " + task.getException());
                        }
                    }
                });
    }

    public static Task<Void> deleteRecipeImage(String filename){
        StorageReference imageRef = recipeRef.child(filename);
        return imageRef.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "deleteRecipeImage() - Successfully deleted recipe image.");
                        }else{
                            Log.d(TAG, "deleteRecipeImage() - Failed to delete image with error: " + task.getException() );
                        }
                    }
                });
    }

    public static Task<Void> deleteUserImage(String username){
        StorageReference imageRef = recipeRef.child(username);
        return imageRef.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "deleteUserImage() - Successfully deleted user image");
                        }else{
                            Log.d(TAG, "deleteUserImage() - Failed to delete image with error: " + task.getException());
                        }
                    }
                });

    }



}
