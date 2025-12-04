package com.qos.latency.analyzer.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.provider.Settings;
import android.location.LocationManager;
import android.os.PowerManager;
import android.util.Log;

/**
 * Gestionnaire du mode économie d'énergie basé sur le niveau de batterie
 *
 * Fonctionnalités :
 * - Surveillance du niveau de batterie
 * - Activation automatique du mode éco si batterie < 60%
 * - Gestion du mode avion, économie d'énergie, localisation
 */
public class BatteryMonitor {
    private static final String TAG = "BatteryMonitor";
    private static final int ECO_MODE_THRESHOLD = 60; // Seuil de batterie en %

    private Context context;
    private boolean ecoModeActive = false;

    public BatteryMonitor(Context context) {
        this.context = context;
    }

    /**
     * Obtient le niveau actuel de la batterie
     * @return niveau de batterie en pourcentage (0-100), ou -1 si erreur
     */
    public int getBatteryLevel() {
        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, filter);

            if (batteryStatus != null) {
                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                if (level != -1 && scale != -1) {
                    float batteryPct = (level / (float) scale) * 100;
                    Log.d(TAG, "Niveau de batterie détecté: " + (int)batteryPct + "%");
                    return (int) batteryPct;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lecture niveau batterie: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Vérifie si le niveau de batterie nécessite l'activation du mode éco
     * @return true si batterie < 60%
     */
    public boolean shouldActivateEcoMode() {
        int batteryLevel = getBatteryLevel();
        boolean shouldActivate = batteryLevel > 0 && batteryLevel < ECO_MODE_THRESHOLD;

        if (shouldActivate) {
            Log.i(TAG, "Mode éco requis: batterie à " + batteryLevel + "% (< " + ECO_MODE_THRESHOLD + "%)");
        } else {
            Log.d(TAG, "Mode éco non nécessaire: batterie à " + batteryLevel + "%");
        }

        return shouldActivate;
    }

    /**
     * Active le mode économie d'énergie complet
     * Note: Certaines actions nécessitent des permissions spéciales sur Android moderne
     */
    public void activateEcoMode() {
        if (ecoModeActive) {
            Log.d(TAG, "Mode éco déjà actif");
            return;
        }

        Log.i(TAG, "========================================");
        Log.i(TAG, "ACTIVATION DU MODE ÉCO");
        Log.i(TAG, "========================================");

        int batteryLevel = getBatteryLevel();
        Log.i(TAG, "Niveau de batterie: " + batteryLevel + "%");

        // 1. Activer le mode avion
        boolean airplaneResult = enableAirplaneMode(true);
        Log.i(TAG, "1. Mode avion: " + (airplaneResult ? "✓ ACTIVÉ" : "✗ ÉCHEC"));

        // 2. Vérifier le mode économie d'énergie système
        boolean powerSaveStatus = checkPowerSaveMode();
        Log.i(TAG, "2. Mode économie système: " + (powerSaveStatus ? "✓ ACTIF" : "○ Inactif"));

        // 3. Désactiver la localisation
        boolean locationResult = disableLocation();
        Log.i(TAG, "3. Localisation: " + (locationResult ? "✓ DÉSACTIVÉE" : "✗ ÉCHEC"));

        // 4. Préparer l'extinction de l'écran
        boolean screenResult = prepareScreenOff();
        Log.i(TAG, "4. Préparation écran: " + (screenResult ? "✓ OK" : "○ Info seulement"));

        ecoModeActive = true;
        Log.i(TAG, "========================================");
        Log.i(TAG, "MODE ÉCO ACTIVÉ");
        Log.i(TAG, "========================================");
    }

    /**
     * Désactive le mode économie d'énergie
     */
    public void deactivateEcoMode() {
        if (!ecoModeActive) {
            Log.d(TAG, "Mode éco déjà inactif");
            return;
        }

        Log.i(TAG, "========================================");
        Log.i(TAG, "DÉSACTIVATION DU MODE ÉCO");
        Log.i(TAG, "========================================");

        // Désactiver le mode avion
        boolean airplaneResult = enableAirplaneMode(false);
        Log.i(TAG, "Mode avion désactivé: " + (airplaneResult ? "✓" : "✗"));

        ecoModeActive = false;
        Log.i(TAG, "Mode éco désactivé");
        Log.i(TAG, "========================================");
    }

    /**
     * Vérifie si le mode éco est actuellement actif
     */
    public boolean isEcoModeActive() {
        return ecoModeActive;
    }

    /**
     * Obtient le seuil de déclenchement du mode éco
     */
    public int getEcoModeThreshold() {
        return ECO_MODE_THRESHOLD;
    }

    // ========================================
    // MÉTHODES PRIVÉES POUR CHAQUE ACTION
    // ========================================

    /**
     * Active ou désactive le mode avion
     * Note: Nécessite WRITE_SECURE_SETTINGS sur Android moderne
     */
    private boolean enableAirplaneMode(boolean enable) {
        try {
            // Méthode compatible Android 4.2+
            Settings.Global.putInt(
                    context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON,
                    enable ? 1 : 0
            );

            // Broadcast pour notifier le changement
            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
            intent.putExtra("state", enable);
            context.sendBroadcast(intent);

            Log.d(TAG, "Mode avion " + (enable ? "activé" : "désactivé"));
            return true;

        } catch (SecurityException e) {
            Log.e(TAG, "Permission manquante pour mode avion: " + e.getMessage());
            Log.e(TAG, "Utilisez: adb shell pm grant com.qos.latency.analyzer android.permission.WRITE_SECURE_SETTINGS");
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Erreur activation mode avion: " + e.getMessage());
            return false;
        }
    }

    /**
     * Vérifie si le mode économie d'énergie système est actif
     * Note: L'application ne peut pas forcer l'activation, seulement vérifier
     */
    private boolean checkPowerSaveMode() {
        try {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                boolean isPowerSaveMode = pm.isPowerSaveMode();
                Log.d(TAG, "Mode économie d'énergie système: " + isPowerSaveMode);
                return isPowerSaveMode;
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur vérification mode économie: " + e.getMessage());
        }
        return false;
    }

    /**
     * Désactive la localisation
     * Note: Sur Android moderne, l'app ne peut pas forcer, seulement vérifier
     */
    private boolean disableLocation() {
        try {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (lm != null) {
                boolean gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                Log.d(TAG, "GPS activé: " + gpsEnabled);
                Log.d(TAG, "Localisation réseau activée: " + networkEnabled);

                // L'app ne peut pas désactiver directement, mais peut le vérifier
                if (!gpsEnabled && !networkEnabled) {
                    Log.d(TAG, "Localisation déjà désactivée");
                    return true;
                } else {
                    Log.w(TAG, "Localisation encore active - nécessite action utilisateur ou ADB");
                    return false;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur vérification localisation: " + e.getMessage());
        }
        return false;
    }

    /**
     * Prépare l'extinction de l'écran
     * Note: L'extinction complète nécessite des permissions spéciales
     */
    private boolean prepareScreenOff() {
        try {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                // Vérifier l'état de l'écran
                boolean isScreenOn = pm.isInteractive();
                Log.d(TAG, "Écran actuellement: " + (isScreenOn ? "allumé" : "éteint"));

                // Note: L'extinction réelle nécessite DEVICE_POWER ou est gérée par le système
                Log.d(TAG, "L'écran s'éteindra automatiquement avec le timeout système");
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur préparation extinction écran: " + e.getMessage());
        }
        return false;
    }

    // ========================================
    // MÉTHODES DE VÉRIFICATION D'ÉTAT
    // ========================================

    /**
     * Vérifie si le mode avion est actuellement activé
     */
    public boolean isAirplaneModeOn() {
        try {
            int airplaneMode = Settings.Global.getInt(
                    context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON,
                    0
            );
            return airplaneMode != 0;
        } catch (Exception e) {
            Log.e(TAG, "Erreur vérification mode avion: " + e.getMessage());
            return false;
        }
    }

    /**
     * Vérifie si le mode économie d'énergie système est activé
     */
    public boolean isPowerSaveModeOn() {
        try {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return pm != null && pm.isPowerSaveMode();
        } catch (Exception e) {
            Log.e(TAG, "Erreur vérification PowerSaveMode: " + e.getMessage());
            return false;
        }
    }

    /**
     * Vérifie si la localisation est activée
     */
    public boolean isLocationEnabled() {
        try {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (lm != null) {
                boolean gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                return gpsEnabled || networkEnabled;
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur vérification localisation: " + e.getMessage());
        }
        return false;
    }

    /**
     * Vérifie si l'écran est allumé
     */
    public boolean isScreenOn() {
        try {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return pm != null && pm.isInteractive();
        } catch (Exception e) {
            Log.e(TAG, "Erreur vérification écran: " + e.getMessage());
            return true; // Par défaut, on suppose que l'écran est allumé
        }
    }

    /**
     * Retourne un résumé complet de l'état du système
     */
    public String getSystemStatusSummary() {
        StringBuilder status = new StringBuilder();
        status.append("État du système:\n");
        status.append("- Batterie: ").append(getBatteryLevel()).append("%\n");
        status.append("- Mode éco app: ").append(ecoModeActive ? "ACTIF" : "INACTIF").append("\n");
        status.append("- Mode avion: ").append(isAirplaneModeOn() ? "OUI" : "NON").append("\n");
        status.append("- Mode économie système: ").append(isPowerSaveModeOn() ? "OUI" : "NON").append("\n");
        status.append("- Localisation: ").append(isLocationEnabled() ? "ACTIVÉE" : "DÉSACTIVÉE").append("\n");
        status.append("- Écran: ").append(isScreenOn() ? "ALLUMÉ" : "ÉTEINT").append("\n");
        return status.toString();
    }
}