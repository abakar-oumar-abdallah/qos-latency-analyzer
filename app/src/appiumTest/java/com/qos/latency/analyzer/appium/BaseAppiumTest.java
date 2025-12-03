package com.qos.latency.analyzer.appium;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.AppiumBy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

/**
 * Classe de base pour tests Appium - CONFIGURÉ POUR ÉMULATEUR
 *
 * Configuration:
 * - Device: Android Emulator (emulator-5554)
 * - Appium Server: http://127.0.0.1:4723
 * - Timeout: 30 secondes (adapté pour émulateur)
 */
public class BaseAppiumTest {

    protected AndroidDriver driver;
    protected WebDriverWait wait;
    protected WebDriverWait longWait;

    @BeforeEach
    public void setUp() throws MalformedURLException, InterruptedException {
        System.out.println("=================================================");
        System.out.println("🚀 CONFIGURATION TESTS APPIUM - ÉMULATEUR");
        System.out.println("=================================================");

        UiAutomator2Options options = new UiAutomator2Options();

        // Configuration de base
        options.setPlatformName("Android");
        options.setAutomationName("UiAutomator2");

        // ✅ CHEMIN APK (adapté pour Jenkins)
        options.setApp("/var/jenkins_home/workspace/QoS-Latency-Analyzer/app/build/outputs/apk/debug/app-debug.apk");

        // ✅ CONFIGURATION ÉMULATEUR
        options.setDeviceName("emulator-5554");  // Nom par défaut émulateur
        options.setUdid("emulator-5554");        // UDID émulateur

        System.out.println("📱 Device: Android Emulator");
        System.out.println("🔧 UDID: emulator-5554");
        System.out.println("📦 APK: app-debug.apk");

        // Options de performance pour émulateur
        options.setNoReset(false);               // Reset app à chaque test
        options.setFullReset(false);             // Ne pas réinstaller à chaque fois
        options.setNewCommandTimeout(Duration.ofSeconds(300));

        // Optimisations pour émulateur (plus rapide)
        options.setCapability("settings[waitForIdleTimeout]", 50);
        options.setCapability("settings[waitForSelectorTimeout]", 500);
        options.setCapability("skipDeviceInitialization", true);  // Plus rapide
        options.setCapability("skipServerInstallation", true);    // Plus rapide

        // URL Appium Server
        String appiumServer = System.getProperty("appium.server", "http://127.0.0.1:4723");
        System.out.println("🌐 Appium Server: " + appiumServer);

        // Connexion au driver
        System.out.println("⏳ Connexion au driver Appium...");
        driver = new AndroidDriver(new URL(appiumServer), options);
        System.out.println("✅ Driver connecté avec succès");

        // Configuration des timeouts (adaptés pour émulateur)
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));      // 30s pour émulateur
        longWait = new WebDriverWait(driver, Duration.ofSeconds(90));  // 90s pour animations

        System.out.println("⏱️  Timeout standard: 30 secondes");
        System.out.println("⏱️  Timeout long: 90 secondes");

        // Attente initiale réduite pour émulateur (plus rapide)
        Thread.sleep(2000);

        System.out.println("=================================================");
        System.out.println("✅ CONFIGURATION TERMINÉE - TESTS PRÊTS");
        System.out.println("=================================================\n");
    }

    @AfterEach
    public void tearDown() {
        System.out.println("\n🧹 Nettoyage session Appium...");
        if (driver != null) {
            driver.quit();
            System.out.println("✅ Session fermée");
        }
    }

    /**
     * Attend et trouve un élément avec scroll automatique si nécessaire
     */
    protected WebElement waitForElementWithScroll(By locator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (Exception e) {
            System.out.println("⚠️  Élément non visible, tentative de scroll...");
            scrollToFindElement(locator);
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        }
    }

    /**
     * Scroll pour trouver un élément
     */
    protected void scrollToFindElement(By locator) {
        try {
            String uiAutomatorText = locator.toString();
            if (uiAutomatorText.contains("text=")) {
                String text = uiAutomatorText.split("text=")[1].replace("]", "");
                driver.findElement(
                        AppiumBy.androidUIAutomator(
                                "new UiScrollable(new UiSelector().scrollable(true))" +
                                        ".scrollIntoView(new UiSelector().text(\"" + text + "\"))"
                        )
                );
                System.out.println("✅ Scroll réussi vers: " + text);
            }
        } catch (Exception e) {
            System.out.println("⚠️  Scroll échoué (élément peut-être hors écran)");
        }
    }

    /**
     * Attend un élément avec timeout long
     */
    protected WebElement waitForElement(By locator) {
        return longWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Clique sur un élément avec retry automatique
     */
    protected void waitAndClick(By locator) {
        int attempts = 0;
        while (attempts < 3) {
            try {
                WebElement element = waitForElementWithScroll(locator);
                element.click();
                System.out.println("✅ Clic réussi sur: " + locator);
                return;
            } catch (Exception e) {
                attempts++;
                System.out.println("⚠️  Tentative " + attempts + "/3 échouée");
                if (attempts >= 3) {
                    System.err.println("❌ Impossible de cliquer après 3 tentatives");
                    throw e;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Récupère le texte d'un élément
     */
    protected String waitAndGetText(By locator) {
        WebElement element = waitForElement(locator);
        String text = element.getText();
        System.out.println("📝 Texte récupéré: " + text);
        return text;
    }

    /**
     * Vérifie si un élément est affiché (timeout par défaut)
     */
    protected boolean isElementDisplayed(By locator) {
        return isElementDisplayed(locator, 5);
    }

    /**
     * Vérifie si un élément est affiché (timeout personnalisé)
     */
    protected boolean isElementDisplayed(By locator, int timeoutSeconds) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            shortWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            System.out.println("✅ Élément visible: " + locator);
            return true;
        } catch (Exception e) {
            System.out.println("❌ Élément non visible après " + timeoutSeconds + "s: " + locator);
            return false;
        }
    }

    /**
     * Attend que le texte d'un élément change
     */
    protected void waitForTextToChange(By locator, String oldText) {
        wait.until(driver -> {
            try {
                String currentText = driver.findElement(locator).getText();
                return !currentText.equals(oldText);
            } catch (Exception e) {
                return false;
            }
        });
        System.out.println("✅ Texte changé depuis: " + oldText);
    }
}