package com.qos.latency.analyzer.appium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test de flux complet avec visualisation chronologique
 * Vérifie l'intégralité du parcours utilisateur
 */
public class CompleteFlowAppiumTest extends BaseAppiumTest {

    @Test
    public void testCompleteExecutionFlowWithVisualization() {
        System.out.println("\n");
        System.out.println("======================================================================");
        System.out.println("  DÉMARRAGE DU TEST APPIUM AVEC VISUALISATION");
        System.out.println("======================================================================");
        System.out.println("ℹ️  Durée estimée : 1 minute 45 secondes");
        System.out.println("======================================================================");
        System.out.println("\n");

        // ====================================================================
        // PHASE 1 : ÉCRAN DE SÉLECTION (5 secondes)
        // ====================================================================
        System.out.println("──────────────────────────────────────────────────────────────────────");
        System.out.println("📍 PHASE 1 : ÉCRAN DE SÉLECTION (5s)");
        System.out.println("──────────────────────────────────────────────────────────────────────");

        By titleLocator = By.xpath("//android.widget.TextView[@text='QoS Latence']");
        WebElement titleElement = wait.until(
                org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(titleLocator)
        );
        assertNotNull(titleElement, "Le titre devrait être visible");
        System.out.println("✅ Application lancée : " + titleElement.getText());

        System.out.println("ℹ️  Observation de l'interface de sélection...");
        sleep(5000);
        System.out.println("⏩ ✓ Phase 1 terminée");
        System.out.println("\n");

        // ====================================================================
        // PHASE 2 : ATTENTE CHARGEMENT APP (5 secondes)
        // ====================================================================
        System.out.println("──────────────────────────────────────────────────────────────────────");
        System.out.println("📍 PHASE 2 : ATTENTE CHARGEMENT APP (5s)");
        System.out.println("──────────────────────────────────────────────────────────────────────");
        System.out.println("ℹ️  Attente que l'app charge les fichiers depuis les assets...");

        waitForFilesLoaded();

        sleep(5000);
        System.out.println("⏩ ✓ Phase 2 terminée");
        System.out.println("\n");

        // ====================================================================
        // PHASE 3 : AVANT CLIC SUR FICHIER (5 secondes)
        // ====================================================================
        System.out.println("──────────────────────────────────────────────────────────────────────");
        System.out.println("📍 PHASE 3 : AVANT CLIC SUR FICHIER (5s)");
        System.out.println("──────────────────────────────────────────────────────────────────────");
        System.out.println("ℹ️  Préparation pour sélectionner data_high_variable_latency.json...");

        sleep(5000);
        System.out.println("⏩ ✓ Phase 3 terminée");
        System.out.println("\n");

        // ====================================================================
        // PHASE 4 : CLIC ET CHARGEMENT (2 secondes)
        // ====================================================================
        System.out.println("──────────────────────────────────────────────────────────────────────");
        System.out.println("📍 PHASE 4 : CLIC ET CHARGEMENT (2s)");
        System.out.println("──────────────────────────────────────────────────────────────────────");
        System.out.println("ℹ️  Clic sur data_high_variable_latency.json...");

        waitAndClick(
                By.xpath("//android.widget.Button[contains(@text, 'DATA_HIGH_VARIABLE_LATENCY') or contains(@text, 'DATA HIGH')]"),
                15
        );

        sleep(2000);
        System.out.println("⏩ ✓ Phase 4 terminée");
        System.out.println("\n");

        // ====================================================================
        // PHASE 5 : ÉCRAN GRAPHIQUE VISIBLE (5 secondes)
        // ====================================================================
        System.out.println("──────────────────────────────────────────────────────────────────────");
        System.out.println("📍 PHASE 5 : ÉCRAN GRAPHIQUE VISIBLE (5s)");
        System.out.println("──────────────────────────────────────────────────────────────────────");
        System.out.println("ℹ️  Vérification de la présence du bouton Lancer...");

        By launchButtonLocator = By.xpath("//android.widget.Button[@text='Lancer l\\'analyse' or @text='Lancer']");
        WebElement launchButton = wait.until(
                org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(launchButtonLocator)
        );
        assertNotNull(launchButton, "Le bouton Lancer devrait être visible");
        System.out.println("✅ Bouton trouvé : " + launchButton.getText());

        sleep(5000);
        System.out.println("⏩ ✓ Phase 5 terminée");
        System.out.println("\n");

        // ====================================================================
        // PHASE 6 : AVANT CLIC LANCER (5 secondes)
        // ====================================================================
        System.out.println("──────────────────────────────────────────────────────────────────────");
        System.out.println("📍 PHASE 6 : AVANT CLIC LANCER (5s)");
        System.out.println("──────────────────────────────────────────────────────────────────────");
        System.out.println("ℹ️  Préparation avant de lancer l'animation...");

        sleep(5000);
        System.out.println("⏩ ✓ Phase 6 terminée");
        System.out.println("\n");

        // ====================================================================
        // PHASE 7 : CLIC LANCER (1 seconde)
        // ====================================================================
        System.out.println("──────────────────────────────────────────────────────────────────────");
        System.out.println("📍 PHASE 7 : CLIC LANCER (1s)");
        System.out.println("──────────────────────────────────────────────────────────────────────");
        System.out.println("ℹ️  Clic sur le bouton Lancer...");

        launchButton.click();

        sleep(1000);
        System.out.println("✅ Bouton Lancer cliqué");
        System.out.println("⏩ ✓ Phase 7 terminée");
        System.out.println("\n");

        // ====================================================================
        // PHASE 8 : ANIMATION EN COURS (60 secondes)
        // ====================================================================
        System.out.println("──────────────────────────────────────────────────────────────────────");
        System.out.println("📍 PHASE 8 : ANIMATION EN COURS (60s)");
        System.out.println("──────────────────────────────────────────────────────────────────────");
        System.out.println("ℹ️  Animation chronologique des paquets réseau...");
        System.out.println("⏳ Observation de l'animation pendant 60 secondes...");

        // Vérification toutes les 10 secondes que l'animation se déroule
        for (int i = 1; i <= 6; i++) {
            sleep(10000);
            System.out.println("    ⏱️  " + (i * 10) + "s écoulées...");

            // Vérifier que l'app n'a pas crashé
            By statusLocator = By.id("com.qos.latency.analyzer:id/tv_status");
            boolean statusExists = driver.findElements(statusLocator).size() > 0;
            assertTrue(statusExists, "L'élément de statut devrait toujours exister (vérification crash)");
        }

        System.out.println("✅ Animation observée pendant 60 secondes");
        System.out.println("⏩ ✓ Phase 8 terminée");
        System.out.println("\n");

        // ====================================================================
        // PHASE 9 : ATTENTE FIN ANIMATION (10 secondes)
        // ====================================================================
        System.out.println("──────────────────────────────────────────────────────────────────────");
        System.out.println("📍 PHASE 9 : ATTENTE FIN ANIMATION (10s)");
        System.out.println("──────────────────────────────────────────────────────────────────────");
        System.out.println("ℹ️  Attente de la fin complète de l'animation...");

        sleep(10000);
        System.out.println("⏩ ✓ Phase 9 terminée");
        System.out.println("\n");

        // ====================================================================
        // PHASE 10 : VÉRIFICATION POST-ANIMATION (5 secondes)
        // ====================================================================
        System.out.println("──────────────────────────────────────────────────────────────────────");
        System.out.println("📍 PHASE 10 : VÉRIFICATION POST-ANIMATION (5s)");
        System.out.println("──────────────────────────────────────────────────────────────────────");
        System.out.println("ℹ️  Vérification de l'état final de l'application...");

        By statusLocator = By.id("com.qos.latency.analyzer:id/tv_status");
        boolean statusVisible = driver.findElements(statusLocator).size() > 0;
        assertTrue(statusVisible, "Le statut devrait être visible après l'animation");

        if (statusVisible) {
            WebElement statusElement = driver.findElement(statusLocator);
            System.out.println("✅ Statut final : " + statusElement.getText());
        }

        sleep(5000);
        System.out.println("⏩ ✓ Phase 10 terminée");
        System.out.println("\n");

        // ====================================================================
        // RÉSUMÉ FINAL
        // ====================================================================
        System.out.println("======================================================================");
        System.out.println("  ✅ TEST COMPLET RÉUSSI");
        System.out.println("======================================================================");
        System.out.println("📊 Toutes les phases ont été exécutées avec succès");
        System.out.println("⏱️  Durée totale : ~1 minute 45 secondes");
        System.out.println("======================================================================");
        System.out.println("\n");
    }
}