package com.example.foodapp.RegisterUITest;

import android.content.Intent;
import android.os.Bundle;

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
import com.example.foodapp.RegisterUI.DateOfBirth;
import com.example.foodapp.RegisterUI.DisplayNameEntry;
import com.example.foodapp.RegisterUI.EmailEntry;
import com.example.foodapp.RegisterUI.GetStarted;
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
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class emailEntryTest {

    private static String TAG = "displayNameTest";
    private static final String displayName = "GrainJane";
    private static final String firstName = "Jane";
    private static final String lastName = "Dough";
    private static final String email = "beatFreak50@gmail.com";
    private static final HashMap<Object, Object> buildUser = new HashMap<>();
    private static Bundle testBundle;

    @Rule
    public IntentsTestRule<EmailEntry> emailActivityRule = new IntentsTestRule<EmailEntry>(EmailEntry.class){
        @Override
        protected Intent getActivityIntent(){
            Intent userData = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EmailEntry.class);
            buildUser.put("username", displayName);
            buildUser.put("firstName", firstName);
            buildUser.put("lastName", lastName);
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
    public void testEmailEntry(){
        onView(ViewMatchers.withId(R.id.emailNextButton)).check(matches(not(isEnabled())));
        onView(withId(R.id.emailInput)).perform(typeText(email)).perform(closeSoftKeyboard());

        onView(withId(R.id.emailNextButton)).check((matches(isEnabled())));

        onView(withId(R.id.emailNextButton)).perform(click());

        Intent recievedIntent = Iterables.getOnlyElement(Intents.getIntents());
        HashMap<Object, Object> userInfo = (HashMap)recievedIntent.getExtras().getSerializable(GetStarted.ARGKEY);
        Assert.assertThat(userInfo.containsKey("email"), is(true));

        intended(hasComponent(DateOfBirth.class.getName()));

    }

    @After
    public void cleanup(){
        IdlingRegistry.getInstance().unregister(MyIdlingResource.getResource());
    }
}
