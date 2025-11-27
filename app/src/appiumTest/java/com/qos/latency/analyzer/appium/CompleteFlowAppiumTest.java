package com.qos.latency.analyzer.appium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Appium avec visualisation complète
 *
 * DURÉE TOTALE : ~105 secondes (1min 45s)
 *
 * Permet d'observer clairement toutes les étapes du flux d'exécution
 */
public class CompleteFlowAppiumTest extends BaseAppiumTest {

    // Constantes de tempo pour la visualisation
    private static final int TEMPO_ECRAN_SELECTION = 5000;      // 5s
    private static final int TEMPO_LISTE_FICHIERS = 5000;        // 5s
    private static final int TEMPO_AVANT_CLIC = 5000;            // 5s
    private static final int TEMPO_GRAPHIQUE_INITIAL = 5000;     // 5s
    private static final int TEMPO_AVANT_LANCER = 5000;          // 5s
    private static final int TEMPO_PREMIERS_PAQUETS = 5000;      // 5s
    private static final int TEMPO_GRAPHIQUE_COMPLET = 20000;    // 20s
    private static final int TEMPO_COMPTE_A_REBOURS = 20000;     // 20s
    private static final int TEMPO_OBSERVATION_LONGUE = 20000;   // 20s
    private static final int TEMPO_ETAT_FINAL = 10000;           // 10s

