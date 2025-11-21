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
import java.util.List;

/**
 * Classe de base pour tous les tests Appium
 * Ajout de méthodes d'attente robustes
 * Support du scroll pour trouver des éléments
 * Meilleure gestion des timeouts
 */
public class BaseAppiumTest {

    protected AndroidDriver driver;
    protected WebDriverWait wait;
    protected WebDriverWait longWait; // Pour les opérations longues

    /**
     * Configuration avant chaque test
     */
    @BeforeEach
    public void setUp() throws MalformedURLException, InterruptedException {
        UiAutomator2Options options = new UiAutomator2Options();

        options.setPlatformName("Android");
        options.setAutomationName("UiAutomator2");
        options.setApp("/var/jenkins_home/workspace/QoS-Latency-Analyzer/app/build/outputs/apk/debug/app-debug.apk");
        options.setDeviceName("SM-S911B");
        options.setUdid("192.168.1.109:5555");

        options.setNoReset(false);
        options.setFullReset(false);
        options.setNewCommandTimeout(Duration.ofSeconds(300));

        // Timeouts plus courts pour éviter les animations
        options.setCapability("settings[waitForIdleTimeout]", 50);
        options.setCapability("settings[waitForSelectorTimeout]", 500);

        driver = new AndroidDriver(new URL("http://127.0.0.1:4723"), options);

        // Deux niveaux de timeout
        wait = new WebDriverWait(driver, Duration.ofSeconds(15)); // Normal
        longWait = new WebDriverWait(driver, Duration.ofSeconds(45)); // Long (pour chargement fichiers)

        // Attendre le démarrage de l'app
        Thread.sleep(2000);
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Attendre qu'un élément soit visible avec scroll automatique
     */
    protected WebElement waitForElementWithScroll(By locator) {
        try {
            // Essayer d'abord sans scroll
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (Exception e) {
            // Si pas visible, scroller et réessayer
            scrollToFindElement(locator);
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        }
    }

    /**
     * Scroller pour trouver un élément (UiAutomator2)
     */
    protected void scrollToFindElement(By locator) {
        try {
            // UiAutomator2 scroll command
            String uiAutomatorText = locator.toString();
            if (uiAutomatorText.contains("text=")) {
                String text = uiAutomatorText.split("text=")[1].replace("]", "");
                driver.findElement(
                        By.androidUIAutomator(
                                "new UiScrollable(new UiSelector().scrollable(true))" +
                                        ".scrollIntoView(new UiSelector().text(\"" + text + "\"))"
                        )
                );
            }
        } catch (Exception e) {
            // Ignorer si le scroll échoue
        }
    }

    /**
     *  Attendre que PLUSIEURS fichiers soient chargés
     */
    protected void waitForFilesLoaded() {
        longWait.until(driver -> {
            try {
                List<WebElement> files = driver.findElements(By.xpath("//android.widget.TextView[contains(@text, '.json') or @text='test_data' or @text='new_data' or contains(@text, 'latency')]"));
                return files.size() > 0; // Au moins 1 fichier visible
            } catch (Exception e) {
                return false;
            }
        });
    }

    /**
     * Helper : Attendre qu'un élément soit visible (original)
     */
    protected WebElement waitForElement(By locator) {
        return longWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Attendre et cliquer avec retry
     */
    protected void waitAndClick(By locator) {
        int attempts = 0;
        while (attempts < 3) {
            try {
                WebElement element = waitForElementWithScroll(locator);
                element.click();
                return; // Succès
            } catch (Exception e) {
                attempts++;
                if (attempts >= 3) throw e;
                try { Thread.sleep(1000); } catch (InterruptedException ie) {}
            }
        }
    }

    /**
     * Helper : Attendre et récupérer le texte d'un élément
     */
    protected String waitAndGetText(By locator) {
        WebElement element = waitForElement(locator);
        return element.getText();
    }

    /**
     * Vérifier qu'un élément est affiché avec timeout court
     */
    protected boolean isElementDisplayed(By locator) {
        return isElementDisplayed(locator, 5);
    }

    /**
     * Vérifier avec timeout personnalisé
     */
    protected boolean isElementDisplayed(By locator, int timeoutSeconds) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            shortWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Attendre que le texte d'un élément change
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
    }
}