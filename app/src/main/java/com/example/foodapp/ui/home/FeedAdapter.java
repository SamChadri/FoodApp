package com.example.foodapp.ui.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.os.FileUriExposedException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.Home;
import com.example.foodapp.R;
import com.example.foodapp.utils.DeviceDimensionsHelper;
import com.example.foodapp.utils.MyIdlingResource;
import com.example.fooddata.FoodLibrary;
import com.example.fooddata.Post;
import com.example.fooddata.Recipe;
import com.example.fooddata.User;
import com.google.android.gms.tasks.OnSuccessListener;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedAdapterHolder> {
    private static String TAG = "FeedAdapter";
    private static int holderKEY = 1;
    private ArrayList<Post> _posts;
    private Home homeActivty;
    private User currUser;
    private HomeViewModel homeViewModel;
    private boolean feedTwoPane;

    public void setHomeViewModel(HomeViewModel homeViewModel){
        this.homeViewModel = homeViewModel;
    }

    private final View.OnClickListener feedClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Post selectedPost = (Post)v.getTag();
            if(feedTwoPane){
                //TODO Implement tablet view.
                //handle case for tablet view

            }else{
                Log.d(TAG, selectedPost.toString());
                NavDirections action = HomeFragmentDirections.actionHomeFragmentToPostDetailFragment(selectedPost.id, R.string.home_origin_id);
                Navigation.findNavController(v).navigate(action);
            }
        }
    };
    private boolean isLiked = false;


    private final View.OnClickListener engagementClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ImageButton likeButton = (ImageButton)v;
            int id = v.getId();
            Post selectedPost = (Post)v.getTag();
            FeedAdapterHolder holder = (FeedAdapterHolder)v.getTag(R.integer.viewHolderKey);
            int position = _posts.indexOf(selectedPost);
            if(id == R.id.cardLikeButton){
                if(!likeButton.isSelected()){
                    selectedPost.likes++;
                    holder.likeCount.setText(Integer.toString(selectedPost.likes));
                    MyIdlingResource.increment();
                    FoodLibrary.updatePostLike(selectedPost.id, false)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    homeViewModel.updatePost(selectedPost);
                                    MyIdlingResource.decrement();
                                }
                            });
                    MyIdlingResource.increment();
                    FoodLibrary.addUserLike(currUser, selectedPost.id)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    homeViewModel.updateUserFields();
                                    MyIdlingResource.decrement();
                                }
                            });
                    likeButton.setSelected(!likeButton.isSelected());
                }else{
                    selectedPost.likes--;
                    holder.likeCount.setText(Integer.toString(selectedPost.likes));
                    MyIdlingResource.increment();
                    FoodLibrary.updatePostLike(selectedPost.id, true)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    MyIdlingResource.decrement();
                                    homeViewModel.updatePost(selectedPost);
                                }
                            });
                    MyIdlingResource.increment();
                    FoodLibrary.removeUserLike(currUser, selectedPost.id)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    MyIdlingResource.decrement();
                                    homeViewModel.updateUserFields();
                                }
                            });
                    likeButton.setSelected(!likeButton.isSelected());
                }
            }else if( id == R.id.cardCommentButton){
                NavDirections action = HomeFragmentDirections.actionHomeFragmentToCreateComment(selectedPost.id, true);
                Navigation.findNavController(v).navigate(action);
            }else{

            }
        }
    };

    public FeedAdapter(ArrayList<Post> posts, User currUser){

        this._posts = posts;
        this.currUser = currUser;
    }


    public static class FeedAdapterHolder extends RecyclerView.ViewHolder{
        public CardView cv;
        public TextView username;
        public TextView caption;
        public ImageView image;
        public ImageButton likeButton;
        public ImageButton commentButton;
        public TextView likeCount;
        public TextView commentCount;

        public FeedAdapterHolder(View v){
            super(v);
            this.cv = v.findViewById(R.id.postCardView);
            this.username = v.findViewById(R.id.cardUsername);
            this.caption = v.findViewById(R.id.cardCaption);
            this.image = v.findViewById(R.id.cardImage);
            this.commentCount = v.findViewById(R.id.cardCommentNum);
            this.likeCount = v.findViewById(R.id.cardLikeNum);
            this.likeButton = v.findViewById(R.id.cardLikeButton);
            this.commentButton = v.findViewById(R.id.cardCommentButton);
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap>{

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
            int imageHeight = (int)DeviceDimensionsHelper.convertDpToPixel(250, view.getContext());
            Bitmap scaledResult = Bitmap.createScaledBitmap(result,displayWidth, imageHeight, true);
            this.view.setImageBitmap(scaledResult);
        }

        private Bitmap scaleImage(Bitmap bm, int width, int height){
            float widthRatio = bm.getWidth() / (float) width;
            float heightRatio = bm.getHeight() / (float) height;
            return Bitmap.createScaledBitmap(bm, (int)(bm.getWidth() *widthRatio), (int)(bm.getHeight() * heightRatio), true);
        }
    }


    @Override
    public FeedAdapter.FeedAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.posts_layout, parent, false);

        FeedAdapterHolder holder = new FeedAdapterHolder(v);
        return holder;
    }


    @Override
    public void onBindViewHolder(FeedAdapterHolder holder, int position){
        holder.username.setText(_posts.get(position).username);
        holder.caption.setText(_posts.get(position).caption);
        holder.likeCount.setText(Integer.toString(_posts.get(position).likes));
        holder.commentCount.setText(Integer.toString(_posts.get(position).comments.size()));
        if(_posts.get(position).imageUrl != null){
            new DownloadImageTask(holder.image).execute(_posts.get(position).imageUrl);
        }

        holder.itemView.setTag(_posts.get(position));
        holder.itemView.setOnClickListener(this.feedClickListener);

        if(currUser.userLikes.contains(_posts.get(position).id)){
            holder.likeButton.setSelected(true);
        }
        boolean userCommented = false;
        for(User.UserComment userComment: homeViewModel.getFoodUser().userComments){
            if(userComment.post_id == _posts.get(position).id){
                userCommented = true;
            }
        }
        if(userCommented){
            holder.commentButton.setSelected(true);
        }else{
            holder.commentButton.setSelected(false);
        }
        holder.likeButton.setOnClickListener(this.engagementClickListener);
        holder.commentButton.setOnClickListener(this.engagementClickListener);

        holder.commentButton.setTag(_posts.get(position));
        holder.likeButton.setTag((_posts.get(position)));
        holder.likeButton.setTag(R.integer.viewHolderKey, holder);

    }

    @Override
    public int getItemCount(){
        return _posts.size();
    }


}
