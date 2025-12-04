package com.qos.latency.analyzer.appium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class QoSLatencyAppiumTest extends BaseAppiumTest {

    @Test
    public void testAppLaunches() {
        System.out.println("========================================");
        System.out.println("✅ TEST : Application se lance correctement");
        System.out.println("========================================");

        // Vérifier que le titre de l'application est affiché
        String title = waitAndGetText(By.id("com.qos.latency.analyzer:id/tv_title"));
        assertTrue(title.contains("QoS"), "Le titre devrait contenir 'QoS'");

        assertTrue(isElementDisplayed(By.id("com.qos.latency.analyzer:id/screen_file_selection")));
        System.out.println("✅ Application lancée avec succès");
    }

    @Test
    public void testSelectFile_TestData() {
        System.out.println("========================================");
        System.out.println("✅ TEST : Sélection du fichier test_data.json");
        System.out.println("========================================");

        waitForFilesLoaded();

        // ✅ CORRECTION : Utiliser getFileButtonLocator qui convertit en majuscules
        By fileLocator = getFileButtonLocator("test_data");

        assertTrue(isElementDisplayed(fileLocator, 10),
                "Le fichier 'test_data' devrait être visible après chargement");

        // Cliquer sur le fichier
        waitAndClick(fileLocator, "test_data.json");

        // Vérifier que l'écran d'animation s'affiche
        assertTrue(isElementDisplayed(By.id("com.qos.latency.analyzer:id/screen_animation"), 10),
                "L'écran d'animation devrait être affiché après sélection du fichier");

        String status = waitAndGetText(By.id("com.qos.latency.analyzer:id/tv_status"));
        assertTrue(status.contains("Chargement") || status.contains("Prêt"),
                "Le statut devrait indiquer que le fichier est chargé");

        System.out.println("✅ Fichier test_data.json sélectionné avec succès");
    }

    @Test
    public void testRefreshButton() {
        System.out.println("========================================");
        System.out.println("✅ TEST : Bouton Actualiser");
        System.out.println("========================================");

        waitForFilesLoaded();

        // ✅ CORRECTION : Utiliser getFileButtonLocator
        By fileLocator = getFileButtonLocator("test_data");
        assertTrue(isElementDisplayed(fileLocator, 10),
                "Le fichier 'test_data' devrait être visible initialement");

        // Cliquer sur le bouton Actualiser
        By refreshButton = By.id("com.qos.latency.analyzer:id/btn_refresh_files");
        waitAndClick(refreshButton, "Bouton Actualiser");

        // Vérifier que les fichiers sont toujours affichés après actualisation
        waitForFilesLoaded();

        assertTrue(isElementDisplayed(fileLocator, 10),
                "Le fichier 'test_data' devrait être visible après actualisation");

        System.out.println("✅ Bouton Actualiser fonctionne correctement");
    }

    @Test
    public void testMultipleFilesListed() {
        System.out.println("========================================");
        System.out.println("✅ TEST : Vérifier que plusieurs fichiers sont listés");
        System.out.println("========================================");

        waitForFilesLoaded();

        // ✅ CORRECTION : Utiliser getFileButtonLocator
        assertTrue(isElementDisplayed(getFileButtonLocator("test_data"), 10));
        assertTrue(isElementDisplayed(getFileButtonLocator("data_all"), 10));

        System.out.println("✅ Plusieurs fichiers sont listés correctement");
    }

    @Test
    public void testBackToFileSelectionAfterSelect() {
        System.out.println("========================================");
        System.out.println("✅ TEST : Retour à la sélection de fichier");
        System.out.println("========================================");

        waitForFilesLoaded();

        // ✅ CORRECTION : Utiliser getFileButtonLocator
        By fileLocator = getFileButtonLocator("test_data");
        waitAndClick(fileLocator, "test_data.json");

        // Vérifier que l'écran d'animation s'affiche
        assertTrue(isElementDisplayed(By.id("com.qos.latency.analyzer:id/screen_animation"), 5));

        // Cliquer sur le bouton "Changer de fichier"
        waitAndClick(By.id("com.qos.latency.analyzer:id/btn_change_file"), "Bouton Changer de fichier");

        // Vérifier que l'écran de sélection est de nouveau affiché
        assertTrue(isElementDisplayed(By.id("com.qos.latency.analyzer:id/screen_file_selection"), 5));

        // Vérifier que les fichiers sont toujours listés
        assertTrue(isElementDisplayed(fileLocator, 10));

        System.out.println("✅ Retour à la sélection de fichier fonctionne correctement");
    }
}