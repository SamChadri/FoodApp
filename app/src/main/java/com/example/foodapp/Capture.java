package com.example.foodapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.foodapp.ui.capture.CaptureViewModel;
import com.example.foodapp.ui.create.Create;
import com.example.foodapp.utils.Classifier;
import com.example.foodapp.utils.ImageUtils;
import com.example.foodapp.utils.MyIdlingResource;
import com.example.imagestorage.ImageStorage;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class Capture extends AppCompatActivity {
    public static final String FILENAME= "captured_image.jpg";

    private static final String TAG = "CaptureActivity";

    private final int MY_PERMISSIONS_REQUEST_CAMERA = 1;

    private int inputSize = 128;
    private CaptureViewModel captureViewModel;
    private ProcessCameraProvider cameraProvider;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private CameraSelector selector;
    private Preview preview;
    private ImageAnalysis imageAnalyzer;
    private Classifier classifier;
    private Context currContext;
    private Executor executor;
    private ImageCapture imageCapture;
    private ImageButton captureButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        final Toolbar toolbar = findViewById(R.id.captureToolbar);

        setSupportActionBar(toolbar);
        ActionBar  ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowTitleEnabled(true);
        ab.setTitle("Capture");

        final CardView cameraLayout = findViewById(R.id.cameraLayout);
        final PreviewView previewView = new PreviewView(this);
        captureButton = findViewById(R.id.captureButton);

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        previewView.setLayoutParams(layoutParams);
        cameraLayout.addView(previewView);


        preview = new Preview.Builder()
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                .build();

        preview.setSurfaceProvider(previewView.getPreviewSurfaceProvider());
        imageAnalyzer = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(inputSize,inputSize))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageCapture =
                new ImageCapture.Builder()
                        .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                        .build();

        selector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        String model_path = "food_model.tflite";
        String label_path = "labels.txt";
        classifier = new Classifier(getResources().getAssets(), model_path, label_path, inputSize);
        executor = new Executor() {
            @Override
            public void execute(Runnable command) {
                new Thread(command).start();
            }
        };

        currContext = this;
        requestPermissions();

    }

    private void buildFoodCamX(Executor executor, Context currContext){

        imageAnalyzer.setAnalyzer(executor,
                new ImageAnalysis.Analyzer() {
                    @Override
                    public void analyze(ImageProxy image) {
                        Bitmap bitmapImage = createBitmap(image, inputSize, inputSize);
                        ArrayList<Classifier.Recognition> results = classifier.interpretImage(bitmapImage);
                        //Log.d(TAG, "Results: " + results.toString());

                    }
                });

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try{
                    cameraProvider = cameraProviderFuture.get();
                    cameraProvider.bindToLifecycle((LifecycleOwner)currContext, selector, imageAnalyzer, preview, imageCapture);
                }catch (ExecutionException | InterruptedException e){
                    Log.d(TAG, e.toString());
                }


            }
        }, ContextCompat.getMainExecutor(currContext));


        final File temp_image  = new File(getCacheDir(), FILENAME);
        temp_image.deleteOnExit();

        ImageCapture.Metadata metadata = new ImageCapture.Metadata();
        metadata.setReversedHorizontal(false);

        final ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions.Builder(temp_image).build();

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"Capture Button Clicked");
                MyIdlingResource.increment();
                imageCapture.takePicture(options, executor, new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        //Navigate to Creation step.
                        Log.d(TAG, "Image successfully saved");
                        try{
                            FileInputStream inputStream = new FileInputStream(temp_image);
                            rotateImage(BitmapFactory.decodeStream(inputStream), temp_image);
                            MyIdlingResource.decrement();
                            Intent intent = new Intent(Capture.this, Create.class);
                            startActivity(intent);

                        }catch(IOException e){
                            Log.d(TAG, "Error processing image: " + e);
                        }

                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.d(TAG, "PictureCapture failed with error: " + exception);
                    }
                });

            }
        });




    }

    private void requestPermissions(){
        Activity thisActivity = this;
        if (ContextCompat.checkSelfPermission(thisActivity,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?

            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(thisActivity,
                    new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        } else {
            buildFoodCamX(executor, currContext);
            // Permission has already been granted
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults){
        switch(requestCode){
            case(MY_PERMISSIONS_REQUEST_CAMERA):{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    buildFoodCamX(executor, currContext);

                }else{

                }
            }
        }
    }


    private Bitmap createBitmap(ImageProxy image, int width, int height){
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        int yRowStride = planes[0].getRowStride();
        int uvRowStride = planes[1].getRowStride();
        int uvPixelStride = planes[1].getPixelStride();
        //Planes go Y =0 U=1 V =2
        byte [] yBuffer = new byte[planes[0].getBuffer().capacity()];
        byte [] uBuffer = new byte[planes[1].getBuffer().capacity()];
        byte [] vBuffer = new byte[planes[2].getBuffer().capacity()];
        planes[0].getBuffer().get(yBuffer);
        planes[1].getBuffer().get(uBuffer);
        planes[2].getBuffer().get(vBuffer);

        int [] rgbBytes = new int[image.getWidth() * image.getHeight()];
        ImageUtils.convertYUV420ToARGB8888(yBuffer, uBuffer, vBuffer,
                image.getWidth(), image.getHeight(),yRowStride, uvRowStride, uvPixelStride, rgbBytes);

        return Bitmap.createBitmap(rgbBytes, width, height, Bitmap.Config.ARGB_8888);
    }


    private void rotateImage(Bitmap bitmapImage, File image) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(image);
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap rotatedImage = Bitmap.createBitmap(bitmapImage, 0 ,0, bitmapImage.getWidth(), bitmapImage.getHeight(), matrix, false);
        rotatedImage.compress(Bitmap.CompressFormat.JPEG, 100, outputStream );
        outputStream.flush();
        outputStream.close();

    }
}
