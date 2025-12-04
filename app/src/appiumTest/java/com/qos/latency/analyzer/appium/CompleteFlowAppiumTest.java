package com.qos.latency.analyzer.appium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompleteFlowAppiumTest extends BaseAppiumTest {

    @Test
    public void testCompleteExecutionFlowWithVisualization() {
        System.out.println("\n======================================================================");
        System.out.println("  DÉMARRAGE DU TEST APPIUM AVEC VISUALISATION");
        System.out.println("======================================================================");
        System.out.println("ℹ️  Durée estimée : 1 minute 45 secondes");
        System.out.println("======================================================================\n");

        // ═══════════════════════════════════════════════════════════════════════════
        // PHASE 1 : ÉCRAN DE SÉLECTION (5 secondes)
        // ═══════════════════════════════════════════════════════════════════════════
        System.out.println("\n──────────────────────────────────────────────────────────────────────");
        System.out.println("📍 PHASE 1 : ÉCRAN DE SÉLECTION (5s)");
        System.out.println("──────────────────────────────────────────────────────────────────────");

        // Vérifier que l'application s'est lancée
        By titleLocator = By.id("com.qos.latency.analyzer:id/tv_title");
        String appTitle = waitAndGetText(titleLocator);
        assertTrue(appTitle.contains("QoS"), "✅ Application lancée : " + appTitle);
        System.out.println("✅ Application lancée : " + appTitle);

        System.out.println("ℹ️  Observation de l'interface de sélection...");
        waitForScreen("⏩ ✓ Phase 1 terminée\n", 5);

        // ═══════════════════════════════════════════════════════════════════════════
        // PHASE 2 : ATTENTE DU CHARGEMENT DES FICHIERS (5 secondes)
        // ═══════════════════════════════════════════════════════════════════════════
        System.out.println("\n──────────────────────────────────────────────────────────────────────");
        System.out.println("📍 PHASE 2 : ATTENTE CHARGEMENT APP (5s)");
        System.out.println("──────────────────────────────────────────────────────────────────────");
        System.out.println("ℹ️  Attente que l'app charge les fichiers depuis les assets...");
        waitForScreen("⏩ ✓ Phase 2 terminée\n", 5);

        // ═══════════════════════════════════════════════════════════════════════════
        // PHASE 3 : AVANT CLIC SUR LE FICHIER (5 secondes)
        // ═══════════════════════════════════════════════════════════════════════════
        System.out.println("\n──────────────────────────────────────────────────────────────────────");
        System.out.println("📍 PHASE 3 : AVANT CLIC SUR FICHIER (5s)");
        System.out.println("──────────────────────────────────────────────────────────────────────");
        System.out.println("ℹ️  Préparation pour sélectionner test_data.json...");
        waitForScreen("⏩ ✓ Phase 3 terminée\n", 5);

        // ═══════════════════════════════════════════════════════════════════════════
        // PHASE 4 : CLIC SUR LE FICHIER ET CHARGEMENT (2 secondes)
        // ═══════════════════════════════════════════════════════════════════════════
        System.out.println("\n──────────────────────────────────────────────────────────────────────");
        System.out.println("📍 PHASE 4 : CLIC ET CHARGEMENT (2s)");
        System.out.println("──────────────────────────────────────────────────────────────────────");
        System.out.println("ℹ️  Clic sur test_data.json...");

        // ✅ CORRECTION : Utiliser getFileButtonLocator qui convertit en majuscules
        By testDataButton = getFileButtonLocator("test_data");
        waitAndClick(testDataButton, "test_data.json");

        waitForScreen("⏩ ✓ Phase 4 terminée\n", 2);

        // ═══════════════════════════════════════════════════════════════════════════
        // PHASE 5 : ÉCRAN D'ANIMATION ET STATUT (5 secondes)
        // ═══════════════════════════════════════════════════════════════════════════
        System.out.println("\n──────────────────────────────────────────────────────────────────────");
        System.out.println("📍 PHASE 5 : ÉCRAN D'ANIMATION (5s)");
        System.out.println("──────────────────────────────────────────────────────────────────────");
        By statusLocator = By.id("com.qos.latency.analyzer:id/tv_status");
        String status = waitAndGetText(statusLocator);
        System.out.println("ℹ️  Statut actuel : " + status);
        waitForScreen("⏩ ✓ Phase 5 terminée\n", 5);

        // ═══════════════════════════════════════════════════════════════════════════
        // PHASE 6 : OBSERVATION DU BOUTON LANCER (5 secondes)
        // ═══════════════════════════════════════════════════════════════════════════
        System.out.println("\n──────────────────────────────────────────────────────────────────────");
        System.out.println("📍 PHASE 6 : BOUTON LANCER (5s)");
        System.out.println("──────────────────────────────────────────────────────────────────────");
        By launchButtonLocator = By.id("com.qos.latency.analyzer:id/btn_launch_animation");
        String buttonText = waitAndGetText(launchButtonLocator);
        System.out.println("ℹ️  Texte du bouton : " + buttonText);
        waitForScreen("⏩ ✓ Phase 6 terminée\n", 5);

        // ═══════════════════════════════════════════════════════════════════════════
        // PHASE 7 : CLIC SUR LE BOUTON LANCER (2 secondes)
        // ═══════════════════════════════════════════════════════════════════════════
        System.out.println("\n──────────────────────────────────────────────────────────────────────");
        System.out.println("📍 PHASE 7 : LANCEMENT ANIMATION (2s)");
        System.out.println("──────────────────────────────────────────────────────────────────────");
        System.out.println("ℹ️  Clic sur le bouton Lancer...");
        waitAndClick(launchButtonLocator, "Bouton Lancer Animation");
        waitForScreen("⏩ ✓ Phase 7 terminée\n", 2);

        // ═══════════════════════════════════════════════════════════════════════════
        // PHASE 8 : DÉBUT DE L'ANIMATION (10 secondes)
        // ═══════════════════════════════════════════════════════════════════════════
        System.out.println("\n──────────────────────────────────────────────────────────────────────");
        System.out.println("📍 PHASE 8 : DÉBUT ANIMATION (10s)");
        System.out.println("──────────────────────────────────────────────────────────────────────");
        String statusAnimation = waitAndGetText(statusLocator);
        System.out.println("ℹ️  Statut : " + statusAnimation);
        waitForScreen("⏩ ✓ Phase 8 terminée\n", 10);

        // ═══════════════════════════════════════════════════════════════════════════
        // PHASE 9 : MILIEU DE L'ANIMATION (10 secondes)
        // ═══════════════════════════════════════════════════════════════════════════
        System.out.println("\n──────────────────────────────────────────────────────────────────────");
        System.out.println("📍 PHASE 9 : MILIEU ANIMATION (10s)");
        System.out.println("──────────────────────────────────────────────────────────────────────");
        System.out.println("ℹ️  L'animation est en cours d'exécution...");
        waitForScreen("⏩ ✓ Phase 9 terminée\n", 10);

        // ═══════════════════════════════════════════════════════════════════════════
        // PHASE 10 : FIN DE L'ANIMATION (10 secondes)
        // ═══════════════════════════════════════════════════════════════════════════
        System.out.println("\n──────────────────────────────────────────────────────────────────────");
        System.out.println("📍 PHASE 10 : FIN ANIMATION (10s)");
        System.out.println("──────────────────────────────────────────────────────────────────────");
        String countdown = waitAndGetText(launchButtonLocator);
        System.out.println("ℹ️  Compte à rebours : " + countdown);
        waitForScreen("⏩ ✓ Phase 10 terminée\n", 10);

        // ═══════════════════════════════════════════════════════════════════════════
        // PHASE 11 : VÉRIFICATION FINALE (5 secondes)
        // ═══════════════════════════════════════════════════════════════════════════
        System.out.println("\n──────────────────────────────────────────────────────────────────────");
        System.out.println("📍 PHASE 11 : VÉRIFICATION FINALE (5s)");
        System.out.println("──────────────────────────────────────────────────────────────────────");
        System.out.println("ℹ️  Vérification de l'état final...");

        // Vérifications finales
        String finalStatus = waitAndGetText(statusLocator);
        String finalButton = waitAndGetText(launchButtonLocator);

        System.out.println("ℹ️  Statut final : " + finalStatus);
        System.out.println("ℹ️  Bouton final : " + finalButton);

        assertTrue(finalStatus.contains("Terminé") || finalStatus.contains("Prêt"),
                "Le statut final devrait indiquer que l'animation est terminée");

        waitForScreen("⏩ ✓ Phase 11 terminée\n", 5);

        System.out.println("\n======================================================================");
        System.out.println("✅ TEST TERMINÉ AVEC SUCCÈS");
        System.out.println("======================================================================\n");
    }
}