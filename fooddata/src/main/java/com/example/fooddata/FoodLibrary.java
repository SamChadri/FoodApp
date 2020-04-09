package com.example.fooddata;

import com.google.android.gms.tasks.*;
import com.google.firebase.firestore.*;

import com.google.firestore.v1.*;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.util.*;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.UrlQuerySanitizer;
import android.os.FileUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import com.example.result.ServerResult;
import com.example.fooddata.Post.Comment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.example.fooddata.Recipe.Rating;
import static android.content.ContentValues.*;


public class FoodLibrary {
    private static String TAG="FoodLibrary";

    public static CollectionReference postCollection;
    public static CollectionReference recipeCollection;
    public static CollectionReference userCollection;

    //TODO: Modify batch requests for each user.

    public static void initDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        postCollection = db.collection("posts");
        recipeCollection = db.collection("recipes");
        userCollection = db.collection("users");

    }

    public static Task<Void> addUser(User _user) {
        return userCollection.document(_user.id).set(_user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "addUser()- User added ");
                        } else {
                            Log.w(TAG, "addUser()-  Error occurred: ", task.getException());

                        }
                    }
                });
    }

    public static Task<Void> addUserLike(User _user, final int post_id){
        return userCollection.document(_user.id).update("userLikes", FieldValue.arrayUnion(post_id))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "addUserLike() - added post to userLikes with ID: " + post_id);
                        }else{
                            Log.d(TAG, "addUserLike() - error occurred: " +  task.getException());
                        }
                    }
                });
    }


    public static Task<Void> removeUserLike(User _user, final int post_id){
        return userCollection.document(_user.id).update("userLikes", FieldValue.arrayRemove(post_id))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "removeUserLike() - removed post from userLikes with ID: " + post_id );
                        }else{
                            Log.d(TAG, "removeUserLike() - error occurred: " + task.getException());
                        }
                    }
                });
    }

    public static Task<Void> addUserComment(User _user, final User.UserComment comment){
        return userCollection.document(_user.id).update("userComments", FieldValue.arrayUnion(comment))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "addUserComment() - added comment to userComments with ID: " + comment.id);
                        }else{
                            Log.d(TAG, "addUserComment() - error occurred:  " + task.getException());
                        }
                    }
                });
    }

    public static Task<Void> removeUserComment(User _user, final User.UserComment comment){
        return userCollection.document(_user.id).update("userComments", FieldValue.arrayRemove(comment))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "addUserComment() - removed comment from userComments with ID: " + comment.id);
                        }else{
                            Log.d(TAG, "addUserComment() - error occurred: " + task.getException());
                        }
                    }
                });
    }

    public static Task<Void> addUserRating(User _user,  final User.UserRating rating){
        return userCollection.document(_user.id).update("userRatings",FieldValue.arrayUnion(rating))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "addUserRating() - added rating to userRatings with ID: " + rating.id);
                        }else{
                            Log.d(TAG, "addUserRating() - error occurred: " + task.getException());
                        }
                    }
                });
    }

    public static Task<Void> removeUserRating(User _user, final User.UserRating rating){
        return userCollection.document(_user.id).update("userRatings", FieldValue.arrayRemove(rating))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "removeUserRating() - removed rating from userRating with ID: " + rating.id);
                        }else{
                            Log.d(TAG, "removeUserRating() - error occurred: " + task.getException());
                        }
                    }
                });
    }

    public static Task<Void> addPost(Post _post) {
        String id = Integer.toString(_post.id);
        return postCollection.document(id).set(_post)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "addPost()- Post added ");
                        } else {
                            Log.w(TAG, "addPost()- Error occurred: ", task.getException());

                        }
                    }
                });

    }

    public static Task<Void> addRecipe(Recipe _recipe) {
        String id = Integer.toString(_recipe.id);
        return recipeCollection.document(id).set(_recipe)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "addRecipe()- Recipe added ");
                        } else {
                            Log.w(TAG, "addRecipe()- Error occurred: ", task.getException());

                        }
                    }
                });
    }

    public static Task<DocumentSnapshot> getUser(String id) {
        return userCollection.document(id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "getUser()- Retrieved user with ID: " + task.getResult().getId());
                        } else {
                            Log.d(TAG, "getUser()- Error occurred: " + task.getException());
                        }
                    }
                });


    }

    public static Task<DocumentSnapshot> getRecipe(String title, String username) {
        String recipeHash = Integer.toString(Recipe.generateId(username, title));
        return recipeCollection.document(recipeHash).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "getRecipe()- Retrieved recipe with ID: " + task.getResult().getId());
                        } else {

                            Log.d(TAG, "getRecipe()- Error occurred: " + task.getException());
                        }
                    }
                });
    }

    public static Task<DocumentSnapshot> getRecipe(Integer id) {
        return recipeCollection.document(Integer.toString(id)).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "getRecipe()- Retrieved recipe with ID: " + task.getResult().getId());
                        } else {
                            Log.d(TAG, "getRecipe()- Error occurred: " + task.getException());
                        }
                    }
                });
    }

    public static Task<Void> updateRecipeRating(Integer id, float rating){
        return recipeCollection.document(Integer.toString(id)).update("rating.numRatings", FieldValue.increment(1), "rating.rating", rating)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "updateRecipeRating() - Updated recipe rating");
                        }else{
                            Log.d(TAG, "updateRecipeRating() - Error occurred: " + task.getException());
                        }
                    }
                });
    }


    public static Task<Void> addRecipeComment(Integer id, Comment comment){
        return recipeCollection.document(Integer.toString(id)).update("comments", FieldValue.arrayUnion(comment))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "addRecipeComment() - Successfully added recipe comment");
                        }else{
                            Log.d(TAG, "addRecipeComment() - Error occurred: " + task.getException());
                        }
                    }
                });

    }

    public static Task<Void> removeRecipeComment(Integer id, Comment comment){
        return recipeCollection.document(Integer.toString(id)).update("comments", FieldValue.arrayRemove(comment))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "removedRecipeComment() - Successfully removed recipe comment");
                        }else{
                            Log.d(TAG, "removedRecipeComment() - Error occurred: " + task.getException());
                        }
                    }
                });

    }


    public static Task<DocumentSnapshot> getPost(Integer id){
        return postCollection.document(Integer.toString(id)).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "getPost() - Retrieved post with ID: " + task.getResult().getId());
                        }else{
                            Log.d(TAG, "getPost() - Error occurredL " + task.getException());
                        }
                    }
                });
    }

    public static Task<Void> updatePostLike(Integer id, boolean decrement){
        int val = decrement ? -1 : 1;
        return postCollection.document(Integer.toString(id)).update("likes", FieldValue.increment(val))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "updatePostLike() - Successfully updated post likes" );
                        }else{
                            Log.d(TAG, "updatePostLike() - Error occurred: " + task.getException());
                        }
                    }
                });

    }


    public static Task<Void> addPostComment(Integer id, Comment comment){
        return postCollection.document(Integer.toString(id)).update("comments", FieldValue.arrayUnion(comment))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "addPostComment() - Successfully added comment");
                        }else{
                            Log.d(TAG, "addPostComment() - Error occurred: " + task.getException());
                        }
                    }
                });
    }

    public static Task<Void>  removePostComment(Integer id, Comment comment){
        return postCollection.document(Integer.toString(id)).update("comments", FieldValue.arrayRemove(comment))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "removePostComment() - Successfully removed comment");
                        }else{
                            Log.d(TAG, "removePostComment() - Error occurred: " + task.getException());

                        }
                    }
                });
    }

    public static Task<QuerySnapshot> getPosts(String username) {
        return postCollection.whereEqualTo("username", username).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "getPosts()- Successful query with length: " + task.getResult().size());

                        } else {
                            Log.d(TAG, "getPosts()- Error occurred: " + task.getException());
                        }
                    }
                });

    }
    //TODO: For batches, update criteria for the query. Maybe user algorithm later
    public static Task<QuerySnapshot> getPostBatch(String username){
        String otherUser = username.equals("NutriClub") ? "DoughChef" : "NutriClub";
        return postCollection.whereIn("username", Arrays.asList(username, otherUser)).limit(30).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "getPostBatch() - Successful query with length " + task.getResult().size());
                        }else{
                            Log.d(TAG, "getPostBatch() - Error occurred: " + task.getException());
                        }
                    }
                });
    }

    public static Task<QuerySnapshot> getPostBatch(String username , DocumentSnapshot cursor){
        String otherUser = username.equals("NutriClub") ? "DoughChef" : "NutriClub";
        return postCollection.whereIn("username", Arrays.asList(username, otherUser)).limit(30).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "getPostBatch() - Successful query with length " + task.getResult().size());
                        }else{
                            Log.d(TAG, "getPostBatch() - Error occurred: " + task.getException());
                        }
                    }
                });
    }

    public static Task<QuerySnapshot> getRecipeBatch(String username){
        String otherUser = username.equals("NutriClub") ? "DoughChef" : "NutriClub";
        return recipeCollection.whereIn("username", Arrays.asList(username, otherUser))
                .whereEqualTo("difficulty", "Beginner").orderBy("time", Query.Direction.DESCENDING).limit(30).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "getRecipeBatch() - Successful query with length " + task.getResult().size());
                        }else{
                            Log.d(TAG, "getRecipeBatch() - Error occurred: " + task.getException());
                        }
                    }
                });
    }

    public static Task<QuerySnapshot> getRecipeBatch(String username, DocumentSnapshot cursor){
        return recipeCollection.whereEqualTo("username", username).limit(30).startAfter(cursor).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "getRecipeBatch() - Successful query with length " + task.getResult().size());
                        }else{
                            Log.d(TAG, "getRecipeBatch() - Error occurred: " + task.getException());
                        }
                    }
                });
    }


    public static Task<Void> deleteUser(String id) {
        return userCollection.document(id).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "deleteUser()- Deleted user");
                        } else {
                            Log.d(TAG, "deleteUser()- Error occurred: " + task.getException());
                        }
                    }
                });
    }

    public static Task<Void> deletePost(int id) {
        return postCollection.document(Integer.toString(id)).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "deletePost()- Deleted user");
                        } else {
                            Log.d(TAG, "deletePost()- Error occurred: " + task.getException());
                        }
                    }
                });

    }

    public static Task<Void> deleteRecipe(int id) {
        return recipeCollection.document(Integer.toString(id)).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "deleteRecipe()- Deleted user");
                        } else {
                            Log.d(TAG, "deleteRecipe()- Error occurred: " + task.getException());
                        }
                    }
                });
    }

    public static Task<QuerySnapshot> validateUser(String username) {
        return userCollection.whereEqualTo("username",username).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "validateUser()- Executed successfully");
                        } else {
                            Log.d(TAG, "validateUser()- Error occurred: " + task.getException());
                        }
                    }
                });
    }

    private static void importCustomRecipes(JSONArray recipeElements){
        for(int i =0; i< recipeElements.length(); i++){
            try{
                JSONObject element = (JSONObject) recipeElements.get(i);
                String title = element.getString("Title");
                String author = element.getString("Author");
                String image = element.getString("Image");
                String description = element.getString("Description");
                int rating = element.getInt("Rating");
                int servings = element.getInt("Servings");
                int totalTime = element.getInt("TotalTime");

                ArrayList<String> ingredients = JSONArrayToList((JSONArray)element.get("Ingredients"));
                ArrayList<String> directions = JSONArrayToList((JSONArray)element.get("Directions"));

                JSONObject nutrition = element.getJSONObject("Nutrition");
                int calories = nutrition.getInt("Calories");
                int fat = nutrition.getInt("Fat");
                int carbohydrates = nutrition.getInt("Carbohydrates");
                int protein = nutrition.getInt("Protein");

                Recipe.Nutrition recipeNutrition = new Recipe.Nutrition(calories, carbohydrates, protein, fat);

                ArrayList<String> imageUrls= new ArrayList<>();
                imageUrls.add(image);

                Recipe entry = new Recipe("NutriClub", title, description, totalTime, servings,
                        imageUrls, directions, ingredients);

                Rating newRating = new Rating(rating, 1);

                entry.rating = newRating;
                entry.nutrition = recipeNutrition;
                entry.author = author;
                entry.difficulty = generateDifficulty();


                addRecipe(entry);

            } catch (JSONException e){
                Log.d(TAG, "importCustomRecipes() - error occurred: " + e);

            }
        }
    }

    private static void importRecipes(JSONArray recipeElements) {
        for (int i = 0; i < recipeElements.length(); i++) {

            try{
                JSONObject element = (JSONObject) recipeElements.get(i);

                String title = element.getString("Title");
                if(title.compareTo("null") == 0){
                    Log.d(TAG, "Null Fields... skipping entry");
                    continue;
                }

                String description = element.getString("Description");
                String imageUrl = element.getString("Image");

                ArrayList<String> ingredients = JSONArrayToList((JSONArray)element.get("Ingredients"));
                ArrayList<String> directions = JSONArrayToList((JSONArray)element.get("Directions"));
                ArrayList<String> facts = JSONArrayToList((JSONArray) element.get("Facts"));

                if(!validList(ingredients) || !validList(directions)){
                    continue;
                }


                title = cleanEntry(title);
                description = cleanEntry(description);
                for (int k = 0; k < ingredients.size(); k++) {
                    ingredients.set(k,cleanEntry(ingredients.get(k), false));
                }
                for (int j = 0; j < directions.size(); j++) {
                    directions.set(j,cleanEntry(directions.get(j), true));
                }

                HashMap<String, Integer> factsMap = parseFacts(facts);

                Uri imageUri = Uri.parse(imageUrl);
                ArrayList<String> imageUrls = new ArrayList<>();
                imageUrls.add(imageUrl);

                Recipe entry = new Recipe("DoughChef", title, description, factsMap.get("totalTime"), factsMap.get("servings"),
                        imageUrls, directions, ingredients);

                entry.numSteps = directions.size();
                entry.numIngredients = ingredients.size();
                entry.difficulty = generateDifficulty();
                entry.rating = new Rating(generateRating(), 1);


                addRecipe(entry);
                Log.d(TAG," importRecipes()- Image Uris: " + imageUrls);
                Log.d(TAG, "importRecipes()- Title: " + title );
                Log.d(TAG, "importRecipes()- Description: " + description);
                Log.d(TAG, "importRecipes()- Directions: " + directions);
                Log.d(TAG, "importRecipes()- Ingredients: " + ingredients);
                Log.d(TAG, "importRecipes()- Difficulty: " + entry.difficulty);
                Log.d(TAG, "importRecipes()- Ingredients: " + entry.rating.rating);

            }catch (JSONException e){
                Log.d(TAG, "importRecipes()- error occurred: " + e);
            }
        }
    }

    private static void importPosts(JSONArray postElements){
        for(int i = 0; i < postElements.length(); i ++){
            try{
                JSONObject element =(JSONObject)postElements.get(i);

                String title = element.getString("Title");
                if(title.compareTo("null") == 0 ){
                    Log.d(TAG, "Null fields skipping entry");
                    continue;
                }


                String imageUrl = element.getString("Image");
                String caption = element.getString("Description");
                ArrayList<String> comments = JSONArrayToList((JSONArray)element.get("Comments"));
                if(!validList(comments)){
                    continue;
                }

                cleanEntry(caption);
                for(int k = 0; k < comments.size(); k++){
                    comments.set(k, cleanEntry(comments.get(k)));
                }
                int likes = element.getInt("Likes");


                Log.d(TAG, "importPosts() - PostNum: " + i + "\n" +
                        "Title: " + title + "\n" +
                        "ImageUrl: " + imageUrl + "\n" +
                        "Caption: " + caption + "\n" +
                        "Likes: " + likes + "\n"+
                        "Comments: " + comments + "\n" +
                        "LengthComments: " + comments.size() + "\n");

                Post entry = new Post("NutriClub", caption, imageUrl);
                ArrayList<Comment> _commentList  = new ArrayList<>();
                for(String comment: comments){
                    _commentList.add(new Comment("NutriClub", comment));

                }
                entry.comments = _commentList;
                entry.likes = likes;


                addPost(entry);

            }catch (JSONException e){
                Log.d(TAG, "importPosts() - error occurred: " + e);
            }
        }
    }
    private static ArrayList<String> JSONArrayToList(JSONArray elements) throws JSONException{
        ArrayList<String> retval = new ArrayList<>();

        for(int i = 0; i < elements.length(); i ++){
            retval.add(elements.getString(i));
        }
        return retval;
    }

    private static int generateRating(){
        return new Random().nextInt(6);
    }

    private static String generateDifficulty(){
        String [] difficultyArray = {"Beginner", "Intermediate", "Advanced", "Expert"};
        return difficultyArray[new Random().nextInt(4)];
    }

    public static void importData(Context context, String filename, String type) throws IOException{
        InputStream stream = context.getAssets().open(filename);
        int size = stream.available();
        byte [] buffer = new byte[size];
        stream.read(buffer);
        stream.close();

        String jsonString = new String(buffer);
        try{
            JSONArray elements = new JSONArray(jsonString);
            Log.d(TAG, "importData()- Read json file successfully");

            if(type.compareTo("Recipe") == 0){
                importRecipes(elements);

            }
            else if(type.compareTo("Post") == 0){
                importPosts(elements);
            }
            else if(type.compareTo("Custom") == 0){
                importCustomRecipes(elements);
            }else{
                throw new RuntimeException("Import type not recognized");
            }

        }catch (JSONException e){
            Log.d(TAG, "importData()- error occurred: " + e);
        }
    }


    private static HashMap<String, Integer> parseFacts(List<String> facts) {

        HashMap<String, Integer> parsedFacts = new HashMap<>();
        int totalTime = -1;
        int servings = -1;
        if (facts == null) {
            Log.d(TAG, "parseFacts()- Fact list is null");
            parsedFacts.put("totalTime", totalTime);
            parsedFacts.put("servings", servings);
            return parsedFacts;
        }

        String factoid1= facts.get(0);
        if (factoid1.contains(",")) {
            //Splits field into ["READY: INTmins", "SERVES: INT"]
            String[] fields = factoid1.split(",");
            if (fields.length == 2) {
                String time = fields[0].split(":")[1];
                String serves = fields[1].split(":")[1];
                Log.d(TAG, "totalTime: " + time);
                Log.d(TAG, "serves: " + serves);
                totalTime = convertString(time, true);
                servings = convertString(serves, false);
            }
        }else {
            String time = factoid1.split(":")[1];
            Log.d(TAG, "totalTime: " + time);
            totalTime = convertString(time, true);
            servings = 0;
        }
        parsedFacts.put("totalTime", totalTime);
        parsedFacts.put("servings", servings);
        return parsedFacts;
    }

    private static int convertString(String number, boolean time ){
        int num = -1;
        if(number == null){
            return -1;
        }
        if(!time){
            if(number.contains("-")){
                number = number.split("-")[0];
            }
            try {
                num = Integer.parseInt(number.trim());
            } catch (NumberFormatException e) {
                Log.d(TAG, "parseFacts()- error occurred: " + e);
            }
            return num;
        }
        if((number.contains("hrs") || number.contains("hr")) && number.contains("mins")){
            String[] num_split = number.trim().split(" ");
            String hours;
            if(number.contains("hrs")){
                hours = num_split[0].replace("hrs", "");
            }else{
                hours = num_split[0].replace("hr", "");
            }
            String mins = num_split[1].replace("mins", "");

            try{
                int hour_num = Integer.parseInt(hours.trim());
                int mins_num = Integer.parseInt(mins.trim());
                hour_num *= 60;
                num = hour_num + mins_num;
            }catch(NumberFormatException e){
                Log.d(TAG, "convertString()- error occurred: " + e);
            }

            return num;
        }
        else if(number.contains("mins")){
            String mins = number.replace("mins", "");

            try{
                num = Integer.parseInt(mins.trim());
            }catch (NumberFormatException e){
                Log.d(TAG, "convertString()- error occurred: " + e);
            }
            return num;

        }
        else{
            String hours;
            if(number.contains("hrs")){
                hours = number.replace("hrs", "");
            }else{
                hours = number.replace("hr", "");
            }

            try{
                num = Integer.parseInt(hours.trim());
                num *= 60;
            }catch (NumberFormatException e){
                Log.d(TAG, "convertString()- error occurred: " + e);
            }
            return num;

        }

    }

    private static String cleanEntry(String entry,boolean isDirection){
      if(entry.contains("\u2044")){
          entry = entry.replace("\u2044", "/");
      }
      if(entry.contains("\u201c")){
          entry = entry.replace("\u201c", "");
      }
      if(entry.contains("\u201d")){
          entry = entry.replace("\u201d", "");
      }
      if(entry.contains("\u00B0")){
          entry = entry.replace("\u00b0", " degrees ");
      }
      if(entry.contains("\u00e9")){
          entry = entry.replace("\u00e9", "e");
      }
      if(isDirection){
          entry = entry.replace(".,", ".");
      }else{
          entry = entry.replace(",", "");
      }
      return entry;
    }

    public static String cleanEntry(String entry){
        if(entry.contains("\u2044")){
            return entry.replace("\u2044", "/");
        }
        if(entry.contains("\u201c")){
            return entry.replace("\u201c", "");
        }
        if(entry.contains("\u201d")){
            return entry.replace("\u201d", "");
        }
        if(entry.contains("\u00B0")){
            return entry.replace("\u00b0", " degrees ");
        }
        if(entry.contains("\u00e9")){
            return entry.replace("\u00e9", "e");
        }
        return entry;
    }

    private static boolean validList( ArrayList<String> list ) {

        for (String item : list) {
            if (item.compareTo("") == 0) {
                return false;
            }

        }
        return true;
    }



}