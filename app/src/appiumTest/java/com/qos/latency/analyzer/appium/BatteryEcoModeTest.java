package com.qos.latency.analyzer.appium;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

/**
 * Tests Appium pour la fonctionnalité Mode Économie d'Énergie
 *
 * Scénarios testés :
 * 1. Activation automatique du mode éco quand batterie < 60%
 * 2. Non-activation du mode éco quand batterie >= 60%
 * 3. Désactivation manuelle du mode éco
 * 4. Vérification de tous les états système
 */
public class BatteryEcoModeTest extends BaseAppiumTest {

    private static final int ECO_MODE_THRESHOLD = 60;
    private static final String APP_PACKAGE = "com.qos.latency.analyzer";

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        // Réinitialiser les paramètres système avant chaque test
        resetSystemSettings();
    }

    /**
     * TEST 1 : Mode éco s'active automatiquement si batterie < 60%
     */
    @Test
    public void testEcoModeActivatesWhenBatteryLow() throws InterruptedException {
        printHeader("TEST 1 : ACTIVATION AUTOMATIQUE MODE ÉCO (BATTERIE FAIBLE)");

        // ========== PHASE 1 : CONFIGURATION ==========
        printPhase("PHASE 1 : CONFIGURATION DU TEST");

        // Obtenir le niveau de batterie actuel
        int currentBatteryLevel = getCurrentBatteryLevel();
        printInfo("Niveau de batterie réel: " + currentBatteryLevel + "%");

        // Simuler une batterie faible
        printInfo("Simulation d'une batterie à 45%...");
        setBatteryLevel(45);
        Thread.sleep(2000);

        // Vérifier la simulation
        int simulatedLevel = getCurrentBatteryLevel();
        printInfo("Niveau simulé: " + simulatedLevel + "%");
        assertTrue(simulatedLevel < ECO_MODE_THRESHOLD,
                "La simulation doit créer un niveau < 60%");

        printProgress("✓ Phase 1 terminée\n");

        // ========== PHASE 2 : LANCEMENT DE L'APP ==========
        printPhase("PHASE 2 : LANCEMENT DE L'APPLICATION");

        // Arrêter l'app si elle tourne
        driver.terminateApp(APP_PACKAGE);
        Thread.sleep(1000);

        // Relancer l'app
        printInfo("Lancement de l'application...");
        driver.activateApp(APP_PACKAGE);
        Thread.sleep(5000); // Temps pour que l'app détecte la batterie et active le mode éco

        printProgress("✓ Phase 2 terminée\n");

        // ========== PHASE 3 : VÉRIFICATION DU MODE ÉCO ==========
        printPhase("PHASE 3 : VÉRIFICATION DE L'ACTIVATION DU MODE ÉCO");

        // 3.1 - Vérifier le mode avion
        printInfo("Vérification du mode avion...");
        boolean airplaneModeOn = isAirplaneModeEnabled();
        printResult("Mode avion", airplaneModeOn, true);
        assertTrue(airplaneModeOn,
                "Le mode avion devrait être activé automatiquement");

        // 3.2 - Vérifier la localisation
        printInfo("Vérification de la localisation...");
        boolean locationEnabled = isLocationEnabled();
        printResult("Localisation", !locationEnabled, true);
        assertFalse(locationEnabled,
                "La localisation devrait être désactivée");

        // 3.3 - Vérifier le mode économie d'énergie système
        printInfo("Vérification du mode économie système...");
        boolean powerSaveOn = isPowerSaveModeEnabled();
        printResult("Mode économie", powerSaveOn, false); // Facultatif

        printProgress("✓ Phase 3 terminée\n");

        // ========== PHASE 4 : VÉRIFICATION DE L'INTERFACE ==========
        printPhase("PHASE 4 : VÉRIFICATION DE L'INTERFACE UTILISATEUR");

        // Vérifier que le bouton "Désactiver Mode Éco" est visible
        By ecoButtonLocator = By.xpath(
                "//*[contains(@text, 'Désactiver') and contains(@text, 'Mode')]"
        );

        boolean ecoButtonVisible = isElementDisplayed(ecoButtonLocator, 5);
        printResult("Bouton 'Désactiver Mode Éco'", ecoButtonVisible, true);
        assertTrue(ecoButtonVisible,
                "Le bouton de désactivation devrait être visible");

        printProgress("✓ Phase 4 terminée\n");

        // ========== RÉSUMÉ ==========
        printSuccess("✅ TEST 1 RÉUSSI : Mode éco activé automatiquement");
        printSeparator();
    }

    /**
     * TEST 2 : Mode éco ne s'active PAS si batterie >= 60%
     */
    @Test
    public void testEcoModeDoesNotActivateWhenBatterySufficient() throws InterruptedException {
        printHeader("TEST 2 : PAS D'ACTIVATION MODE ÉCO (BATTERIE SUFFISANTE)");

        // ========== PHASE 1 : CONFIGURATION ==========
        printPhase("PHASE 1 : CONFIGURATION DU TEST");

        printInfo("Simulation d'une batterie à 75%...");
        setBatteryLevel(75);
        Thread.sleep(2000);

        int batteryLevel = getCurrentBatteryLevel();
        printInfo("Niveau simulé: " + batteryLevel + "%");
        assertTrue(batteryLevel >= ECO_MODE_THRESHOLD,
                "La batterie doit être >= 60%");

        printProgress("✓ Phase 1 terminée\n");

        // ========== PHASE 2 : LANCEMENT DE L'APP ==========
        printPhase("PHASE 2 : LANCEMENT DE L'APPLICATION");

        driver.terminateApp(APP_PACKAGE);
        Thread.sleep(1000);
        driver.activateApp(APP_PACKAGE);
        Thread.sleep(5000);

        printProgress("✓ Phase 2 terminée\n");

        // ========== PHASE 3 : VÉRIFICATION ==========
        printPhase("PHASE 3 : VÉRIFICATION QUE LE MODE ÉCO N'EST PAS ACTIVÉ");

        // Le mode avion ne doit PAS être activé
        boolean airplaneModeOn = isAirplaneModeEnabled();
        printResult("Mode avion", !airplaneModeOn, true);
        assertFalse(airplaneModeOn,
                "Le mode avion NE devrait PAS être activé");

        // Le bouton de désactivation ne doit PAS être visible
        By ecoButtonLocator = By.xpath(
                "//*[contains(@text, 'Désactiver') and contains(@text, 'Mode')]"
        );

        boolean ecoButtonVisible = isElementDisplayed(ecoButtonLocator, 3);
        printResult("Bouton 'Désactiver Mode Éco'", !ecoButtonVisible, true);
        assertFalse(ecoButtonVisible,
                "Le bouton de désactivation NE devrait PAS être visible");

        printProgress("✓ Phase 3 terminée\n");

        printSuccess("✅ TEST 2 RÉUSSI : Mode éco non activé (batterie suffisante)");
        printSeparator();
    }

    /**
     * TEST 3 : Désactivation manuelle du mode éco
     */
    @Test
    public void testManualEcoModeDeactivation() throws InterruptedException {
        printHeader("TEST 3 : DÉSACTIVATION MANUELLE DU MODE ÉCO");

        // ========== PHASE 1 : ACTIVER LE MODE ÉCO ==========
        printPhase("PHASE 1 : ACTIVATION DU MODE ÉCO");

        setBatteryLevel(40);
        Thread.sleep(2000);

        driver.terminateApp(APP_PACKAGE);
        Thread.sleep(1000);
        driver.activateApp(APP_PACKAGE);
        Thread.sleep(5000);

        // Vérifier que le mode éco est actif
        boolean airplaneModeOn = isAirplaneModeEnabled();
        assertTrue(airplaneModeOn, "Le mode éco doit être actif au départ");

        printProgress("✓ Phase 1 terminée : Mode éco activé\n");

        // ========== PHASE 2 : DÉSACTIVATION MANUELLE ==========
        printPhase("PHASE 2 : DÉSACTIVATION MANUELLE");

        // Trouver et cliquer sur le bouton de désactivation
        By ecoButtonLocator = By.xpath(
                "//*[contains(@text, 'Désactiver') and contains(@text, 'Mode')]"
        );

        printInfo("Recherche du bouton de désactivation...");
        assertTrue(isElementDisplayed(ecoButtonLocator, 5),
                "Le bouton de désactivation doit être visible");

        printInfo("Clic sur le bouton...");
        waitAndClick(ecoButtonLocator);
        Thread.sleep(3000);

        printProgress("✓ Phase 2 terminée\n");

        // ========== PHASE 3 : VÉRIFICATION ==========
        printPhase("PHASE 3 : VÉRIFICATION DE LA DÉSACTIVATION");

        // Le mode avion doit être désactivé
        boolean airplaneModeStillOn = isAirplaneModeEnabled();
        printResult("Mode avion", !airplaneModeStillOn, true);
        assertFalse(airplaneModeStillOn,
                "Le mode avion devrait être désactivé");

        // Le bouton ne doit plus être visible
        boolean ecoButtonStillVisible = isElementDisplayed(ecoButtonLocator, 2);
        printResult("Bouton toujours visible", !ecoButtonStillVisible, true);
        assertFalse(ecoButtonStillVisible,
                "Le bouton ne devrait plus être visible");

        printProgress("✓ Phase 3 terminée\n");

        printSuccess("✅ TEST 3 RÉUSSI : Désactivation manuelle fonctionnelle");
        printSeparator();
    }

    /**
     * TEST 4 : Vérification complète de tous les états système
     */
    @Test
    public void testCompleteSystemStateVerification() throws InterruptedException {
        printHeader("TEST 4 : VÉRIFICATION COMPLÈTE DES ÉTATS SYSTÈME");

        // Activer le mode éco
        setBatteryLevel(35);
        Thread.sleep(2000);

        driver.terminateApp(APP_PACKAGE);
        Thread.sleep(1000);
        driver.activateApp(APP_PACKAGE);
        Thread.sleep(5000);

        // Récupérer tous les états
        printPhase("ÉTATS SYSTÈME");

        int batteryLevel = getCurrentBatteryLevel();
        boolean airplaneMode = isAirplaneModeEnabled();
        boolean powerSave = isPowerSaveModeEnabled();
        boolean location = isLocationEnabled();
        boolean screenOn = isScreenOn();

        printInfo("Batterie: " + batteryLevel + "%");
        printInfo("Mode avion: " + (airplaneMode ? "✓ ACTIVÉ" : "✗ Désactivé"));
        printInfo("Mode économie: " + (powerSave ? "✓ ACTIVÉ" : "○ Désactivé"));
        printInfo("Localisation: " + (location ? "✗ Activée" : "✓ DÉSACTIVÉE"));
        printInfo("Écran: " + (screenOn ? "Allumé" : "Éteint"));

        // Assertions
        assertTrue(batteryLevel < ECO_MODE_THRESHOLD, "Batterie doit être < 60%");
        assertTrue(airplaneMode, "Mode avion doit être activé");
        assertFalse(location, "Localisation doit être désactivée");

        printSuccess("✅ TEST 4 RÉUSSI : Tous les états vérifiés");
        printSeparator();
    }

    // ========================================
    // MÉTHODES UTILITAIRES
    // ========================================

    /**
     * Réinitialise les paramètres système à leur état normal
     */
    private void resetSystemSettings() {
        try {
            printInfo("Réinitialisation des paramètres système...");

            // Désactiver le mode avion
            executeShellCommand("settings put global airplane_mode_on 0");
            executeShellCommand("am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false");

            // Réinitialiser le niveau de batterie
            executeShellCommand("dumpsys battery reset");

            Thread.sleep(1000);
            printInfo("✓ Paramètres réinitialisés");

        } catch (Exception e) {
            printInfo("⚠ Impossible de réinitialiser : " + e.getMessage());
        }
    }

    /**
     * Récupère le niveau de batterie actuel
     */
    private int getCurrentBatteryLevel() {
        try {
            String output = executeShellCommand("dumpsys battery | grep level");

            if (output.contains("level:")) {
                String levelStr = output.split("level:")[1].trim().split("\n")[0].trim();
                return Integer.parseInt(levelStr);
            }
        } catch (Exception e) {
            printInfo("Erreur lecture batterie: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Simule un niveau de batterie spécifique
     */
    private void setBatteryLevel(int level) {
        try {
            executeShellCommand("dumpsys battery set level " + level);
            executeShellCommand("dumpsys battery set status 3"); // 3 = déchargement
        } catch (Exception e) {
            printInfo("Erreur simulation batterie: " + e.getMessage());
        }
    }

    /**
     * Vérifie si le mode avion est activé
     */
    private boolean isAirplaneModeEnabled() {
        try {
            String result = executeShellCommand("settings get global airplane_mode_on");
            return "1".equals(result.trim());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Vérifie si le mode économie d'énergie est activé
     */
    private boolean isPowerSaveModeEnabled() {
        try {
            String result = executeShellCommand("settings get global low_power");
            return "1".equals(result.trim());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Vérifie si la localisation est activée
     */
    private boolean isLocationEnabled() {
        try {
            String result = executeShellCommand("settings get secure location_providers_allowed");
            return result.contains("gps") || result.contains("network");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Vérifie si l'écran est allumé
     */
    private boolean isScreenOn() {
        try {
            String result = executeShellCommand("dumpsys power | grep 'Display Power'");
            return result.contains("state=ON");
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Exécute une commande shell ADB
     */
    private String executeShellCommand(String command) {
        try {
            Map<String, Object> params = Map.of("command", command);
            Object result = driver.executeScript("mobile: shell", params);
            return result != null ? result.toString() : "";
        } catch (Exception e) {
            return "";
        }
    }

    // ========================================
    // MÉTHODES D'AFFICHAGE
    // ========================================

    private void printHeader(String title) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("  " + title);
        System.out.println("=".repeat(80));
    }

    private void printPhase(String phase) {
        System.out.println("\n" + "─".repeat(80));
        System.out.println("📍 " + phase);
        System.out.println("─".repeat(80));
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

    private void printSeparator() {
        System.out.println("=".repeat(80) + "\n");
    }

    private void printResult(String label, boolean actual, boolean expected) {
        String status = (actual == expected) ? "✅" : "❌";
        System.out.println(status + " " + label + ": " +
                (actual ? "OUI" : "NON") +
                " (attendu: " + (expected ? "OUI" : "NON") + ")");
    }
}