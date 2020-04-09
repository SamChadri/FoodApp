package com.example.foodapp;

import android.content.Intent;
import android.content.res.Resources;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.ext.truth.content.IntentSubject.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.ArgumentMatchers.booleanThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavAction;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.core.internal.deps.guava.collect.Iterables;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.espresso.util.HumanReadables;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.example.authlibrary.AuthLib;
import com.example.foodapp.ui.home.FeedAdapter;
import com.example.foodapp.ui.home.HomeFragment;
import com.example.foodapp.ui.home.HomeFragmentDirections;
import com.example.foodapp.ui.home.HomeViewModel;
import com.example.foodapp.ui.home.PostDetailFragment;
import com.example.foodapp.ui.home.PostDetailFragmentDirections;
import com.example.foodapp.ui.home.RecipeAdapter;
import com.example.foodapp.ui.home.RecipeDetailFragment;
import com.example.foodapp.ui.home.RecipeDetailFragmentDirections;
import com.example.foodapp.utils.MyIdlingResource;
import com.example.fooddata.FoodLibrary;
import com.example.fooddata.Post;
import com.example.fooddata.Recipe;
import com.example.fooddata.User;
import com.example.imagestorage.ImageStorage;
import com.example.foodapp.LiveDataTestUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthResult;

import org.checkerframework.framework.qual.AnnotatedFor;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class HomeActivityTest {

    private static final String TAG = "HomeActivityTest";
    private static final String email = "samchadri@gmail.com";
    private static final String password = "password";
    private static final int testItemPosition = 1;
    private static final String commentText = "You shitted on them two times Dr. Dre?....  Oh you smell that?";
    private static boolean successfullLogin = false;
    private static HomeViewModel viewModel;

    @Rule
    public ActivityTestRule<Home> homeActivityRule = new ActivityTestRule<>(Home.class, false, false);

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    //TODO: Figure out a way to test Register and Password Reset.


    @Before
    public void init(){

        FoodLibrary.initDatabase();
        AuthLib.initAuth();
        ImageStorage.init();


        IdlingRegistry.getInstance().register(MyIdlingResource.getResource());


        try {
            CountDownLatch latch = new CountDownLatch(1);

            AuthLib.userLogin(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            successfullLogin = task.isSuccessful();
                            latch.countDown();
                        }
                    });


            latch.await();
            homeActivityRule.launchActivity(null);
            LiveDataTestUtil.resetInternalMap();

        }catch(InterruptedException e) {
            Assert.fail("Login Interrupted");
        }



    }


    @Test
    public void homeDataRetrieval() {


        viewModel = ViewModelProviders.of(homeActivityRule.getActivity()).get(HomeViewModel.class);
        try{
            HashMap libStatus = LiveDataTestUtil.getOrAwaitValue(viewModel.getLibStatus());
            LiveDataTestUtil.getOrAwaitValue(viewModel.getLibStatus());
            LiveDataTestUtil.getOrAwaitValue(viewModel.getLibStatus());
            Assert.assertThat(libStatus.containsKey("userRetrieved"), is(true));
            Assert.assertThat(libStatus.containsKey("postsRetrieved"), is(true));
            Assert.assertThat(libStatus.containsKey("recipesRetrieved"), is(true));

        }catch (InterruptedException e){
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testClickPostItem(){

        viewModel = ViewModelProviders.of(homeActivityRule.getActivity()).get(HomeViewModel.class);
        NavController navController = mock(NavController.class);
        Navigation.setViewNavController(homeActivityRule.getActivity().findViewById(R.id.nav_host_fragment),
                navController);


        onView(withId(R.id.homeRecyclerView)).perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));

        RecyclerView recyclerView = viewModel.currRecyclerView;
        FeedAdapter.FeedAdapterHolder viewHolder = (FeedAdapter.FeedAdapterHolder)recyclerView.findViewHolderForAdapterPosition(1);
        Post post = (Post)viewHolder.itemView.getTag();

        NavDirections action = HomeFragmentDirections.actionHomeFragmentToPostDetailFragment(post.id, R.string.home_origin_id);
        verify(navController).navigate(action);
    }

    @Test
    public void testClickRecipeItem(){
        viewModel = ViewModelProviders.of(homeActivityRule.getActivity()).get(HomeViewModel.class);


        onView(withId(R.id.homeTabLayout)).perform(selectTabAtPosition(1));
        NavController navController = mock(NavController.class);
        Navigation.setViewNavController(homeActivityRule.getActivity().findViewById(R.id.nav_host_fragment),
                navController);



        onView(withId(R.id.homeRecyclerView)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        RecyclerView recyclerView = viewModel.currRecyclerView;
        RecipeAdapter.RecipeAdapterHolder viewHolder = (RecipeAdapter.RecipeAdapterHolder)recyclerView.findViewHolderForAdapterPosition(0);
        Recipe recipe = (Recipe)viewHolder.itemView.getTag();

        NavDirections action = HomeFragmentDirections.actionHomeFragmentToRecipeDetailFragment(recipe.id, R.string.home_origin_id);
        verify(navController).navigate(action);

    }

    @Test
    public void testCardLike(){
        viewModel = ViewModelProviders.of(homeActivityRule.getActivity()).get(HomeViewModel.class);

        onView(withId(R.id.homeTabLayout)).perform(selectTabAtPosition(0));
        RecyclerView recyclerView = viewModel.currRecyclerView;

        onView(withId(R.id.homeRecyclerView)).perform(RecyclerViewActions.scrollToPosition(2));

        Log.d(TAG, "Adapter count: " + (recyclerView.getAdapter().getItemCount()));
        FeedAdapter.FeedAdapterHolder viewHolder = (FeedAdapter.FeedAdapterHolder)recyclerView.findViewHolderForAdapterPosition(1);

        Post post = (Post)viewHolder.itemView.getTag();
        int postLikes = post.likes;
        onView(withId(R.id.homeRecyclerView)).perform(RecyclerViewActions.actionOnItemAtPosition(1, clickChildViewWithId(viewHolder.likeButton.getId())));
        postLikes++;
        //Check ViewHolder updated likes in the UI
        LiveDataTestUtil.setDataInit(true);
        try{
            LiveDataTestUtil.getOrAwaitValue(viewModel.getLibStatus());
            HashMap liveUpdate = LiveDataTestUtil.getOrAwaitValue(viewModel.getLibStatus());
            Assert.assertThat(liveUpdate.containsKey("postUpdated"), is(true));
            Assert.assertThat(liveUpdate.containsKey("userUpdated"), is(true));

        }catch(InterruptedException e){
            Assert.fail(e.getMessage());
        }
        Assert.assertThat(viewHolder.likeCount.getText().toString(), is(Integer.toString(postLikes)));
        ArrayList<Post> posts = viewModel.getPosts();

        for(Post _post: posts){
            if(_post.id == post.id){
                //Check ViewHolder like count matches updatedViewModel like count
                Assert.assertThat(Integer.toString(postLikes), is(Integer.toString(_post.likes)));
            }
        }

        onView(withId(R.id.homeRecyclerView)).perform(RecyclerViewActions.actionOnItemAtPosition(1, clickChildViewWithId(viewHolder.likeButton.getId())));
        postLikes--;

        LiveDataTestUtil.setDataInit(true);
        LiveDataTestUtil.resetInternalMap();
        viewModel.clearUpdates();
        try{
            LiveDataTestUtil.getOrAwaitValue(viewModel.getLibStatus());
            HashMap liveUpdate = LiveDataTestUtil.getOrAwaitValue(viewModel.getLibStatus());
            Assert.assertThat(liveUpdate.containsKey("postUpdated"), is(true));
            Assert.assertThat(liveUpdate.containsKey("userUpdated"), is(true));

        }catch(InterruptedException e){
            Assert.fail(e.getMessage());
        }
        Assert.assertThat(viewHolder.likeCount.getText().toString(), is(Integer.toString(postLikes)));

        for(Post _post: posts){
            if(_post.id == post.id){
                //Check ViewHolder like count matches updatedViewModel like count
                Assert.assertThat(Integer.toString(postLikes), is(Integer.toString(_post.likes)));
            }
        }


    }

    @Test
    public void testRecipeRating(){
        viewModel = ViewModelProviders.of(homeActivityRule.getActivity()).get(HomeViewModel.class);

        onView(withId(R.id.homeTabLayout)).perform(selectTabAtPosition(1));
        onView(withId(R.id.homeRecyclerView)).perform(RecyclerViewActions.scrollToPosition(1));

        RecyclerView recyclerView = viewModel.currRecyclerView;
        RecipeAdapter.RecipeAdapterHolder viewHolder = (RecipeAdapter.RecipeAdapterHolder)recyclerView.findViewHolderForAdapterPosition(1);

        float rating = 3;
        Recipe recipe = (Recipe)viewHolder.itemView.getTag();
        float ogRating = recipe.rating.rating;

                onView(withId(R.id.homeRecyclerView)).
                perform(RecyclerViewActions.actionOnItemAtPosition(1,inputRating(viewHolder.rating.getId(),rating)));


        try{
            LiveDataTestUtil.setDataInit(true);
            LiveDataTestUtil.getOrAwaitValue(viewModel.getLibStatus());
            HashMap liveData = LiveDataTestUtil.getOrAwaitValue(viewModel.getLibStatus());

            Assert.assertThat(liveData.containsKey("recipeUpdated"), is(true));
            Assert.assertThat(liveData.containsKey("userUpdated"), is(true));

        }catch(InterruptedException e){

            Assert.fail(e.getMessage());
        }

        for(Recipe _recipe: viewModel.getRecipes()){
            if(_recipe.id == recipe.id){
                Assert.assertThat(_recipe.rating.rating, is(recipe.rating.rating));
            }
        }

        User.UserRating userRating = (User.UserRating)viewModel.getTag("newUserRating");

        try{
            CountDownLatch latch = new CountDownLatch(2);
            FoodLibrary.updateRecipeRating(recipe.id, ogRating)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                latch.countDown();
                            }
                        }
                    });
            FoodLibrary.removeUserRating(viewModel.getFoodUser(), userRating)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                latch.countDown();
                            }
                        }
                    });

            if(!latch.await(5, TimeUnit.SECONDS)){
                throw new InterruptedException("FoodLibrary connection timeout");
            }

        } catch(InterruptedException e){
            Assert.fail(e.getMessage());
        }

    }


    @Test
    public void testFeedCommentNav(){
        viewModel = ViewModelProviders.of(homeActivityRule.getActivity()).get(HomeViewModel.class);

        onView(withId(R.id.homeRecyclerView)).perform(RecyclerViewActions.scrollToPosition(1));

        NavController mockNavController = mock(NavController.class);
        Navigation.setViewNavController(homeActivityRule.getActivity().findViewById(R.id.nav_host_fragment), mockNavController);

        RecyclerView recyclerView = viewModel.currRecyclerView;
        FeedAdapter.FeedAdapterHolder viewHolder = (FeedAdapter.FeedAdapterHolder)recyclerView.findViewHolderForAdapterPosition(1);

        Post post = (Post)viewHolder.itemView.getTag();
        onView(withId(R.id.homeRecyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(1, clickChildViewWithId(viewHolder.commentButton.getId())));


        NavDirections action = HomeFragmentDirections.actionHomeFragmentToCreateComment(post.id, true);
        verify(mockNavController).navigate(action);
    }


    @Test
    public void testPostDetailCommentNav(){
        viewModel = ViewModelProviders.of(homeActivityRule.getActivity()).get(HomeViewModel.class);

        onView(withId(R.id.homeRecyclerView)).perform(RecyclerViewActions.scrollToPosition(1));

        RecyclerView recyclerView = viewModel.currRecyclerView;
        FeedAdapter.FeedAdapterHolder viewHolder = (FeedAdapter.FeedAdapterHolder)recyclerView.findViewHolderForAdapterPosition(1);

        Post post = (Post)viewHolder.itemView.getTag();


        onView(withId(R.id.homeRecyclerView)).perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));

        NavController mockNavController = mock(NavController.class);
        Navigation.setViewNavController(homeActivityRule.getActivity().findViewById(R.id.nav_host_fragment),mockNavController);

        onView(withId(R.id.postCommentButton)).perform(click());

        NavDirections action = PostDetailFragmentDirections.actionPostDetailFragmentToCreateComment(post.id, true);
        verify(mockNavController).navigate(action);

    }

    @Test
    public void testRecipeDetailCommentNav(){
        viewModel = ViewModelProviders.of(homeActivityRule.getActivity()).get(HomeViewModel.class);

        onView(withId(R.id.homeTabLayout)).perform(selectTabAtPosition(1));
        onView(withId(R.id.homeRecyclerView)).perform(RecyclerViewActions.scrollToPosition(1));

        RecyclerView recyclerView = viewModel.currRecyclerView;
        RecipeAdapter.RecipeAdapterHolder viewHolder = (RecipeAdapter.RecipeAdapterHolder)recyclerView.findViewHolderForAdapterPosition(1);

        Recipe recipe = (Recipe)viewHolder.itemView.getTag();

        onView(withId(R.id.homeRecyclerView)).perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));

        NavController mockNavController = mock(NavController.class);
        Navigation.setViewNavController(homeActivityRule.getActivity().findViewById(R.id.nav_host_fragment), mockNavController);

        onView(withId(R.id.recipeCommentButton)).perform(click());

        NavDirections action = RecipeDetailFragmentDirections.actionRecipeDetailFragmentToCreateComment(recipe.id, false);
        verify(mockNavController).navigate(action);

    }


    @Test
    public void testCreatePostComment(){
        viewModel = ViewModelProviders.of(homeActivityRule.getActivity()).get(HomeViewModel.class);

        onView(withId(R.id.homeRecyclerView)).perform(RecyclerViewActions.scrollToPosition(testItemPosition));

        RecyclerView recyclerView = viewModel.currRecyclerView;
        FeedAdapter.FeedAdapterHolder viewHolder = (FeedAdapter.FeedAdapterHolder)recyclerView.findViewHolderForAdapterPosition(testItemPosition);

        Post post = (Post)viewHolder.itemView.getTag();

        onView(withId(R.id.homeRecyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(testItemPosition, clickChildViewWithId(viewHolder.commentButton.getId())));


        onView(withId(R.id.newComment)).perform(typeText(commentText)).perform(closeSoftKeyboard());

        onView(withId(R.id.newCommentButton)).perform(click());

        LiveDataTestUtil.setDataInit(true);

        try{
            LiveDataTestUtil.getOrAwaitValue(viewModel.getLibStatus());
            LiveDataTestUtil.getOrAwaitValue(viewModel.getLibStatus());
            HashMap updatedStatus = LiveDataTestUtil.getOrAwaitValue(viewModel.getLibStatus());

            Assert.assertThat(updatedStatus.containsKey("postUpdated"), is(true));
            Assert.assertThat(updatedStatus.containsKey("userUpdated"), is(true));
            Assert.assertThat(updatedStatus.containsKey("postCommentCreated"), is(true));

        }catch(InterruptedException e){
            Assert.fail(e.getMessage());
        }


        try{
            CountDownLatch latch = new CountDownLatch(2);
            Post.Comment comment = (Post.Comment) viewModel.getTag("newComment");
            User.UserComment userComment = (User.UserComment) viewModel.getTag("userPostComment");
            FoodLibrary.removePostComment(post.id, comment)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            latch.countDown();
                        }
                    });

            FoodLibrary.removeUserComment(viewModel.getFoodUser(), userComment)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            latch.countDown();
                        }
                    });

            if(!latch.await(5, TimeUnit.SECONDS)){
                throw new InterruptedException("FoodLibrary tasks did not complete successfully.");
            }
        } catch (InterruptedException e){
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testCreateRecipeComment(){
        viewModel = ViewModelProviders.of(homeActivityRule.getActivity()).get(HomeViewModel.class);

        onView(withId(R.id.homeTabLayout)).perform(selectTabAtPosition(1));

        onView(withId(R.id.homeRecyclerView)).perform(RecyclerViewActions.scrollToPosition(testItemPosition));

        RecyclerView recyclerView = viewModel.currRecyclerView;
        RecipeAdapter.RecipeAdapterHolder viewHolder = (RecipeAdapter.RecipeAdapterHolder)
                recyclerView.findViewHolderForAdapterPosition(testItemPosition);

        Recipe recipe = (Recipe)viewHolder.itemView.getTag();

        onView(withId(R.id.homeRecyclerView)).perform(RecyclerViewActions.actionOnItemAtPosition(testItemPosition, click()));

        onView(withId(R.id.recipeCommentButton)).perform(click());

        onView(withId(R.id.newComment)).perform(typeText(commentText)).perform(closeSoftKeyboard());
        onView(withId(R.id.newCommentButton)).perform(click());


        LiveDataTestUtil.setDataInit(true);
        try{
            LiveDataTestUtil.getOrAwaitValue(viewModel.getLibStatus());
            LiveDataTestUtil.getOrAwaitValue(viewModel.getLibStatus());
            HashMap updates = LiveDataTestUtil.getOrAwaitValue(viewModel.getLibStatus());

            Assert.assertThat(updates.containsKey("recipeCommentCreated"), is(true));
            Assert.assertThat(updates.containsKey("recipeUpdated"), is(true));
            Assert.assertThat(updates.containsKey("userRetrieved"), is(true));

        }catch(InterruptedException e){
            Assert.fail(e.getMessage());
        }

        User.UserComment userComment = (User.UserComment) viewModel.getTag("userRecipeComment");
        Post.Comment recipeComment = (Post.Comment) viewModel.getTag("newComment");

        try{
            CountDownLatch latch = new CountDownLatch(2);

            FoodLibrary.removeRecipeComment(recipe.id, recipeComment)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            latch.countDown();
                        }
                    });

            FoodLibrary.removeUserComment(viewModel.getFoodUser(), userComment)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            latch.countDown();
                        }
                    });

            if(!latch.await(5, TimeUnit.SECONDS)){
                throw new InterruptedException("FoodLibrary call did not return successfully");
            }

        }catch(InterruptedException e){
            Assert.fail(e.getMessage());

        }

    }

    @Test
    public void testNavigation(){
        viewModel = ViewModelProviders.of(homeActivityRule.getActivity()).get(HomeViewModel.class);


        NavController mockNavController = Navigation.findNavController(homeActivityRule.getActivity(), R.id.nav_host_fragment);
        NavigationView navView = homeActivityRule.getActivity().findViewById(R.id.nav_view);
        NavigationView mockNavView = mock(NavigationView.class);


        //HomeFragment NavigationTest
        performNavigationTest(mockNavController, mockNavController.getCurrentDestination().getId(), true);

        //ProfileFragment NavigationTest
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_view)).perform(clickNavItem(R.id.nav_profile));
        performNavigationTest(mockNavController, mockNavController.getCurrentDestination().getId(), true);
        Espresso.pressBack();

        //DiscoverFragment NavigationTest
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_view)).perform(clickNavItem(R.id.nav_discover));
        performNavigationTest(mockNavController, mockNavController.getCurrentDestination().getId(), true);
        Espresso.pressBack();


        //PostDetail NavigationTest
        onView(withId(R.id.homeRecyclerView)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        performNavigationTest(mockNavController, mockNavController.getCurrentDestination().getId(), false);
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_view)).perform(clickNavItem(R.id.nav_home));
        Assert.assertThat(mockNavController.getCurrentDestination().getId(), is(R.id.homeFragment));

        //RecipeDetail NavigationTest
        onView(withId(R.id.homeTabLayout)).perform(selectTabAtPosition(1));
        onView(withId(R.id.homeRecyclerView)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        performNavigationTest(mockNavController, mockNavController.getCurrentDestination().getId(), false);
        Espresso.pressBack();
        Assert.assertThat(mockNavController.getCurrentDestination().getId(), is(R.id.homeFragment));


        //CommentFragment Navigation Test
        onView(withId(R.id.homeTabLayout)).perform(selectTabAtPosition(0));
        onView(withId(R.id.homeRecyclerView)).perform(RecyclerViewActions.scrollToPosition(0));
        FeedAdapter.FeedAdapterHolder viewHolder = (FeedAdapter.FeedAdapterHolder)
                viewModel.currRecyclerView.findViewHolderForAdapterPosition(0);

        onView(withId(R.id.homeRecyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(viewHolder.commentButton.getId())));

        performNavigationTest(mockNavController, mockNavController.getCurrentDestination().getId(), false);
        Espresso.pressBack();



    }

    private void performNavigationTest(NavController navController, int currFragment, boolean navFlag){

        onView(withId(R.id.nav_view)).perform(focusNavView());


        if(currFragment == R.id.homeFragment && navFlag){
            Log.d(TAG, "CurrFragment and GetDestination are the same");
            onView(withId(R.id.nav_view)).perform(focusNavView());
        }else{
            onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
            onView(withId(R.id.nav_view)).perform(clickNavItem(R.id.nav_home));
            Assert.assertThat(navController.getCurrentDestination().getId(), is(R.id.homeFragment));

            Espresso.pressBack();
        }


        if(currFragment == R.id.discoverFragment && navFlag){
            Log.d(TAG, "CurrFragment and GetDestination are the same");
            onView(withId(R.id.nav_view)).perform(focusNavView());
        }else{
            onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
            onView(withId(R.id.nav_view)).perform(clickNavItem(R.id.nav_discover));
            Assert.assertThat(navController.getCurrentDestination().getId(), is(R.id.discoverFragment));

            Espresso.pressBack();

        }


        if(currFragment == R.id.profileFragment && navFlag){
            Log.d(TAG, "CurrFragment and GetDestination are the same");
            onView(withId(R.id.nav_view)).perform(focusNavView());
        }else{
            onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
            onView(withId(R.id.nav_view)).perform(clickNavItem(R.id.nav_profile));
            Assert.assertThat(navController.getCurrentDestination().getId(), is(R.id.profileFragment));

            Espresso.pressBack();

        }


    }




    @NonNull
    private static ViewAction selectTabAtPosition(final int position) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return allOf(isDisplayed(), isAssignableFrom(TabLayout.class));
            }

            @Override
            public String getDescription() {
                return "with tab at index: " + String.valueOf(position);
            }

            @Override
            public void perform(UiController uiController, View view) {
                if (view instanceof TabLayout) {
                    TabLayout tabLayout = (TabLayout) view;
                    TabLayout.Tab tab = tabLayout.getTabAt(position);

                    if (tab != null) {
                        tab.select();
                    }
                }
            }
        };
    }

    @NonNull
    public static ViewAction clickNavItem(final int id){
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return allOf(isDisplayed(), isEnabled(), isAssignableFrom(NavigationView.class));
            }

            @Override
            public String getDescription() {
                return null;
            }

            private String getErrorMessage(Menu menu, View view) {
                String newLine = System.getProperty("line.separator");
                StringBuilder errorMessage =
                        new StringBuilder("Menu item was not found, " + "available menu items:")
                                .append(newLine);
                for (int position = 0; position < menu.size(); position++) {
                    errorMessage.append("[MenuItem] position=").append(position);
                    MenuItem menuItem = menu.getItem(position);
                    if (menuItem != null) {
                        CharSequence itemTitle = menuItem.getTitle();
                        if (itemTitle != null) {
                            errorMessage.append(", title=").append(itemTitle);
                        }
                        if (view.getResources() != null) {
                            int itemId = menuItem.getItemId();
                            try {
                                errorMessage.append(", id=");
                                String menuItemResourceName = view.getResources().getResourceName(itemId);
                                errorMessage.append(menuItemResourceName);
                            } catch (Resources.NotFoundException nfe) {
                                errorMessage.append("not found");
                            }
                        }
                        errorMessage.append(newLine);
                    }
                }
                return errorMessage.toString();
            }

            @Override
            public void perform(UiController uiController, View view) {
                if(view instanceof NavigationView){
                    NavigationView navigationView = (NavigationView) view;
                    navigationView.bringToFront();
                    navigationView.requestFocus();
                    Menu menu = navigationView.getMenu();
                    if (null == menu.findItem(id)) {
                        throw new PerformException.Builder()
                                .withActionDescription(this.getDescription())
                                .withViewDescription(HumanReadables.describe(view))
                                .withCause(new RuntimeException(getErrorMessage(menu, view)))
                                .build();
                    }
                    menu.performIdentifierAction(id, 0);
                }
            }
        };
    }

    @NonNull
    public static ViewAction focusNavView(){
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(NavigationView.class);
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public void perform(UiController uiController, View view) {
                if(view instanceof NavigationView){
                    NavigationView navView = (NavigationView)view;
                    navView.bringToFront();
                    navView.requestFocus();
                }
            }
        };

    }

    @NonNull
    public static ViewAction clickChildViewWithId(final int id){
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return null;
            }

            @Override
            public String getDescription() {
                return "Clicked child view within ViewHolder";
            }

            @Override
            public void perform(UiController uiController, View view) {
                ImageButton imageButton  = view.findViewById(id);
                imageButton.performClick();
            }
        };
    }

    @NonNull
    public static ViewAction inputRating(final int id, float rating){
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return null;
            }

            @Override
            public String getDescription() {
                return "Setting RatingBar rating to : " + rating;
            }

            @Override
            public void perform(UiController uiController, View view) {
                RatingBar ratingBar = view.findViewById(id);
                ratingBar.getOnRatingBarChangeListener().onRatingChanged(ratingBar, rating, true);
            }
        };
    }







    @After
    public void cleanup(){

        IdlingRegistry.getInstance().unregister(MyIdlingResource.getResource());
    }
}
