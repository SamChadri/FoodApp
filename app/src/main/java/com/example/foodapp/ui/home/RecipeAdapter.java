package com.example.foodapp.ui.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.R;
import com.example.foodapp.utils.DeviceDimensionsHelper;
import com.example.fooddata.FoodLibrary;
import com.example.fooddata.Recipe;
import com.example.fooddata.User;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeAdapterHolder> {
    private static String TAG = "RecipeAdapter";

    public ArrayList<Recipe> recipes;
    public boolean twoPane;
    public User currUser;
    private HomeViewModel viewModel;


    public void setViewModel(HomeViewModel homeViewModel){
        this.viewModel = homeViewModel;
    }
    public View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Recipe _recipe =(Recipe) v.getTag();
            if(twoPane){
                //TODO Implement tablet view MasterDetail
            }else{
                NavDirections action = HomeFragmentDirections.actionHomeFragmentToRecipeDetailFragment(_recipe.id, R.string.home_origin_id);
                Navigation.findNavController(v).navigate(action);
            }
        }
    };

    public RatingBar.OnRatingBarChangeListener ratingListener = new RatingBar.OnRatingBarChangeListener() {
        @Override
        public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
            if(fromUser){
                Recipe recipe = (Recipe)ratingBar.getTag();
                recipe.rating.updateRating(rating);
                FoodLibrary.updateRecipeRating(recipe.id, recipe.rating.rating)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                viewModel.updateRecipe(recipe);
                            }
                        });
                User.UserRating userRating = new User.UserRating(currUser.username, recipe.id, rating);
                viewModel.setTag("newUserRating", userRating);
                FoodLibrary.addUserRating(currUser, userRating)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                viewModel.updateUserFields();
                            }
                        });
            }else{

            }

        }
    };

    public static class RecipeAdapterHolder extends RecyclerView.ViewHolder{

        public TextView username;
        public ImageView recipeImage;
        public ImageButton profileButton;
        public TextView recipeTitle;
        public TextView totalTime;
        public TextView difficulty;
        public RatingBar rating;


        public RecipeAdapterHolder(View v){
            super(v);
            username = v.findViewById(R.id.recipeCardUsername);
            recipeImage = v.findViewById(R.id.recipeCardImage);
            profileButton = v.findViewById(R.id.recipeCardUserProfile);
            recipeTitle = v.findViewById(R.id.recipeCardTitle);
            totalTime = v.findViewById(R.id.recipeCardTime);
            difficulty = v.findViewById(R.id.recipeCardDifficulty);
            rating = v.findViewById(R.id.cardRatingBar);
        }

    }
    public RecipeAdapter(ArrayList<Recipe> recipes, User currUser){
        this.recipes = recipes;
        this.currUser = currUser;
    }

    @Override
    public int getItemCount(){
        return recipes.size();
    }


    @Override
    public RecipeAdapter.RecipeAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_layout, parent, false);

        RecipeAdapter.RecipeAdapterHolder holder = new RecipeAdapterHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecipeAdapterHolder holder, int position) {
        holder.recipeTitle.setText(recipes.get(position).title);
        Log.d(TAG, "Recipe Title: " + recipes.get(position).title);
        holder.totalTime.setText(Integer.toString(recipes.get(position).totalTime));
        if(!recipes.get(position).images.isEmpty() && recipes.get(position).images.get(0) != null){
            new DownloadImageTask(holder.recipeImage).execute(recipes.get(position).images.get(0));
        }
        holder.difficulty.setText(recipes.get(position).difficulty);
        holder.rating.setRating(recipes.get(position).rating.rating);
        holder.username.setText(recipes.get(position).username);

        holder.itemView.setOnClickListener(this.listener);
        holder.itemView.setTag(recipes.get(position));

        holder.rating.setTag(recipes.get(position));
        holder.rating.setOnRatingBarChangeListener(this.ratingListener);


    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        public ImageView view;
        public DownloadImageTask(ImageView view){
            this.view = view;
        }

        @Override
        protected Bitmap doInBackground(String... args){
            String url = args[0];
            Bitmap image = null;
            try{
                InputStream stream = new URL(url).openStream();
                image = BitmapFactory.decodeStream(stream);

            }catch (IOException e){
                Log.d(TAG,"DownloadImageTask() - Error occurred: " + e);
            }
            return image;
        }

        @Override
        protected void onPostExecute(Bitmap result){
            int displayWidth = (int)DeviceDimensionsHelper.convertDpToPixel(375, view.getContext());
            int imageHeight = (int)DeviceDimensionsHelper.convertDpToPixel(375, view.getContext());
            Bitmap scaledResult = scaleImage(result, displayWidth, imageHeight);
            //Bitmap scaledResult = Bitmap.createScaledBitmap(result,displayWidth, imageHeight, true);
            this.view.setImageBitmap(scaledResult);
        }

        private Bitmap scaleImage(Bitmap bm, int width, int height){
            float widthRatio = width / (float) bm.getWidth();
            float heightRatio = height / (float) bm.getHeight();
            return Bitmap.createScaledBitmap(bm, (int)(bm.getWidth() *widthRatio), (int)(bm.getHeight() * heightRatio), true);
        }
    }

}
