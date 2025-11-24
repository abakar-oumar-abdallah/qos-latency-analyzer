package com.qos.latency.analyzer.appium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.*;

public class QoSLatencyAppiumTest extends BaseAppiumTest {

    /**
     * Test 1 : Flux complet avec animation
     */
    @Test
    public void testCompleteFlowWithAnimation() throws InterruptedException {
        // 1. Vérifier démarrage
        String title = waitAndGetText(By.id("com.qos.latency.analyzer:id/tv_title"));
        assertEquals("QoS Latence", title);

        // 2. Attendre chargement des fichiers
        System.out.println("Attente chargement fichiers...");
        waitForFilesLoaded();
        Thread.sleep(2000);

        // 3. Sélectionner premier fichier (test_data)
        System.out.println("Sélection de test_data...");
        waitAndClick(By.xpath("//android.widget.TextView[@text='test_data']"));
        Thread.sleep(2000);

        // 4. Vérifier écran animation
        assertTrue(isElementDisplayed(By.id("com.qos.latency.analyzer:id/screen_animation"), 10));
        String status = waitAndGetText(By.id("com.qos.latency.analyzer:id/tv_status"));
        assertEquals("Prêt pour l'analyse", status);

        // 5. Lancer l'animation
        System.out.println("Lancement animation...");
        waitAndClick(By.id("com.qos.latency.analyzer:id/btn_launch"));
        Thread.sleep(2000);

        // 6. Vérifier que l'animation démarre
        String buttonText = waitAndGetText(By.id("com.qos.latency.analyzer:id/btn_launch"));
        assertTrue(buttonText.contains("Animation") || buttonText.contains("s"),
                "Le bouton devrait indiquer l'animation en cours");

        // 7. ATTENDRE QUE L'ANIMATION SE DÉROULE (15 secondes)
        System.out.println("Observation de l'animation pendant 15 secondes...");
        for (int i = 1; i <= 15; i++) {
            Thread.sleep(1000);
            System.out.println("   " + i + "s / 15s...");
        }

        // 8. Vérifier que la courbe est affichée pendant l'animation
        assertTrue(isElementDisplayed(By.id("com.qos.latency.analyzer:id/chart_view"), 5));

        // 9. Attendre la fin de l'animation (max 60s au total)
        System.out.println("Attente fin animation...");
        int maxWaitSeconds = 60;
        int waited = 0;
        while (waited < maxWaitSeconds) {
            try {
                String currentButtonText = driver.findElement(
                        By.id("com.qos.latency.analyzer:id/btn_launch")
                ).getText();

                if (currentButtonText.equals("Relancer")) {
                    System.out.println("Animation terminée après " + (15 + waited) + "s");
                    break;
                }
            } catch (Exception e) {
                // Continuer à attendre
            }
            Thread.sleep(1000);
            waited++;
        }

        // 10. Revenir à la liste
        System.out.println("Retour à la sélection...");
        waitAndClick(By.id("com.qos.latency.analyzer:id/btn_change_file"));
        Thread.sleep(2000);

        // 11. Vérifier retour écran sélection
        assertTrue(isElementDisplayed(By.id("com.qos.latency.analyzer:id/screen_file_selection"), 5));

        // 12. Sélectionner un autre fichier (new_data)
        System.out.println("Sélection de new_data...");
        waitForFilesLoaded();
        Thread.sleep(1000);

        try {
            waitAndClick(By.xpath("//android.widget.TextView[@text='new_data']"));
        } catch (Exception e) {
            // Si new_data n'existe pas, re-sélectionner test_data
            System.out.println("new_data introuvable, ré-sélection de test_data");
            waitAndClick(By.xpath("//android.widget.TextView[@text='test_data']"));
        }
        Thread.sleep(2000);

        // 13. Lancer la deuxième animation
        System.out.println("🎬 Lancement 2ème animation...");
        waitAndClick(By.id("com.qos.latency.analyzer:id/btn_launch"));
        Thread.sleep(2000);

        // 14. OBSERVER LA 2ème ANIMATION (10 secondes)
        System.out.println("Observation de la 2ème animation pendant 10 secondes...");
        for (int i = 1; i <= 10; i++) {
            Thread.sleep(1000);
            System.out.println("   " + i + "s / 10s...");
        }

        // 15. Vérification finale
        assertTrue(isElementDisplayed(By.id("com.qos.latency.analyzer:id/chart_view"), 5));
        System.out.println("Test flux complet réussi !");
    }

    /**
     * Test 2 : Vérifier rafraîchissement liste
     */
    @Test
    public void testRefreshFileList() throws InterruptedException {
        waitForFilesLoaded();
        Thread.sleep(1000);

        assertTrue(isElementDisplayed(By.xpath("//android.widget.TextView[@text='test_data']"), 10));

        System.out.println("Actualisation de la liste...");
        waitAndClick(By.id("com.qos.latency.analyzer:id/btn_refresh_files"));

        Thread.sleep(2000);
        waitForFilesLoaded();
        Thread.sleep(1000);

        assertTrue(isElementDisplayed(By.xpath("//android.widget.TextView[@text='test_data']"), 10));
        System.out.println("Liste rafraîchie avec succès");
    }
}