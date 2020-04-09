package com.example.foodapp.utils;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class SampleDiscover {

    private static String TAG = "SampleDiscover";
    public int id;
    public String title;
    public String imageUrl;
    public String description;




    public SampleDiscover(String title, String imageUrl){
        this.id = generateID(title, imageUrl);
        this.title = title;
        this.imageUrl = imageUrl;
    }

    private int generateID(String title, String imageUrl){
        return (imageUrl + title).hashCode();

    }

    public static class SampleDiscoverData{

        private ArrayList<SampleDiscover> discoverData;
        private AssetManager assetManager;

        public ArrayList<SampleDiscover> getDiscoverData(){
            return discoverData;
        }

        public SampleDiscoverData(AssetManager assetManager){
            this.assetManager = assetManager;
            initData();
        }

        private void initData(){

            discoverData = new ArrayList<>();

            String [] titleArray = new String []{"Cuisines", "Cookbooks", "Chefs", "Restaurants",
                    "Meal Plans", "Classes", "BreakFast", "Lunch", "Dinner", "Shows", "Dessert"};

            String [] imageLinks = null;

            try{
                InputStream stream = assetManager.open("discoverLinks.txt");
                byte [] inputBuffer = new byte [stream.available()];
                stream.read(inputBuffer);

                String links = new String(inputBuffer);

                imageLinks = links.split("\n");

            }catch(IOException e){

                Log.d(TAG, "Error occurred: " + e);
            }


            if(imageLinks != null){
                for(int i = 0; i < titleArray.length; i++){
                    discoverData.add( new SampleDiscover(titleArray[i], imageLinks[i]));
                }
            }

        }

    }

}
