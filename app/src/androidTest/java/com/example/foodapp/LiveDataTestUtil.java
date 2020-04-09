package com.example.foodapp;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.opencensus.stats.Aggregation;

public class LiveDataTestUtil {
    private static String TAG ="LiveDataTestUtil";
    private static HashMap map = new HashMap();
    private static boolean dataInit = false;
    private static boolean dataUpdate = false;
    private static int waitLength = 10;

    public static void setWaitLength(int length){
        waitLength = length;
    }

    public static void setDataInit(boolean flag){
        dataInit = flag;
    }

    public static void resetInternalMap(){
        map = new HashMap<>();
    }

    public static <T> T getOrAwaitValue(final LiveData<T> liveData) throws InterruptedException {
        final Object[] data = new Object[1];
        final CountDownLatch latch = new CountDownLatch(1);
        Observer<T> observer = new Observer<T>() {
            @Override
            public void onChanged(@Nullable T o) {
                if(!map.equals(o) && !dataInit){
                    data[0] = o;
                    HashMap hashMap = (HashMap)o;
                    map = (HashMap)hashMap.clone();
                    Log.d(TAG, "NewHashMap KeySet: " + hashMap.keySet().toString());
                    Log.d(TAG, "Removing Observer...\n");
                    Log.d(TAG, "DataInitFlag: " + dataInit);
                    latch.countDown();
                    liveData.removeObserver(this);
                }else{
                    HashMap hashMap = (HashMap)o;
                    if(dataInit){
                        dataInit = false;
                        Log.d(TAG, "Data Was already initialized in the observed map. Continuing to Observe...");
                        Log.d(TAG, "Received MapKeySet: " + hashMap.keySet().toString());
                    }else{
                        Log.d(TAG, "Observable data stayed the same");
                        Log.d(TAG, "Received  MapKeySet: " + hashMap.keySet().toString());
                        Log.d(TAG, "Original MapKeySet: " + map.keySet().toString());
                        Log.d(TAG,"Continuing to Observe... \n");


                    }


                }
            }
        };
        liveData.observeForever(observer);
        // Don't wait indefinitely if the LiveData is not set.
        if (!latch.await(waitLength, TimeUnit.SECONDS)) {
            throw new RuntimeException("LiveData value was never set.");
        }
        //noinspection unchecked
        return (T) data[0];
    }


    public static <T> Boolean getOrAwaitBooleanValue(LiveData<Boolean> liveData) throws InterruptedException{
        final Object[] data= new Object[1];
        final CountDownLatch latch = new CountDownLatch(1);
        Observer<Boolean> observer = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean t) {
                data[0] = t;
                latch.countDown();
                liveData.removeObserver(this);
                Log.d(TAG, "Changed Boolean value: " + t);
            }
        };

        liveData.observeForever(observer);

        if(!latch.await(5, TimeUnit.SECONDS)){
            throw new InterruptedException("User request timeout");
        }

        return (Boolean)data[0];
    }
}
