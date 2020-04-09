package com.example.fooddata;

import android.graphics.Bitmap;
import android.net.Uri;
import com.example.fooddata.Post.Comment;
import com.google.firebase.Timestamp;

import java.util.*;



public class Recipe{
    public static class Rating{
        public float rating;
        public int numRatings;


        public Rating(){}

        public Rating(float rating, int numRatings){
            this.rating = rating;
            this.numRatings = numRatings;
        }

        public void updateRating(float newRating){
            float total = rating * numRatings;
            total += newRating;
            numRatings ++;
            rating = total/numRatings;

        }
    }

    public static class Nutrition{
        public int calories;
        public int carbohydrates;
        public int protein;
        public int fat;

        public Nutrition(){}

        public Nutrition(int calories, int carbohydrates, int protein, int fat){
            this.calories = calories;
            this.carbohydrates = carbohydrates;
            this.protein = protein;
            this.fat =fat;
        }
    }

    public int id;
    public String username;
    public String title;

    public String description;
    public String author;

    public ArrayList<String> images;
    public ArrayList<Comment> comments;
    public ArrayList<String> tags;
    public Rating rating;
    public String course;
    public String cuisine;
    public String difficulty;

    public int numSteps = 0;
    public int numIngredients = 0;

    public Timestamp time;

    public ArrayList<String> directions;
    public ArrayList<String> ingredients;
    public Nutrition nutrition;

    public int prepTime = 0;
    public int cookTime = 0;
    public int totalTime = 0;
    public int servings = 0;

    public void setID(){
        this.id = generateId(this.username, this.title);
    }

    public Recipe(){
        this.time = new Timestamp(new Date());
        directions = new ArrayList<>();
        ingredients = new ArrayList<>();
        nutrition = new Nutrition();
        rating = new Rating();


        images = new ArrayList<>();
        comments = new ArrayList<>();
    }

    public Recipe(String username, String title, String description, int totalTime, int servings,
                  ArrayList<String> images, ArrayList<String>directions, ArrayList<String> ingredients){
        this.time = new Timestamp(new Date());
        this.id = generateId(username, title);
        this.username = username;
        this.title = title;
        this.description = description;
        this.totalTime = totalTime;
        this.servings = servings;
        this.images = images;
        this.directions = directions;
        this.ingredients = ingredients;
    }

    /**
     * TODO: Method not compatible with Firebase. Generates cycle due to URI List. Fix later on
    public ArrayList<Uri> getImageUris(){
        ArrayList<Uri> retval = new ArrayList<>();
        for(String image: images){
            retval.add(Uri.parse(image));
        }
        return retval;
    }
    */
    public static int generateId(String username, String title){
        String val  = username + title;
        return val.hashCode();
    }

}
