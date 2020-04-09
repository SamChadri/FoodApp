package com.example.fooddata;

import android.graphics.Bitmap;
import android.net.Uri;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import com.google.firebase.Timestamp;

public class Post {

    public static class Comment{

        public int id;
        public String username;
        public String comment;
        public String imageUrl;
        public Timestamp time;
        public int likes;
        public ArrayList<Comment> replies = new ArrayList<>();

        public Comment(){}

        public Comment(String username, String comment){
            this.id = generateId(username, comment);
            this.time =  new Timestamp(new Date());
            this.username =  username;
            this.comment = comment;
        }

        private int generateId(String username, String comment){
            String val = comment + username;
            return val.hashCode();
        }
    }


    public int id;
    public String username;
    public String caption;
    public Timestamp time;
    public String imageUrl;
    public int likes = 0;
    public ArrayList<Comment> comments = new ArrayList<>();

    public void setID(){
        this.id = generateId(this.username, this.caption);
    }

    public Post(){
        this.time = new Timestamp(new Date());
    }

    public Post(String username, String caption, String imageUrl){
        this.id = generateId(username, caption);
        this.time = new Timestamp(new Date());
        this.username = username;
        this.caption = caption;
        this.imageUrl = imageUrl;
    }

    /*
    public Uri getImageUri(){
        return Uri.parse(imageUrl);
    }
    */

    private int generateId( String username, String caption){
        String val = username + caption;
        return val.hashCode();
    }


}
