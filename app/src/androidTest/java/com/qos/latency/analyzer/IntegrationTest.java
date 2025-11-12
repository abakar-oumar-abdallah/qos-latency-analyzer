package com.qos.latency.analyzer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class IntegrationTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testCompleteUserFlow() throws InterruptedException {
        onView(withId(R.id.screen_file_selection))
                .check(matches(isDisplayed()));

        Thread.sleep(1000);

        onView(withText("test_data"))
                .check(matches(isDisplayed()))
                .perform(scrollTo(), click());

        Thread.sleep(1000);

        onView(withId(R.id.screen_animation))
                .check(matches(isDisplayed()));

        onView(withId(R.id.tv_status))
                .check(matches(withText("Prêt pour l'analyse")));

        onView(withId(R.id.chart_view))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btn_launch))
                .check(matches(isDisplayed()))
                .check(matches(withText("Lancer")));
    }

    @Test
    public void testMultipleFileSelections() throws InterruptedException {
        Thread.sleep(1000);

        onView(withText("test_data"))
                .perform(scrollTo(), click());

        Thread.sleep(1000);

        onView(withId(R.id.tv_status))
                .check(matches(withText("Prêt pour l'analyse")));

        onView(withId(R.id.btn_change_file))
                .perform(click());

        Thread.sleep(1000);

        onView(withId(R.id.screen_file_selection))
                .check(matches(isDisplayed()));

        onView(withText("test_data"))
                .perform(scrollTo(), click());

        Thread.sleep(1000);

        onView(withId(R.id.tv_status))
                .check(matches(withText("Prêt pour l'analyse")));
    }

    @Test
    public void testRefreshFileList() throws InterruptedException {
        Thread.sleep(1000);

        onView(withId(R.id.btn_refresh_files))
                .perform(click());

        Thread.sleep(1000);

        onView(withText("test_data"))
                .check(matches(isDisplayed()));
    }

    /**
     * Test amélioré de l'animation
     */
    @Test
    public void testAnimationStart() throws InterruptedException {
        Thread.sleep(1000);

        // Sélectionner un fichier
        onView(withText("test_data"))
                .perform(scrollTo(), click());

        Thread.sleep(1000);

        // Vérifier l'état initial du bouton
        onView(withId(R.id.btn_launch))
                .check(matches(withText("Lancer")))
                .check(matches(isEnabled()));

        // Lancer l'animation
        onView(withId(R.id.btn_launch))
                .perform(click());

        // Vérification IMMÉDIATE : le bouton change d'état
        Thread.sleep(300);

        onView(withId(R.id.btn_launch))
                .check(matches(withText("Animation...")));

        // Attendre que l'animation progresse
        Thread.sleep(3000);

        // À ce stade, l'animation devrait être en pause de visualisation
        // Le bouton devrait afficher le countdown (ex: "27s")
        onView(withId(R.id.btn_launch))
                .check(matches(withText(containsString("s"))));

        // Vérifier que le statut est toujours affiché
        onView(withId(R.id.tv_status))
                .check(matches(isDisplayed()));
    }

    /**
     * Nouveau test : Vérifier que l'animation change le statut
     */
    @Test
    public void testAnimationChangesStatus() throws InterruptedException {
        Thread.sleep(1000);

        onView(withText("test_data"))
                .perform(scrollTo(), click());

        Thread.sleep(1000);

        // Statut initial
        onView(withId(R.id.tv_status))
                .check(matches(withText("Prêt pour l'analyse")));

        // Lancer l'animation
        onView(withId(R.id.btn_launch))
                .perform(click());

        // Attendre un peu
        Thread.sleep(2000);

        // Le statut doit avoir changé (ne plus être "Prêt pour l'analyse")
        onView(withId(R.id.tv_status))
                .check(matches(not(withText("Prêt pour l'analyse"))));
    }
}