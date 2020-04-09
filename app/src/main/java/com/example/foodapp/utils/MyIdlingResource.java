package com.example.foodapp.utils;

import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.idling.CountingIdlingResource;

public class MyIdlingResource {
    private static CountingIdlingResource mCountingIdlingResource = new CountingIdlingResource("my_Idling_Resource");

    public static void increment(){
        mCountingIdlingResource.increment();
    }

    public static void decrement(){
        mCountingIdlingResource.decrement();
    }

    public static CountingIdlingResource getResource(){
        return mCountingIdlingResource;
    }
}
