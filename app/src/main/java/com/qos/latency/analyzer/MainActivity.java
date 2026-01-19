package com.qos.latency.analyzer;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.qos.latency.analyzer.controller.LatencyController;
import com.qos.latency.analyzer.model.LatencyModel;
import com.qos.latency.analyzer.view.ChartView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Activité principale de l'application d'analyse de latence réseau.
 *
 * Cette classe gère l'interface utilisateur complète de l'application, incluant :
 * - L'écran de sélection des fichiers JSON
 * - L'écran d'animation du graphique
 * - La coordination entre le modèle, la vue et le contrôleur
 * - La gestion des événements utilisateur (boutons, sélection de fichiers)
 *
 * L'activité implémente le pattern MVC (Model-View-Controller) et sert de point
 * d'entrée principal pour l'utilisateur.
 *
 * @author Équipe QoS Gaming
 * @version 3.2
 */
public class MainActivity extends AppCompatActivity implements LatencyController.ControllerListener {

    private static final String TAG = "QoS_MainActivity";

    // ========================================
    // CONSTANTES POUR COPIE ASSETS
    // ========================================
    private static final String PREFS_NAME = "QoS_Prefs";
    private static final String KEY_ASSETS_COPIED = "assets_copied_v2";
    private static final String QOS_DATA_FOLDER = "QoS_Data";

    // Composants MVC
    private LatencyModel model;
    private LatencyController controller;
    private ChartView chartView;

    // Gestion des écrans
    private View[] screens = new View[2];
    private String selectedFileName = "";

    // Éléments de l'interface utilisateur
    private TextView tvStatus;
    private TextView tvSeriesInfo;
    private Button btnLaunch;
    private TextView tvSelectedFile;
    private LinearLayout fileSelectionContainer;

    /**
     * Méthode appelée à la création de l'activité.
     * Initialise tous les composants et affiche l'écran de sélection de fichier.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "========================================");
        Log.d(TAG, "MainActivity onCreate()");
        Log.d(TAG, "Android Version: " + android.os.Build.VERSION.SDK_INT);
        Log.d(TAG, "========================================");

        // ========================================
        // NOUVEAU : Copie automatique des assets
        // Sans permissions requises !
        // ========================================
        copyAssetsToAppStorageIfNeeded();

        initViews();
        initMVC();
        setupEvents();

        showScreen(0);
        loadAvailableFiles();
    }

    // ========================================
    // NOUVELLES MÉTHODES POUR COPIE ASSETS
    // (Sans permissions requises)
    // ========================================

    /**
     * Copie les assets vers le stockage de l'app seulement si pas déjà fait.
     * Utilise getExternalFilesDir() qui ne nécessite AUCUNE permission sur Android 11+
     */
    private void copyAssetsToAppStorageIfNeeded() {
        android.content.SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean assetsCopied = prefs.getBoolean(KEY_ASSETS_COPIED, false);

        if (!assetsCopied) {
            Log.d(TAG, "🔄 Première exécution : copie des assets");
            boolean success = copyAssetsToAppStorage();

            if (success) {
                prefs.edit().putBoolean(KEY_ASSETS_COPIED, true).apply();
                Log.d(TAG, "✅ Assets copiés avec succès");
                Toast.makeText(this,
                        "Fichiers de données initialisés",
                        Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "❌ Échec de la copie des assets");
            }
        } else {
            Log.d(TAG, "✅ Assets déjà copiés précédemment");
        }
    }

    /**
     * Retourne le dossier de destination pour les fichiers JSON.
     * Utilise getExternalFilesDir() qui ne nécessite AUCUNE permission.
     *
     * @return Dossier QoS_Data dans le stockage de l'app
     */
    private File getQoSDataDirectory() {
        // getExternalFilesDir() ne nécessite AUCUNE permission sur Android 11+
        File appExternalDir = getExternalFilesDir(null);

        if (appExternalDir == null) {
            Log.e(TAG, "❌ getExternalFilesDir() retourne null");
            return null;
        }

        File qosDataDir = new File(appExternalDir, QOS_DATA_FOLDER);
        Log.d(TAG, "📂 Dossier QoS_Data : " + qosDataDir.getAbsolutePath());

        return qosDataDir;
    }

