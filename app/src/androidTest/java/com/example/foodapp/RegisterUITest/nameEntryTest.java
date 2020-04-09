package com.example.foodapp.RegisterUITest;

import android.content.Intent;

import androidx.test.espresso.core.internal.deps.guava.collect.Iterables;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.ext.truth.content.IntentSubject.assertThat;
import static org.hamcrest.Matchers.not;

import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.foodapp.R;
import com.example.foodapp.RegisterUI.DisplayNameEntry;
import com.example.foodapp.RegisterUI.NameEntry;

import static androidx.test.espresso.intent.Intents.intended;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class nameEntryTest {
    @Rule
    public IntentsTestRule<NameEntry> mActivityRule = new IntentsTestRule<>(
            NameEntry.class);

    private static final String firstName = "Jane";
    private static final String lastName = "Dough";


    @Test
    public void nameEntryTest() {
        // Context of the app under test.
        onView(ViewMatchers.withId(R.id.nextButton2)).check(matches(not(isEnabled())));

        onView(withId(R.id.firstName)).perform(typeText(firstName));
        onView(withId(R.id.lastName)).perform(typeText(lastName)).perform(closeSoftKeyboard());

        onView(withId((R.id.nextButton2))).check(matches(isEnabled()));

        onView(withId(R.id.nextButton2)).perform(click());


        Intent receivedIntent = Iterables.getOnlyElement(Intents.getIntents());

        assertThat(receivedIntent).extras().containsKey("userHashMap");


        intended(hasComponent(DisplayNameEntry.class.getName()));
    }

}
