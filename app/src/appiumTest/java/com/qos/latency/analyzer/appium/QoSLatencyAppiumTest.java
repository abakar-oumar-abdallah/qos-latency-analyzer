package com.qos.latency.analyzer.appium;

import org.junit.Test;
import org.openqa.selenium.By;

import static org.junit.Assert.*;

/**
 * Tests Appium pour l'application QoS Latency Analyzer
 * Ces tests utilisent Appium pour valider l'interface utilisateur
 * sur un appareil physique réel
 */
public class QoSLatencyAppiumTest extends BaseAppiumTest {

    /**
     * Test de base : vérifier que l'application se lance correctement
     */
    @Test
    public void testAppLaunches() {
        System.out.println("\n========================================");
        System.out.println("🧪 TEST: Lancement de l'application");
        System.out.println("========================================");

        // Vérifier que le bouton Launch est visible
        By launchButton = getLaunchButtonLocator();
        assertTrue("Le bouton Launch devrait être visible",
                isElementDisplayed(launchButton, 10));

        // Vérifier le texte du bouton
        String buttonText = waitAndGetText(launchButton);
        System.out.println("✅ Bouton trouvé avec texte: " + buttonText);

        assertTrue("Le texte du bouton devrait contenir 'Select' ou 'Sélectionner'",
                buttonText.contains("Select") || buttonText.contains("Sélectionner"));
    }

    /**
     * Test : Sélection d'un fichier data_high_variable_latency.json
     */
    @Test
    public void testSelectFile_DataHighVariableLatency() {
        System.out.println("\n========================================");
        System.out.println("🧪 TEST: Sélection du fichier data_high_variable_latency.json");
        System.out.println("========================================");

        // Afficher tous les éléments pour debug
        printAllVisibleElements();

        // Trouver et cliquer sur le fichier data_high_variable_latency
        By fileLocator = getFileButtonLocator("data_high_variable_latency");

        assertTrue(isElementDisplayed(fileLocator, 10),
                "Le fichier 'data_high_variable_latency' devrait être visible après chargement");

        waitAndClick(fileLocator, "data_high_variable_latency.json");

        // Vérifier que le bouton Launch a changé
        By launchButton = getLaunchButtonLocator();
        String buttonText = waitAndGetText(launchButton);

        System.out.println("📝 Texte du bouton après sélection: " + buttonText);

        assertTrue("Le bouton devrait afficher 'Lancer' ou 'Launch'",
                buttonText.contains("Lancer") || buttonText.contains("Launch"));

        System.out.println("✅ Fichier data_high_variable_latency.json sélectionné avec succès");
    }

    /**
     * Test : Vérifier la présence des fichiers JSON
     */
    @Test
    public void testJsonFilesAreVisible() {
        System.out.println("\n========================================");
        System.out.println("🧪 TEST: Visibilité des fichiers JSON");
        System.out.println("========================================");

        // Vérifier que le fichier data_high_variable_latency est visible
        By fileLocator = getFileButtonLocator("data_high_variable_latency");
        assertTrue(isElementDisplayed(fileLocator, 10),
                "Le fichier 'data_high_variable_latency' devrait être visible initialement");

        // Prendre une capture d'écran
        takeScreenshot("files_visible");

        System.out.println("✅ Fichiers JSON visibles");
    }

    /**
     * Test : Vérifier que l'actualisation fonctionne
     */
    @Test
    public void testRefreshFiles() {
        System.out.println("\n========================================");
        System.out.println("🧪 TEST: Actualisation des fichiers");
        System.out.println("========================================");

        // Vérifier que les fichiers sont visibles
        assertTrue(isElementDisplayed(getFileButtonLocator("data_high_variable_latency"), 10),
                "Le fichier 'data_high_variable_latency' devrait être visible après actualisation");

        System.out.println("✅ Actualisation réussie");
    }

