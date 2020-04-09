package com.example.foodapp.RegisterUITest;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.core.internal.deps.guava.collect.Iterables;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.authlibrary.AuthLib;
import com.example.foodapp.R;
import com.example.foodapp.RegisterUI.DisplayNameEntry;
import com.example.foodapp.RegisterUI.EmailEntry;
import com.example.foodapp.RegisterUI.GetStarted;
import com.example.foodapp.RegisterUI.NameEntry;
import com.example.foodapp.utils.MyIdlingResource;
import com.example.fooddata.FoodLibrary;
import com.example.imagestorage.ImageStorage;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.ext.truth.content.IntentSubject.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import 	androidx.test.ext.truth.os.BundleSubject;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class displayNameTest {

    private static String TAG = "displayNameTest";
    private static final String displayName = "GrainJane";
    private static final String firstName = "Jane";
    private static final String lastName = "Dough";
    private static final HashMap<Object, Object> buildUser = new HashMap<>();
    private static Bundle testBundle;

    @Rule
    public IntentsTestRule<DisplayNameEntry> mActivityRule = new IntentsTestRule<DisplayNameEntry>(
            DisplayNameEntry.class){
        @Override
        protected Intent getActivityIntent(){
            Intent userData = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), DisplayNameEntry.class);
            buildUser.put("firstName",firstName );
            buildUser.put("lastName", lastName);
            userData.putExtra(GetStarted.ARGKEY, buildUser);
            buildUser.put("username", displayName);
            testBundle = userData.getExtras();
            return userData;
        }
    };

    @Before
    public void init(){
        AuthLib.initAuth();
        FoodLibrary.initDatabase();
        ImageStorage.init();

        IdlingRegistry.getInstance().register(MyIdlingResource.getResource());
    }





    @Test
    //TODO Replcae sleep with a better alternative
    public void createDisplayName() throws InterruptedException{

        onView(ViewMatchers.withId(R.id.nextButton3)).check(matches(not(isEnabled())));


        onView(withId(R.id.displayNameInput)).perform(typeText(displayName)).perform(closeSoftKeyboard());

        onView(withId(R.id.nextButton3)).check(matches(isEnabled()));

        onView(withId(R.id.nextButton3)).perform(click());

        Intent recievedIntent = Iterables.getOnlyElement(Intents.getIntents());
        Assert.assertThat((((HashMap)recievedIntent.getExtras().getSerializable(GetStarted.ARGKEY)).containsKey("username")), is(true));
        intended(hasComponent(EmailEntry.class.getName()));
    }

    @After
    public void cleanUp(){
        IdlingRegistry.getInstance().unregister(MyIdlingResource.getResource());
    }

}
