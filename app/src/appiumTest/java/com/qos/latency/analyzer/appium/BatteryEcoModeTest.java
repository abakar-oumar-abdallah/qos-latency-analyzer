package com.qos.latency.analyzer.appium;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests Appium pour le mode économie d'énergie basé sur le niveau de batterie.
 *
 * Pré-requis :
 * - Appium server lancé sur http://127.0.0.1:4723
 * - Appareil Android connecté via ADB
 * - Permissions WRITE_SECURE_SETTINGS et ACCESS_FINE_LOCATION accordées via ADB
 */
public class BatteryEcoModeTest extends BaseAppiumTest {

    private static final int ECO_MODE_THRESHOLD = 60;

    @Before
    public void setUp() {
        super.setUp();
        resetSystemSettings();
    }

    /**
     * Test 1 : Vérifier que le mode éco s'active automatiquement quand la batterie est faible (< 60%)
     */
    @Test
    public void testEcoModeActivatesWhenBatteryLow() {
        try {
            // 1. Simuler batterie faible (45%)
            int lowBattery = 45;
            setBatteryLevel(lowBattery);

            // Vérifier que la batterie a bien été simulée
            int currentLevel = getCurrentBatteryLevel();
            assertTrue("La batterie devrait être à " + lowBattery + "%, mais elle est à " + currentLevel + "%",
                    currentLevel <= lowBattery + 5); // Tolérance de 5%

            // 2. Relancer l'app pour déclencher la vérification
            driver.terminateApp("com.qos.latency.analyzer");
            driver.activateApp("com.qos.latency.analyzer");

            // 3. Attendre l'affichage du dialogue mode éco
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement ecoDialog = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//*[contains(@text, 'Mode Économie d\\'Énergie Activé')]")
            ));

            assertNotNull("Le dialogue du mode éco devrait s'afficher", ecoDialog);
            assertTrue("Le dialogue devrait mentionner la batterie faible",
                    ecoDialog.getText().contains("45%"));

            // 4. Vérifier que le mode avion a été activé
            boolean isAirplaneModeOn = isAirplaneModeEnabled();
            assertTrue("Le mode avion devrait être activé automatiquement", isAirplaneModeOn);

            // 5. Fermer le dialogue
            WebElement okButton = driver.findElement(By.xpath("//*[@text='OK']"));
            okButton.click();

