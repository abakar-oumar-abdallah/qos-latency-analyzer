package com.qos.latency.analyzer.appium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests Appium pour l'application QoS Latency Analyzer
 */
public class QoSLatencyAppiumTest extends BaseAppiumTest {

    /**
     * Test 1 : Vérifier que l'app démarre correctement
     */
    @Test
    public void testAppLaunches() throws InterruptedException {
        String title = waitAndGetText(By.id("com.qos.latency.analyzer:id/tv_title"));
        assertEquals("QoS Latence", title);

        assertTrue(isElementDisplayed(By.id("com.qos.latency.analyzer:id/screen_file_selection")));
    }

    /**
     * Test 2 : Sélectionner le fichier test_data
     */
    @Test
    public void testSelectFile_TestData() throws InterruptedException {
        Thread.sleep(3000);

        waitAndClick(By.xpath("//android.widget.TextView[@text='test_data']"));
        Thread.sleep(2000);

        assertTrue(isElementDisplayed(By.id("com.qos.latency.analyzer:id/screen_animation")));

        String status = waitAndGetText(By.id("com.qos.latency.analyzer:id/tv_status"));
        assertEquals("Prêt pour l'analyse", status);
    }

    /**
     * Test 3 : Bouton "Actualiser"
     */
    @Test
    public void testRefreshButton() throws InterruptedException {
        Thread.sleep(3000);

        assertTrue(isElementDisplayed(By.xpath("//android.widget.TextView[@text='test_data']")));

        waitAndClick(By.id("com.qos.latency.analyzer:id/btn_refresh_files"));
        Thread.sleep(3000);

        assertTrue(isElementDisplayed(By.xpath("//android.widget.TextView[@text='test_data']")));
    }
}