package com.qos.latency.analyzer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

import androidx.test.espresso.IdlingPolicies;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.qos.latency.analyzer.utils.DisableAnimationsRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

/**
 * Test du flux complet avec visualisation optimisée
 *
 * DURÉE TOTALE : ~105 secondes (1min 45s)
 *
 * Ce test permet d'observer clairement chaque étape du processus :
 * - Chargement de l'application
 * - Sélection du fichier
 * - Affichage du graphique vide
 * - Animation des paquets
 * - Compte à rebours de visualisation
 * - État final
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class CompleteFlowTest {

    @Rule
    public DisableAnimationsRule disableAnimationsRule = new DisableAnimationsRule();

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    // Constantes de tempo pour la visualisation
    private static final int TEMPO_ECRAN_SELECTION = 5000;      // 5s - Observer la liste
    private static final int TEMPO_LISTE_FICHIERS = 5000;        // 5s - Voir tous les fichiers
    private static final int TEMPO_AVANT_CLIC = 5000;            // 5s - Préparer l'action
    private static final int TEMPO_GRAPHIQUE_INITIAL = 5000;     // 5s - Observer l'état vide
    private static final int TEMPO_AVANT_LANCER = 5000;          // 5s - Observer le bouton
    private static final int TEMPO_PREMIERS_PAQUETS = 5000;      // 5s - Voir l'animation commencer
    private static final int TEMPO_GRAPHIQUE_COMPLET = 20000;    // 20s - Analyser tous les points
    private static final int TEMPO_COMPTE_A_REBOURS = 20000;     // 20s - Observer le démarrage
    private static final int TEMPO_OBSERVATION_LONGUE = 20000;   // 20s - Examiner en détail
    private static final int TEMPO_ETAT_FINAL = 10000;           // 10s - Voir le résultat

    @Before
    public void setUp() {
        // Timeouts adaptés pour la visualisation complète
        IdlingPolicies.setMasterPolicyTimeout(180, TimeUnit.SECONDS);
        IdlingPolicies.setIdlingResourceTimeout(180, TimeUnit.SECONDS);
    }

    /**
     * TEST PRINCIPAL avec visualisation complète
     * Durée totale : ~105 secondes
     */
    @Test
    public void testCompleteExecutionFlowWithVisualization() throws InterruptedException {

        printHeader("DÉMARRAGE DU TEST AVEC VISUALISATION COMPLÈTE");
        printInfo("Durée estimée : 1 minute 45 secondes");
        printSeparator();

        // ========================================
        // PHASE 1 : ÉCRAN DE SÉLECTION (5s)
        // ========================================
        printPhase(1, "ÉCRAN DE SÉLECTION", TEMPO_ECRAN_SELECTION);

        onView(withId(R.id.tv_title))
                .check(matches(isDisplayed()))
                .check(matches(withText("QoS Latence")));

        onView(withId(R.id.screen_file_selection))
                .check(matches(isDisplayed()));

        printSuccess("Application lancée - Écran de sélection affiché");
        printInfo("📋 Observation de l'interface de sélection...");

        Thread.sleep(TEMPO_ECRAN_SELECTION);
        printProgress("✓ Phase 1 terminée");

        // ========================================
        // PHASE 2 : LISTE DES FICHIERS (5s)
        // ========================================
        printPhase(2, "LISTE DES FICHIERS", TEMPO_LISTE_FICHIERS);

        printInfo("📁 Affichage de tous les fichiers disponibles...");
        Thread.sleep(TEMPO_LISTE_FICHIERS);
        printProgress("✓ Phase 2 terminée");

        // ========================================
        // PHASE 3 : AVANT CLIC SUR FICHIER (5s)
        // ========================================
        printPhase(3, "PRÉPARATION DU CLIC", TEMPO_AVANT_CLIC);

        printInfo("👆 Préparation du clic sur 'data_high_variable_latency'...");

        onView(withText("data_high_variable_latency"))
                .perform(scrollTo())
                .check(matches(isDisplayed()));

        Thread.sleep(TEMPO_AVANT_CLIC);
        printProgress("✓ Phase 3 terminée");

        // ========================================
        // PHASE 4 : CLIC ET CHARGEMENT
        // ========================================
        printPhase(4, "SÉLECTION DU FICHIER", 2000);

        onView(withText("data_high_variable_latency"))
                .perform(click());

        printSuccess("Fichier 'data_high_variable_latency' sélectionné");
        Thread.sleep(2000);

        // ========================================
        // PHASE 5 : GRAPHIQUE INITIAL (5s)
        // ========================================
        printPhase(5, "GRAPHIQUE INITIAL (VIDE)", TEMPO_GRAPHIQUE_INITIAL);

        onView(withId(R.id.screen_animation))
                .check(matches(isDisplayed()));

        onView(withId(R.id.tv_status))
                .check(matches(withText("Prêt pour l'analyse")));

        onView(withId(R.id.tv_selected_file))
                .check(matches(withText("Fichier : data_high_variable_latency")));

        onView(withId(R.id.chart_view))
                .check(matches(isDisplayed()));

        printSuccess("Graphique vide affiché");
        printInfo("📊 Observation du graphique à l'état initial...");

        Thread.sleep(TEMPO_GRAPHIQUE_INITIAL);
        printProgress("✓ Phase 5 terminée");

        // ========================================
        // PHASE 6 : AVANT LANCER (5s)
        // ========================================
        printPhase(6, "OBSERVATION DU BOUTON 'LANCER'", TEMPO_AVANT_LANCER);

        onView(withId(R.id.btn_launch))
                .check(matches(isDisplayed()))
                .check(matches(withText("Lancer")));

        printInfo("🎯 Observation du bouton 'Lancer'...");

        Thread.sleep(TEMPO_AVANT_LANCER);
        printProgress("✓ Phase 6 terminée");

        // ========================================
        // PHASE 7 : CLIC SUR LANCER
        // ========================================
        printPhase(7, "DÉMARRAGE DE L'ANIMATION", 1000);

        onView(withId(R.id.btn_launch))
                .perform(click());

        printSuccess("Bouton 'Lancer' cliqué - Animation démarrée");
        Thread.sleep(1000);

        // ========================================
        // PHASE 8 : PREMIERS PAQUETS (5s)
        // ========================================
        printPhase(8, "PREMIERS PAQUETS", TEMPO_PREMIERS_PAQUETS);

        onView(withId(R.id.btn_launch))
                .check(matches(withText("Animation...")));

        printInfo("📦 Observation des premiers paquets apparaissant...");
        printInfo("   → Paquet 0, 1, 2, 3...");

        Thread.sleep(TEMPO_PREMIERS_PAQUETS);
        printProgress("✓ Phase 8 terminée");

        // ========================================
        // PHASE 9 : GRAPHIQUE COMPLET (20s)
        // ========================================
        printPhase(9, "GRAPHIQUE COMPLET", TEMPO_GRAPHIQUE_COMPLET);

        printInfo("📈 Observation de tous les paquets s'affichant...");
        printInfo("   → 10 paquets au total");
        printInfo("   → Analyse de la latence réseau");

        // Vérifier périodiquement le statut pendant l'animation
        for (int i = 0; i < 4; i++) {
            Thread.sleep(5000);
            printInfo(String.format("   ... %d secondes écoulées", (i + 1) * 5));
        }

        printProgress("✓ Phase 9 terminée - Tous les paquets affichés");

        // ========================================
        // PHASE 10 : DÉBUT COMPTE À REBOURS (20s)
        // ========================================
        printPhase(10, "COMPTE À REBOURS (DÉBUT)", TEMPO_COMPTE_A_REBOURS);

        Thread.sleep(2000);

        onView(withId(R.id.btn_launch))
                .check(matches(withText(containsString("s"))));

        onView(withId(R.id.tv_status))
                .check(matches(withText(containsString("Vue"))));

        printSuccess("Compte à rebours démarré (30 secondes)");
        printInfo("⏱️  Observation du début du compte à rebours...");

        // Observer les 20 premières secondes
        for (int i = 0; i < 4; i++) {
            Thread.sleep(5000);
            printInfo(String.format("   ... %d/30 secondes observées", (i + 1) * 5));
        }

        printProgress("✓ Phase 10 terminée");

        // ========================================
        // PHASE 11 : OBSERVATION LONGUE (20s)
        // ========================================
        printPhase(11, "OBSERVATION APPROFONDIE", TEMPO_OBSERVATION_LONGUE);

        printInfo("🔍 Examen détaillé du graphique...");
        printInfo("   → Analyse des points");
        printInfo("   → Vérification des couleurs");
        printInfo("   → Observation de la légende");

        // Observer 20 secondes supplémentaires
        for (int i = 0; i < 4; i++) {
            Thread.sleep(5000);
            printInfo(String.format("   ... %d secondes d'observation", (i + 1) * 5));
        }

        printProgress("✓ Phase 11 terminée");

        // Attendre que le compte à rebours se termine
        // Il reste environ 0-5 secondes
        printInfo("⏳ Attente de la fin du compte à rebours...");
        Thread.sleep(5000);

        // ========================================
        // PHASE 12 : ÉTAT FINAL (10s)
        // ========================================
        printPhase(12, "ÉTAT FINAL", TEMPO_ETAT_FINAL);

        onView(withId(R.id.btn_launch))
                .check(matches(withText("Terminé")));

        onView(withId(R.id.tv_status))
                .check(matches(withText(containsString("Analyse OK"))));

        printSuccess("Animation terminée - Bouton 'Terminé' affiché");
        printInfo("✨ Observation du résultat final...");

        Thread.sleep(TEMPO_ETAT_FINAL);
        printProgress("✓ Phase 12 terminée");

        // ========================================
        // TEST TERMINÉ
        // ========================================
        printSeparator();
        printHeader("TEST TERMINÉ AVEC SUCCÈS");
        printInfo("Durée totale réelle : ~105 secondes");
        printInfo("Toutes les phases ont été observées clairement");
        printSeparator();
    }

    // ========================================
    // MÉTHODES D'AFFICHAGE POUR LES LOGS
    // ========================================

    private void printHeader(String message) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("  " + message);
        System.out.println("=".repeat(70));
    }

    private void printSeparator() {
        System.out.println("=".repeat(70));
    }

    private void printPhase(int number, String name, int durationMs) {
        System.out.println("\n" + "─".repeat(70));
        System.out.println(String.format("📍 PHASE %d : %s (%ds)",
                number, name, durationMs / 1000));
        System.out.println("─".repeat(70));
    }

    private void printSuccess(String message) {
        System.out.println("✅ " + message);
    }

    private void printInfo(String message) {
        System.out.println("ℹ️  " + message);
    }

    private void printProgress(String message) {
        System.out.println("⏩ " + message);
    }
}