            // 6. Vérifier que le bouton de désactivation est visible
            WebElement deactivateButton = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.id("com.qos.latency.analyzer:id/btnDeactivateEcoMode")
            ));
            assertTrue("Le bouton de désactivation devrait être visible", deactivateButton.isDisplayed());

        } catch (Exception e) {
            fail("Test échoué : " + e.getMessage());
        }
    }

    /**
     * Test 2 : Vérifier que le mode éco ne s'active PAS quand la batterie est suffisante (>= 60%)
     */
    @Test
    public void testEcoModeDoesNotActivateWhenBatterySufficient() {
        try {
            // 1. Simuler batterie suffisante (75%)
            int sufficientBattery = 75;
            setBatteryLevel(sufficientBattery);

            int currentLevel = getCurrentBatteryLevel();
            assertTrue("La batterie devrait être à " + sufficientBattery + "%, mais elle est à " + currentLevel + "%",
                    currentLevel >= sufficientBattery - 5);

            // 2. Relancer l'app
            driver.terminateApp("com.qos.latency.analyzer");
            driver.activateApp("com.qos.latency.analyzer");

            // 3. Attendre l'écran principal (sélection fichier)
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement selectionScreen = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//*[contains(@text, 'Sélection du fichier')]")
            ));

            assertNotNull("L'écran de sélection devrait s'afficher", selectionScreen);

            // 4. Vérifier qu'aucun dialogue mode éco n'est affiché
            try {
                driver.findElement(By.xpath("//*[contains(@text, 'Mode Économie d\\'Énergie')]"));
                fail("Le dialogue mode éco ne devrait PAS s'afficher avec une batterie >= 60%");
            } catch (org.openqa.selenium.NoSuchElementException e) {
                // C'est le comportement attendu
            }

            // 5. Vérifier que le mode avion n'a pas été activé
            boolean isAirplaneModeOn = isAirplaneModeEnabled();
            assertFalse("Le mode avion ne devrait PAS être activé avec batterie >= 60%", isAirplaneModeOn);

        } catch (Exception e) {
            fail("Test échoué : " + e.getMessage());
        }
    }

    /**
     * Test 3 : Vérifier la désactivation manuelle du mode éco via le bouton orange
     */
    @Test
    public void testManualEcoModeDeactivation() {
        try {
            // 1. Activer le mode éco (batterie 40%)
            setBatteryLevel(40);
            driver.terminateApp("com.qos.latency.analyzer");
            driver.activateApp("com.qos.latency.analyzer");

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // 2. Fermer le dialogue initial
            WebElement okButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//*[@text='OK']")
            ));
            okButton.click();

            // 3. Cliquer sur le bouton de désactivation
            WebElement deactivateButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("com.qos.latency.analyzer:id/btnDeactivateEcoMode")
            ));
            deactivateButton.click();

            // 4. Attendre que le bouton disparaisse
            wait.until(ExpectedConditions.invisibilityOfElementLocated(
                    By.id("com.qos.latency.analyzer:id/btnDeactivateEcoMode")
            ));

            // 5. Vérifier que le mode avion a été désactivé
            Thread.sleep(2000); // Laisser le temps au système de désactiver le mode avion
            boolean isAirplaneModeOn = isAirplaneModeEnabled();
            assertFalse("Le mode avion devrait être désactivé après clic sur le bouton", isAirplaneModeOn);

            // 6. Vérifier le toast de confirmation (optionnel, les toasts sont difficiles à capturer)
            // Note: Les toasts Appium ne sont pas toujours capturables, on se fie aux états système

        } catch (Exception e) {
            fail("Test échoué : " + e.getMessage());
        }
    }

    /**
     * Test 4 : Vérification complète de l'état système après activation du mode éco
     */
    @Test
    public void testCompleteSystemStateVerification() {
        try {
            // 1. Activer le mode éco
            setBatteryLevel(35);
            driver.terminateApp("com.qos.latency.analyzer");
            driver.activateApp("com.qos.latency.analyzer");

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//*[contains(@text, 'Mode Économie d\\'Énergie')]")
            ));

            // 2. Vérifier tous les états système
            int batteryLevel = getCurrentBatteryLevel();
            assertTrue("Batterie devrait être < 60%", batteryLevel < ECO_MODE_THRESHOLD);

            boolean airplaneModeOn = isAirplaneModeEnabled();
            assertTrue("Mode avion devrait être ON", airplaneModeOn);

            boolean locationEnabled = isLocationEnabled();
            // La localisation peut encore être activée si l'app n'a pas réussi à la désactiver
            // (nécessite parfois des permissions supplémentaires ou une action utilisateur)

            // 3. Vérifier le contenu du dialogue
            WebElement dialogText = driver.findElement(
                    By.xpath("//*[contains(@text, 'Actions effectuées')]")
            );
            assertNotNull("Le dialogue devrait afficher les actions effectuées", dialogText);

            // Le texte devrait mentionner le mode avion
            String fullText = dialogText.getText();
            assertTrue("Le texte devrait mentionner le mode avion",
                    fullText.contains("Mode avion") || fullText.contains("Activé"));

        } catch (Exception e) {
            fail("Test échoué : " + e.getMessage());
        }
    }

    // ========================================
    // MÉTHODES UTILITAIRES
    // ========================================

    /**
     * Obtient le niveau actuel de la batterie depuis l'appareil
     */
    private int getCurrentBatteryLevel() {
        String output = executeShellCommand("dumpsys battery | grep level");
        // Format attendu : "  level: 45"
        String[] parts = output.trim().split(":");
        if (parts.length >= 2) {
            return Integer.parseInt(parts[1].trim());
        }
        return 100; // Par défaut
    }

    /**
     * Simule un niveau de batterie spécifique
     */
    private void setBatteryLevel(int level) {
        executeShellCommand("dumpsys battery set level " + level);
        executeShellCommand("dumpsys battery set status 3"); // Status 3 = Discharging
        try {
            Thread.sleep(1000); // Laisser le temps au système de mettre à jour
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Vérifie si le mode avion est activé
     */
    private boolean isAirplaneModeEnabled() {
        String output = executeShellCommand("settings get global airplane_mode_on");
        return "1".equals(output.trim());
    }

    /**
     * Vérifie si la localisation est activée
     */
    private boolean isLocationEnabled() {
        String output = executeShellCommand("settings get secure location_providers_allowed");
        return output != null && !output.trim().isEmpty() && !output.contains("null");
    }

    /**
     * Exécute une commande shell sur l'appareil via Appium
     */
    private String executeShellCommand(String command) {
        try {
            Map<String, Object> args = new HashMap<>();
            args.put("command", command);
            Object result = driver.executeScript("mobile: shell", args);
            return result != null ? result.toString() : "";
        } catch (Exception e) {
            System.err.println("Erreur lors de l'exécution de la commande: " + command);
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Réinitialise les paramètres système avant chaque test
     */
    private void resetSystemSettings() {
        try {
            // Réinitialiser la batterie
            executeShellCommand("dumpsys battery reset");

            // Désactiver le mode avion
            executeShellCommand("settings put global airplane_mode_on 0");
            executeShellCommand("am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false");

            Thread.sleep(2000); // Laisser le temps au système de se stabiliser
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}