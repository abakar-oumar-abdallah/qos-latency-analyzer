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

        // ✅ CORRECTION : Lecture robuste de la propriété phone.udid avec logs de debug
        String phoneUdid = System.getProperty("phone.udid");

        // Logs de debug pour diagnostic
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("📋 DEBUG - Configuration Appium");
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("Propriété 'phone.udid' reçue : " + phoneUdid);

        // Afficher toutes les propriétés pertinentes
        System.out.println("\n🔍 Propriétés système pertinentes :");
        System.getProperties().forEach((key, value) -> {
            String keyStr = key.toString();
            if (keyStr.contains("phone") || keyStr.contains("appium")) {
                System.out.println("   " + keyStr + " = " + value);
            }
        });

        // Vérification et fallback
        if (phoneUdid == null || phoneUdid.trim().isEmpty()) {
            phoneUdid = "192.168.1.109:5555";
            System.out.println("\n⚠️  ATTENTION : Propriété 'phone.udid' non reçue");
            System.out.println("   → Utilisation du port par défaut : " + phoneUdid);
        } else {
            System.out.println("\n✅ Propriété 'phone.udid' correctement reçue");
        }

        System.out.println("\n🔌 Connexion à l'appareil : " + phoneUdid);
        System.out.println("═══════════════════════════════════════════════════════\n");

        options.setUdid(phoneUdid);

        options.setNoReset(false);
        options.setFullReset(false);
        options.setNewCommandTimeout(Duration.ofSeconds(300));

        options.setCapability("settings[waitForIdleTimeout]", 50);
        options.setCapability("settings[waitForSelectorTimeout]", 500);

        // URL dynamique pour supporter exécution locale et Jenkins
        String appiumServer = System.getProperty("appium.server", "http://127.0.0.1:4723");
        System.out.println("🌐 Serveur Appium : " + appiumServer);

        driver = new AndroidDriver(new URL(appiumServer), options);

        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        longWait = new WebDriverWait(driver, Duration.ofSeconds(90));

        System.out.println("✅ Driver Appium initialisé avec succès\n");
        Thread.sleep(2000);
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            System.out.println("🛑 Fermeture du driver Appium");
            driver.quit();
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