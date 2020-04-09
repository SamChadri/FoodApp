package com.example.foodapp.utils;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.FileUtils;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import org.tensorflow.lite.Interpreter;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.*;

public class Classifier {
    private static String TAG = "Classifier";

    private Interpreter interpreter;
    private ArrayList<String> labelList;
    private int pixelSize = 3;
    private float threshold = 0.2f;
    private int maxResult = 3;

    public AssetManager assetManager;
    public String modelPath;
    public String labelPath;
    public int inputSize;

    public Classifier(AssetManager assetManager, String modelPath, String labelPath, int inputSize){

        this.assetManager = assetManager;
        this.modelPath = modelPath;
        this.labelPath = labelPath;
        this.inputSize = inputSize;


        ByteBuffer buffer = loadModel(assetManager,modelPath);
        interpreter = new Interpreter(buffer);
        labelList = loadLabelList(assetManager, labelPath);


    }

    public static class Recognition{
        public String id = " ";
        public String title = " ";
        public float confidence = 0F;

        public Recognition(String id, String title, float confidence){
            this.id = id;
            this.title = title;
            this.confidence = confidence;
        }

        @Override
        public String toString(){
            return "Title = " + title + " Confidence = " + confidence;
        }
    }


    private ByteBuffer loadModel(AssetManager assetManager, String modelPath){
        try{
            AssetFileDescriptor fd = assetManager.openFd(modelPath);
            long startOffset = fd.getStartOffset();
            long size = fd.getDeclaredLength();
            FileChannel channel = fd.createInputStream().getChannel();
            ByteBuffer buffer = ByteBuffer.allocateDirect((int)size);
            buffer.order(ByteOrder.nativeOrder());
            channel.read(buffer);
            return buffer;
            //return channel.map(FileChannel.MapMode.READ_ONLY, startOffset, size);

        }catch(IOException e){
            Log.d(TAG, e.toString() );
        }

        return null;
    }

    private ArrayList<String> loadLabelList(AssetManager assetManager, String labelPath) {

        try{
            FileReader reader =  new FileReader(assetManager.openFd(labelPath).getFileDescriptor());
            InputStream stream = assetManager.open(labelPath);
            byte [] inputBuffer = new byte[stream.available()];

            stream.read(inputBuffer);

            String input = new String(inputBuffer);
            Log.d(TAG, "Labels Input: " + input);
            return new ArrayList<String>(Arrays.asList(input.split("\n")));

        }catch(IOException e){
            Log.d(TAG, e.toString());

        }
        return new ArrayList<String>();

    }

    public ArrayList<Recognition> interpretImage(Bitmap image){
        Bitmap scaledImage = Bitmap.createScaledBitmap(image,inputSize, inputSize, false);
        ByteBuffer input = convertBitmapToByteBuffer(scaledImage);
        Log.d(TAG, "LabelSize= " + labelList.size());
        float [][] result = new float[1][labelList.size()];
        interpreter.run(input, result);
        return getSortedResult(result);


    }


    private void addPixelValue(ByteBuffer byteBuffer, int value){
        byteBuffer.put(Integer.valueOf((value >> 16) & 0xFF).byteValue());
        byteBuffer.put(Integer.valueOf((value >> 8 ) & 0xFF).byteValue());
        byteBuffer.put(Integer.valueOf(value & 0xFF).byteValue());


    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap){
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * pixelSize);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[inputSize * inputSize];

        byteBuffer.rewind();
        bitmap.getPixels(intValues, 0 , inputSize, 0, 0, bitmap.getWidth(), bitmap.getHeight());

        int pixels = 0;
        for(int i = 0; i < inputSize; i++){
            for(int k = 0; k < inputSize; k++ ){
                int val = intValues[pixels++];
                addPixelValue(byteBuffer, val);
            }
        }

        return byteBuffer;
    }


    private ArrayList<Recognition> getSortedResult(float[][] labelListResult){

        PriorityQueue<Recognition> priorityQueue = new PriorityQueue<>(labelListResult[0].length, new Comparator<Recognition>() {
            @Override
            public int compare(Recognition o1, Recognition o2) {
                if( o1.confidence < o2.confidence){
                    return -1;
                }else if( o1.confidence == o2.confidence){
                    return 0;
                }else{
                    return 1;
                }
            }
        });

        Log.d(TAG, "First row confidence: " + labelListResult[0]);
        for(int i = 0; i < labelListResult[0].length; i++){
            Log.d(TAG, "Item: " + labelList.get(i) + "\n" +
                    "Confidence: " + labelListResult[0][i]);
            if(labelListResult[0][i] >= threshold){
                float confindence = (labelListResult[0][i]);
                Log.d(TAG,  "Selected Item: " + labelList.get(i) + "\n"
                        + "Above Threshold Confidence: " + confindence);
                priorityQueue.add(new Recognition(i + "", i >= labelList.size() ? "Unknown" : labelList.get(i), (confindence/255.0f) ));
            }
        }

        ArrayList<Recognition> result = new ArrayList<>();

        int capacity = Math.min(priorityQueue.size(), maxResult);
        Log.d(TAG, "PriorityQue Size: " +  priorityQueue.size());
        for(int i = 0; i < capacity; i++){
            result.add(priorityQueue.poll());
        }
        return result;
    }

}
