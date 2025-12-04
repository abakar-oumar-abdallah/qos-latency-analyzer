package com.qos.latency.analyzer.appium;

import org.junit.Test;
import org.openqa.selenium.By;

import static org.junit.Assert.*;

public class QoSLatencyAppiumTest extends BaseAppiumTest {

    @Test
    public void testAppLaunches() {
        System.out.println("\n🧪 TEST: Lancement de l'application");

        // Attendre que tous les fichiers soient visibles
        String[] files = {"data_all", "data_high_variable_latency", "data_loss_7%", "data_mixed"};

        for (String fileName : files) {
            By locator = getFileButtonLocator(fileName);
            assertTrue("Le fichier '" + fileName + "' devrait être visible",
                    isElementDisplayed(locator, 15));
            System.out.println("✅ Fichier visible: " + fileName);
        }

        // Vérifier le bouton Launch
        By launchButton = getLaunchButtonLocator();
        assertTrue("Le bouton Launch devrait être visible",
                isElementDisplayed(launchButton, 10));

        String buttonText = waitAndGetText(launchButton);
        assertTrue("Le texte du bouton devrait contenir 'Launch' ou 'Lancer'",
                buttonText.contains("Launch") || buttonText.contains("Lancer"));
    }

    // Tu peux garder les autres tests tels quels mais assure-toi que
    // getFileButtonLocator() et isElementDisplayed() attendent assez longtemps.
}