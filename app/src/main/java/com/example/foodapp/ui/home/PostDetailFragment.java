package com.example.foodapp.ui.home;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.foodapp.FragManagerViewModel;
import com.example.foodapp.R;
import com.example.foodapp.utils.CommentAdapter;
import com.example.foodapp.utils.DeviceDimensionsHelper;
import com.example.fooddata.FoodLibrary;
import com.example.fooddata.Post;
import com.example.fooddata.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link PostDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PostDetailFragment extends Fragment {
    //TODO: Clean this shit up.
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private int post_id;
    private int origin_id;


    private RecyclerView.Adapter commentAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView recyclerView;

    private TabLayout tabLayout;
    private int currTab;


    private ImageButton likeButton;
    private ImageButton commentButton;
    private static final String TAG = "PostDetailFragment";

    public static boolean createComment;

    private FragManagerViewModel fragManagerViewModel;
    private HomeViewModel homeViewModel;

    private Post _post;

    private View root;
    private TextView postUsername;
    private ImageView postImage;
    private TextView postCaption;
    private TextView likeNum;
    private  TextView commentNum;

    private LayoutInflater inflater;
    private ViewGroup container;

    private boolean errorTest = false;

    public PostDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PostDetailFragment.
     */
    public static PostDetailFragment newInstance(String param1, String param2) {
        PostDetailFragment fragment = new PostDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        post_id = PostDetailFragmentArgs.fromBundle(getArguments()).getID();
        origin_id = PostDetailFragmentArgs.fromBundle(getArguments()).getOriginId();

        Activity activity = this.getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_full_post, container, false);
        postUsername = root.findViewById(R.id.postUsername);
        postImage = root.findViewById(R.id.postImage);
        postCaption = root.findViewById(R.id.postCaption);
        likeNum = root.findViewById(R.id.postLikeNum);
        commentNum = root.findViewById(R.id.postCommentNum);
        recyclerView = root.findViewById(R.id.commentRecyclerView);
        likeButton = root.findViewById(R.id.postLikeButton);
        commentButton = root.findViewById(R.id.postCommentButton);

        layoutManager = new LinearLayoutManager(root.getContext());
        recyclerView.setLayoutManager(layoutManager);
        createComment = false;
        errorTest = true;

        this.inflater = inflater;
        this.container = container;


        //TODO Get rid of like button



        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();

        tabLayout = activity.findViewById(R.id.homeTabLayout);
        currTab = getResources().getInteger(R.integer.feedTabPosition);

        fragManagerViewModel =
                ViewModelProviders.of(getActivity()).get(FragManagerViewModel.class);

        homeViewModel =
                ViewModelProviders.of(getActivity()).get(HomeViewModel.class);

        fragManagerViewModel.setFragStatus(R.id.postDetailFragment);
        fragManagerViewModel.getArgs().putInt("ID", post_id);
        fragManagerViewModel.getArgs().putInt("origin_id", origin_id );

        if(homeViewModel.getFoodUser().userLikes.contains(post_id)){
            Log.d(TAG, "User has liked this post");
            likeButton.setSelected(true);

        }else{
            likeButton.setSelected(false);
            Log.d(TAG, "User has not liked this post");
        }

        for(User.UserComment userComment: homeViewModel.getFoodUser().userComments){
            if(userComment.post_id == post_id){
                commentButton.setSelected(true);
                Log.d(TAG, "User has commented on this post");
            }
        }

        Log.d(TAG, "visibility: " + (tabLayout.getVisibility() == View.VISIBLE));
        tabLayout.setVisibility(View.GONE);


        FoodLibrary.getPost(post_id).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    _post = task.getResult().toObject(Post.class);

                    postUsername.setText(_post.username);
                    if(_post.imageUrl != null){
                        new DownloadImageTask(postImage).execute(_post.imageUrl);
                    }
                    postCaption.setText(_post.caption);
                    likeNum.setText(Integer.toString(_post.likes));
                    commentNum.setText(Integer.toString(_post.comments.size()));
                    commentAdapter = new CommentAdapter(_post.comments);
                    recyclerView.setAdapter(commentAdapter);



                    likeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(!likeButton.isSelected()){
                                FoodLibrary.updatePostLike(_post.id, false)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                homeViewModel.updatePost(_post);
                                            }
                                        });
                                FoodLibrary.addUserLike(homeViewModel.getFoodUser(), post_id)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                homeViewModel.updateUserFields();
                                            }
                                        });
                                likeNum.setText(Integer.toString(_post.likes + 1));
                                likeButton.setSelected(!likeButton.isSelected());
                            }else{
                                //TODO decrement here
                                FoodLibrary.updatePostLike(_post.id, true)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                homeViewModel.updatePost(_post);
                                            }
                                        });
                                FoodLibrary.removeUserLike(homeViewModel.getFoodUser(), post_id)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                homeViewModel.updateUserFields();
                                            }
                                        });
                                likeNum.setText(Integer.toString(_post.likes - 1));
                                likeButton.setSelected(!likeButton.isSelected());
                            }
                        }
                    });

                    commentButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            createComment = true;
                            NavDirections action = PostDetailFragmentDirections.actionPostDetailFragmentToCreateComment(_post.id, true);
                            Navigation.findNavController(v).navigate(action);
                        }
                    });

                }else{
                    Snackbar.make(root, "Ay Bruh...I don't know what to tell ya big fella.", Snackbar.LENGTH_LONG).show();                   //TODO: Implement error page.
                }
            }
        });

    }


    @Override
    public void onDestroyView(){
        super.onDestroyView();

        if(origin_id == R.string.home_origin_id && !createComment){
            Log.d(TAG, "currTab: " + currTab);
            tabLayout.getTabAt(currTab).select();
            //TODO: Put this is in destination changed listener.
        }
    }

    @Override
    public void onPause(){
        super.onPause();

        homeViewModel.clearUpdates();
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
            int displayWidth = DeviceDimensionsHelper.getDisplayWidth(view.getContext());
            int imageHeight = (int)DeviceDimensionsHelper.convertDpToPixel(350, view.getContext());
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
