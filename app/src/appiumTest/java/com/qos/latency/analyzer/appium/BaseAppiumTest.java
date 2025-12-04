package com.qos.latency.analyzer.appium;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;
import java.time.Duration;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Base pour les tests Appium.
 * Fournit les helpers pour interagir avec les éléments de l'UI.
 */
public class BaseAppiumTest {

    protected AppiumDriver<MobileElement> driver;

    /**
     * Initialisation du driver Appium avant chaque test
     */
    public BaseAppiumTest() {
        try {
            DesiredCapabilities caps = new DesiredCapabilities();
            caps.setCapability("platformName", "Android");
            caps.setCapability("deviceName", "Android Emulator"); // ou ton device réel
            caps.setCapability("appPackage", "com.qos.latency.analyzer");
            caps.setCapability("appActivity", "com.qos.latency.analyzer.MainActivity");
            caps.setCapability("automationName", "UiAutomator2");
            caps.setCapability("noReset", true);

            driver = new AndroidDriver<>(new URL("http://127.0.0.1:4723/wd/hub"), caps);
            Thread.sleep(2000); // Attendre le lancement de l'application
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Vérifie si un élément est affiché avec un timeout
     */
    protected boolean isElementDisplayed(By locator, int timeoutSec) {
        int waited = 0;
        while (waited < timeoutSec) {
            try {
                List<MobileElement> elements = driver.findElements(locator);
                if (!elements.isEmpty() && elements.get(0).isDisplayed()) {
                    return true;
                }
                Thread.sleep(1000);
                waited++;
            } catch (Exception e) {
                // Ignorer les exceptions et attendre
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                waited++;
            }
        }
        return false;
    }

    /**
     * Clique sur un élément après l'avoir attendu
     */
    protected void waitAndClick(By locator, String description) {
        assertTrue(description + " doit être visible", isElementDisplayed(locator, 10));
        driver.findElement(locator).click();
        System.out.println("✅ Clic sur : " + description);
    }

    /**
     * Récupère le texte d'un élément après l'avoir attendu
     */
    protected String waitAndGetText(By locator) {
        assertTrue("Élément doit être visible pour récupérer le texte", isElementDisplayed(locator, 10));
        return driver.findElement(locator).getText();
    }

    /**
     * Prendre une capture d'écran
     */
    protected void takeScreenshot(String filename) {
        // Pour simplifier, on peut intégrer une librairie comme Appium Screenshot ici
        System.out.println("📸 Capture d'écran : " + filename);
    }

    /**
     * Affiche tous les éléments visibles dans la console pour debug
     */
    protected void printAllVisibleElements() {
        List<MobileElement> elements = driver.findElements(By.xpath("//*"));
        System.out.println("📋 Éléments visibles : " + elements.size());
        for (MobileElement e : elements) {
            try {
                if (e.isDisplayed()) {
                    System.out.println(" - " + e.getText() + " | id: " + e.getId());
                }
            } catch (Exception ignored) {}
        }
    }

    // --- LOCATORS SPECIFIQUES À L'APPLICATION ---

    protected By getLaunchButtonLocator() {
        return By.id("com.qos.latency.analyzer:id/btn_launch");
    }

    protected By getFileButtonLocator(String fileName) {
        // Chaque fichier est un Button dont le texte contient le nom simplifié
        return By.xpath("//android.widget.Button[contains(@text, '" + fileName + "')]");
    }

    protected By getProgressBarLocator() {
        return By.xpath("//android.widget.ProgressBar");
    }

    protected By getStatusTextLocator() {
        return By.id("com.qos.latency.analyzer:id/tv_status");
    }

    /**
     * Ferme le driver après chaque test
     */
    protected void tearDown() {
        if (driver != null) {
            driver.quit();
            System.out.println("🧹 Driver fermé");
        }
    }
}
