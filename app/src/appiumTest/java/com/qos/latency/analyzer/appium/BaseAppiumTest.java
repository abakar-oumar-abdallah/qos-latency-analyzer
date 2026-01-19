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
import java.util.List;

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

        driver = new AndroidDriver(new URL("http://127.0.0.1:4723"), options);

        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        longWait = new WebDriverWait(driver, Duration.ofSeconds(45));

        Thread.sleep(2000);
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
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

    protected void waitForFilesLoaded() {
        longWait.until(driver -> {
            try {
                List<WebElement> files = driver.findElements(
                        By.xpath("//android.widget.TextView[contains(@text, '.json') or @text='test_data' or @text='new_data' or contains(@text, 'latency')]")
                );
                return files.size() > 0;
            } catch (Exception e) {
                return false;
            }
        });
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