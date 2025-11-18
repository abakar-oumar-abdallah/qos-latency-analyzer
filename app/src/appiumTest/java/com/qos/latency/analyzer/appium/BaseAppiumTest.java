package com.qos.latency.analyzer.appium;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
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
 * Classe de base pour tous les tests Appium
 * Configure la connexion au serveur Appium et au téléphone
 */
public class BaseAppiumTest {

    protected AndroidDriver driver;
    protected WebDriverWait wait;

    /**
     * Configuration avant chaque test
     * Démarre une session Appium et installe l'app
     */
    @BeforeEach
    public void setUp() throws MalformedURLException, InterruptedException {
        // Configuration des capabilities Appium
        UiAutomator2Options options = new UiAutomator2Options();

        // Informations de l'application
        options.setPlatformName("Android");
        options.setAutomationName("UiAutomator2");

        // Chemin de l'APK (pour Jenkins)
        options.setApp("/var/jenkins_home/workspace/QoS-Latency-Analyzer/app/build/outputs/apk/debug/app-debug.apk");

        // Configuration du device
        options.setDeviceName("SM-S911B");
        options.setUdid("192.168.1.109:5555");

        // Options supplémentaires
        options.setNoReset(false);
        options.setFullReset(false);
        options.setNewCommandTimeout(Duration.ofSeconds(300));

        // Désactiver les animations
        options.setCapability("settings[waitForIdleTimeout]", 100);
        options.setCapability("settings[waitForSelectorTimeout]", 1000);

        // Connexion au serveur Appium
        driver = new AndroidDriver(
                new URL("http://127.0.0.1:4723"),
                options
        );

        // Configuration du timeout d'attente
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        // Attendre que l'app soit complètement chargée
        Thread.sleep(3000);
    }

    /**
     * Nettoyage après chaque test
     */
    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Helper : Attendre qu'un élément soit visible
     */
    protected WebElement waitForElement(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Helper : Attendre et cliquer sur un élément
     */
    protected void waitAndClick(By locator) {
        WebElement element = waitForElement(locator);
        element.click();
    }

    /**
     * Helper : Attendre et récupérer le texte d'un élément
     */
    protected String waitAndGetText(By locator) {
        WebElement element = waitForElement(locator);
        return element.getText();
    }

    /**
     * Helper : Vérifier qu'un élément est affiché
     */
    protected boolean isElementDisplayed(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}