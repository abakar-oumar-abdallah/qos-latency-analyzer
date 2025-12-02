package com.qos.latency.analyzer.appium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

public class CompleteFlowAppiumTest extends BaseAppiumTest {

    // ==================== CONSTANTES DE TEMPO ====================
    private static final int TEMPO_ECRAN_SELECTION = 5000;      // 5s - Observer l'interface
    private static final int TEMPO_APP_CHARGEMENT = 5000;       // 5s - Attente chargement app
    private static final int TEMPO_AVANT_CLIC = 5000;           // 5s - Préparer l'action
    private static final int TEMPO_NAVIGATION = 2000;           // 2s - Navigation
    private static final int TEMPO_GRAPHIQUE_INITIAL = 5000;    // 5s - Observer l'état vide
    private static final int TEMPO_AVANT_LANCER = 5000;         // 5s - Observer le bouton "Lancer"
    private static final int TEMPO_DEMARRAGE = 1000;            // 1s - Clic sur "Lancer"
    private static final int TEMPO_PREMIERS_PAQUETS = 5000;     // 5s - Voir l'animation commencer
    private static final int TEMPO_GRAPHIQUE_COMPLET = 20000;   // 20s - Analyser tous les points
    private static final int TEMPO_COMPTE_A_REBOURS = 20000;    // 20s - Observer le compte à rebours
    private static final int TEMPO_OBSERVATION_LONGUE = 20000;  // 20s - Examiner en détail
    private static final int TEMPO_ETAT_FINAL = 10000;          // 10s - Voir le résultat final

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Affiche un en-tête formaté dans les logs
     */
    private void printHeader(String title) {
        System.out.println("\n======================================================================");
        System.out.println("  " + title);
        System.out.println("======================================================================");
    }

    /**
     * Affiche une phase du test avec séparateur
     */
    private void printPhase(String phase) {
        System.out.println("\n──────────────────────────────────────────────────────────────────────");
        System.out.println("📍 " + phase);
        System.out.println("──────────────────────────────────────────────────────────────────────");
    }

    /**
     * Affiche un message de succès
     */
    private void printSuccess(String message) {
        System.out.println("✅ " + message);
    }

    /**
     * Affiche un message d'information
     */
    private void printInfo(String message) {
        System.out.println("ℹ️  " + message);
    }

    /**
     * Affiche un message de progression
     */
    private void printProgress(String message) {
        System.out.println("⏩ " + message);
    }

    /**
     * Pause avec affichage
     */
    private void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // ==================== TEST PRINCIPAL ====================

