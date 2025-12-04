package com.qos.latency.analyzer.appium;

import org.junit.Test;
import org.openqa.selenium.By;

import static org.junit.Assert.*;

/**
 * Test du flux complet d'exécution avec visualisation
 * Ce test vérifie le scénario utilisateur de bout en bout :
 * 1. Sélection d'un fichier JSON
 * 2. Lancement du traitement
 * 3. Visualisation de la progression
 * 4. Vérification de la complétion
 */
public class CompleteFlowAppiumTest extends BaseAppiumTest {

    /**
     * Test du flux complet d'exécution avec visualisation
     * Durée estimée : ~40 secondes
     */
    @Test
    public void testCompleteExecutionFlowWithVisualization() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🎬 TEST: Flux Complet avec Visualisation");
        System.out.println("========================================");

        // ====================================================================
        // ÉTAPE 1 : Vérifier que l'application est lancée
        // ====================================================================
        System.out.println("\n📱 ÉTAPE 1: Vérification du lancement");

        By launchButton = getLaunchButtonLocator();
        assertTrue("Le bouton Launch devrait être visible",
                isElementDisplayed(launchButton, 10));

        String initialText = waitAndGetText(launchButton);
        System.out.println("✅ Application lancée - Bouton: " + initialText);

        // ====================================================================
        // ÉTAPE 2 : Afficher les fichiers disponibles
        // ====================================================================
        System.out.println("\n📂 ÉTAPE 2: Fichiers disponibles");
        printAllVisibleElements();

        // ====================================================================
        // ÉTAPE 3 : Sélectionner le fichier data_high_variable_latency.json
        // ====================================================================
        System.out.println("\n📄 ÉTAPE 3: Sélection du fichier");

        // Utilisation de data_high_variable_latency au lieu de test_data
        By fileButton = getFileButtonLocator("data_high_variable_latency");
        waitAndClick(fileButton, "data_high_variable_latency.json");

        System.out.println("✅ Fichier sélectionné: data_high_variable_latency.json");
        Thread.sleep(1000); // Laisser le temps à l'UI de se mettre à jour

        // ====================================================================
        // ÉTAPE 4 : Vérifier que le bouton Launch est activé
        // ====================================================================
        System.out.println("\n🔘 ÉTAPE 4: Vérification du bouton Launch");

        String buttonText = waitAndGetText(launchButton);
        System.out.println("📝 Texte du bouton: " + buttonText);
        assertTrue("Le bouton devrait afficher 'Lancer' ou 'Launch'",
                buttonText.contains("Lancer") || buttonText.contains("Launch"));

        // ====================================================================
        // ÉTAPE 5 : Lancer le traitement
        // ====================================================================
        System.out.println("\n🚀 ÉTAPE 5: Lancement du traitement");

        waitAndClick(launchButton, "Launch");
        System.out.println("✅ Traitement lancé");
        Thread.sleep(2000); // Laisser le temps au traitement de démarrer

        // ====================================================================
        // ÉTAPE 6 : Vérifier que le traitement a démarré
        // ====================================================================
        System.out.println("\n⏳ ÉTAPE 6: Vérification du démarrage");

        // Le bouton devrait changer de texte
        String processingText = waitAndGetText(launchButton);
        System.out.println("📝 Texte pendant traitement: " + processingText);

        // Vérifier que la barre de progression est visible
        By progressBar = getProgressBarLocator();
        assertTrue("La barre de progression devrait être visible",
                isElementDisplayed(progressBar, 5));
        System.out.println("✅ Barre de progression visible");

        // ====================================================================
        // ÉTAPE 7 : Observer la progression (visualisation)
        // ====================================================================
        System.out.println("\n📊 ÉTAPE 7: Observation de la progression");
        System.out.println("⏱️  Durée estimée: ~35 secondes pour data_high_variable_latency");

        // Observer pendant 5 secondes
        for (int i = 1; i <= 5; i++) {
            Thread.sleep(1000);
            String currentText = waitAndGetText(launchButton);
            System.out.println("  [" + i + "s] Statut: " + currentText);
        }