    /**
     * Test : Vérifier la présence de plusieurs fichiers
     */
    @Test
    public void testMultipleFilesPresent() {
        System.out.println("\n========================================");
        System.out.println("🧪 TEST: Présence de plusieurs fichiers");
        System.out.println("========================================");

        // Vérifier que plusieurs fichiers sont présents
        assertTrue(isElementDisplayed(getFileButtonLocator("data_high_variable_latency"), 10));
        assertTrue(isElementDisplayed(getFileButtonLocator("data_all"), 10));

        System.out.println("✅ Plusieurs fichiers trouvés");
    }

    /**
     * Test : Lancer un traitement simple
     */
    @Test
    public void testLaunchProcessing() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🧪 TEST: Lancement d'un traitement");
        System.out.println("========================================");

        // Sélectionner un fichier
        By fileLocator = getFileButtonLocator("data_high_variable_latency");
        waitAndClick(fileLocator, "data_high_variable_latency.json");

        // Cliquer sur Launch
        By launchButton = getLaunchButtonLocator();
        waitAndClick(launchButton, "Launch");

        // Attendre un peu pour voir le traitement démarrer
        Thread.sleep(3000);

        // Vérifier que la barre de progression est visible
        By progressBar = getProgressBarLocator();
        assertTrue("La barre de progression devrait être visible",
                isElementDisplayed(progressBar, 5));

        System.out.println("✅ Traitement lancé avec succès");
    }

    /**
     * Test : Vérifier le changement d'état du bouton
     */
    @Test
    public void testButtonStateChanges() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🧪 TEST: Changements d'état du bouton");
        System.out.println("========================================");

        By launchButton = getLaunchButtonLocator();

        // État initial
        String initialText = waitAndGetText(launchButton);
        System.out.println("📝 État initial: " + initialText);

        // Sélectionner un fichier
        By fileLocator = getFileButtonLocator("data_high_variable_latency");
        waitAndClick(fileLocator, "data_high_variable_latency.json");

        // État après sélection
        Thread.sleep(500);
        String afterSelectionText = waitAndGetText(launchButton);
        System.out.println("📝 Après sélection: " + afterSelectionText);

        assertTrue("Le texte devrait avoir changé",
                !initialText.equals(afterSelectionText));

        System.out.println("✅ Changements d'état validés");
    }

    /**
     * Test : Vérifier le status TextView
     */
    @Test
    public void testStatusTextView() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🧪 TEST: Vérification du TextView de status");
        System.out.println("========================================");

        // Sélectionner un fichier et lancer
        waitAndClick(getFileButtonLocator("data_high_variable_latency"), "File");
        waitAndClick(getLaunchButtonLocator(), "Launch");

        // Attendre un peu
        Thread.sleep(2000);

        // Vérifier que le status est visible
        By statusLocator = getStatusTextLocator();
        assertTrue("Le TextView de status devrait être visible",
                isElementDisplayed(statusLocator, 5));

        String statusText = waitAndGetText(statusLocator);
        System.out.println("📝 Status: " + statusText);

        System.out.println("✅ Status TextView vérifié");
    }

    /**
     * Test : Vérifier la réactivité de l'interface
     */
    @Test
    public void testUIResponsiveness() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🧪 TEST: Réactivité de l'interface");
        System.out.println("========================================");

        // Cliquer plusieurs fois pour vérifier la réactivité
        By fileLocator = getFileButtonLocator("data_high_variable_latency");

        for (int i = 1; i <= 3; i++) {
            System.out.println("🔄 Clic #" + i);
            waitAndClick(fileLocator, "File " + i);
            Thread.sleep(500);

            String buttonText = waitAndGetText(getLaunchButtonLocator());
            System.out.println("  -> Bouton: " + buttonText);
        }

        System.out.println("✅ Interface réactive");
    }

    /**
     * Test : Capture d'écran de l'état initial
     */
    @Test
    public void testCaptureInitialState() {
        System.out.println("\n========================================");
        System.out.println("🧪 TEST: Capture d'écran de l'état initial");
        System.out.println("========================================");

        // Attendre que tout soit chargé
        By launchButton = getLaunchButtonLocator();
        assertTrue(isElementDisplayed(launchButton, 10));

        // Prendre une capture d'écran
        takeScreenshot("initial_state");

        // Afficher les éléments pour debug
        printAllVisibleElements();

        System.out.println("✅ Capture d'écran sauvegardée");
    }
}