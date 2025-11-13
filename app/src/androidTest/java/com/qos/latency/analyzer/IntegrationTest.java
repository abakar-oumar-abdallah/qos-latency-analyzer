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

import com.qos.latency.analyzer.utils.DisableAnimationsRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests d'intégration complets
 * Testent le flux utilisateur de bout en bout
 *
 * Note: Les délais (Thread.sleep) sont augmentés pour la CI/CD car :
 * - Les tests sur téléphone physique via ADB WiFi sont plus lents
 * - Le chargement des fichiers peut prendre du temps
 * - Cela évite les "flaky tests" dus au timing
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class IntegrationTest {

    // ⭐ CRITIQUE : Désactiver les animations système AVANT tous les tests
    @Rule
    public DisableAnimationsRule disableAnimationsRule = new DisableAnimationsRule();

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Test du flux complet : Démarrage → Sélection → Animation
     *
     * CORRIGÉ : Délais augmentés + animations désactivées
     */
    @Test
    public void testCompleteUserFlow() throws InterruptedException {
        // Vérifier l'écran de démarrage
        onView(withId(R.id.screen_file_selection))
                .check(matches(isDisplayed()));

        // AUGMENTÉ ENCORE : Attendre que la liste soit COMPLÈTEMENT chargée
        Thread.sleep(3500);  // Était 2500ms → maintenant 3500ms

        // Sélectionner un fichier
        onView(withText("test_data"))
                .check(matches(isDisplayed()))
                .perform(scrollTo(), click());

        // Attendre la navigation
        Thread.sleep(2000);  // Était 1500ms → maintenant 2000ms

        // Vérifier la navigation vers l'écran d'animation
        onView(withId(R.id.screen_animation))
                .check(matches(isDisplayed()));

        // Vérifier le statut
        onView(withId(R.id.tv_status))
                .check(matches(withText("Prêt pour l'analyse")));

        // Vérifier le graphique
        onView(withId(R.id.chart_view))
                .check(matches(isDisplayed()));

        // Vérifier le bouton
        onView(withId(R.id.btn_launch))
                .check(matches(isDisplayed()))
                .check(matches(withText("Lancer")));
    }

    /**
     * Test de sélections multiples
     */
    @Test
    public void testMultipleFileSelections() throws InterruptedException {
        Thread.sleep(3000);  // Était 2000ms → maintenant 3000ms

        onView(withText("test_data"))
                .perform(scrollTo(), click());

        Thread.sleep(2000);  // Était 1500ms → maintenant 2000ms

        onView(withId(R.id.tv_status))
                .check(matches(withText("Prêt pour l'analyse")));

        onView(withId(R.id.btn_change_file))
                .perform(click());

        Thread.sleep(2000);  // Était 1500ms → maintenant 2000ms

        onView(withId(R.id.screen_file_selection))
                .check(matches(isDisplayed()));

        onView(withText("test_data"))
                .perform(scrollTo(), click());

        Thread.sleep(2000);  // Était 1500ms → maintenant 2000ms

        onView(withId(R.id.tv_status))
                .check(matches(withText("Prêt pour l'analyse")));
    }

    /**
     * Test de rafraîchissement - CRITIQUE
     *
     * CORRIGÉ : Délais maximaux + animations désactivées
     */
    @Test
    public void testRefreshFileList() throws InterruptedException {
        // AUGMENTÉ AU MAXIMUM : Chargement initial
        Thread.sleep(4000);  // Était 2500ms → maintenant 4000ms

        // Cliquer sur actualiser
        onView(withId(R.id.btn_refresh_files))
                .perform(click());

        // AUGMENTÉ AU MAXIMUM : Rechargement
        Thread.sleep(4000);  // Était 2500ms → maintenant 4000ms

        // Vérifier que les fichiers sont affichés
        onView(withText("test_data"))
                .check(matches(isDisplayed()));
    }

    /**
     * Test de lancement d'animation
     */
    @Test
    public void testAnimationStart() throws InterruptedException {
        Thread.sleep(3000);  // Était 2000ms → maintenant 3000ms

        onView(withText("test_data"))
                .perform(scrollTo(), click());

        Thread.sleep(2000);  // Était 1500ms → maintenant 2000ms

        onView(withId(R.id.btn_launch))
                .check(matches(withText("Lancer")))
                .check(matches(isEnabled()));

        onView(withId(R.id.btn_launch))
                .perform(click());

        Thread.sleep(300);

        onView(withId(R.id.btn_launch))
                .check(matches(withText("Animation...")));

        Thread.sleep(3000);

        onView(withId(R.id.btn_launch))
                .check(matches(withText(containsString("s"))));

        onView(withId(R.id.tv_status))
                .check(matches(isDisplayed()));
    }

    /**
     * Test changement de statut lors de l'animation
     */
    @Test
    public void testAnimationChangesStatus() throws InterruptedException {
        Thread.sleep(3000);  // Était 2000ms → maintenant 3000ms

        onView(withText("test_data"))
                .perform(scrollTo(), click());

        Thread.sleep(2000);  // Était 1500ms → maintenant 2000ms

        onView(withId(R.id.tv_status))
                .check(matches(withText("Prêt pour l'analyse")));

        onView(withId(R.id.btn_launch))
                .perform(click());

        Thread.sleep(2000);

        onView(withId(R.id.tv_status))
                .check(matches(not(withText("Prêt pour l'analyse"))));
    }
}