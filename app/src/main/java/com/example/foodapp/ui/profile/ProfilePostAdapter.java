package com.example.foodapp.ui.profile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.R;
import com.example.foodapp.utils.DeviceDimensionsHelper;
import com.example.fooddata.Post;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
public class ProfilePostAdapter extends RecyclerView.Adapter<ProfilePostAdapter.ProfilePostViewHolder> {
    public static String TAG = "ProfilePostAdapter";

    public ArrayList<Post> _posts;

    public ProfilePostAdapter(ArrayList<Post> posts){
        _posts = posts;
    }
    public View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Post post = (Post)v.getTag();
            NavDirections action = ProfileFragmentDirections.actionProfileFragmentToPostDetailFragment(post.id, R.string.profile_origin_id);
            Navigation.findNavController(v).navigate(action);
        }
    };

    public static class ProfilePostViewHolder extends RecyclerView.ViewHolder{
        public ImageView postImage;
        public ImageButton likeButton;
        public ImageButton commentButton;
        public ImageButton repostButton;

        public TextView likeNum;
        public TextView commentNum;

        public ProfilePostViewHolder(View view){
            super(view);

            postImage = view.findViewById(R.id.profilePostImage);
            likeButton = view.findViewById(R.id.profilePostLikeButton);
            commentButton = view.findViewById(R.id.profilePostCommentButton);
            repostButton = view.findViewById(R.id.profilePostRepostButton);
            likeNum = view.findViewById(R.id.profilePostLikeNum);
            commentNum = view.findViewById(R.id.profilePostCommentNum);
        }
    }

    @Override
    public ProfilePostViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_posts, parent, false);

        return new ProfilePostViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ProfilePostViewHolder viewHolder, int position){
        viewHolder.likeNum.setText(Integer.toString(_posts.get(position).likes));
        viewHolder.commentNum.setText(Integer.toString(_posts.get(position).comments.size()));
        if(_posts.get(position).imageUrl != null){
            new DownloadImageTask(viewHolder.postImage).execute(_posts.get(position).imageUrl);
        }

        viewHolder.itemView.setTag(_posts.get(position));
        viewHolder.itemView.setOnClickListener(onClickListener);

    }

    @Override
    public int getItemCount(){
        return _posts.size();
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

