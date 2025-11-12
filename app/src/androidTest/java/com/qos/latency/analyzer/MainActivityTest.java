package com.qos.latency.analyzer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.qos.latency.analyzer.utils.DisableAnimationsRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {

    // ⭐ AJOUTEZ CETTE RULE EN PREMIER
    @Rule
    public DisableAnimationsRule disableAnimationsRule = new DisableAnimationsRule();

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testActivityLaunches() {
        onView(withId(R.id.tv_title))
                .check(matches(isDisplayed()))
                .check(matches(withText("QoS Latence")));
    }

    @Test
    public void testFileSelectionScreenDisplayedOnStart() {
        onView(withId(R.id.screen_file_selection))
                .check(matches(isDisplayed()));

        onView(withId(R.id.screen_animation))
                .check(matches(not(isDisplayed())));
    }

    @Test
    public void testFileSelectionInstructionsDisplayed() {
        onView(withId(R.id.tv_selected_file))
                .check(matches(isDisplayed()))
                .check(matches(withText("Choisissez un fichier JSON")));
    }

    @Test
    public void testRefreshButtonExists() {
        onView(withId(R.id.btn_refresh_files))
                .check(matches(isDisplayed()))
                .check(matches(withText("Actualiser")));
    }

    @Test
    public void testFileContainerExists() {
        onView(withId(R.id.file_selection_container))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testFileSelection_NavigatesToAnimationScreen() throws InterruptedException {
        Thread.sleep(1000);

        onView(withText("test_data"))
                .perform(scrollTo(), click());

        Thread.sleep(1000);

        onView(withId(R.id.screen_animation))
                .check(matches(isDisplayed()));

        onView(withId(R.id.screen_file_selection))
                .check(matches(not(isDisplayed())));
    }

    @Test
    public void testFileSelection_DisplaysFileName() throws InterruptedException {
        Thread.sleep(1000);

        onView(withText("test_data"))
                .perform(scrollTo(), click());

        Thread.sleep(1000);

        onView(withId(R.id.tv_selected_file))
                .check(matches(withText("Fichier : test_data")));
    }

    @Test
    public void testFileSelection_ShowsReadyStatus() throws InterruptedException {
        Thread.sleep(1000);

        onView(withText("test_data"))
                .perform(scrollTo(), click());

        Thread.sleep(1000);

        onView(withId(R.id.tv_status))
                .check(matches(withText("Prêt pour l'analyse")));
    }

    @Test
    public void testFileSelection_LaunchButtonEnabled() throws InterruptedException {
        Thread.sleep(1000);

        onView(withText("test_data"))
                .perform(scrollTo(), click());

        Thread.sleep(1000);

        onView(withId(R.id.btn_launch))
                .check(matches(isDisplayed()))
                .check(matches(withText("Lancer")));
    }

    @Test
    public void testBackToFileSelection() throws InterruptedException {
        Thread.sleep(1000);

        onView(withText("test_data"))
                .perform(scrollTo(), click());

        Thread.sleep(1000);

        onView(withId(R.id.btn_change_file))
                .perform(click());

        Thread.sleep(1000);

        onView(withId(R.id.screen_file_selection))
                .check(matches(isDisplayed()));

        onView(withId(R.id.screen_animation))
                .check(matches(not(isDisplayed())));
    }
}