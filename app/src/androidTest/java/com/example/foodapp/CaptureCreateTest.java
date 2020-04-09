package com.example.foodapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.RadioButton;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.core.internal.deps.guava.collect.Iterables;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.actionWithAssertions;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import com.example.authlibrary.AuthLib;
import com.example.foodapp.ui.create.Create;
import com.example.foodapp.ui.create.CreatePostViewModel;
import com.example.foodapp.ui.create.CreateRecipeViewModel;
import com.example.foodapp.ui.home.HomeFragment;
import com.example.foodapp.utils.MyIdlingResource;
import com.example.fooddata.FoodLibrary;
import com.example.fooddata.Post;
import com.example.fooddata.Recipe;
import com.example.imagestorage.ImageStorage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthResult;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.opencensus.stats.Aggregation;

import static net.bytebuddy.matcher.ElementMatchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.AllOf.allOf;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class CaptureCreateTest {
    private static final String TAG = "CaptureCreateTest";
    public static AlertDialog.Builder builder;
    private static final String email = "samchadri@gmail.com";
    private static final String password = "password";
    private static final String userPrompt = "Attempting to test camera usage. Please point camera at any desired object.";
    private static final String userOk = "Bet";
    private static final String userCancel = "Yeet";
    private static final String postCaptionText = "Add a little sugar.";
    private static CreatePostViewModel createPostViewModel;
    private static CreateRecipeViewModel createRecipeViewModel;
    private static FragmentScenario scenario;
    private static MutableLiveData<Boolean> dialogLiveData;

    private static final String recipeTitle = "Drank";
    private static final String recipeDescription = "That good good.";
    private static final String servings = "1";
    private static final String totalTime = "1";

    private static final String [] ingredients = new String[]{"HERB", "DRIP", "MUG"};

    private static final String [] directions = new String []{"Get hot", "Drop the dime bag", "Sip Tea..."};


    public static LiveData<Boolean> getDialogLiveData() {
        return dialogLiveData;
    }

    public static class TestDialog extends DialogFragment{
        public MutableLiveData<Boolean> requestStatus = new MutableLiveData<>();
        public FragmentScenario scenario;





        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){

            builder = new AlertDialog.Builder(this.getActivity());

            builder.setMessage(userPrompt)
                    .setPositiveButton(userOk, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialogLiveData.postValue(true);
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(userCancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which){
                            dialogLiveData.postValue(false);
                            dialog.dismiss();
                        }
                    });


            return builder.create();
        }
    }

    @Rule
    public IntentsTestRule<Capture> captureTestRule = new IntentsTestRule<>(Capture.class, false, false);

    @Rule
    public ActivityTestRule<Create> createTestRule = new ActivityTestRule<>(Create.class, false, false);

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void init(){
        FoodLibrary.initDatabase();
        AuthLib.initAuth();
        ImageStorage.init();

        dialogLiveData =new MutableLiveData<>();
        IdlingRegistry.getInstance().register(MyIdlingResource.getResource());


        try {
            CountDownLatch latch = new CountDownLatch(1);

            AuthLib.userLogin(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            latch.countDown();
                        }
                    });


            latch.await();
            LiveDataTestUtil.resetInternalMap();

        }catch(InterruptedException e) {
            Assert.fail("Login Interrupted");
        }
    }


    @Test
    public void testCreatePost(){

        CountDownLatch countLatch = new CountDownLatch(1);
        scenario = FragmentScenario.launch(TestDialog.class);

        Runnable dialogCheck = new Runnable() {
            @Override
            public void run() {
                try{
                    Boolean status = LiveDataTestUtil.getOrAwaitBooleanValue(getDialogLiveData());
                    Assert.assertThat(status, is(true));
                }catch (InterruptedException e){
                    Assert.fail(e.getMessage());
                }

                countLatch.countDown();
            }
        };
        //InstrumentationRegistry.getInstrumentation().runOnMainSync(dialogCheck);
        Thread thread = new Thread(dialogCheck);
        thread.start();

        try{
            countLatch.await();
        }catch (InterruptedException e){
            Assert.fail("Damn...");
        }

        Log.d(TAG, "Slept fro 3 seconds");
        captureTestRule.launchActivity(null);

        onView(withId(R.id.captureButton)).perform(click());

        intending(hasComponent(Create.class.getName()));

        Intent newIntent  = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), Create.class);
        captureTestRule.finishActivity();

        createTestRule.launchActivity(newIntent);

        createPostViewModel = ViewModelProviders.of(createTestRule.getActivity()).get(CreatePostViewModel.class);


        onView(withId(R.id.captionEditText)).perform(ViewActions.typeText(postCaptionText)).perform(closeSoftKeyboard());

        onView(withId(R.id.addPostButton)).perform(click());

        LiveDataTestUtil.setDataInit(true);
        LiveDataTestUtil.setWaitLength(15);
        try{
            LiveDataTestUtil.getOrAwaitValue(createPostViewModel.getFoodLibStatus());
            LiveDataTestUtil.getOrAwaitValue(createPostViewModel.getFoodLibStatus());
            HashMap libStatus = LiveDataTestUtil.getOrAwaitValue(createPostViewModel.getFoodLibStatus());

            Assert.assertThat(libStatus.containsKey("postImageUploaded"), is(true));
            Assert.assertThat(libStatus.containsKey("imageUrlRetrieved"), is(true));
            Assert.assertThat(libStatus.containsKey("postAdded"), is(true));

        }catch (InterruptedException e){
            Assert.fail(e.getMessage());
        }


        String filename = (String)createPostViewModel.getTag("imageFilename");
        Post post = (Post)createPostViewModel.getTag("newPost");


        try{
            CountDownLatch latch = new CountDownLatch(2);

            ImageStorage.deletePostImage(filename)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            latch.countDown();
                        }
                    });

            FoodLibrary.deletePost(post.id)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            latch.countDown();
                        }
                    });

            if(!latch.await(5, TimeUnit.SECONDS)){
                throw new RuntimeException("Network call failure.");
            }

        } catch(InterruptedException e){
            Assert.fail(e.getMessage());
        }

    }

    @Test
    public void testCreateRecipe(){
        CountDownLatch countLatch = new CountDownLatch(1);

        scenario = FragmentScenario.launch(TestDialog.class);
        Runnable checkDialog = new Runnable() {
            @Override
            public void run() {
                try{
                    Boolean result = LiveDataTestUtil.getOrAwaitBooleanValue(getDialogLiveData());
                    Assert.assertThat(result, is(true));
                }catch (InterruptedException e){
                    Assert.fail(e.getMessage());
                }

                countLatch.countDown();
            }
        };

        Thread awaitThread = new Thread(checkDialog);
        awaitThread.start();

        try{
            countLatch.await();
        }catch (InterruptedException e){
            Assert.fail("Damn...");
        }
        captureTestRule.launchActivity(null);

        onView(withId(R.id.captureButton)).perform(click());

        intending(hasComponent(Create.class.getName()));

        Intent newIntent  = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), Create.class);
        captureTestRule.finishActivity();

        createTestRule.launchActivity(newIntent);

        createRecipeViewModel = ViewModelProviders.of(createTestRule.getActivity()).get(CreateRecipeViewModel.class);

        onView(withId(R.id.createTabLayout)).perform(selectTabAtPosition(1));

        onView(withId(R.id.recipeTitleText)).perform(typeText(recipeTitle)).perform(closeSoftKeyboard());
        onView(withId(R.id.recipeDescriptionText)).perform(typeText(recipeDescription)).perform(closeSoftKeyboard());

        onView(withId(R.id.ServingsText)).perform(typeText(servings)).perform(closeSoftKeyboard());
        onView(withId(R.id.totalTimeText)).perform(typeText(totalTime)).perform(closeSoftKeyboard());

        onView(withId(R.id.intermediateRadio)).perform(click());


        onView(withId(R.id.direction3Text)).perform(updatedScrollTo());


        onView(withId(R.id.direction1Text)).perform(typeText(directions[0])).perform(closeSoftKeyboard());
        onView(withId(R.id.direction2Text)).perform(typeText(directions[1])).perform(closeSoftKeyboard());
        onView(withId(R.id.direction3Text)).perform(typeText(directions[2])).perform(closeSoftKeyboard());

        onView(withId(R.id.ingredient3Text)).perform(updatedScrollTo());

        onView(withId(R.id.ingredient1Text)).perform(typeText(ingredients[0])).perform(closeSoftKeyboard());
        onView(withId(R.id.ingredient2Text)).perform(typeText(ingredients[1])).perform(closeSoftKeyboard());
        onView(withId(R.id.ingredient3Text)).perform(typeText(ingredients[2])).perform(closeSoftKeyboard());

        onView(withId(R.id.addRecipeButton)).perform(updatedScrollTo());

        onView(withId(R.id.addRecipeButton)).perform(click());

        LiveDataTestUtil.setDataInit(true);
        LiveDataTestUtil.setWaitLength(15);

        try{
            LiveDataTestUtil.getOrAwaitValue(createRecipeViewModel.getFoodLibStatus());
            LiveDataTestUtil.getOrAwaitValue(createRecipeViewModel.getFoodLibStatus());
            HashMap updates = LiveDataTestUtil.getOrAwaitValue(createRecipeViewModel.getFoodLibStatus());

            Assert.assertThat(updates.containsKey("recipeImageUploaded"), is(true));
            Assert.assertThat(updates.containsKey("imageUrlRetrieved"), is(true));
            Assert.assertThat(updates.containsKey("recipeAdded"), is(true));
        }catch (InterruptedException e){
            Assert.fail(e.getMessage());
        }


        Recipe recipe = (Recipe)createRecipeViewModel.getTag("newRecipe");
        String filename = (String)createRecipeViewModel.getTag("recipeFilename");
        CountDownLatch latch = new CountDownLatch(2);

        try{
            ImageStorage.deleteRecipeImage(filename)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            latch.countDown();
                        }
                    });

            FoodLibrary.deleteRecipe(recipe.id)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            latch.countDown();
                        }
                    });

            if(!latch.await(5, TimeUnit.SECONDS)){
                throw new InterruptedException("FoodStore network call failure");
            }
        }catch (InterruptedException e){
            Assert.fail(e.getMessage());
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
    private static ViewAction updatedScrollTo(){
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return Matchers.allOf(
                        isDescendantOfA(isAssignableFrom(NestedScrollView.class)),
                        withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE));
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public void perform(UiController uiController, View view) {
                Rect rect = new Rect();
                view.getDrawingRect(rect);
                if (!view.requestRectangleOnScreen(rect, true /* immediate */)) {
                    Log.w(TAG, "Scrolling to view was requested, but none of the parents scrolled.");
                }
            }
        };
    }



    @After
    public void cleanup(){

        IdlingRegistry.getInstance().unregister(MyIdlingResource.getResource());
    }

}
