package com.example.foodapp.RegisterUITest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.espresso.core.internal.deps.guava.collect.Iterables;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.authlibrary.AuthLib;
import com.example.foodapp.R;
import com.example.foodapp.RegisterUI.CreatePassword;
import com.example.foodapp.RegisterUI.DateOfBirth;

import com.example.foodapp.RegisterUI.GetStarted;
import com.example.foodapp.RegisterUI.verifyRegistration;
import com.example.foodapp.ui.create.Create;
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

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class createPasswordTest {

    private static final String displayName = "GrainJane";
    private static final String firstName = "Jane";
    private static final String lastName = "Dough";
    private static final String email = "beatFreak50@gmail.com";
    private static final String password = "password5";
    private static final HashMap<Object, Object> buildUser = new HashMap<>();


    @Rule
    public IntentsTestRule<CreatePassword> createPasswordTest = new IntentsTestRule<CreatePassword>(CreatePassword.class){
        @Override
        protected Intent getActivityIntent(){
            Intent userData = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), CreatePassword.class);
            buildUser.put("firstName", firstName);
            buildUser.put("lastName", lastName);
            buildUser.put("username", displayName);
            buildUser.put("email", email);
            buildUser.put("year", 2020);
            buildUser.put("month", 3);
            buildUser.put("day", 15);
            userData.putExtra(GetStarted.ARGKEY, buildUser);
            return userData;
        }
    };


    @Before
    public void init(){
        FoodLibrary.initDatabase();
        AuthLib.initAuth();
        ImageStorage.init();

        IdlingRegistry.getInstance().register(MyIdlingResource.getResource());
    }

    @Test
    public void testCreatePassword(){
        onView(ViewMatchers.withId(R.id.finishRegisterButton)).check(matches(not(isEnabled())));

        onView(withId(R.id.passwordInput)).perform(typeText(password)).perform(closeSoftKeyboard());

        onView(withId(R.id.finishRegisterButton)).check(matches(isEnabled())).perform(click());

        Intent receivedIntent = Iterables.getOnlyElement(Intents.getIntents());

        String preference_key = InstrumentationRegistry.getInstrumentation().getTargetContext().getString(R.string.preference_file_key);
        SharedPreferences sharePref = InstrumentationRegistry.getInstrumentation().getTargetContext().getSharedPreferences(preference_key, Context.MODE_PRIVATE);
        HashMap<Object, Object> userInfo = (HashMap)sharePref.getAll();

        Assert.assertThat(userInfo.containsKey("password"), is(true));
        Assert.assertThat(userInfo.containsKey("year"), is(true));
        Assert.assertThat(userInfo.containsKey("month"), is(true));
        Assert.assertThat(userInfo.containsKey("day"), is(true));
        Assert.assertThat(userInfo.containsKey("email"), is(true));
        Assert.assertThat(userInfo.containsKey("username"), is(true));
        Assert.assertThat(userInfo.containsKey("firstName"), is(true));
        Assert.assertThat(userInfo.containsKey("lastName"), is(true));

        intended(hasComponent(verifyRegistration.class.getName()));
    }


    @After
    public void cleanup(){

        IdlingRegistry.getInstance().unregister(MyIdlingResource.getResource());
    }


}
