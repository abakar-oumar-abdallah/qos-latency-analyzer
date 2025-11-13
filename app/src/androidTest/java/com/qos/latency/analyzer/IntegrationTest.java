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

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Test du flux complet : Démarrage → Sélection → Animation
     *
     * CORRIGÉ : Délais augmentés pour éviter les échecs sur CI/CD
     */
    @Test
    public void testCompleteUserFlow() throws InterruptedException {
        // Vérifier l'écran de démarrage
        onView(withId(R.id.screen_file_selection))
                .check(matches(isDisplayed()));

        // AUGMENTÉ : Attendre que la liste des fichiers soit complètement chargée
        Thread.sleep(2500);  // Était 1000ms → maintenant 2500ms

        // Sélectionner un fichier
        onView(withText("test_data"))
                .check(matches(isDisplayed()))
                .perform(scrollTo(), click());

        // Attendre la navigation et le chargement
        Thread.sleep(1500);  // Était 1000ms → maintenant 1500ms

        // Vérifier la navigation vers l'écran d'animation
        onView(withId(R.id.screen_animation))
                .check(matches(isDisplayed()));

        // Vérifier le statut
        onView(withId(R.id.tv_status))
                .check(matches(withText("Prêt pour l'analyse")));

        // Vérifier que le graphique est visible
        onView(withId(R.id.chart_view))
                .check(matches(isDisplayed()));

        // Vérifier que le bouton de lancement est prêt
        onView(withId(R.id.btn_launch))
                .check(matches(isDisplayed()))
                .check(matches(withText("Lancer")));
    }

    /**
     * Test du flux : Sélection → Animation → Retour → Nouvelle sélection
     */
    @Test
    public void testMultipleFileSelections() throws InterruptedException {
        // Attendre le chargement initial
        Thread.sleep(2000);  // Était 1000ms → maintenant 2000ms

        // Première sélection
        onView(withText("test_data"))
                .perform(scrollTo(), click());

        Thread.sleep(1500);  // Était 1000ms → maintenant 1500ms

        onView(withId(R.id.tv_status))
                .check(matches(withText("Prêt pour l'analyse")));

        // Retour à la sélection
        onView(withId(R.id.btn_change_file))
                .perform(click());

        Thread.sleep(1500);  // Était 1000ms → maintenant 1500ms

        onView(withId(R.id.screen_file_selection))
                .check(matches(isDisplayed()));

        // Deuxième sélection (même fichier)
        onView(withText("test_data"))
                .perform(scrollTo(), click());

        Thread.sleep(1500);  // Était 1000ms → maintenant 1500ms

        // Vérifier que tout fonctionne encore
        onView(withId(R.id.tv_status))
                .check(matches(withText("Prêt pour l'analyse")));
    }

    /**
     * Test de rafraîchissement de la liste de fichiers
     *
     * CORRIGÉ : Délais augmentés pour éviter les échecs sur CI/CD
     */
    @Test
    public void testRefreshFileList() throws InterruptedException {
        // AUGMENTÉ : Attendre le chargement initial
        Thread.sleep(2500);  // Était 1000ms → maintenant 2500ms

        // Cliquer sur actualiser
        onView(withId(R.id.btn_refresh_files))
                .perform(click());

        // AUGMENTÉ : Attendre que la liste soit rechargée
        Thread.sleep(2500);  // Était 1000ms → maintenant 2500ms

        // Vérifier que les fichiers sont toujours affichés
        onView(withText("test_data"))
                .check(matches(isDisplayed()));
    }

    /**
     * Test de lancement d'animation (début seulement)
     */
    @Test
    public void testAnimationStart() throws InterruptedException {
        // Attendre le chargement
        Thread.sleep(2000);  // Était 1000ms → maintenant 2000ms

        // Sélectionner un fichier
        onView(withText("test_data"))
                .perform(scrollTo(), click());

        Thread.sleep(1500);  // Était 1000ms → maintenant 1500ms

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
     * Test : Vérifier que l'animation change le statut
     */
    @Test
    public void testAnimationChangesStatus() throws InterruptedException {
        // Attendre le chargement
        Thread.sleep(2000);  // Était 1000ms → maintenant 2000ms

        onView(withText("test_data"))
                .perform(scrollTo(), click());

        Thread.sleep(1500);  // Était 1000ms → maintenant 1500ms

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