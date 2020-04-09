package com.example.foodapp.ui.create;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.foodapp.Capture;
import com.example.foodapp.FragManagerViewModel;
import com.example.foodapp.Home;
import com.example.foodapp.R;
import com.example.foodapp.utils.Classifier;
import com.example.foodapp.utils.DeviceDimensionsHelper;
import com.example.fooddata.Post;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class CreatePost extends Fragment {

    private CreatePostViewModel mViewModel;
    private static String TAG = "CreatePost";

    private ImageButton postImageButton;
    private EditText caption;
    private ImageButton aviButton;
    private Button addPostButton;


    private Classifier classifier;
    private int inputSize = 128;

    private LayoutInflater inflater;
    private ViewGroup container;

    private FragManagerViewModel fragManagerViewModel;

    private View root;

    public static CreatePost newInstance() {
        return new CreatePost();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.create_post_fragment, container, false);



        this.inflater = inflater;
        this.container = container;

        postImageButton = root.findViewById(R.id.addPostPicButton);
        caption = root.findViewById(R.id.captionEditText);
        aviButton = root.findViewById(R.id.aviButton);
        addPostButton = root.findViewById(R.id.addPostButton);
        String model_path = "food_model.tflite";
        String label_path = "labels.txt";
        classifier = new Classifier(getResources().getAssets(), model_path, label_path, inputSize);



        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("imageRotated", true);
        super.onSaveInstanceState(outState);

    }

        @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(getActivity()).get(CreatePostViewModel.class);

        fragManagerViewModel =
                    ViewModelProviders.of(getActivity()).get(FragManagerViewModel.class);

        fragManagerViewModel.setFragStatus(this.getId());
        fragManagerViewModel.currFragment = R.id.createPost;
            // TODO: When predictions are respectable, display those bitches.
        File image = new File(getActivity().getCacheDir(), Capture.FILENAME);
        if(image.exists()){
            try{
                FileInputStream fileStream = new FileInputStream(image);
                Bitmap bitmapImage = BitmapFactory.decodeStream(fileStream);

                ArrayList<Classifier.Recognition> results = classifier.interpretImage(bitmapImage);
                Log.d(TAG, "Results: " + results);
                int newHeight = (int) DeviceDimensionsHelper.convertDpToPixel(250, root.getContext());
                int newWidth = (int)DeviceDimensionsHelper.convertDpToPixel(250, root.getContext());
                Bitmap scaledImage = Bitmap.createScaledBitmap(bitmapImage, newWidth, newHeight, false);
                postImageButton.setImageBitmap(scaledImage);
                //postImageButton.setRotation(90);


            }catch(IOException e ){

                Log.d(TAG, "Error occurred: " + e);

            }
        }

        mViewModel.getFoodLibStatus().observe(getViewLifecycleOwner(), new Observer<HashMap>() {
            @Override
            public void onChanged(HashMap hashMap) {
                if(hashMap.containsKey("userRetrieved") && !hashMap.containsKey("postImageUploaded")){
                    if((boolean)hashMap.get("userRetrieved")){
                        mViewModel.newPost.username = mViewModel.getFoodUser().username;

                    }else{
                        Snackbar.make(root, "Ay Bruh...I don't know what to tell ya big fella.", Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });

        addPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mViewModel.getFoodUser() != null){
                    mViewModel.newPost.caption = caption.getText().toString();
                    mViewModel.newPost.setID();
                    mViewModel.addPostImage(image);
                    mViewModel.getFoodLibStatus().observe(getViewLifecycleOwner(), new Observer<HashMap>() {
                        @Override
                        public void onChanged(HashMap hashMap) {
                            if(hashMap.containsKey("postImageUploaded") && hashMap.containsKey("imageUrlRetrieved") && !hashMap.containsKey("postAdded")){
                                if((boolean)hashMap.get("postImageUploaded") && (boolean)hashMap.get("imageUrlRetrieved")){
                                    mViewModel.addPost();
                                    Intent intent = new Intent(getActivity(), Home.class);
                                    startActivity(intent);

                                }else{
                                    Snackbar.make(root, "Ay Bruh...I don't know what to tell ya big fella.", Snackbar.LENGTH_LONG).show();

                                }
                            }
                        }
                    });
                    //get Picture
                }


            }
        });



    }

    private void rotateImage(Bitmap bitmapImage, File image) throws IOException{
        FileOutputStream outputStream = new FileOutputStream(image);
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap rotatedImage = Bitmap.createBitmap(bitmapImage, 0 ,0, bitmapImage.getWidth(), bitmapImage.getHeight(), matrix, false);
        rotatedImage.compress(Bitmap.CompressFormat.JPEG, 100, outputStream );
        outputStream.flush();
        outputStream.close();

    }


}
