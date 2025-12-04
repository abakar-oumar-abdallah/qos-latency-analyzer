package com.qos.latency.analyzer.appium;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Classe de base pour tous les tests Appium
 * Fournit les méthodes communes de configuration et d'interaction
 */
public abstract class BaseAppiumTest {

    protected AppiumDriver driver;
    protected WebDriverWait shortWait;
    protected WebDriverWait mediumWait;
    protected WebDriverWait longWait;

    // Configuration de l'application
    private static final String APP_PACKAGE = "com.qos.latency.analyzer";
    private static final String APP_ACTIVITY = ".MainActivity";
    private static final String APPIUM_SERVER_URL = "http://127.0.0.1:4723";

    @Before
    public void setUp() throws MalformedURLException {
        System.out.println("\n========================================");
        System.out.println("🚀 Initialisation du test Appium");
        System.out.println("========================================");

        String apkPath = getApkPath();
        System.out.println("📦 APK: " + apkPath);

        UiAutomator2Options options = new UiAutomator2Options();
        options.setDeviceName("Android Device");
        options.setPlatformName("Android");
        options.setApp(apkPath);
        options.setAppPackage(APP_PACKAGE);
        options.setAppActivity(APP_ACTIVITY);
        options.setNoReset(false);
        options.setFullReset(false);
        options.setAutoGrantPermissions(true);
        options.setNewCommandTimeout(Duration.ofSeconds(300));

        System.out.println("🔌 Connexion au serveur Appium: " + APPIUM_SERVER_URL);
        driver = new AndroidDriver(new URL(APPIUM_SERVER_URL), options);

        shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
        mediumWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        longWait = new WebDriverWait(driver, Duration.ofSeconds(30));

        System.out.println("✅ Driver Appium initialisé avec succès");

        waitForFilesLoaded();
    }

    /**
     * Attente robuste du chargement de la page principale + fichiers JSON
     */
    protected void waitForFilesLoaded() {
        System.out.println("⏳ Attente du chargement des fichiers...");
        try {
            handlePermissionsPopup();
            waitForMainLayout();
            waitForJsonFiles();
        } catch (Exception e) {
            System.out.println("❌ Erreur lors du chargement des fichiers: " + e.getMessage());
            printAllVisibleElements();
            takeScreenshot("waitForFilesLoaded_error");
            throw e;
        }
    }

    /**
     * Gère un popup Android potentiel "Autoriser"
     */
    private void handlePermissionsPopup() {
        try {
            Thread.sleep(2000);
            List<WebElement> allowButtons = driver.findElements(
                    By.xpath("//*[contains(@text, 'Autoriser') or contains(@text, 'Allow')]")
            );
            if (!allowButtons.isEmpty()) {
                System.out.println("🔔 Popup permissions détecté → clic sur 'Autoriser'");
                allowButtons.get(0).click();
            }
        } catch (Exception ignored) {}
    }

    /**
     * Attendre le layout principal
     */
    private void waitForMainLayout() {
        try {
            System.out.println("🔍 Vérification du layout principal...");
            longWait.until(ExpectedConditions.presenceOfElementLocated(
                    By.id(APP_PACKAGE + ":id/btn_launch")
            ));
            System.out.println("✅ Layout principal détecté.");
        } catch (Exception e) {
            System.out.println("⚠️ Layout principal introuvable, on continue quand même...");
        }
    }

    /**
     * Attendre l’apparition des fichiers JSON
     */
    private void waitForJsonFiles() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));

        wait.until(driver -> {
            try {
                List<WebElement> files = driver.findElements(
                        By.xpath("//android.widget.Button[contains(@text, 'data_') or contains(@text, 'test_') or contains(@text, 'new_')]")
                );

                if (!files.isEmpty()) {
                    System.out.println("✅ Fichiers chargés : " + files.size() + " fichier(s) trouvé(s)");
                    for (WebElement file : files) {
                        System.out.println("  - " + file.getText());
                    }
                    return true;
                }
                return false;
            } catch (Exception ex) {
                return false;
            }
        });
    }

    @After
    public void tearDown() {
        System.out.println("\n========================================");
        System.out.println("🧹 Nettoyage du test");
        System.out.println("========================================");

        if (driver != null) {
            driver.quit();
            System.out.println("✅ Driver fermé");
        }
    }

    protected By getFileButtonLocator(String fileName) {
        String cleanFileName = fileName.replace(".json", "").replace(".JSON", "");
        return By.xpath("//android.widget.Button[contains(@text, '" + cleanFileName + "')]");
    }

    protected By getLaunchButtonLocator() {
        return By.id(APP_PACKAGE + ":id/btn_launch");
    }

    protected By getStatusTextLocator() {
        return By.id(APP_PACKAGE + ":id/tv_status");
    }

    protected By getProgressBarLocator() {
        return By.id(APP_PACKAGE + ":id/progressBar");
    }

    protected void waitAndClick(By locator, String elementName) {
        System.out.println("🔍 Recherche de l'élément: " + elementName);
        WebElement element = mediumWait.until(ExpectedConditions.elementToBeClickable(locator));
        System.out.println("✅ Élément trouvé: " + elementName);
        element.click();
        System.out.println("👆 Clic effectué sur: " + elementName);
    }

    protected String waitAndGetText(By locator) {
        WebElement element = mediumWait.until(ExpectedConditions.presenceOfElementLocated(locator));
        return element.getText();
    }

    protected boolean isElementDisplayed(By locator, int timeoutSeconds) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            return element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    protected void waitForTextInElement(By locator, String expectedText, int timeoutSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        wait.until(driver -> {
            try {
                WebElement element = driver.findElement(locator);
                String actualText = element.getText();
                System.out.println("📝 Texte actuel: '" + actualText + "' | Attendu: '" + expectedText + "'");
                return actualText.contains(expectedText);
            } catch (Exception e) {
                return false;
            }
        });
    }

    private String getApkPath() {
        String projectDir = System.getProperty("user.dir");

        if (projectDir.endsWith("/app")) {
            projectDir = projectDir.substring(0, projectDir.length() - 4);
        }

        String apkPath = projectDir + "/app/build/outputs/apk/debug/app-debug.apk";
        File apkFile = new File(apkPath);
        if (!apkFile.exists()) {
            fail("❌ APK non trouvé: " + apkPath + "\n💡 Exécutez d'abord: ./gradlew assembleDebug");
        }
        return apkPath;
    }

    protected void printAllVisibleElements() {
        System.out.println("\n📋 Éléments visibles:");
        List<WebElement> elements = driver.findElements(By.xpath("//*[@text]"));
        for (WebElement element : elements) {
            try {
                String text = element.getText();
                if (text != null && !text.isEmpty()) {
                    System.out.println("  - " + element.getTagName() + ": " + text);
                }
            } catch (Exception ignored) {}
        }
    }

    protected void takeScreenshot(String fileName) {
        try {
            File screenshot = driver.getScreenshotAs(org.openqa.selenium.OutputType.FILE);
            String projectDir = System.getProperty("user.dir");

            if (projectDir.endsWith("/app")) {
                projectDir = projectDir.substring(0, projectDir.length() - 4);
            }

            String destPath = projectDir + "/app/build/screenshots/" + fileName + ".png";
            new File(projectDir + "/app/build/screenshots/").mkdirs();

            java.nio.file.Files.copy(
                    screenshot.toPath(),
                    new File(destPath).toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            System.out.println("📸 Capture d'écran: " + destPath);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la capture d'écran: " + e.getMessage());
        }
    }
}
