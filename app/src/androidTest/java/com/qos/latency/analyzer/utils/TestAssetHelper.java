package com.qos.latency.analyzer.utils;

import android.content.Context;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Helper pour copier les fichiers de test dans les assets de l'application
 */
public class TestAssetHelper {

    /**
     * Copie un fichier depuis androidTest/assets vers main/assets
     * pour que l'application puisse le charger pendant les tests
     */
    public static void copyTestAssetToApp(Context context, String fileName) throws IOException {
        // Cette méthode peut être utilisée si nécessaire
        // Pour l'instant, nous utilisons directement le fichier dans main/assets
    }

    /**
     * Vérifie qu'un fichier existe dans les assets
     */
    public static boolean assetExists(Context context, String fileName) {
        try {
            String[] files = context.getAssets().list("");
            if (files != null) {
                for (String file : files) {
                    if (file.equals(fileName)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}