    @Test
    public void testCompleteExecutionFlowWithVisualization() throws InterruptedException {

        printHeader("DÉMARRAGE DU TEST APPIUM AVEC VISUALISATION");
        printInfo("Durée estimée : 1 minute 45 secondes");
        printSeparator();

        // ========================================
        // PHASE 1 : ÉCRAN DE SÉLECTION (5s)
        // ========================================
        printPhase(1, "ÉCRAN DE SÉLECTION", TEMPO_ECRAN_SELECTION);

        By titleLocator = By.id("com.qos.latency.analyzer:id/tv_title");
        String title = waitAndGetText(titleLocator);

        assertEquals("QoS Latence", title);
        printSuccess("Application lancée : " + title);

        By selectionScreenLocator = By.id("com.qos.latency.analyzer:id/screen_file_selection");
        assertTrue(isElementDisplayed(selectionScreenLocator));

        printInfo("📋 Observation de l'interface de sélection...");
        Thread.sleep(TEMPO_ECRAN_SELECTION);
        printProgress("✓ Phase 1 terminée");

        // ========================================
        // PHASE 2 : LISTE DES FICHIERS (5s)
        // ========================================
        printPhase(2, "LISTE DES FICHIERS", TEMPO_LISTE_FICHIERS);

        printInfo("📁 Chargement et affichage des fichiers...");
        waitForFilesLoaded();

        printInfo("   Fichiers disponibles chargés");
        Thread.sleep(TEMPO_LISTE_FICHIERS);
        printProgress("✓ Phase 2 terminée");

        // ========================================
        // PHASE 3 : AVANT CLIC SUR FICHIER (5s)
        // ========================================
        printPhase(3, "PRÉPARATION DU CLIC", TEMPO_AVANT_CLIC);

        By fileLocator = By.xpath("//android.widget.TextView[@text='test_data']");
        assertTrue(isElementDisplayed(fileLocator, 10));

        printInfo("👆 Fichier 'test_data' visible - Préparation du clic...");
        Thread.sleep(TEMPO_AVANT_CLIC);
        printProgress("✓ Phase 3 terminée");

        // ========================================
        // PHASE 4 : CLIC ET CHARGEMENT
        // ========================================
        printPhase(4, "SÉLECTION DU FICHIER", 2000);

        waitAndClick(fileLocator);
        printSuccess("Fichier 'test_data' sélectionné");
        Thread.sleep(2000);

        // ========================================
        // PHASE 5 : GRAPHIQUE INITIAL (5s)
        // ========================================
        printPhase(5, "GRAPHIQUE INITIAL (VIDE)", TEMPO_GRAPHIQUE_INITIAL);

        By animationScreenLocator = By.id("com.qos.latency.analyzer:id/screen_animation");
        assertTrue(isElementDisplayed(animationScreenLocator, 10));

        By statusLocator = By.id("com.qos.latency.analyzer:id/tv_status");
        String status = waitAndGetText(statusLocator);
        assertEquals("Prêt pour l'analyse", status);

        By chartLocator = By.id("com.qos.latency.analyzer:id/chart_view");
        assertTrue(isElementDisplayed(chartLocator));

        printSuccess("Graphique vide affiché - Statut : " + status);
        printInfo("📊 Observation du graphique à l'état initial...");

        Thread.sleep(TEMPO_GRAPHIQUE_INITIAL);
        printProgress("✓ Phase 5 terminée");

        // ========================================
        // PHASE 6 : AVANT LANCER (5s)
        // ========================================
        printPhase(6, "OBSERVATION DU BOUTON 'LANCER'", TEMPO_AVANT_LANCER);

        By launchButtonLocator = By.id("com.qos.latency.analyzer:id/btn_launch");
        String buttonText = waitAndGetText(launchButtonLocator);
        assertEquals("Lancer", buttonText);

        printInfo("🎯 Bouton 'Lancer' affiché et prêt");
        Thread.sleep(TEMPO_AVANT_LANCER);
        printProgress("✓ Phase 6 terminée");

        // ========================================
        // PHASE 7 : CLIC SUR LANCER
        // ========================================
        printPhase(7, "DÉMARRAGE DE L'ANIMATION", 1000);

        waitAndClick(launchButtonLocator);
        printSuccess("Bouton 'Lancer' cliqué - Animation démarrée");
        Thread.sleep(1000);

        // ========================================
        // PHASE 8 : PREMIERS PAQUETS (5s)
        // ========================================
        printPhase(8, "PREMIERS PAQUETS", TEMPO_PREMIERS_PAQUETS);

        buttonText = waitAndGetText(launchButtonLocator);
        assertEquals("Animation...", buttonText);

        printInfo("📦 Animation en cours : " + buttonText);
        printInfo("   Observation des premiers paquets...");

        Thread.sleep(TEMPO_PREMIERS_PAQUETS);
        printProgress("✓ Phase 8 terminée");

        // ========================================
        // PHASE 9 : GRAPHIQUE COMPLET (20s)
        // ========================================
        printPhase(9, "GRAPHIQUE COMPLET", TEMPO_GRAPHIQUE_COMPLET);

        printInfo("📈 Observation de tous les paquets s'affichant...");

        for (int i = 0; i < 4; i++) {
            Thread.sleep(5000);
            printInfo(String.format("   ... %d/20 secondes écoulées", (i + 1) * 5));
        }

        printProgress("✓ Phase 9 terminée - Tous les paquets affichés");

        // ========================================
        // PHASE 10 : DÉBUT COMPTE À REBOURS (20s)
        // ========================================
        printPhase(10, "COMPTE À REBOURS (DÉBUT)", TEMPO_COMPTE_A_REBOURS);

        Thread.sleep(2000);

        buttonText = waitAndGetText(launchButtonLocator);
        assertTrue(buttonText.contains("s"));

        status = waitAndGetText(statusLocator);
        assertTrue(status.contains("Vue"));

        printSuccess("Compte à rebours démarré : " + buttonText);
        printInfo("⏱️  Observation des 20 premières secondes...");

        for (int i = 0; i < 4; i++) {
            Thread.sleep(5000);
            String currentButtonText = waitAndGetText(launchButtonLocator);
            printInfo(String.format("   ... %s restantes", currentButtonText));
        }

        printProgress("✓ Phase 10 terminée");

        // ========================================
        // PHASE 11 : OBSERVATION LONGUE (20s)
        // ========================================
        printPhase(11, "OBSERVATION APPROFONDIE", TEMPO_OBSERVATION_LONGUE);

        printInfo("🔍 Examen détaillé du graphique...");

        for (int i = 0; i < 4; i++) {
            Thread.sleep(5000);
            printInfo(String.format("   ... %d/20 secondes d'observation", (i + 1) * 5));
        }

        printProgress("✓ Phase 11 terminée");

        // Attendre la fin du compte à rebours
        printInfo("⏳ Attente de la fin du compte à rebours...");
        Thread.sleep(5000);

        // ========================================
        // PHASE 12 : ÉTAT FINAL (10s)
        // ========================================
        printPhase(12, "ÉTAT FINAL", TEMPO_ETAT_FINAL);

        buttonText = waitAndGetText(launchButtonLocator);
        assertEquals("Terminé", buttonText);

        status = waitAndGetText(statusLocator);
        assertTrue(status.contains("Analyse OK"));

        printSuccess("Animation terminée - Bouton : " + buttonText);
        printSuccess("Statut final : " + status);
        printInfo("✨ Observation du résultat final...");

        Thread.sleep(TEMPO_ETAT_FINAL);
        printProgress("✓ Phase 12 terminée");

        // ========================================
        // TEST TERMINÉ
        // ========================================
        printSeparator();
        printHeader("TEST APPIUM TERMINÉ AVEC SUCCÈS");
        printInfo("Durée totale : ~105 secondes");
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
