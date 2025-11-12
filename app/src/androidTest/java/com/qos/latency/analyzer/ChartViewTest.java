package com.qos.latency.analyzer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.qos.latency.analyzer.utils.DisableAnimationsRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ChartViewTest {

    // ⭐ AJOUTEZ CETTE RULE
    @Rule
    public DisableAnimationsRule disableAnimationsRule = new DisableAnimationsRule();

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testChartViewExists() throws InterruptedException {
        Thread.sleep(1000);

        onView(withText("test_data"))
                .perform(scrollTo(), click());

        Thread.sleep(1000);

        onView(withId(R.id.chart_view))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testChartLabelDisplayed() throws InterruptedException {
        Thread.sleep(1000);

        onView(withText("test_data"))
                .perform(scrollTo(), click());

        Thread.sleep(1000);

        onView(withText("Graphique"))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testSeriesInfoDisplayed() throws InterruptedException {
        Thread.sleep(1000);

        onView(withText("test_data"))
                .perform(scrollTo(), click());

        Thread.sleep(1000);

        onView(withId(R.id.tv_series_info))
                .check(matches(isDisplayed()))
                .check(matches(withText("Temps (s) vs RTT (ms)")));
    }
}