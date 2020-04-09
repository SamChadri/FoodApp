package com.example.foodapp;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.example.authlibrary.AuthLib;
import com.example.foodapp.loginHelper.SignIn;
import com.example.foodapp.ui.discover.DiscoverFragment;
import com.example.foodapp.ui.discover.DiscoverFragmentDirections;
import com.example.foodapp.ui.home.CreateCommentDirections;
import com.example.foodapp.ui.home.HomeFragment;
import com.example.foodapp.ui.home.HomeFragmentDirections;
import com.example.foodapp.ui.home.HomeViewModel;
import com.example.foodapp.ui.home.PostDetailFragment;
import com.example.foodapp.ui.home.PostDetailFragmentDirections;
import com.example.foodapp.ui.home.RecipeDetailFragmentDirections;
import com.example.foodapp.ui.profile.ProfileFragmentDirections;
import com.example.foodapp.utils.MyIdlingResource;
import com.example.fooddata.FoodLibrary;
import com.example.fooddata.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.TextView;


public class Home extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;
    private static String TAG = "HomeActivity";
    private FragManagerViewModel fragManagerViewModel;
    private HomeViewModel homeViewModel;
    private TextView navUsername;
    private TextView navEmail;
    private int currentNightMode;

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        //currentNightMode = newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK;
    }

    public class NightModeSwitch implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if(currentNightMode == Configuration.UI_MODE_NIGHT_YES){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);


            }else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
            // Code to undo the user's last action
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        fragManagerViewModel =
                ViewModelProviders.of(this).get(FragManagerViewModel.class);

        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);

        Log.d(TAG, "navItems set");

        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        NightModeSwitch modeSwitch = new NightModeSwitch();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentNightMode == Configuration.UI_MODE_NIGHT_YES){
                    Snackbar.make(view, "Flip the switch?", Snackbar.LENGTH_SHORT).setAction("Bet", modeSwitch ).show();
                    //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

                }else{
                    Snackbar.make(view, "Does it come in black?", Snackbar.LENGTH_SHORT).setAction( "Say less", modeSwitch).show();
                    //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    Log.d(TAG, "NightMode: " + (currentNightMode == Configuration.UI_MODE_NIGHT_NO));

                }

            }
        });
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        final NavigationView navigationView = findViewById(R.id.nav_view);
        final TabLayout tabLayout = findViewById(R.id.homeTabLayout);
        navigationView.bringToFront();

        View headerView = navigationView.getHeaderView(0);
        navUsername = headerView.findViewById(R.id.NavUsername);
        navEmail = headerView.findViewById(R.id.navEmail);

        homeViewModel.getUserStatus().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                navUsername.setText(user.username);
                navEmail.setText(user.email);
            }
        });


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_discover, R.id.nav_profile,
                R.id.nav_capture, R.id.nav_share, R.id.nav_send, R.id.nav_logout)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                if(destination.getId() == R.id.homeFragment){
                    tabLayout.setVisibility(View.VISIBLE);
                }else{
                    if(destination.getId() == R.id.recipeDetailFragment){
                        toolbar.setNavigationIcon(R.drawable.custom_back);
                    }else{
                    }
                    tabLayout.setVisibility(View.GONE);
                }
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                MyIdlingResource.increment();
                int id = menuItem.getItemId();
                switch(id){
                    case(R.id.nav_home):{
                        NavDirections  action = createDirections(R.id.nav_home);
                        if(action == null){
                            drawer.closeDrawers();
                            break;
                        }
                        navController.navigate(action);
                        tabLayout.getTabAt(0).select();
                        drawer.closeDrawers();
                        break;
                    }
                    case(R.id.nav_profile): {
                        Log.d(TAG, "Navigating to Profile " + (R.id.nav_profile) + " - " + id);
                        NavDirections action = createDirections(R.id.nav_profile);
                        if (action == null) {
                            drawer.closeDrawers();
                            break;
                        }
                        navController.navigate(action);
                        drawer.closeDrawers();
                        break;
                    }

                    case(R.id.nav_capture): {
                        Log.d(TAG, "Navigating to  Capture " + (R.id.nav_capture) + " - " + id);
                        Intent intent = new Intent(Home.this, Capture.class);
                        startActivity(intent);
                        drawer.closeDrawers();
                        break;
                    }
                    case(R.id.nav_discover): {
                        Log.d(TAG, "Navigating to Discover " + (R.id.nav_discover) + " - " + id);
                        NavDirections action = createDirections(R.id.nav_discover);
                        if (action == null) {
                            drawer.closeDrawers();
                            break;
                        }
                        navController.navigate(action);
                        drawer.closeDrawers();
                        break;
                    }
                    case(R.id.nav_logout):{
                        AuthLib.userLogout();
                        Intent intent = new Intent(Home.this, SignIn.class);
                        startActivity(intent);
                        drawer.closeDrawers();
                        break;
                    }

                }
                MyIdlingResource.decrement();
                return NavigationUI.onNavDestinationSelected(menuItem, navController);
            }
        });
    }

    public NavDirections createDirections(int destinationFrag){
        NavDirections action = null;
        Log.d(TAG, "CurrFragment: " + (fragManagerViewModel.currFragment));
        Log.d(TAG, "DestinationFragment: " + destinationFrag);
        switch (fragManagerViewModel.currFragment){
            case(R.id.homeFragment):{
                if(destinationFrag == R.id.nav_profile){
                    action = HomeFragmentDirections.actionHomeFragmentToProfileFragment(R.string.home_origin_id);
                    Log.d(TAG, "Destination is the profile." );
                }else if(destinationFrag == R.id.nav_discover){
                    action = HomeFragmentDirections.actionHomeFragmentToDiscoverFragment();
                    Log.d(TAG, "Destination: Discover: Origin: Home. \n Returning: " + action.toString());
                }
                break;
            }
            case(R.id.discoverFragment):{
                if(destinationFrag == R.id.nav_home){
                    action = DiscoverFragmentDirections.actionDiscoverFragmentToHomeFragment();

                }else if(destinationFrag == R.id.nav_profile){
                    action = DiscoverFragmentDirections.actionDiscoverFragmentToProfileFragment(R.string.discover_origin_id);
                }
                break;
            }
            case(R.id.profileFragment):{
                Log.d(TAG, "Case: Origin Fragment");
                if(destinationFrag == R.id.nav_home){
                    action = ProfileFragmentDirections.actionProfileFragmentToHomeFragment();
                }else if(destinationFrag == R.id.nav_discover){
                    action = ProfileFragmentDirections.actionProfileFragmentToDiscoverFragment();
                    Log.d(TAG, "Destination: Discover. Origin: Profile");
                }
                break;
            }
            case(R.id.postDetailFragment):{
                if(destinationFrag == R.id.nav_home){
                    action = PostDetailFragmentDirections.actionPostDetailFragmentToHomeFragment();

                }else if(destinationFrag == R.id.nav_discover){
                    action = PostDetailFragmentDirections.actionPostDetailFragmentToDiscoverFragment();

                }else if(destinationFrag == R.id.nav_profile){ //
                    action = PostDetailFragmentDirections.actionPostDetailFragmentToProfileFragment(R.string.post_detail_origin_id);

                }
                break;
            }
            case(R.id.recipeDetailFragment):{
                if(destinationFrag == R.id.nav_home){
                    action = RecipeDetailFragmentDirections.actionRecipeDetailFragmentToHomeFragment();
                }else if(destinationFrag == R.id.nav_discover){
                    action = RecipeDetailFragmentDirections.actionRecipeDetailFragmentToDiscoverFragment();
                }else if(destinationFrag == R.id.nav_profile){
                    action = RecipeDetailFragmentDirections.actionRecipeDetailFragmentToProfileFragment(R.string.recipe_detail_origin_id);

                }
                break;

            }

            case(R.id.createComment):{
                if(destinationFrag == R.id.nav_home){
                    action = CreateCommentDirections.actionCreateCommentToHomeFragment();
                }else if(destinationFrag == R.id.nav_discover){
                    action = CreateCommentDirections.actionCreateCommentToDiscoverFragment();
                }else if(destinationFrag == R.id.nav_profile){
                    action = CreateCommentDirections.actionCreateCommentToProfileFragment(R.string.create_comment_origin_id);
                }
            }
        }
        return action;
}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void reloadPage(View view) {
        Log.d(TAG, "Reload Button clicked");
        //getSupportFragmentManager().beginTransaction().detach(fragManagerViewModel.currFragment).attach(fragManagerViewModel.currFragment).commit();
        navController.navigate(fragManagerViewModel.currFragment, fragManagerViewModel.getArgs() );

    }
}
