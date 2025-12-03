package com.qos.latency.analyzer.appium;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;

public class BaseAppiumTest {
    protected AndroidDriver driver;
    protected WebDriverWait wait;
    protected WebDriverWait longWait;
    protected WebDriverWait extraLongWait;

    @BeforeEach
    public void setUp() throws MalformedURLException {
        System.out.println("========================================");
        System.out.println("🚀 INITIALISATION DU TEST APPIUM");
        System.out.println("========================================");

        String appiumServer = System.getProperty("appium.server", "http://127.0.0.1:4723");
        System.out.println("📡 Connexion à Appium: " + appiumServer);

        UiAutomator2Options options = new UiAutomator2Options();
        options.setApp("/var/jenkins_home/workspace/QoS-Latency-Analyzer/app/build/outputs/apk/debug/app-debug.apk");
        options.setDeviceName("SM-S911B");
        options.setUdid("192.168.1.109:5555");
        options.setPlatformName("Android");
        options.setAutomationName("UiAutomator2");
        options.setNoReset(false);
        options.setFullReset(false);
        options.setNewCommandTimeout(Duration.ofSeconds(300));
        options.setCapability("settings[waitForIdleTimeout]", 50);
        options.setCapability("settings[waitForSelectorTimeout]", 500);

        driver = new AndroidDriver(new URL(appiumServer), options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        longWait = new WebDriverWait(driver, Duration.ofSeconds(90));
        extraLongWait = new WebDriverWait(driver, Duration.ofSeconds(90));

        System.out.println("✅ Driver Appium initialisé");
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            System.out.println("🛑 Fermeture du driver Appium");
            driver.quit();
        }
    }

    protected WebElement waitForElementWithScroll(By locator) {
        return wait.until(driver -> {
            try {
                return driver.findElement(locator);
            } catch (Exception e) {
                System.out.println("⚠️ Élément non trouvé, tentative de scroll...");
                scrollDown();
                try {
                    return driver.findElement(locator);
                } catch (Exception ex) {
                    return null;
                }
            }
        });
    }

    protected void scrollDown() {
        driver.executeScript("mobile: scrollGesture", Map.of(
                "left", 100,
                "top", 100,
                "width", 200,
                "height", 200,
                "direction", "down",
                "percent", 50.0
        ));
    }

    // ✅ CORRECTION : XPath en MAJUSCULES
    protected void waitForFilesLoaded() {
        System.out.println("⏳ Attente du chargement des fichiers depuis assets...");
        longWait.until(driver -> {
            try {
                List<WebElement> files = driver.findElements(
                        By.xpath("//android.widget.Button[contains(@text, 'TEST_') or contains(@text, 'DATA_') or contains(@text, 'NEW_')]")
                );

                if (!files.isEmpty()) {
                    System.out.println("✅ Fichiers chargés : " + files.size() + " fichier(s) trouvé(s)");
                    for (WebElement file : files) {
                        System.out.println("  - " + file.getText());
                    }
                    return true;
                }
                return false;
            } catch (Exception e) {
                return false;
            }
        });
    }

    protected void waitAndClick(By locator, String elementName) {
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                System.out.println("ℹ️  Clic sur " + elementName + "...");
                WebElement element = waitForElementWithScroll(locator);
                if (element != null && element.isDisplayed()) {
                    element.click();
                    System.out.println("✅ Clic réussi sur " + elementName);
                    return;
                }
            } catch (Exception e) {
                System.out.println("⚠️ Tentative " + attempt + "/" + maxAttempts + " échouée");
                if (attempt < maxAttempts) {
                    scrollDown();
                }
            }
        }
        throw new RuntimeException("❌ Échec du clic après " + maxAttempts + " tentatives: " + locator);
    }

    // ✅ CORRECTION : XPath en MAJUSCULES pour test_data
    protected By getFileButtonLocator(String fileName) {
        String upperFileName = fileName.toUpperCase().replace(".JSON", "");
        return By.xpath("//android.widget.Button[contains(@text, '" + upperFileName + "')]");
    }

    protected void waitForScreen(String screenDescription, int seconds) {
        System.out.println("ℹ️  " + screenDescription + "...");
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("⏩ ✓ Phase terminée\n");
    }
}