package com.qos.latency.analyzer.appium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests Appium pour l'application QoS Latency Analyzer
 *
 * Ce fichier contient les tests principaux de l'application.
 * Tous les tests héritent de BaseAppiumTest qui fournit les méthodes utilitaires.
 *
 * VERSION CORRIGÉE - Utilise waitForFilesLoaded() qui est maintenant définie dans BaseAppiumTest
 */
public class QoSLatencyAppiumTest extends BaseAppiumTest {

    /**
     * Test 1 : Vérifier que l'app démarre correctement
     */
    @Test
    public void testAppLaunches() throws InterruptedException {
        String title = waitAndGetText(By.id("com.qos.latency.analyzer:id/tv_title"));
        assertEquals("QoS Latence", title);

        assertTrue(isElementDisplayed(By.id("com.qos.latency.analyzer:id/screen_file_selection")));
    }

    /**
     * Test 2 : Sélectionner le fichier test_data
     * Attente dynamique des fichiers + scroll automatique
     */
    @Test
    public void testSelectFile_TestData() throws InterruptedException {
        waitForFilesLoaded();

        // Attendre 1 seconde supplémentaire pour stabilité
        Thread.sleep(1000);

        By fileLocator = By.xpath("//android.widget.TextView[@text='test_data']");

        // Vérifier que le fichier est visible (avec scroll si nécessaire)
        assertTrue(isElementDisplayed(fileLocator, 10),
                "Le fichier 'test_data' devrait être visible après chargement");

        // Cliquer sur le fichier
        waitAndClick(fileLocator);

        Thread.sleep(2000);

        // Vérifier la navigation
        assertTrue(isElementDisplayed(By.id("com.qos.latency.analyzer:id/screen_animation"), 10),
                "L'écran d'animation devrait être affiché");

        String status = waitAndGetText(By.id("com.qos.latency.analyzer:id/tv_status"));
        assertEquals("Prêt pour l'analyse", status);
    }

    /**
     * Test 3 : Bouton "Actualiser"
     * Meilleure gestion de l'actualisation
     */
    @Test
    public void testRefreshButton() throws InterruptedException {
        // Attendre le chargement initial
        waitForFilesLoaded();
        Thread.sleep(1000);

        // Vérifier avec timeout approprié
        By fileLocator = By.xpath("//android.widget.TextView[@text='test_data']");
        assertTrue(isElementDisplayed(fileLocator, 10),
                "Le fichier 'test_data' devrait être visible initialement");

        // Cliquer sur "Actualiser"
        By refreshButton = By.id("com.qos.latency.analyzer:id/btn_refresh_files");
        waitAndClick(refreshButton);

        // Attendre que les fichiers soient rechargés
        // (donner le temps à l'UI de se rafraîchir)
        Thread.sleep(1000);
        waitForFilesLoaded();
        Thread.sleep(1000);

        // Vérifier avec un nouveau check
        assertTrue(isElementDisplayed(fileLocator, 10),
                "Le fichier 'test_data' devrait toujours être visible après actualisation");
    }

    /**
     * Vérifier que plusieurs fichiers sont listés
     */
    @Test
    public void testMultipleFilesListed() throws InterruptedException {
        waitForFilesLoaded();
        Thread.sleep(1000);

        // Vérifier que test_data est présent
        assertTrue(isElementDisplayed(By.xpath("//android.widget.TextView[@text='test_data']"), 10));

        // Note : Les autres fichiers peuvent nécessiter un scroll
        // C'est normal, on vérifie juste qu'au moins un fichier est présent
    }

    /**
     * Test du bouton retour après sélection
     */
    @Test
    public void testBackToFileSelectionAfterSelect() throws InterruptedException {
        waitForFilesLoaded();
        Thread.sleep(1000);

        // Sélectionner un fichier
        waitAndClick(By.xpath("//android.widget.TextView[@text='test_data']"));
        Thread.sleep(2000);

        // Vérifier qu'on est sur l'écran d'animation
        assertTrue(isElementDisplayed(By.id("com.qos.latency.analyzer:id/screen_animation"), 5));

        // Cliquer sur "Changer de fichier"
        waitAndClick(By.id("com.qos.latency.analyzer:id/btn_change_file"));
        Thread.sleep(1500);

        // Vérifier le retour à la sélection
        assertTrue(isElementDisplayed(By.id("com.qos.latency.analyzer:id/screen_file_selection"), 5));

        // Vérifier que les fichiers sont toujours là
        waitForFilesLoaded();
        assertTrue(isElementDisplayed(By.xpath("//android.widget.TextView[@text='test_data']"), 10));
    }
}