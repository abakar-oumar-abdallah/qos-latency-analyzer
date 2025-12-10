package com.qos.latency.analyzer.appium;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests Appium pour la fonctionnalité Mode Économie d'Énergie
 * VERSION FINALE : AppiumBy + androidUIAutomator (Appium 9+)
 */
public class BatteryEcoModeTest extends BaseAppiumTest {

    private WebDriverWait wait;

    @BeforeEach
    public void setUp() throws MalformedURLException, InterruptedException {
        super.setUp();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        resetSystemSettings();
    }

    @Test
    public void testEcoModeActivatesWhenBatteryLow() {
        try {
            // Définir niveau batterie bas (45%)
            setBatteryLevel(45);
            Thread.sleep(2000);

            // Vérifier que la batterie est bien à 45%
            int batteryLevel = getCurrentBatteryLevel();
            assertTrue(batteryLevel == 45, "La batterie devrait être à 45%, mais elle est à " + batteryLevel + "%");

            // Attendre que le dialogue apparaisse
            Thread.sleep(3000);

            // CORRECTION : Utiliser AppiumBy.androidUIAutomator (Appium 9+)
            WebElement dialogTitle = wait.until(ExpectedConditions.presenceOfElementLocated(
                    AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Mode Économie\")")
            ));
            assertNotNull(dialogTitle, "Le dialogue du Mode Économie d'Énergie devrait être affiché");

            // Vérifier le message du dialogue
            WebElement dialogMessage = driver.findElement(
                    AppiumBy.androidUIAutomator("new UiSelector().textContains(\"La batterie est faible\")")
            );
            assertNotNull(dialogMessage, "Le message d'avertissement devrait être affiché");

            // Cliquer sur OK pour activer le mode économie
            WebElement okButton = wait.until(ExpectedConditions.elementToBeClickable(
                    AppiumBy.androidUIAutomator("new UiSelector().text(\"OK\")")
            ));
            okButton.click();
            Thread.sleep(2000);

            // Vérifier que le mode avion est activé
            assertTrue(isAirplaneModeEnabled(), "Le mode avion devrait être activé");

            // Vérifier que le bouton de désactivation est visible
            WebElement deactivateButton = wait.until(ExpectedConditions.presenceOfElementLocated(
                    AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"com.qos.latency.analyzer:id/deactivateButton\")")
            ));
            assertTrue(deactivateButton.isDisplayed(), "Le bouton de désactivation devrait être visible");

        } catch (Exception e) {
            fail("Test échoué : " + e.getMessage());
        }
    }

    @Test
    public void testEcoModeDoesNotActivateWhenBatterySufficient() {
        try {
            // Définir niveau batterie suffisant (75%)
            setBatteryLevel(75);
            Thread.sleep(2000);

            // Vérifier que la batterie est bien à 75%
            int batteryLevel = getCurrentBatteryLevel();
            assertTrue(batteryLevel == 75, "La batterie devrait être à 75%, mais elle est à " + batteryLevel + "%");

            // Attendre un peu pour s'assurer qu'aucun dialogue n'apparaît
            Thread.sleep(3000);

            // CORRECTION : Utiliser AppiumBy.androidUIAutomator
            try {
                driver.findElement(
                        AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Mode Économie\")")
                );
                fail("Le dialogue du Mode Économie d'Énergie ne devrait PAS être affiché");
            } catch (org.openqa.selenium.NoSuchElementException e) {
                // C'est le comportement attendu - le dialogue ne doit pas apparaître
            }

            // Vérifier que l'écran de sélection de jeu est affiché
            WebElement gameSelectionTitle = driver.findElement(
                    AppiumBy.androidUIAutomator("new UiSelector().text(\"Sélectionner un jeu\")")
            );
            assertNotNull(gameSelectionTitle, "L'écran de sélection de jeu devrait être affiché");

            // Vérifier que le mode avion n'est PAS activé
            assertFalse(isAirplaneModeEnabled(), "Le mode avion ne devrait PAS être activé");

        } catch (Exception e) {
            fail("Test échoué : " + e.getMessage());
        }
    }

    @Test
    public void testManualEcoModeDeactivation() {
        try {
            // Définir niveau batterie bas (40%)
            setBatteryLevel(40);
            Thread.sleep(2000);

            // Attendre que le dialogue apparaisse
            Thread.sleep(3000);

            // CORRECTION : Utiliser AppiumBy.androidUIAutomator
            WebElement dialogTitle = wait.until(ExpectedConditions.presenceOfElementLocated(
                    AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Mode Économie\")")
            ));
            assertNotNull(dialogTitle, "Le dialogue du Mode Économie d'Énergie devrait être affiché");

            // Cliquer sur OK pour activer le mode économie
            WebElement okButton = wait.until(ExpectedConditions.elementToBeClickable(
                    AppiumBy.androidUIAutomator("new UiSelector().text(\"OK\")")
            ));
            okButton.click();
            Thread.sleep(2000);

            // Vérifier que le mode avion est activé
            assertTrue(isAirplaneModeEnabled(), "Le mode avion devrait être activé");

            // Cliquer sur le bouton de désactivation
            WebElement deactivateButton = wait.until(ExpectedConditions.elementToBeClickable(
                    AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"com.qos.latency.analyzer:id/deactivateButton\")")
            ));
            deactivateButton.click();
            Thread.sleep(2000);

            // Vérifier que le mode avion est désactivé
            assertFalse(isAirplaneModeEnabled(), "Le mode avion devrait être désactivé");

        } catch (Exception e) {
            fail("Test échoué : " + e.getMessage());
        }
    }

    @Test
    public void testCompleteSystemStateVerification() {
        try {
            // Définir niveau batterie très bas (35%)
            setBatteryLevel(35);
            Thread.sleep(2000);

            // Attendre que le dialogue apparaisse
            Thread.sleep(3000);

            // CORRECTION : Utiliser AppiumBy.androidUIAutomator
            WebElement dialogTitle = wait.until(ExpectedConditions.presenceOfElementLocated(
                    AppiumBy.androidUIAutomator("new UiSelector().textContains(\"Mode Économie\")")
            ));
            assertNotNull(dialogTitle, "Le dialogue du Mode Économie d'Énergie devrait être affiché");

            // Cliquer sur OK
            WebElement okButton = wait.until(ExpectedConditions.elementToBeClickable(
                    AppiumBy.androidUIAutomator("new UiSelector().text(\"OK\")")
            ));
            okButton.click();
            Thread.sleep(2000);

            // Vérifications complètes de l'état du système
            assertTrue(isAirplaneModeEnabled(), "Mode avion devrait être activé");
            assertFalse(isLocationEnabled(), "Localisation devrait être désactivée");

            // Vérifier le niveau de batterie
            int batteryLevel = getCurrentBatteryLevel();
            assertTrue(batteryLevel <= 60, "Mode éco devrait être actif pour batterie <= 60%");

            // Vérifier l'interface utilisateur
            WebElement deactivateButton = driver.findElement(
                    AppiumBy.androidUIAutomator("new UiSelector().resourceId(\"com.qos.latency.analyzer:id/deactivateButton\")")
            );
            assertTrue(deactivateButton.isDisplayed(), "Bouton de désactivation devrait être visible");
            assertTrue(deactivateButton.isEnabled(), "Bouton de désactivation devrait être actif");

        } catch (Exception e) {
            fail("Test échoué : " + e.getMessage());
        }
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Récupère le niveau de batterie actuel du dispositif
     */
    private int getCurrentBatteryLevel() {
        try {
            String output = executeShellCommand("dumpsys battery | grep level");
            String[] lines = output.split("\n");
            for (String line : lines) {
                if (line.trim().startsWith("level:")) {
                    String levelStr = line.split(":")[1].trim();
                    return Integer.parseInt(levelStr);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture du niveau de batterie: " + e.getMessage());
        }
        return 100; // Valeur par défaut
    }

    /**
     * Définit le niveau de batterie du dispositif pour les tests
     */
    private void setBatteryLevel(int level) {
        try {
            executeShellCommand("dumpsys battery set level " + level);
            executeShellCommand("dumpsys battery set status 3"); // 3 = discharging
        } catch (Exception e) {
            System.err.println("Erreur lors de la définition du niveau de batterie: " + e.getMessage());
        }
    }

    /**
     * Vérifie si le mode avion est activé
     */
    private boolean isAirplaneModeEnabled() {
        try {
            String output = executeShellCommand("settings get global airplane_mode_on");
            return output.trim().equals("1");
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification du mode avion: " + e.getMessage());
            return false;
        }
    }

    /**
     * Vérifie si la localisation est activée
     */
    private boolean isLocationEnabled() {
        try {
            String output = executeShellCommand("settings get secure location_mode");
            return !output.trim().equals("0");
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification de la localisation: " + e.getMessage());
            return false;
        }
    }

    /**
     * Exécute une commande shell sur le dispositif
     */
    private String executeShellCommand(String command) {
        try {
            Map<String, Object> args = new HashMap<>();
            args.put("command", command);
            Object result = ((AndroidDriver) driver).executeScript("mobile: shell", args);
            return result != null ? result.toString() : "";
        } catch (Exception e) {
            System.err.println("Erreur lors de l'exécution de la commande: " + command);
            throw new RuntimeException(e);
        }
    }

    /**
     * Réinitialise les paramètres système avant chaque test
     */
    private void resetSystemSettings() {
        try {
            executeShellCommand("dumpsys battery reset");
            Thread.sleep(1000);
            executeShellCommand("settings put global airplane_mode_on 0");
            executeShellCommand("am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false");
            Thread.sleep(1000);
        } catch (Exception e) {
            System.err.println("Erreur lors de la réinitialisation des paramètres: " + e.getMessage());
        }
    }
}