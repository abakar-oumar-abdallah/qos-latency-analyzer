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

import androidx.test.espresso.Espresso;
import androidx.test.espresso.IdlingPolicies;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.qos.latency.analyzer.utils.DisableAnimationsRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

/**
 * Tests d'intégration complets - VERSION CORRIGÉE
 *
 * PRINCIPALES :
 * - Augmentation des timeouts IdlingResource
 * - Meilleure gestion des attentes avec waitForView()
 * - Utilisation de perform() au lieu de check() avant les actions
 * - Gestion des éléments qui nécessitent un scroll
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class IntegrationTest {

    @Rule
    public DisableAnimationsRule disableAnimationsRule = new DisableAnimationsRule();

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Configuration des politiques d'attente Espresso
     */
    @Before
    public void setUp() {
        // Augmenter les timeouts Espresso pour la CI/CD
        IdlingPolicies.setMasterPolicyTimeout(60, TimeUnit.SECONDS);
        IdlingPolicies.setIdlingResourceTimeout(60, TimeUnit.SECONDS);
    }

    /**
     * Helper pour attendre qu'une vue soit présente
     */
    private void waitForView(int viewId, long timeoutMillis) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            try {
                onView(withId(viewId)).check(matches(isDisplayed()));
                return; // Vue trouvée
            } catch (Exception e) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Helper pour attendre qu'un texte soit présent
     */
    private void waitForText(String text, long timeoutMillis) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            try {
                onView(withText(text)).check(matches(isDisplayed()));
                return; // Texte trouvé
            } catch (Exception e) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Test du flux complet : Démarrage → Sélection → Animation
     * Utilisation des nouvelles helpers
     */
    @Test
    public void testCompleteUserFlow() throws InterruptedException {
        // Vérifier l'écran de démarrage
        onView(withId(R.id.screen_file_selection))
                .check(matches(isDisplayed()));

        // Attendre avec helper personnalisé
        waitForText("test_data", 10000); // 10 secondes max

        // Forcer Espresso à attendre
        Espresso.onIdle();
        Thread.sleep(1000); // Sécurité supplémentaire

        // D'abord scrollTo, puis check, puis click
        onView(withText("test_data"))
                .perform(scrollTo()); // Scroller d'abord

        Thread.sleep(500); // Laisser le scroll se terminer

        onView(withText("test_data"))
                .check(matches(isDisplayed())) // Vérifier qu'il est visible
                .perform(click()); // Puis cliquer

        // Attendre la navigation avec helper
        waitForView(R.id.screen_animation, 5000);
        Thread.sleep(1000);

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
        waitForText("test_data", 10000);
        Espresso.onIdle();
        Thread.sleep(1000);

        onView(withText("test_data"))
                .perform(scrollTo());
        Thread.sleep(500);

        onView(withText("test_data"))
                .perform(click());

        waitForView(R.id.screen_animation, 5000);
        Thread.sleep(1000);

        onView(withId(R.id.tv_status))
                .check(matches(withText("Prêt pour l'analyse")));

        onView(withId(R.id.btn_change_file))
                .perform(click());

        waitForView(R.id.screen_file_selection, 5000);
        Thread.sleep(1000);

        onView(withId(R.id.screen_file_selection))
                .check(matches(isDisplayed()));

        waitForText("test_data", 10000);
        Thread.sleep(1000);

        onView(withText("test_data"))
                .perform(scrollTo());
        Thread.sleep(500);

        onView(withText("test_data"))
                .perform(click());

        waitForView(R.id.screen_animation, 5000);
        Thread.sleep(1000);

        onView(withId(R.id.tv_status))
                .check(matches(withText("Prêt pour l'analyse")));
    }

    /**
     * Test de rafraîchissement - CRITIQUE
     * Nouvelle approche avec helpers
     */
    @Test
    public void testRefreshFileList() throws InterruptedException {
        // Attendre le chargement initial
        waitForText("test_data", 10000);
        Espresso.onIdle();
        Thread.sleep(1500);

        // Vérifier que le fichier est présent
        onView(withText("test_data"))
                .perform(scrollTo()); // Scroller vers le fichier

        Thread.sleep(500);

        onView(withText("test_data"))
                .check(matches(isDisplayed())); // Vérifier la visibilité

        // Cliquer sur actualiser
        onView(withId(R.id.btn_refresh_files))
                .perform(click());

        // Attendre le rechargement
        Thread.sleep(2000); // Temps pour l'animation de refresh
        Espresso.onIdle(); // Attendre que l'UI soit idle

        // Attendre que les fichiers réapparaissent
        waitForText("test_data", 10000);
        Thread.sleep(1500);

        // Scroller à nouveau si nécessaire
        onView(withText("test_data"))
                .perform(scrollTo());

        Thread.sleep(500);

        // Vérifier que les fichiers sont affichés
        onView(withText("test_data"))
                .check(matches(isDisplayed()));
    }

    /**
     * Test de lancement d'animation
     */
    @Test
    public void testAnimationStart() throws InterruptedException {
        waitForText("test_data", 10000);
        Espresso.onIdle();
        Thread.sleep(1000);

        onView(withText("test_data"))
                .perform(scrollTo());
        Thread.sleep(500);

        onView(withText("test_data"))
                .perform(click());

        waitForView(R.id.screen_animation, 5000);
        Thread.sleep(1000);

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
        waitForText("test_data", 10000);
        Espresso.onIdle();
        Thread.sleep(1000);

        onView(withText("test_data"))
                .perform(scrollTo());
        Thread.sleep(500);

        onView(withText("test_data"))
                .perform(click());

        waitForView(R.id.screen_animation, 5000);
        Thread.sleep(1000);

        onView(withId(R.id.tv_status))
                .check(matches(withText("Prêt pour l'analyse")));

        onView(withId(R.id.btn_launch))
                .perform(click());

        Thread.sleep(2000);

        onView(withId(R.id.tv_status))
                .check(matches(not(withText("Prêt pour l'analyse"))));
    }
}