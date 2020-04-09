package com.example.foodapp.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.FragManagerViewModel;
import com.example.foodapp.R;
import com.example.fooddata.FoodLibrary;
import com.example.fooddata.Post;
import com.example.fooddata.Recipe;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.util.*;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

public class HomeFragment extends Fragment {
    private static String TAG = "HomeFragment";
    private HomeViewModel homeViewModel;
    private ArrayList<Post> posts;
    private ArrayList<Recipe> recipes;
    private RecyclerView recyclerView;
    private FeedAdapter feedAdapter;
    private RecipeAdapter recipeAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private View root;
    private boolean errorTest = false;
    private FragManagerViewModel fragManagerViewModel;
    private Snackbar recipeError;
    private Snackbar postsError;

    private ViewGroup container;
    private LayoutInflater inflater;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        root = inflater.inflate(R.layout.fragment_home, container, false);

        this.container = container;
        this.inflater = inflater;


        //FeedAdapter adapter = new FeedAdapter();
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        fragManagerViewModel =
                ViewModelProviders.of(getActivity()).get(FragManagerViewModel.class);

        fragManagerViewModel.setFragStatus(R.id.homeFragment);
        Log.d(TAG, "HomeFragment ID: " + this.getId());

        homeViewModel =
                ViewModelProviders.of(getActivity()).get(HomeViewModel.class);
        TabLayout tabLayout = getActivity().findViewById(R.id.homeTabLayout);
        NavigationView navView = getActivity().findViewById(R.id.nav_view);


        //TODO: Either implement loading screen, or create a loading icon until things are ready.



        /**
         navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Log.d(TAG, "ItemSelected: " + menuItem.toString());
        return false;
        }
        });
         */

        recyclerView = root.findViewById(R.id.homeRecyclerView);

        layoutManager = new LinearLayoutManager(root.getContext());
        recyclerView.setLayoutManager(layoutManager);

        homeViewModel.currRecyclerView = recyclerView;

        homeViewModel.setTabLayout(tabLayout);
        final TabLayout.Tab feedTab = tabLayout.getTabAt(0);
        final TabLayout.Tab recipeTab = tabLayout.getTabAt(1);
        Log.d(TAG, "Selected tab position: " + tabLayout.getSelectedTabPosition());
        tabLayout.setEnabled(false);
        Toast.makeText(root.getContext(), "Page loaded", Toast.LENGTH_SHORT);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(recipeTab == tab){
                    Log.d(TAG, "RecipeTab Selected");
                    recyclerView.setAdapter(recipeAdapter);
                    if(recipeError != null){
                        recipeError.show();
                    }
                }else{
                    recyclerView.setAdapter(feedAdapter);
                    if(postsError != null){
                        postsError.show();
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if(tab == feedTab){
                    recyclerView.setAdapter(feedAdapter);
                    if(postsError != null){
                        postsError.show();
                    }
                }else{
                    recyclerView.setAdapter(recipeAdapter);
                    if(recipeError != null){
                        recipeError.show();
                    }
                }

            }
        });
        homeViewModel.getLibStatus().observe(getViewLifecycleOwner(), new Observer<HashMap>() {
            @Override
            public void onChanged(HashMap hashMap) {
                if(hashMap.containsKey("recipesRetrieved") && !hashMap.containsKey("recipesInit")){
                    if((boolean)hashMap.get("recipesRetrieved") ){
                        Log.d(TAG, "recipes have been retrieved");
                        hashMap.put("recipesInit", true);
                        recipes = homeViewModel.getRecipes();
                        recipeAdapter = new RecipeAdapter(recipes, homeViewModel.getFoodUser());
                        recipeAdapter.setViewModel(homeViewModel);
                        tabLayout.setEnabled(true);
                    }else{
                        recipeError = Snackbar.make(root, "Ay Bruh...I don't know what to tell ya big fella.", Snackbar.LENGTH_LONG);
                    }
                }

            }
        });



        homeViewModel.getLibStatus().observe(getViewLifecycleOwner(), new Observer<HashMap>() {
            @Override
            public void onChanged(HashMap hashMap) {
                if(hashMap.containsKey("postsRetrieved") && !hashMap.containsKey("postsInit")){
                    if((boolean)hashMap.get("postsRetrieved")){
                        Log.d(TAG, "Posts have been retrieved");
                        hashMap.put("postsInit", true);
                        posts = homeViewModel.getPosts();
                        feedAdapter = new FeedAdapter(posts, homeViewModel.getFoodUser());
                        feedAdapter.setHomeViewModel(homeViewModel);
                        recyclerView.setAdapter(feedAdapter);
                        tabLayout.setEnabled(true);
                    }else{
                        postsError = Snackbar.make(root, "Ay Bruh...I don't know what to tell ya big fella.", Snackbar.LENGTH_LONG);
                        postsError.show();
                    }
                }
            }
        });
    }

    @Override
    public void onPause(){
        super.onPause();
        homeViewModel.clearUpdates();
        //homeViewModel.getlibData();

    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        homeViewModel.getLibStatus().getValue().remove("postsInit");
        homeViewModel.getLibStatus().getValue().remove("recipesInit");
    }

}