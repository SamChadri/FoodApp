package com.example.foodapp.ui.create;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;

import androidx.appcompat.widget.Toolbar;
import androidx.navigation.fragment.NavHostFragment;

import com.example.foodapp.FragManagerViewModel;
import com.example.foodapp.R;
import com.example.fooddata.Recipe;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

public class Create extends AppCompatActivity {
    private static String TAG = "CreateActivity";
    private static CreateRecipeViewModel recipeViewModel;
    private FragManagerViewModel fragManagerViewModel;
    private NavController navController;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        recipeViewModel =
                ViewModelProviders.of(this).get(CreateRecipeViewModel.class);

        fragManagerViewModel =
                ViewModelProviders.of(this).get(FragManagerViewModel.class);

        Toolbar toolbar = findViewById(R.id.createToolBar);
        tabLayout = findViewById(R.id.createTabLayout);


        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);



        TabLayout.Tab postTab = tabLayout.getTabAt(0);
        TabLayout.Tab recipeTab = tabLayout.getTabAt(1);
        Log.d(TAG, "Currently selected tab: " + tabLayout.getSelectedTabPosition());
        navController = Navigation.findNavController(this, R.id.createFragment);

        if(savedInstanceState != null){
            tabLayout.getTabAt((int)savedInstanceState.get("currentTab")).select();
        }


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab == postTab){
                    NavDirections action = CreateRecipeDirections.actionCreateRecipeToCreatePost();
                    Navigation.findNavController(Create.this, R.id.createFragment).navigate(action);
                }else{
                    NavDirections action = CreatePostDirections.actionCreatePostToCreateRecipe();
                    Navigation.findNavController(Create.this, R.id.createFragment).navigate(action);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    public void onRadioButtonClicked(View view){
        List<Fragment> fragment = getSupportFragmentManager().getFragments();
        Log.d(TAG, "Fragment: " + fragment);
        Recipe newRecipe = recipeViewModel.newRecipe;


        boolean checked = ((RadioButton) view).isChecked();

        switch(view.getId()){
            case(R.id.beginnerRadio):{
                if(checked){
                    newRecipe.difficulty = "Beginner";
                }
                break;
            }
            case(R.id.intermediateRadio):{
                if(checked){
                    Log.d(TAG, "Difficulty checked: Intermediate");
                    newRecipe.difficulty = "Intermediate";
                }
                break;
            }
            case(R.id.advancedRadio):{
                if(checked){
                    newRecipe.difficulty = "Advanced";
                }
                break;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("currentTab", tabLayout.getSelectedTabPosition());

    }


        public void reloadPage(View view) {
        Log.d(TAG, "Reload Button clicked");
        //getSupportFragmentManager().beginTransaction().detach(fragManagerViewModel.currFragment).attach(fragManagerViewModel.currFragment).commit();
        Navigation.findNavController(view).navigate(fragManagerViewModel.currFragment, fragManagerViewModel.getArgs());


    }


}