    /**
     * Copie tous les fichiers JSON depuis assets/ vers le stockage de l'app.
     * Ne nécessite AUCUNE permission sur Android 11+
     *
     * @return true si succès, false sinon
     */
    private boolean copyAssetsToAppStorage() {
        try {
            File qosDataDir = getQoSDataDirectory();

            if (qosDataDir == null) {
                Log.e(TAG, "❌ Impossible d'obtenir le dossier de destination");
                return false;
            }

            if (!qosDataDir.exists()) {
                boolean created = qosDataDir.mkdirs();
                if (created) {
                    Log.d(TAG, "📂 Dossier créé: " + qosDataDir.getAbsolutePath());
                } else {
                    Log.e(TAG, "❌ Impossible de créer le dossier");
                    return false;
                }
            } else {
                Log.d(TAG, "📂 Dossier existe: " + qosDataDir.getAbsolutePath());
            }

            String[] assetFiles = getAssets().list("");

            if (assetFiles == null || assetFiles.length == 0) {
                Log.w(TAG, "⚠️  Aucun fichier dans assets/");
                return false;
            }

            int copiedCount = 0;

            for (String filename : assetFiles) {
                if (filename.endsWith(".json")) {
                    File destination = new File(qosDataDir, filename);
                    boolean copied = copyAssetFile(filename, destination);

                    if (copied) {
                        copiedCount++;
                        Log.d(TAG, "✅ Copié [" + copiedCount + "]: " + filename);
                    }
                }
            }

            Log.d(TAG, "========================================");
            Log.d(TAG, "✅ COPIE TERMINÉE: " + copiedCount + " fichier(s)");
            Log.d(TAG, "📍 Destination: " + qosDataDir.getAbsolutePath());
            Log.d(TAG, "========================================");

            return copiedCount > 0;

        } catch (IOException e) {
            Log.e(TAG, "❌ Erreur copie assets: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Copie un fichier asset individuel vers une destination
     *
     * @param assetFilename Nom du fichier dans assets/
     * @param destination Fichier de destination
     * @return true si succès, false sinon
     */
    private boolean copyAssetFile(String assetFilename, File destination) {
        InputStream in = null;
        OutputStream out = null;

        try {
            if (destination.exists()) {
                Log.d(TAG, "⏭️  Fichier existe (skip): " + assetFilename);
                return true;
            }

            in = getAssets().open(assetFilename);
            out = new FileOutputStream(destination);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            out.flush();
            return true;

        } catch (IOException e) {
            Log.e(TAG, "❌ Erreur copie [" + assetFilename + "]: " + e.getMessage());
            return false;

        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
            } catch (IOException e) {
                Log.e(TAG, "Erreur fermeture flux: " + e.getMessage());
            }
        }
    }

    // ========================================
    // MÉTHODES ORIGINALES (INCHANGÉES)
    // ========================================

    /**
     * Récupère toutes les références vers les éléments de l'interface utilisateur.
     * Cette méthode doit être appelée après setContentView().
     */
    private void initViews() {
        screens[0] = findViewById(R.id.screen_file_selection);
        screens[1] = findViewById(R.id.screen_animation);

        tvSelectedFile = findViewById(R.id.tv_selected_file);
        fileSelectionContainer = findViewById(R.id.file_selection_container);
        tvStatus = findViewById(R.id.tv_status);
        tvSeriesInfo = findViewById(R.id.tv_series_info);
        btnLaunch = findViewById(R.id.btn_launch);
        chartView = findViewById(R.id.chart_view);
    }

    /**
     * Initialise les composants du pattern MVC.
     */
    private void initMVC() {
        model = new LatencyModel();
        controller = new LatencyController(model, chartView, this);
        controller.setListener(this);
    }

    /**
     * Configure tous les gestionnaires d'événements pour les boutons de l'interface.
     */
    private void setupEvents() {
        btnLaunch.setOnClickListener(v -> {
            if (!controller.isAnimating() && model.hasData()) {
                controller.startAnimation();
            }
        });

        findViewById(R.id.btn_change_file).setOnClickListener(v -> {
            if (controller != null) {
                controller.stopAnimation();
            }
            showScreen(0);
        });

        findViewById(R.id.btn_refresh_files).setOnClickListener(v -> loadAvailableFiles());
    }

    /**
     * Charge et affiche tous les fichiers JSON disponibles dans le stockage de l'app.
     */
    private void loadAvailableFiles() {
        fileSelectionContainer.removeAllViews();
        List<String> availableFiles = LatencyModel.getAvailableDataFiles(this);

        if (availableFiles.isEmpty()) {
            TextView noFilesText = new TextView(this);
            noFilesText.setText("Aucun fichier JSON trouvé");
            noFilesText.setTextSize(16);
            noFilesText.setTextColor(0xFFE53935);
            fileSelectionContainer.addView(noFilesText);
            return;
        }

        for (String fileName : availableFiles) {
            Button fileButton = createSimpleFileButton(fileName);
            fileSelectionContainer.addView(fileButton);
        }
    }

    /**
     * Crée un bouton stylisé pour la sélection d'un fichier.
     */
    private Button createSimpleFileButton(String fileName) {
        Button button = new Button(this);
        button.setText(fileName.replace(".json", ""));
        button.setTextColor(0xFFFFFFFF);
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF2196F3));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 20);
        button.setLayoutParams(params);

