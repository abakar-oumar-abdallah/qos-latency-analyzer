package com.qos.latency.analyzer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.qos.latency.analyzer.utils.DisableAnimationsRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests spécifiques pour les fichiers JSON réels du projet
 *
 * Fichiers testés :
 * - test_data.json
 * - data_high_variable_latency.json
 * - new_data.json
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class SpecificFilesTest {

    @Rule
    public DisableAnimationsRule disableAnimationsRule = new DisableAnimationsRule();

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Test 1 : Sélection du fichier test_data
     */
    @Test
    public void testSelectFile_TestData() throws InterruptedException {
        // Attendre le chargement complet de la liste
        Thread.sleep(3500);

        // Sélectionner test_data
        onView(withText("test_data"))
                .check(matches(isDisplayed()))
                .perform(scrollTo(), click());

        // Attendre la navigation
        Thread.sleep(2000);

        // Vérifier l'écran d'animation
        onView(withId(R.id.screen_animation))
                .check(matches(isDisplayed()));

        // Vérifier le statut
        onView(withId(R.id.tv_status))
                .check(matches(withText("Prêt pour l'analyse")));

        // Vérifier le nom du fichier affiché
        onView(withId(R.id.tv_selected_file))
                .check(matches(withText("Fichier : test_data")));

        // Vérifier le graphique
        onView(withId(R.id.chart_view))
                .check(matches(isDisplayed()));

        // Vérifier le bouton de lancement
        onView(withId(R.id.btn_launch))
                .check(matches(isDisplayed()))
                .check(matches(withText("Lancer")));
    }

    /**
     * Test 2 : Sélection du fichier data_high_variable_latency
     */
    @Test
    public void testSelectFile_HighVariableLatency() throws InterruptedException {
        Thread.sleep(3500);

        // Sélectionner data_high_variable_latency
        onView(withText("data_high_variable_latency"))
                .check(matches(isDisplayed()))
                .perform(scrollTo(), click());

        Thread.sleep(2000);

        onView(withId(R.id.screen_animation))
                .check(matches(isDisplayed()));

        onView(withId(R.id.tv_status))
                .check(matches(withText("Prêt pour l'analyse")));

        onView(withId(R.id.tv_selected_file))
                .check(matches(withText("Fichier : data_high_variable_latency")));

        onView(withId(R.id.chart_view))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btn_launch))
                .check(matches(isDisplayed()))
                .check(matches(withText("Lancer")));
    }

    /**
     * Test 3 : Sélection du fichier new_data
     */
    @Test
    public void testSelectFile_NewData() throws InterruptedException {
        Thread.sleep(3500);

        // Sélectionner new_data
        onView(withText("new_data"))
                .check(matches(isDisplayed()))
                .perform(scrollTo(), click());

        Thread.sleep(2000);

        onView(withId(R.id.screen_animation))
                .check(matches(isDisplayed()));

        onView(withId(R.id.tv_status))
                .check(matches(withText("Prêt pour l'analyse")));

        onView(withId(R.id.tv_selected_file))
                .check(matches(withText("Fichier : new_data")));

        onView(withId(R.id.chart_view))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btn_launch))
                .check(matches(isDisplayed()))
                .check(matches(withText("Lancer")));
    }

    /**
     * Test 4 : Lancement d'animation avec test_data
     */
    @Test
    public void testLaunchAnimation_WithTestData() throws InterruptedException {
        Thread.sleep(3500);

        onView(withText("test_data"))
                .perform(scrollTo(), click());

        Thread.sleep(2000);

        // Vérifier l'état initial du bouton
        onView(withId(R.id.btn_launch))
                .check(matches(isDisplayed()))
                .check(matches(withText("Lancer")))
                .check(matches(isEnabled()));

        // Lancer l'animation
        onView(withId(R.id.btn_launch))
                .perform(click());

        // Attendre un peu
        Thread.sleep(500);

        // Vérifier que le bouton change d'état
        onView(withId(R.id.btn_launch))
                .check(matches(withText("Animation...")));

        // Attendre que l'animation progresse
        Thread.sleep(3000);

        // Le bouton devrait maintenant afficher un countdown
        onView(withId(R.id.btn_launch))
                .check(matches(isDisplayed()));
    }

    /**
     * Test 5 : Lancement d'animation avec data_high_variable_latency
     */
    @Test
    public void testLaunchAnimation_WithHighVariableLatency() throws InterruptedException {
        Thread.sleep(3500);

        onView(withText("data_high_variable_latency"))
                .perform(scrollTo(), click());

        Thread.sleep(2000);

        onView(withId(R.id.btn_launch))
                .check(matches(isDisplayed()))
                .check(matches(withText("Lancer")))
                .check(matches(isEnabled()));

        onView(withId(R.id.btn_launch))
                .perform(click());

        Thread.sleep(500);

        onView(withId(R.id.btn_launch))
                .check(matches(withText("Animation...")));
    }

    /**
     * Test 6 : Changer de fichier entre test_data et new_data
     */
    @Test
    public void testChangeFile_BetweenTestDataAndNewData() throws InterruptedException {
        Thread.sleep(3500);

        // Sélectionner test_data
        onView(withText("test_data"))
                .perform(scrollTo(), click());

        Thread.sleep(2000);

        // Vérifier qu'on est sur test_data
        onView(withId(R.id.tv_selected_file))
                .check(matches(withText("Fichier : test_data")));

        // Cliquer sur "Changer de fichier"
        onView(withId(R.id.btn_change_file))
                .perform(click());

        Thread.sleep(2000);

        // Vérifier qu'on est revenu à l'écran de sélection
        onView(withId(R.id.screen_file_selection))
                .check(matches(isDisplayed()));

        // Sélectionner new_data
        onView(withText("new_data"))
                .perform(scrollTo(), click());

        Thread.sleep(2000);

        // Vérifier qu'on est maintenant sur new_data
        onView(withId(R.id.tv_selected_file))
                .check(matches(withText("Fichier : new_data")));

        // Vérifier que tout fonctionne
        onView(withId(R.id.tv_status))
                .check(matches(withText("Prêt pour l'analyse")));
    }

    /**
     * Test 7 : Vérifier que tous les 3 fichiers apparaissent dans la liste
     */
    @Test
    public void testAllThreeFilesVisible() throws InterruptedException {
        Thread.sleep(3500);

        // Vérifier que test_data est visible
        onView(withText("test_data"))
                .check(matches(isDisplayed()));

        // Vérifier que data_high_variable_latency est visible
        onView(withText("data_high_variable_latency"))
                .perform(scrollTo())
                .check(matches(isDisplayed()));

        // Vérifier que new_data est visible
        onView(withText("new_data"))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    /**
     * Test 8 : Cycle complet avec test_data
     * Sélection → Animation → Retour → Nouvelle sélection
     */
    @Test
    public void testCompleteFlow_WithTestData() throws InterruptedException {
        Thread.sleep(3500);

        // Sélection
        onView(withText("test_data"))
                .perform(scrollTo(), click());

        Thread.sleep(2000);

        onView(withId(R.id.tv_status))
                .check(matches(withText("Prêt pour l'analyse")));

        // Lancer animation
        onView(withId(R.id.btn_launch))
                .perform(click());

        Thread.sleep(1000);

        // Retour (si le bouton existe pendant l'animation)
        // Note: Ceci dépend de votre implémentation

        // Changer de fichier
        onView(withId(R.id.btn_change_file))
                .perform(click());

        Thread.sleep(2000);

        // Re-sélectionner
        onView(withText("test_data"))
                .perform(scrollTo(), click());

        Thread.sleep(2000);

        onView(withId(R.id.tv_status))
                .check(matches(withText("Prêt pour l'analyse")));
    }
}