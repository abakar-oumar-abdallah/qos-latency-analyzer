package com.qos.latency.analyzer.appium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests Appium pour l'application QoS Latency Analyzer
 */
public class QoSLatencyAppiumTest extends BaseAppiumTest {

    @Test
    public void testAppLaunches() {
        System.out.println("🚀 Test de lancement de l'application...");

        By titleLocator = By.xpath("//android.widget.TextView[@text='QoS Latence']");
        WebElement titleElement = wait.until(
                org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(titleLocator)
        );

        assertNotNull(titleElement, "Le titre de l'application devrait être visible");
        assertEquals("QoS Latence", titleElement.getText());

        System.out.println("✅ Application lancée avec succès");
    }

    @Test
    public void testSelectFile_TestData() {
        System.out.println("📁 Test de sélection du fichier DATA_HIGH_VARIABLE_LATENCY...");

        waitForFilesLoaded();

        By fileLocator = By.xpath("//android.widget.Button[@text='DATA_HIGH_VARIABLE_LATENCY']");
        boolean fileExists = driver.findElements(fileLocator).size() > 0;

        assertTrue(fileExists, "Le fichier 'DATA_HIGH_VARIABLE_LATENCY' devrait être visible après chargement");

        if (fileExists) {
            waitAndClick(fileLocator, 10);
            System.out.println("✅ Fichier DATA_HIGH_VARIABLE_LATENCY sélectionné");

            sleep(2000);

            By launchButtonLocator = By.xpath("//android.widget.Button[contains(@text, 'Lancer')]");
            boolean launchButtonExists = driver.findElements(launchButtonLocator).size() > 0;
            assertTrue(launchButtonExists, "Le bouton 'Lancer' devrait être visible après sélection");
        }
    }

    @Test
    public void testRefreshButton() {
        System.out.println("🔄 Test du bouton Actualiser...");

        waitForFilesLoaded();

        String testFileName = "DATA_HIGH_VARIABLE_LATENCY";
        By fileLocator = By.xpath("//android.widget.Button[@text='" + testFileName + "']");
        boolean initialFileExists = driver.findElements(fileLocator).size() > 0;

        assertTrue(initialFileExists, "Le fichier 'DATA_HIGH_VARIABLE_LATENCY' devrait être visible initialement");

        By refreshButtonLocator = By.xpath("//android.widget.Button[@text='Actualiser']");
        waitAndClick(refreshButtonLocator, 10);

        System.out.println("✅ Bouton Actualiser cliqué");

        sleep(2000);

        boolean fileStillExists = driver.findElements(fileLocator).size() > 0;
        assertTrue(fileStillExists, "Le fichier devrait toujours être visible après actualisation");

        System.out.println("✅ Test du bouton Actualiser réussi");
    }

    @Test
    public void testMultipleFilesListed() {
        System.out.println("📋 Test de la liste des fichiers...");

        waitForFilesLoaded();

        By fileButtonsLocator = By.xpath("//android.widget.Button[contains(@resource-id, '') and @clickable='true']");

        int fileCount = driver.findElements(fileButtonsLocator).size();

        assertTrue(fileCount >= 7, "Il devrait y avoir au moins 7 fichiers listés (trouvés: " + fileCount + ")");

        boolean testDataExists = driver.findElements(
                By.xpath("//android.widget.Button[@text='DATA_HIGH_VARIABLE_LATENCY']")
        ).size() > 0;

        assertTrue(testDataExists, "Le fichier DATA_HIGH_VARIABLE_LATENCY devrait être dans la liste");

        System.out.println("✅ " + fileCount + " fichiers trouvés dans la liste");
    }

    @Test
    public void testBackToFileSelectionAfterSelect() {
        System.out.println("⬅️ Test du retour à la sélection de fichier...");

        waitForFilesLoaded();

        By testDataLocator = By.xpath("//android.widget.Button[@text='DATA_HIGH_VARIABLE_LATENCY']");
        waitAndClick(testDataLocator, 10);

        System.out.println("✅ Fichier sélectionné, passage à l'écran d'analyse");

        sleep(2000);

        By changeFileButtonLocator = By.xpath("//android.widget.Button[@text='Changer fichier']");
        waitAndClick(changeFileButtonLocator, 10);

        System.out.println("✅ Bouton 'Changer fichier' cliqué");

        sleep(1000);

        By fileSelectionTitleLocator = By.xpath("//android.widget.TextView[@text='Sélection du fichier']");
        boolean backToFileSelection = driver.findElements(fileSelectionTitleLocator).size() > 0;

        assertTrue(backToFileSelection, "Devrait être de retour à l'écran de sélection de fichier");

        System.out.println("✅ Retour à l'écran de sélection réussi");
    }
}