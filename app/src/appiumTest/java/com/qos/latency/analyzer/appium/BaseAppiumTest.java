package com.qos.latency.analyzer.appium;

import io.appium.java_client.AppiumDriver;
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
 * Gère l'initialisation et la fermeture du driver
 */
public class BaseAppiumTest {

    protected AndroidDriver driver;
    protected WebDriverWait wait;

    @BeforeEach
    public void setUp() throws MalformedURLException {
        UiAutomator2Options options = new UiAutomator2Options();
        options.setCapability("platformName", "ANDROID");
        options.setCapability("appium:deviceName", "SM-S911B");
        options.setCapability("appium:udid", "192.168.1.109:5555");
        options.setCapability("appium:app", "/var/jenkins_home/workspace/QoS-Latency-Analyzer/app/build/outputs/apk/debug/app-debug.apk");
        options.setCapability("appium:automationName", "UiAutomator2");
        options.setCapability("appium:noReset", false);
        options.setCapability("appium:fullReset", false);
        options.setCapability("appium:newCommandTimeout", 300);
        options.setCapability("appium:settings[waitForIdleTimeout]", 50);
        options.setCapability("appium:settings[waitForSelectorTimeout]", 500);

        String appiumServer = System.getProperty("appium.server", "http://127.0.0.1:4723");
        driver = new AndroidDriver(new URL(appiumServer), options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Attend que les fichiers soient chargés depuis les assets
     * Vérifie la présence du fichier DATA_HIGH_VARIABLE_LATENCY
     */
    protected void waitForFilesLoaded() {
        System.out.println("⏳ Attente du chargement des fichiers depuis les assets...");

        try {
            Thread.sleep(3000);

            By fileLocator = By.xpath("//android.widget.Button[@text='DATA_HIGH_VARIABLE_LATENCY']");

            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(fileLocator));
                System.out.println("✅ Fichier DATA_HIGH_VARIABLE_LATENCY détecté");
            } catch (Exception e) {
                System.out.println("⚠️ Aucun fichier trouvé avec les locators spécifiques, attente de 5 secondes...");
                Thread.sleep(5000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Attend qu'un élément soit visible et effectue un scroll si nécessaire
     */
    protected WebElement waitForElementWithScroll(By locator, int timeoutSeconds) {
        try {
            WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            return customWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (Exception e) {
            System.out.println("⚠️ Élément non trouvé immédiatement, tentative de scroll...");
            scrollDown();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }

            WebDriverWait retryWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            return retryWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        }
    }

    /**
     * Attend qu'un élément soit visible puis clique dessus
     */
    protected void waitAndClick(By locator, int timeoutSeconds) {
        WebElement element = waitForElementWithScroll(locator, timeoutSeconds);
        element.click();
    }

    /**
     * Effectue un scroll vers le bas
     */
    protected void scrollDown() {
        int startX = driver.manage().window().getSize().width / 2;
        int startY = (int) (driver.manage().window().getSize().height * 0.8);
        int endY = (int) (driver.manage().window().getSize().height * 0.2);

        driver.executeScript("mobile: scrollGesture",
                java.util.Map.of(
                        "left", startX,
                        "top", endY,
                        "width", 0,
                        "height", startY - endY,
                        "direction", "down",
                        "percent", 0.75
                )
        );
    }

    /**
     * Attend qu'un élément soit cliquable
     */
    protected WebElement waitForClickable(By locator, int timeoutSeconds) {
        WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        return customWait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /**
     * Vérifie si un élément est présent
     */
    protected boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Attend un certain délai
     */
    protected void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}