package com.example.foodapp.ui.profile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;

import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.R;
import com.example.foodapp.utils.DeviceDimensionsHelper;
import com.example.fooddata.Recipe;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class ProfileRecipeAdapter extends RecyclerView.Adapter<ProfileRecipeAdapter.ProfileRecipeViewHolder> {

    public static String TAG = "ProfileRecipeAdapter";
    public ArrayList<Recipe> _recipes;
    public View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Recipe recipe = (Recipe)v.getTag();
            NavDirections action = ProfileFragmentDirections.actionProfileFragmentToRecipeDetailFragment(recipe.id, R.string.profile_origin_id);
            Navigation.findNavController(v).navigate(action);
        }
    };


    public ProfileRecipeAdapter(ArrayList<Recipe> recipes){
        _recipes = recipes;
    }

    public static class ProfileRecipeViewHolder extends RecyclerView.ViewHolder{

        public ImageView recipeImage;
        public RatingBar recipeRating;

        public ProfileRecipeViewHolder(View view){
            super(view);
            recipeImage = view.findViewById(R.id.profileRecipeImage);
            recipeRating = view.findViewById(R.id.profileRecipeRating);
        }
    }

    @Override
    public ProfileRecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_recipe, parent, false);

        return new ProfileRecipeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ProfileRecipeViewHolder viewHolder, int position){
        viewHolder.recipeRating.setRating(_recipes.get(position).rating.rating);
        if(_recipes.get(position).images.get(0) != null){
            new DownloadImageTask(viewHolder.recipeImage).execute(_recipes.get(position).images.get(0));
        }
        viewHolder.itemView.setTag(_recipes.get(position));
        viewHolder.itemView.setOnClickListener(onClickListener);
    }

    @Override
    public int getItemCount(){
        return _recipes.size();
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
            int displayWidth = (int)DeviceDimensionsHelper.convertDpToPixel(100, view.getContext());
            int imageHeight = (int)DeviceDimensionsHelper.convertDpToPixel(100, view.getContext());
            Bitmap scaledResult = Bitmap.createScaledBitmap(result,displayWidth, imageHeight, true);
            this.view.setImageBitmap(scaledResult);
        }

        private Bitmap scaleImage(Bitmap bm, int width, int height){
            float widthRatio = bm.getWidth() / (float) width;
            float heightRatio = bm.getHeight() / (float) height;
            return Bitmap.createScaledBitmap(bm, (int)(bm.getWidth() *widthRatio), (int)(bm.getHeight() * heightRatio), true);
        }
    }
}