    @Test
    public void testCompleteExecutionFlowWithVisualization() {
        printHeader("DÉMARRAGE DU TEST APPIUM AVEC VISUALISATION");
        printInfo("Durée estimée : 1 minute 45 secondes");
        System.out.println("======================================================================\n");

        // ========== PHASE 1 : ÉCRAN DE SÉLECTION ==========
        printPhase("PHASE 1 : ÉCRAN DE SÉLECTION (5s)");

        By titleLocator = By.id("com.qos.latency.analyzer:id/tv_title");
        String appTitle = waitAndGetText(titleLocator);
        printSuccess("Application lancée : " + appTitle);

        printInfo("Observation de l'interface de sélection...");
        sleep(TEMPO_ECRAN_SELECTION);
        printProgress("✓ Phase 1 terminée\n");

        // ========== PHASE 2 : ATTENTE CHARGEMENT APP ==========
        printPhase("PHASE 2 : ATTENTE CHARGEMENT APP (5s)");
        printInfo("Attente que l'app charge les fichiers depuis les assets...");
        sleep(TEMPO_APP_CHARGEMENT);
        printProgress("✓ Phase 2 terminée\n");

        // ========== PHASE 3 : AVANT CLIC SUR FICHIER ==========
        printPhase("PHASE 3 : AVANT CLIC SUR FICHIER (5s)");
        printInfo("Préparation pour sélectionner test_data.json...");
        sleep(TEMPO_AVANT_CLIC);
        printProgress("✓ Phase 3 terminée\n");

        // ========== PHASE 4 : CLIC ET CHARGEMENT ==========
        printPhase("PHASE 4 : CLIC ET CHARGEMENT (2s)");
        printInfo("Clic sur test_data.json...");

        // Clic sur le fichier test_data.json (sans extension dans le bouton)
        By testDataButton = By.xpath("//android.widget.Button[contains(@text, 'test_data') or contains(@text, 'test data')]");
        waitAndClick(testDataButton);

        printSuccess("Fichier sélectionné");
        sleep(TEMPO_NAVIGATION);
        printProgress("✓ Phase 4 terminée\n");

        // ========== PHASE 5 : GRAPHIQUE INITIAL ==========
        printPhase("PHASE 5 : GRAPHIQUE INITIAL (5s)");

        By statusLocator = By.id("com.qos.latency.analyzer:id/tv_status");
        String status = waitAndGetText(statusLocator);
        printSuccess("Statut actuel : " + status);

        printInfo("Observation du graphique vide...");
        sleep(TEMPO_GRAPHIQUE_INITIAL);
        printProgress("✓ Phase 5 terminée\n");

        // ========== PHASE 6 : AVANT LANCER ==========
        printPhase("PHASE 6 : AVANT LANCER (5s)");

        By launchButtonLocator = By.id("com.qos.latency.analyzer:id/btn_launch");
        String buttonText = waitAndGetText(launchButtonLocator);
        printSuccess("Bouton trouvé : " + buttonText);

        printInfo("Observation du bouton 'Lancer'...");
        sleep(TEMPO_AVANT_LANCER);
        printProgress("✓ Phase 6 terminée\n");

        // ========== PHASE 7 : DÉMARRAGE ANIMATION ==========
        printPhase("PHASE 7 : DÉMARRAGE ANIMATION (1s)");
        printInfo("Clic sur le bouton 'Lancer'...");

        waitAndClick(launchButtonLocator);
        printSuccess("Animation démarrée");

        sleep(TEMPO_DEMARRAGE);
        printProgress("✓ Phase 7 terminée\n");

        // ========== PHASE 8 : PREMIERS PAQUETS ==========
        printPhase("PHASE 8 : PREMIERS PAQUETS (5s)");
        printInfo("Observation des premiers paquets...");

        String statusAnimation = waitAndGetText(statusLocator);
        printInfo("Statut : " + statusAnimation);

        sleep(TEMPO_PREMIERS_PAQUETS);
        printProgress("✓ Phase 8 terminée\n");

        // ========== PHASE 9 : GRAPHIQUE COMPLET ==========
        printPhase("PHASE 9 : GRAPHIQUE COMPLET (20s)");
        printInfo("Analyse de tous les points sur le graphique...");

        sleep(TEMPO_GRAPHIQUE_COMPLET);
        printProgress("✓ Phase 9 terminée\n");

        // ========== PHASE 10 : COMPTE À REBOURS DÉBUT ==========
        printPhase("PHASE 10 : COMPTE À REBOURS DÉBUT (20s)");
        printInfo("⏱Observation du compte à rebours (30s → 10s)...");

        sleep(TEMPO_COMPTE_A_REBOURS);

        String countdown = waitAndGetText(launchButtonLocator);
        printInfo("Temps restant affiché : " + countdown);
        printProgress("✓ Phase 10 terminée\n");

        // ========== PHASE 11 : OBSERVATION LONGUE ==========
        printPhase("PHASE 11 : OBSERVATION LONGUE (20s)");
        printInfo("Examen détaillé du graphique final...");

        sleep(TEMPO_OBSERVATION_LONGUE);
        printProgress("✓ Phase 11 terminée\n");

        // ========== PHASE 12 : ÉTAT FINAL ==========
        printPhase("PHASE 12 : ÉTAT FINAL (10s)");
        printInfo("Vérification de l'état final...");

        sleep(TEMPO_ETAT_FINAL);

        String finalStatus = waitAndGetText(statusLocator);
        String finalButton = waitAndGetText(launchButtonLocator);

        printSuccess("État final du système :");
        printInfo("  → Statut : " + finalStatus);
        printInfo("  → Bouton : " + finalButton);

        printProgress("✓ Phase 12 terminée\n");

        // ========== RÉSUMÉ FINAL ==========
        printHeader("TEST TERMINÉ AVEC SUCCÈS");
        printSuccess("Toutes les 12 phases ont été exécutées");
        printInfo("Durée totale : 105 secondes (1 min 45s)");
        System.out.println("======================================================================\n");
    }
}