package com.example.fooddata;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.*;


public class User {

    private static String TAG = "UserObject";
    public static class UserRating{
        public int id;
        public int recipe_id;
        public String username;
        public float rating;
        public Timestamp time;

        public UserRating(){}

        public UserRating(String username, int recipe_id, float rating){
            generateId(username);
            this.username = username;
            this.recipe_id = recipe_id;
            this.rating = rating;
        }

        private void generateId(String username){
            time = new Timestamp(new Date());
            id = (username + time.toString()).hashCode();
        }
    }

    public static class UserComment{
        public int id;
        public int recipe_id;
        public int post_id;
        public String username;
        public Timestamp time;

        public UserComment(){}

        public UserComment(String username, boolean isPost, int id){
            generateId(username);
            this.username = username;
            if(isPost){
                this.post_id = id;
            }else{
                this.recipe_id = id;
            }
        }


        private void generateId(String username){
            time = new Timestamp(new Date());
            id = (username + time.toString()).hashCode();
        }
    }

    public String id;
    public String username;
    public String firstName;
    public String lastName;
    public String email;
    public Timestamp dateOfBirth;
    public Timestamp dateJoined;

    public Bitmap avi;

    public ArrayList<Recipe> recipes = new ArrayList<>();
    public ArrayList<Post> posts = new ArrayList<>();

    public ArrayList<UserRating> userRatings = new ArrayList<>();
    public ArrayList<Integer> userLikes = new ArrayList<>();
    public ArrayList<UserComment> userComments = new ArrayList<>();

    public boolean verified = false;

    public List<User> followers = new ArrayList<>();
    public List<User> following = new ArrayList<>();

    public User(){}

    public User(String username, String firstName, String lastName, String email){
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }





}
