package com.example.foodapp.RegisterUITest;

import android.content.Intent;

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
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class dateOfBirthTest {

    private static final String displayName = "GrainJane";
    private static final String firstName = "Jane";
    private static final String lastName = "Dough";
    private static final String email = "beatFreak50@gmail.com";
    private static final HashMap<Object, Object> buildUser = new HashMap<>();


    @Rule
    public IntentsTestRule<DateOfBirth> dobTestRule = new IntentsTestRule<DateOfBirth>(DateOfBirth.class){
        @Override
        protected Intent getActivityIntent(){
            Intent userData = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), DateOfBirth.class);
            buildUser.put("firstName", firstName);
            buildUser.put("lastName", lastName);
            buildUser.put("username", displayName);
            buildUser.put("email", email);
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
    public void testDateOfBirth(){
        onView(ViewMatchers.withId(R.id.dobSelectButton)).perform(click());

        onView(withId(DateOfBirth.datePickerID))
                .perform(PickerActions.setDate(2020,3,15));

        onView(withId(DateOfBirth.pickerButtonID)).perform(click());

        Intent receivedIntent = Iterables.getOnlyElement(Intents.getIntents());
        HashMap<Object, Object> userInfo = (HashMap)receivedIntent.getExtras().getSerializable(GetStarted.ARGKEY);
        Assert.assertThat(userInfo.containsKey("year"), is(true));
        Assert.assertThat(userInfo.containsKey("month"), is(true));
        Assert.assertThat(userInfo.containsKey("day"), is(true));

        intended(hasComponent(CreatePassword.class.getName()));
    }



    @After
    public void cleanUp(){

        IdlingRegistry.getInstance().unregister(MyIdlingResource.getResource());
    }


}
