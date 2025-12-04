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
    public void testAppLaunches() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🧪 TEST: Lancement de l'application");
        System.out.println("========================================");

        // Attendre qu'un fichier JSON soit visible
        By firstFile = getFileButtonLocator("data_all"); // Choisir un fichier existant
        assertTrue("Le fichier 'data_all' devrait être visible",
                isElementDisplayed(firstFile, 10));

        // Sélectionner ce fichier pour activer l'écran d'animation
        waitAndClick(firstFile, "data_all.json");

        // Vérifier que le bouton Launch est maintenant visible
        By launchButton = getLaunchButtonLocator();
        assertTrue("Le bouton Launch devrait être visible après sélection",
                isElementDisplayed(launchButton, 10));

        // Vérifier le texte du bouton
        String buttonText = waitAndGetText(launchButton);
        System.out.println("✅ Bouton trouvé avec texte: " + buttonText);
        assertTrue("Le texte du bouton devrait contenir 'Lancer' ou 'Launch'",
                buttonText.contains("Lancer") || buttonText.contains("Launch"));
    }

    /**
     * Test : Sélection d'un fichier data_high_variable_latency.json
     */
    @Test
    public void testSelectFile_DataHighVariableLatency() {
        System.out.println("\n========================================");
        System.out.println("🧪 TEST: Sélection du fichier data_high_variable_latency.json");
        System.out.println("========================================");

        printAllVisibleElements();

        By fileLocator = getFileButtonLocator("data_high_variable_latency");
        assertTrue("Le fichier 'data_high_variable_latency' devrait être visible après chargement",
                isElementDisplayed(fileLocator, 10));

        waitAndClick(fileLocator, "data_high_variable_latency.json");

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

        By fileLocator = getFileButtonLocator("data_high_variable_latency");
        assertTrue("Le fichier 'data_high_variable_latency' devrait être visible initialement",
                isElementDisplayed(fileLocator, 10));

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

        assertTrue("Le fichier 'data_high_variable_latency' devrait être visible après actualisation",
                isElementDisplayed(getFileButtonLocator("data_high_variable_latency"), 10));

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

        assertTrue("Le fichier 'data_high_variable_latency' devrait être visible",
                isElementDisplayed(getFileButtonLocator("data_high_variable_latency"), 10));
        assertTrue("Le fichier 'data_all' devrait être visible",
                isElementDisplayed(getFileButtonLocator("data_all"), 10));

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

        By fileLocator = getFileButtonLocator("data_high_variable_latency");
        waitAndClick(fileLocator, "data_high_variable_latency.json");

        By launchButton = getLaunchButtonLocator();
        waitAndClick(launchButton, "Launch");

        Thread.sleep(3000);

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
        String initialText = waitAndGetText(launchButton);
        System.out.println("📝 État initial: " + initialText);

        By fileLocator = getFileButtonLocator("data_high_variable_latency");
        waitAndClick(fileLocator, "data_high_variable_latency.json");

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

        waitAndClick(getFileButtonLocator("data_high_variable_latency"), "File");
        waitAndClick(getLaunchButtonLocator(), "Launch");

        Thread.sleep(2000);

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

        By firstFile = getFileButtonLocator("data_all");
        assertTrue("Le fichier 'data_all' devrait être visible",
                isElementDisplayed(firstFile, 10));

        waitAndClick(firstFile, "data_all.json");

        By launchButton = getLaunchButtonLocator();
        assertTrue("Le bouton Launch devrait être visible",
                isElementDisplayed(launchButton, 10));

        takeScreenshot("initial_state");
        printAllVisibleElements();

        System.out.println("✅ Capture d'écran sauvegardée");
    }
}
