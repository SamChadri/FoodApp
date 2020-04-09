package com.example.foodapp.ui.capture;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProviders;
import android.util.Size;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.foodapp.R;
import com.example.foodapp.utils.Classifier;
import com.example.foodapp.utils.ImageUtils;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class CaptureFragment extends Fragment {

    private static String TAG = "CaptureFragment";
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

    //TODO: Check for the for buildFoodCamX after requesting initial permissions.
    //TODO: Implement the back button press

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        captureViewModel =
                ViewModelProviders.of(this).get(CaptureViewModel.class);
        View root = inflater.inflate(R.layout.fragment_capture, container, false);
        cameraProviderFuture = ProcessCameraProvider.getInstance(root.getContext());
        final CardView cameraLayout = root.findViewById(R.id.cameraLayout);
        final PreviewView previewView = new PreviewView(root.getContext());

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        previewView.setLayoutParams(layoutParams);
        cameraLayout.addView(previewView);


        preview = new Preview.Builder().build();

        preview.setSurfaceProvider(previewView.getPreviewSurfaceProvider());
        imageAnalyzer = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(inputSize,inputSize))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageCapture =
                new ImageCapture.Builder()
                        .setTargetRotation(getActivity().getWindowManager().getDefaultDisplay().getRotation())
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

        currContext = root.getContext();
        requestPermissions();

        return root;
    }

    private void buildFoodCamX(Executor executor, Context currContext){

        imageAnalyzer.setAnalyzer(executor,
                new ImageAnalysis.Analyzer() {
                    @Override
                    public void analyze(ImageProxy image) {
                        Bitmap bitmapImage = createBitmap(image);
                        ArrayList<Classifier.Recognition> results = classifier.interpretImage(bitmapImage);
                        Log.d(TAG, "Results: " + results.toString());

                    }
                });

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try{
                    cameraProvider = cameraProviderFuture.get();
                    cameraProvider.bindToLifecycle((LifecycleOwner)currContext, selector, imageAnalyzer, preview);
                }catch (ExecutionException | InterruptedException e){
                    Log.d(TAG, e.toString());
                }


            }
        }, ContextCompat.getMainExecutor(currContext));

        imageCapture.takePicture(executor, new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                super.onCaptureSuccess(image);

                Toast.makeText(currContext, "Picture Taken", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void requestPermissions(){
        Activity thisActivity = getActivity();
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

    private Bitmap createBitmap(ImageProxy image){
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

        return Bitmap.createBitmap(rgbBytes, inputSize, inputSize, Bitmap.Config.ARGB_8888);
    }
}