package com.qos.latency.analyzer.utils;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.IOException;

/**
 * Rule JUnit qui désactive automatiquement les animations système
 * avant chaque test et les réactive après
 */
public class DisableAnimationsRule implements TestRule {

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                // Désactiver les animations avant le test
                disableAnimations();

                try {
                    // Exécuter le test
                    base.evaluate();
                } finally {
                    // Réactiver les animations après le test (optionnel)
                    // enableAnimations();
                }
            }
        };
    }

    /**
     * Désactive les 3 types d'animations système
     */
    private void disableAnimations() throws IOException {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // Désactiver l'échelle d'animation de fenêtre
        device.executeShellCommand("settings put global window_animation_scale 0");

        // Désactiver l'échelle d'animation de transition
        device.executeShellCommand("settings put global transition_animation_scale 0");

        // Désactiver l'échelle de durée d'animation
        device.executeShellCommand("settings put global animator_duration_scale 0");
    }

    /**
     * Réactive les animations (si nécessaire)
     */
    private void enableAnimations() throws IOException {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        device.executeShellCommand("settings put global window_animation_scale 1");
        device.executeShellCommand("settings put global transition_animation_scale 1");
        device.executeShellCommand("settings put global animator_duration_scale 1");
    }
}