package com.qos.latency.analyzer;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
 * @version 3.1
 */
public class MainActivity extends AppCompatActivity implements LatencyController.ControllerListener {

    private static final String TAG = "QoS_MainActivity";

    // ========================================
    // NOUVEAUX ATTRIBUTS POUR COPIE ASSETS
    // ========================================
    private static final String PREFS_NAME = "QoS_Prefs";
    private static final String KEY_ASSETS_COPIED = "assets_copied_v1";
    private static final int REQUEST_PERMISSIONS = 100;
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
        Log.d(TAG, "Android Version: " + Build.VERSION.SDK_INT);
        Log.d(TAG, "========================================");

        // ========================================
        // NOUVEAU : Vérifier et copier les assets
        // ========================================
        checkAndRequestPermissions();

        initViews();
        initMVC();
        setupEvents();

        showScreen(0);
        loadAvailableFiles();
    }

    // ========================================
    // NOUVELLES MÉTHODES POUR COPIE ASSETS
    // ========================================

    /**
     * Vérifie si les permissions de stockage sont accordées
     */
    private boolean hasStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int readPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            int writePermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return readPermission == PackageManager.PERMISSION_GRANTED &&
                    writePermission == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    /**
     * Vérifie et demande les permissions nécessaires
     */
    private void checkAndRequestPermissions() {
        if (hasStoragePermissions()) {
            Log.d(TAG, "✅ Permissions de stockage accordées");
            copyAssetsToExternalStorageIfNeeded();
        } else {
            Log.d(TAG, "⚠️  Demande des permissions de stockage");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        },
                        REQUEST_PERMISSIONS);
            }
        }
    }

    /**
     * Callback après demande de permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                Log.d(TAG, "✅ Permissions accordées par l'utilisateur");
                copyAssetsToExternalStorageIfNeeded();
            } else {
                Log.e(TAG, "❌ Permissions refusées");
                Toast.makeText(this,
                        "Permissions nécessaires pour copier les fichiers",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Copie les assets vers le stockage externe seulement si pas déjà fait
     */
    private void copyAssetsToExternalStorageIfNeeded() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean assetsCopied = prefs.getBoolean(KEY_ASSETS_COPIED, false);

        if (!assetsCopied) {
            Log.d(TAG, "🔄 Première exécution : copie des assets");
            boolean success = copyAssetsToExternalStorage();

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
     * Copie tous les fichiers JSON depuis assets/ vers /storage/emulated/0/QoS_Data/
     * @return true si succès, false sinon
     */
    private boolean copyAssetsToExternalStorage() {
        try {
            File qosDataDir = new File(Environment.getExternalStorageDirectory(), QOS_DATA_FOLDER);

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
        // Récupération des écrans principaux
        screens[0] = findViewById(R.id.screen_file_selection);
        screens[1] = findViewById(R.id.screen_animation);

        // Récupération des composants UI
        tvSelectedFile = findViewById(R.id.tv_selected_file);
        fileSelectionContainer = findViewById(R.id.file_selection_container);
        tvStatus = findViewById(R.id.tv_status);
        tvSeriesInfo = findViewById(R.id.tv_series_info);
        btnLaunch = findViewById(R.id.btn_launch);
        chartView = findViewById(R.id.chart_view);
    }

    /**
     * Initialise les composants du pattern MVC.
     * Crée les instances du modèle, contrôleur et établit les liens entre eux.
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
        // Bouton de lancement de l'animation
        btnLaunch.setOnClickListener(v -> {
            if (!controller.isAnimating() && model.hasData()) {
                controller.startAnimation();
            }
        });

        // Bouton pour revenir à l'écran de sélection de fichier
        findViewById(R.id.btn_change_file).setOnClickListener(v -> {
            if (controller != null) {
                controller.stopAnimation();
            }
            showScreen(0);
        });

        // Bouton pour rafraîchir la liste des fichiers
        findViewById(R.id.btn_refresh_files).setOnClickListener(v -> loadAvailableFiles());
    }

    /**
     * Charge et affiche tous les fichiers JSON disponibles dans les assets.
     * Crée dynamiquement un bouton pour chaque fichier trouvé.
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
     *
     * @param fileName Nom du fichier JSON à représenter
     * @return Bouton configuré avec le bon style et gestionnaire d'événement
     */
    private Button createSimpleFileButton(String fileName) {
        Button button = new Button(this);
        button.setText(fileName.replace(".json", ""));
        button.setTextColor(0xFFFFFFFF);
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF2196F3));

        // Configuration de la mise en page
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 20);
        button.setLayoutParams(params);

        // Gestionnaire d'événement pour la sélection du fichier
        button.setOnClickListener(v -> selectFile(fileName));
        return button;
    }

    /**
     * Traite la sélection d'un fichier par l'utilisateur.
     * Charge les données du fichier et passe à l'écran d'animation.
     *
     * @param fileName Nom du fichier sélectionné
     */
    private void selectFile(String fileName) {
        selectedFileName = fileName;
        tvSelectedFile.setText("Fichier : " + fileName.replace(".json", ""));

        // Réinitialise les composants MVC pour le nouveau fichier
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
     *
     * @param screenIndex Index de l'écran à afficher (0=sélection, 1=animation)
     */
    private void showScreen(int screenIndex) {
        for (int i = 0; i < screens.length; i++) {
            screens[i].setVisibility(i == screenIndex ? View.VISIBLE : View.GONE);
        }

        // Configuration spéciale pour l'écran d'animation
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
     * Utilise le temps de réception pour les paquets reçus, le temps d'envoi pour les perdus.
     *
     * @param packet Données du paquet à analyser
     * @return Temps d'affichage en millisecondes
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
     *
     * @param packet Données du paquet
     * @return Texte décrivant le statut du paquet
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
    // Ces méthodes sont appelées par le contrôleur pour notifier des changements d'état

    /**
     * Appelé quand l'animation démarre.
     * Met à jour l'interface pour indiquer que l'animation est en cours.
     */
    @Override
    public void onAnimationStarted() {
        btnLaunch.setEnabled(false);
        btnLaunch.setText("Animation...");
        btnLaunch.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0xFFFF9800));
    }

    /**
     * Appelé quand l'animation se termine.
     */
    @Override
    public void onAnimationFinished() {
        tvStatus.setText("Animation terminée");
    }

    /**
     * Appelé au début d'une nouvelle analyse.
     *
     * @param analysisName Nom/description de l'analyse qui commence
     */
    @Override
    public void onAnalysisStarted(String analysisName) {
        tvStatus.setText(analysisName);
        tvSeriesInfo.setText("Format request_array");
    }

    /**
     * Appelé à chaque fois qu'un nouveau paquet est affiché sur le graphique.
     * Met à jour les informations affichées à l'utilisateur.
     *
     * @param packet Données du paquet qui vient d'être affiché
     * @param displayedCount Nombre de paquets déjà affichés
     * @param totalCount Nombre total de paquets à afficher
     */
    @Override
    public void onPacketDisplayed(LatencyModel.RequestData packet, int displayedCount, int totalCount) {
        String statusText = getStatusText(packet);
        double displayTimeMs = getDisplayTimeMs(packet);

        tvStatus.setText(String.format("Paquet %d/%d : %s à %.0f ms",
                displayedCount, totalCount, statusText, displayTimeMs));

        // Affiche des informations détaillées selon le type de paquet
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

    /**
     * Appelé pendant la pause de visualisation, à chaque seconde.
     *
     * @param remainingSeconds Nombre de secondes restantes avant la fin
     */
    @Override
    public void onVisualizationPauseCountdown(int remainingSeconds) {
        tvStatus.setText("Vue - " + remainingSeconds + " secondes restantes");
        btnLaunch.setText(remainingSeconds + "s");
        if (remainingSeconds <= 3) {
            btnLaunch.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFFFF5722));
        }
    }

    /**
     * Appelé quand la pause de visualisation se termine.
     * Remet l'interface dans son état initial.
     */
    @Override
    public void onVisualizationPauseFinished() {
        tvStatus.setText("Analyse OK – Sélectionnez fichier");
        btnLaunch.setText("Terminé");
        btnLaunch.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0xFF4CAF50));
    }

    /**
     * Appelé quand l'activité est détruite.
     * Nettoie les ressources pour éviter les fuites mémoire.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (controller != null) {
            controller.cleanup();
        }
    }
}