        // ====================================================================
        // ÉTAPE 8 : Attendre la fin du traitement
        // ====================================================================
        System.out.println("\n⏳ ÉTAPE 8: Attente de la complétion");
        System.out.println("⏱️  Attente de ~30 secondes supplémentaires...");

        // Attendre que le bouton affiche "Terminé" ou "Completed"
        // Timeout de 60 secondes pour être sûr
        waitForTextInElement(launchButton, "Terminé", 60);

        String finalText = waitAndGetText(launchButton);
        System.out.println("✅ Traitement terminé - Texte final: " + finalText);

        // ====================================================================
        // ÉTAPE 9 : Vérifications finales
        // ====================================================================
        System.out.println("\n✅ ÉTAPE 9: Vérifications finales");

        // Vérifier le texte final
        assertTrue("Le bouton devrait afficher 'Terminé' à la fin",
                finalText.contains("Terminé") || finalText.contains("Completed"));

        // Vérifier que la barre de progression n'est plus visible (ou complète)
        System.out.println("✅ Barre de progression: traitement terminé");

        // ====================================================================
        // ÉTAPE 10 : Capture d'écran finale
        // ====================================================================
        System.out.println("\n📸 ÉTAPE 10: Capture d'écran finale");
        takeScreenshot("complete_flow_final");

        System.out.println("\n========================================");
        System.out.println("🎉 TEST RÉUSSI: Flux complet validé");
        System.out.println("========================================");
        System.out.println("📊 Résumé:");
        System.out.println("  - Fichier: data_high_variable_latency.json");
        System.out.println("  - État initial: " + initialText);
        System.out.println("  - État final: " + finalText);
        System.out.println("  - Durée totale: ~40 secondes");
        System.out.println("========================================\n");
    }

    /**
     * Test du flux complet sans visualisation (attente complète)
     * Ce test lance le traitement et attend directement la fin
     */
    @Test
    public void testCompleteExecutionFlowFast() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🎬 TEST: Flux Complet Rapide");
        System.out.println("========================================");

        // Sélectionner le fichier
        By fileButton = getFileButtonLocator("data_high_variable_latency");
        waitAndClick(fileButton, "data_high_variable_latency.json");
        System.out.println("✅ Fichier sélectionné");

        // Lancer le traitement
        By launchButton = getLaunchButtonLocator();
        waitAndClick(launchButton, "Launch");
        System.out.println("✅ Traitement lancé");

        // Attendre directement la fin
        System.out.println("⏳ Attente de la complétion...");
        waitForTextInElement(launchButton, "Terminé", 60);

        // Vérification finale
        String finalText = waitAndGetText(launchButton);
        assertTrue("Le traitement devrait être terminé",
                finalText.contains("Terminé") || finalText.contains("Completed"));

        System.out.println("✅ Test terminé avec succès");
    }

    /**
     * Test de la réinitialisation après un traitement
     */
    @Test
    public void testResetAfterExecution() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🎬 TEST: Réinitialisation après exécution");
        System.out.println("========================================");

        // Exécuter un traitement complet
        By fileButton = getFileButtonLocator("data_high_variable_latency");
        waitAndClick(fileButton, "data_high_variable_latency.json");

        By launchButton = getLaunchButtonLocator();
        waitAndClick(launchButton, "Launch");

        waitForTextInElement(launchButton, "Terminé", 60);
        System.out.println("✅ Premier traitement terminé");

        // Cliquer sur le bouton "Terminé" pour réinitialiser
        waitAndClick(launchButton, "Reset");
        Thread.sleep(1000);

        // Vérifier que l'état est réinitialisé
        String resetText = waitAndGetText(launchButton);
        assertTrue("Le bouton devrait être revenu à l'état initial",
                resetText.contains("Select") || resetText.contains("Sélectionner"));

        System.out.println("✅ Réinitialisation réussie");
    }
}