        button.setOnClickListener(v -> selectFile(fileName));
        return button;
    }

    /**
     * Traite la sélection d'un fichier par l'utilisateur.
     */
    private void selectFile(String fileName) {
        selectedFileName = fileName;
        tvSelectedFile.setText("Fichier : " + fileName.replace(".json", ""));

        model = new LatencyModel();
        controller = new LatencyController(model, chartView, this);
        controller.setListener(this);

        try {
            model.loadData(this, fileName);
            showScreen(1);
            tvStatus.setText("Prêt pour l'analyse");
        } catch (Exception e) {
            tvStatus.setText("Erreur : " + e.getMessage());
        }
    }

    /**
     * Affiche l'écran spécifié et masque tous les autres.
     */
    private void showScreen(int screenIndex) {
        for (int i = 0; i < screens.length; i++) {
            screens[i].setVisibility(i == screenIndex ? View.VISIBLE : View.GONE);
        }

        if (screenIndex == 1 && !selectedFileName.isEmpty()) {
            tvStatus.setText("Prêt pour l'analyse");
            tvSeriesInfo.setText("Temps (s) vs RTT (ms)");
            btnLaunch.setEnabled(true);
            btnLaunch.setText("Lancer");
            btnLaunch.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFF4CAF50));
        }
    }

    /**
     * Calcule le temps d'affichage en millisecondes selon le statut du paquet.
     */
    private double getDisplayTimeMs(LatencyModel.RequestData packet) {
        switch (packet.getStatus()) {
            case RECEIVED:
            case DUPLICATED:
            case REVERSED:
                return packet.getRxTimeRelative() * 1000;
            case LOST:
            default:
                return packet.getTxTimeRelative() * 1000;
        }
    }

    /**
     * Convertit le statut d'un paquet en texte lisible pour l'interface.
     */
    private String getStatusText(LatencyModel.RequestData packet) {
        switch (packet.getStatus()) {
            case RECEIVED: return "Reçu";
            case LOST: return "PERDU";
            case DUPLICATED: return "Dupliqué";
            case REVERSED: return "Inversé";
            default: return "Inconnu";
        }
    }

    // Implémentation des callbacks du ControllerListener

    @Override
    public void onAnimationStarted() {
        btnLaunch.setEnabled(false);
        btnLaunch.setText("Animation...");
        btnLaunch.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0xFFFF9800));
    }

    @Override
    public void onAnimationFinished() {
        tvStatus.setText("Animation terminée");
    }

    @Override
    public void onAnalysisStarted(String analysisName) {
        tvStatus.setText(analysisName);
        tvSeriesInfo.setText("Format request_array");
    }

    @Override
    public void onPacketDisplayed(LatencyModel.RequestData packet, int displayedCount, int totalCount) {
        String statusText = getStatusText(packet);
        double displayTimeMs = getDisplayTimeMs(packet);

        tvStatus.setText(String.format("Paquet %d/%d : %s à %.0f ms",
                displayedCount, totalCount, statusText, displayTimeMs));

        if (packet.getStatus() == LatencyModel.PacketStatus.RECEIVED) {
            tvSeriesInfo.setText(String.format("Séq %d : RTT=%.1f ms, RX=%.0f ms",
                    packet.getSequenceNumber(), packet.getRtt(), displayTimeMs));
        } else if (packet.getStatus() == LatencyModel.PacketStatus.LOST) {
            tvSeriesInfo.setText(String.format("Séq %d : %s, TX=%.0f ms",
                    packet.getSequenceNumber(), statusText, displayTimeMs));
        } else {
            tvSeriesInfo.setText(String.format("Séq %d : %s, RTT=%.1f ms",
                    packet.getSequenceNumber(), statusText, packet.getRtt()));
        }
    }

    @Override
    public void onVisualizationPauseCountdown(int remainingSeconds) {
        tvStatus.setText("Vue - " + remainingSeconds + " secondes restantes");
        btnLaunch.setText(remainingSeconds + "s");
        if (remainingSeconds <= 3) {
            btnLaunch.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFFFF5722));
        }
    }

    @Override
    public void onVisualizationPauseFinished() {
        tvStatus.setText("Analyse OK – Sélectionnez fichier");
        btnLaunch.setText("Terminé");
        btnLaunch.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0xFF4CAF50));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (controller != null) {
            controller.cleanup();
        }
    }
}