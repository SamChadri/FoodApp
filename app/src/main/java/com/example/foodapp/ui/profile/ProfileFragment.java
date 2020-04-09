package com.example.foodapp.ui.profile;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.FragManagerViewModel;
import com.example.foodapp.R;
import com.example.foodapp.ui.home.FeedAdapter;
import com.example.foodapp.ui.home.ItemAdapter;
import com.example.foodapp.ui.home.RecipeAdapter;
import com.example.fooddata.Post;
import com.example.fooddata.Recipe;
import com.example.fooddata.User;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private static String TAG= "ProfileFragment";

    private RecyclerView.Adapter recipeAdapter;
    private RecyclerView.Adapter postAdater;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Recipe> recipes;
    private ArrayList<Post> posts;
    private User currUser;

    private TabLayout tabLayout;
    private int origin_id;
    private int currTab;

    private FragManagerViewModel fragManagerViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        origin_id = ProfileFragmentArgs.fromBundle(getArguments()).getOriginId();
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                ViewModelProviders.of(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        final TextView username = root.findViewById(R.id.profileUsername);
        final TextView description = root.findViewById(R.id.profileDescription);
        final TextView followerNum = root.findViewById(R.id.profileFollowerNum);
        final TextView followingNum = root.findViewById(R.id.profileFollowingNum);
        final TextView dateJoined = root.findViewById(R.id.profileDateJoined);

        final TabLayout profileTabLayout = root.findViewById(R.id.profileTabLayout);


        final RecyclerView profileRecyclerView = root.findViewById(R.id.profileRecyclerView);

        layoutManager = new GridLayoutManager(root.getContext(),3);
        profileRecyclerView.setLayoutManager(layoutManager);

        profileViewModel.getfoodLibStatus().observe(getViewLifecycleOwner(), new Observer<HashMap>() {
            @Override
            public void onChanged(HashMap hashMap) {
                if(hashMap.containsKey("userRetrieved")){
                    if((boolean)hashMap.get("userRetrieved")){
                        currUser = profileViewModel.getUser();
                        username.setText(currUser.username);
                        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getContext());
                        dateJoined.setText(dateFormat.format(currUser.dateJoined.toDate()));
                        //TODO: Add user description field;
                        //TODO: initialize arrays to empty arrays instead of null values...I think I did this
                        //TODO: Add sample avi for later...maybe
                    }
                }

                if(hashMap.containsKey("postsRetrieved")){
                    if((boolean)hashMap.get("postsRetrieved")){
                        posts = profileViewModel.getPosts();
                        postAdater = new ProfilePostAdapter(posts);
                        profileRecyclerView.setAdapter(postAdater);
                    }else{
                        Snackbar.make(root, "Ay Bruh...I don't know what to tell ya big fella.", Snackbar.LENGTH_LONG).show();

                    }
                }

                if(hashMap.containsKey("recipesRetrieved")){
                    if((boolean)hashMap.get("recipesRetrieved")){
                        recipes = profileViewModel.getRecipes();
                        recipeAdapter = new ProfileRecipeAdapter(recipes);

                    }else{
                        Snackbar.make(root, "Ay Bruh...I don't know what to tell ya big fella.", Snackbar.LENGTH_LONG).show();

                        // set error page
                    }
                }
            }
        });



        final TabLayout.Tab postTab = profileTabLayout.getTabAt(0);
        final TabLayout.Tab recipeTab = profileTabLayout.getTabAt(1);
        final TabLayout.Tab likeTab = profileTabLayout.getTabAt(2);

        profileTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab == recipeTab){
                    profileRecyclerView.setAdapter(recipeAdapter);
                }else if(tab == postTab){
                    profileRecyclerView.setAdapter(postAdater);
                }else{
                    //set like tab
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if(tab == recipeTab){
                    profileRecyclerView.setAdapter(recipeAdapter);
                }else if(tab == postTab){
                    profileRecyclerView.setAdapter(postAdater);
                }else{
                    //set like tab
                }

            }
        });


        return root;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();

        tabLayout = activity.findViewById(R.id.homeTabLayout);
        currTab = tabLayout.getSelectedTabPosition();

        fragManagerViewModel =
                ViewModelProviders.of(getActivity()).get(FragManagerViewModel.class);

        fragManagerViewModel.setFragStatus(R.id.profileFragment);



        if(tabLayout.getVisibility() == View.VISIBLE){
            tabLayout.setVisibility(View.GONE);
        }

    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();

        if(origin_id == R.string.home_origin_id){
           // tabLayout.setVisibility(View.VISIBLE);
            //Log.d(TAG, "Visibility set to VISIBLE");
            //tabLayout.getTabAt(currTab).select();
        }
    }


}