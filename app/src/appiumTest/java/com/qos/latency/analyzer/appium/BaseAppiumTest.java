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

public class BaseAppiumTest {

    protected AndroidDriver driver;
    protected WebDriverWait wait;
    protected WebDriverWait longWait;

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

        options.setCapability("settings[waitForIdleTimeout]", 50);
        options.setCapability("settings[waitForSelectorTimeout]", 500);

        // URL dynamique pour supporter exécution locale et Jenkins
        String appiumServer = System.getProperty("appium.server", "http://127.0.0.1:4723");
        driver = new AndroidDriver(new URL(appiumServer), options);

        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        longWait = new WebDriverWait(driver, Duration.ofSeconds(90));

        Thread.sleep(2000);
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Attend que les fichiers soient chargés depuis les assets
     * Cette méthode attend qu'au moins un fichier soit visible dans la liste
     * ou que l'indicateur de chargement disparaisse
     *
     * OPTION 1 : ATTENTE INTELLIGENTE
     * - Détecte automatiquement la présence des fichiers
     * - Utilise un fallback si les fichiers ne sont pas trouvés
     * - Performance optimale avec attente dynamique
     */
    protected void waitForFilesLoaded() {
        try {
            // Option 1 : Attendre qu'au moins un élément de fichier soit visible
            // On cherche n'importe quel TextView qui pourrait être un nom de fichier
            By anyFileLocator = By.xpath("//android.widget.TextView[@text='test_data' or @text='packets_data' or @text='network_data']");

            longWait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(anyFileLocator),
                    ExpectedConditions.visibilityOfElementLocated(By.xpath("//android.widget.TextView[contains(@text, 'data')]"))
            ));

            // Petite pause supplémentaire pour s'assurer que tout est stable
            Thread.sleep(500);

        } catch (Exception e) {
            // Si on ne trouve pas de fichier spécifique, on attend simplement un délai fixe
            // (comme dans CompleteFlowAppiumTest)
            System.out.println("⚠️ Aucun fichier trouvé avec les locators spécifiques, attente de 5 secondes...");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    protected WebElement waitForElementWithScroll(By locator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (Exception e) {
            scrollToFindElement(locator);
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        }
    }

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
            }
        } catch (Exception e) {
            // Ignorer si le scroll échoue
        }
    }

    protected WebElement waitForElement(By locator) {
        return longWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void waitAndClick(By locator) {
        int attempts = 0;
        while (attempts < 3) {
            try {
                WebElement element = waitForElementWithScroll(locator);
                element.click();
                return;
            } catch (Exception e) {
                attempts++;
                if (attempts >= 3) throw e;
                try { Thread.sleep(1000); } catch (InterruptedException ie) {}
            }
        }
    }

    protected String waitAndGetText(By locator) {
        WebElement element = waitForElement(locator);
        return element.getText();
    }

    protected boolean isElementDisplayed(By locator) {
        return isElementDisplayed(locator, 5);
    }

    protected boolean isElementDisplayed(By locator, int timeoutSeconds) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            shortWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

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