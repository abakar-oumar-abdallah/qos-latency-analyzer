package com.qos.latency.analyzer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.qos.latency.analyzer.model.LatencyModel;
import com.qos.latency.analyzer.utils.BatteryMonitor;
import com.qos.latency.analyzer.view.ChartView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Activité principale de l'application QoS Latency Analyzer
 *
 * Fonctionnalités :
 * - Sélection de fichiers JSON contenant des données de latence
 * - Visualisation graphique animée des paquets réseau
 * - Gestion automatique du mode économie d'énergie basé sur la batterie
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // Vues principales
    private TextView tvTitle;
    private TextView tvSelectedFile;
    private TextView tvStatus;
    private TextView tvSeriesInfo;
    private Button btnLaunch;
    private Button btnChangeFile;
    private Button btnRefreshFiles;
    private Button btnDeactivateEcoMode;
    private ChartView chartView;

    // Conteneurs d'écrans
    private LinearLayout screenFileSelection;
    private LinearLayout screenAnimation;
    private LinearLayout fileSelectionContainer;

    // Modèle de données et gestionnaire de batterie
    private LatencyModel model;
    private BatteryMonitor batteryMonitor;

    // Gestion de l'animation
    private Handler animationHandler;
    private boolean isAnimating = false;
    private int currentPacketIndex = 0;
    private static final int ANIMATION_DELAY_MS = 300; // 300ms entre chaque paquet

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "========================================");
        Log.i(TAG, "DÉMARRAGE DE L'APPLICATION");
        Log.i(TAG, "========================================");

        // 1. Initialiser le modèle et le moniteur de batterie
        model = new LatencyModel();
        batteryMonitor = new BatteryMonitor(this);
        animationHandler = new Handler(Looper.getMainLooper());

        // 2. Initialiser les vues
        initializeViews();

        // 3. Vérifier la batterie et activer le mode éco si nécessaire
        checkBatteryAndActivateEcoMode();

        // 4. Charger les fichiers disponibles
        loadAvailableFiles();

        // 5. Configurer les listeners
        setupListeners();

        Log.i(TAG, "Application initialisée avec succès");
    }

    /**
     * Initialise toutes les vues de l'interface
     */
    private void initializeViews() {
        Log.d(TAG, "Initialisation des vues...");

        // Vues communes
        tvTitle = findViewById(R.id.tv_title);

        // Écran de sélection de fichier
        screenFileSelection = findViewById(R.id.screen_file_selection);
        tvSelectedFile = findViewById(R.id.tv_selected_file);
        btnRefreshFiles = findViewById(R.id.btn_refresh_files);
        fileSelectionContainer = findViewById(R.id.file_selection_container);

        // Écran d'animation
        screenAnimation = findViewById(R.id.screen_animation);
        tvStatus = findViewById(R.id.tv_status);
        tvSeriesInfo = findViewById(R.id.tv_series_info);
        chartView = findViewById(R.id.chart_view);
        btnLaunch = findViewById(R.id.btn_launch);
        btnChangeFile = findViewById(R.id.btn_change_file);

        // Créer le bouton de désactivation du mode éco
        btnDeactivateEcoMode = new Button(this);
        btnDeactivateEcoMode.setId(View.generateViewId());
        btnDeactivateEcoMode.setText("Désactiver Mode Éco");
        btnDeactivateEcoMode.setBackgroundColor(0xFFFF9800); // Orange
        btnDeactivateEcoMode.setTextColor(0xFFFFFFFF);
        btnDeactivateEcoMode.setVisibility(View.GONE); // Caché par défaut

        // Configuration du layout pour le bouton éco
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(32, 20, 32, 20);
        btnDeactivateEcoMode.setLayoutParams(params);
        btnDeactivateEcoMode.setPadding(20, 20, 20, 20);
        btnDeactivateEcoMode.setTextSize(16);

        // Ajouter le bouton à l'écran de sélection (après le titre)
        if (screenFileSelection instanceof LinearLayout) {
            ((LinearLayout) screenFileSelection).addView(btnDeactivateEcoMode, 1);
        }
    }

    /**
     * Vérifie le niveau de batterie et active le mode éco si nécessaire
     */
    private void checkBatteryAndActivateEcoMode() {
        Log.d(TAG, "========================================");
        Log.d(TAG, "VÉRIFICATION DU NIVEAU DE BATTERIE");
        Log.d(TAG, "========================================");

        int batteryLevel = batteryMonitor.getBatteryLevel();
        Log.i(TAG, "Niveau de batterie actuel: " + batteryLevel + "%");
        Log.i(TAG, "Seuil mode éco: " + batteryMonitor.getEcoModeThreshold() + "%");

        if (batteryMonitor.shouldActivateEcoMode()) {
            Log.i(TAG, "→ ACTIVATION DU MODE ÉCO NÉCESSAIRE");

            // Activer le mode éco
            batteryMonitor.activateEcoMode();

            // Afficher un message à l'utilisateur
            String message = String.format(
                    "Mode éco activé automatiquement\n(Batterie: %d%% < %d%%)",
                    batteryLevel,
                    batteryMonitor.getEcoModeThreshold()
            );

            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            Log.i(TAG, message);

            // Afficher le bouton de désactivation
            btnDeactivateEcoMode.setVisibility(View.VISIBLE);

            // Afficher une alerte détaillée
            showEcoModeActivatedDialog();

        } else {
            Log.i(TAG, "→ Batterie suffisante - Mode éco non nécessaire");
            btnDeactivateEcoMode.setVisibility(View.GONE);
        }

        // Afficher le résumé dans les logs
        Log.d(TAG, "\n" + batteryMonitor.getSystemStatusSummary());
        Log.d(TAG, "========================================\n");
    }

    /**
     * Affiche une boîte de dialogue informant l'utilisateur de l'activation du mode éco
     */
    private void showEcoModeActivatedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🔋 Mode Économie d'Énergie Activé");

        String message = String.format(
                "Votre batterie est faible (%d%%).\n\n" +
                        "Actions effectuées:\n" +
                        "• Mode avion: %s\n" +
                        "• Localisation: %s\n" +
                        "• Mode économie: %s\n\n" +
                        "Vous pouvez désactiver manuellement ce mode avec le bouton orange.",
                batteryMonitor.getBatteryLevel(),
                batteryMonitor.isAirplaneModeOn() ? "✓ Activé" : "✗ Échec",
                batteryMonitor.isLocationEnabled() ? "✗ Encore active" : "✓ Désactivée",
                batteryMonitor.isPowerSaveModeOn() ? "✓ Actif" : "○ Non forcé"
        );

        builder.setMessage(message);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.setNeutralButton("Désactiver maintenant", (dialog, which) -> {
            deactivateEcoMode();
            dialog.dismiss();
        });

        builder.show();
    }

    /**
     * Désactive manuellement le mode éco
     */
    private void deactivateEcoMode() {
        Log.i(TAG, "========================================");
        Log.i(TAG, "DÉSACTIVATION MANUELLE DU MODE ÉCO");
        Log.i(TAG, "========================================");

        batteryMonitor.deactivateEcoMode();
        btnDeactivateEcoMode.setVisibility(View.GONE);

        Toast.makeText(this, "Mode éco désactivé", Toast.LENGTH_SHORT).show();

        Log.d(TAG, "\n" + batteryMonitor.getSystemStatusSummary());
        Log.d(TAG, "========================================\n");
    }

    /**
     * Configure tous les listeners des boutons
     */
    private void setupListeners() {
        // Bouton actualiser
        btnRefreshFiles.setOnClickListener(v -> {
            Log.d(TAG, "Actualisation de la liste des fichiers");
            loadAvailableFiles();
            Toast.makeText(this, "Liste actualisée", Toast.LENGTH_SHORT).show();
        });

        // Bouton lancer l'animation
        btnLaunch.setOnClickListener(v -> {
            if (!isAnimating) {
                startAnimation();
            }
        });

        // Bouton changer de fichier
        btnChangeFile.setOnClickListener(v -> {
            showScreen(screenFileSelection);
            tvSelectedFile.setText("Choisissez un fichier JSON");

            // Arrêter l'animation si elle est en cours
            if (isAnimating) {
                stopAnimation();
            }
        });

        // Bouton désactiver mode éco
        btnDeactivateEcoMode.setOnClickListener(v -> deactivateEcoMode());
    }

    /**
     * Charge la liste des fichiers JSON disponibles dans les assets
     */
    private void loadAvailableFiles() {
        Log.d(TAG, "Chargement des fichiers disponibles...");

        fileSelectionContainer.removeAllViews();

        try {
            String[] files = getAssets().list("");
            List<String> jsonFiles = new ArrayList<>();

            // Filtrer les fichiers JSON
            if (files != null) {
                for (String file : files) {
                    if (file.endsWith(".json")) {
                        jsonFiles.add(file);
                        Log.d(TAG, "Fichier trouvé: " + file);
                    }
                }
            }

            if (jsonFiles.isEmpty()) {
                Log.w(TAG, "Aucun fichier JSON trouvé dans les assets");
                TextView noFilesText = new TextView(this);
                noFilesText.setText("Aucun fichier disponible");
                noFilesText.setPadding(20, 20, 20, 20);
                noFilesText.setTextSize(16);
                noFilesText.setTextColor(0xFF666666);
                fileSelectionContainer.addView(noFilesText);
                return;
            }

            // Créer un bouton pour chaque fichier
            for (String file : jsonFiles) {
                Button fileButton = createFileButton(file);
                fileSelectionContainer.addView(fileButton);
            }

            Log.i(TAG, jsonFiles.size() + " fichier(s) JSON chargé(s)");

        } catch (IOException e) {
            Log.e(TAG, "Erreur chargement fichiers: " + e.getMessage());
            Toast.makeText(this, "Erreur chargement fichiers", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Crée un bouton pour sélectionner un fichier
     */
    private Button createFileButton(String fileName) {
        Button button = new Button(this);

        // Nom d'affichage sans extension
        String displayName = fileName.replace(".json", "").replace("_", " ");
        button.setText(displayName);

        // Style
        button.setBackgroundColor(0xFF2196F3); // Bleu
        button.setTextColor(0xFFFFFFFF);
        button.setPadding(20, 20, 20, 20);
        button.setTextSize(16);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 20);
        button.setLayoutParams(params);

        // Action au clic
        button.setOnClickListener(v -> selectFile(fileName));

        return button;
    }

    /**
     * Sélectionne un fichier et charge ses données
     */
    private void selectFile(String fileName) {
        Log.i(TAG, "Fichier sélectionné: " + fileName);

        try {
            model.loadData(this, fileName);

            if (model.hasData()) {
                String displayName = model.getDisplayFileName();
                tvSelectedFile.setText("Fichier : " + displayName);
                tvStatus.setText("Prêt pour l'analyse");

                // Préparer le graphique
                chartView.clearAllSeries();
                chartView.setTimestampMode(true);
                chartView.invalidate();

                // Réinitialiser les contrôles d'animation
                btnLaunch.setEnabled(true);
                btnLaunch.setText("Lancer");
                isAnimating = false;
                currentPacketIndex = 0;

                showScreen(screenAnimation);

                Log.i(TAG, "Données chargées: " +
                        model.getRequestArrayData().getRequestDataList().size() + " paquets");
            }

        } catch (Exception e) {
            Log.e(TAG, "Erreur chargement fichier: " + e.getMessage());
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Lance l'animation des paquets réseau
     */
    private void startAnimation() {
        if (!model.hasData()) {
            Toast.makeText(this, "Aucune donnée chargée", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.i(TAG, "========================================");
        Log.i(TAG, "DÉMARRAGE DE L'ANIMATION");
        Log.i(TAG, "========================================");

        isAnimating = true;
        currentPacketIndex = 0;

        btnLaunch.setEnabled(false);
        btnLaunch.setText("Animation...");
        tvStatus.setText("Animation en cours...");

        // Effacer le graphique avant de commencer
        chartView.clearAllSeries();
        chartView.setTimestampMode(true);

        // Démarrer l'animation progressive
        animateNextPacket();
    }

    /**
     * Anime le paquet suivant dans la séquence
     */
    private void animateNextPacket() {
        if (!isAnimating) {
            return;
        }

        List<LatencyModel.RequestData> packets = model.getRequestArrayData().getRequestDataList();

        if (currentPacketIndex < packets.size()) {
            // Ajouter le paquet actuel au graphique
            LatencyModel.RequestData packet = packets.get(currentPacketIndex);
            chartView.addPacketPoint(packet);

            Log.d(TAG, String.format("Paquet %d/%d ajouté - Statut: %s, RTT: %.2fms",
                    currentPacketIndex + 1,
                    packets.size(),
                    packet.getStatus(),
                    packet.getRtt()
            ));

            currentPacketIndex++;

            // Mettre à jour le statut
            tvStatus.setText(String.format("Animation: %d/%d paquets",
                    currentPacketIndex, packets.size()));

            // Planifier le prochain paquet
            animationHandler.postDelayed(this::animateNextPacket, ANIMATION_DELAY_MS);

        } else {
            // Animation terminée
            finishAnimation();
        }
    }

    /**
     * Termine l'animation et met à jour l'interface
     */
    private void finishAnimation() {
        Log.i(TAG, "Animation terminée - Tous les paquets affichés");
        Log.i(TAG, "========================================\n");

        isAnimating = false;

        btnLaunch.setEnabled(true);
        btnLaunch.setText("Terminé");
        tvStatus.setText("Analyse terminée - " +
                model.getRequestArrayData().getRequestDataList().size() + " paquets affichés");

        Toast.makeText(this, "Animation terminée", Toast.LENGTH_SHORT).show();
    }

    /**
     * Arrête l'animation en cours
     */
    private void stopAnimation() {
        if (isAnimating) {
            Log.d(TAG, "Arrêt de l'animation");
            isAnimating = false;
            animationHandler.removeCallbacksAndMessages(null);

            btnLaunch.setEnabled(true);
            btnLaunch.setText("Lancer");
            tvStatus.setText("Animation arrêtée");
        }
    }

    /**
     * Affiche un écran et masque l'autre
     */
    private void showScreen(View screenToShow) {
        screenFileSelection.setVisibility(
                screenToShow == screenFileSelection ? View.VISIBLE : View.GONE
        );
        screenAnimation.setVisibility(
                screenToShow == screenAnimation ? View.VISIBLE : View.GONE
        );

        Log.d(TAG, "Écran affiché: " +
                (screenToShow == screenFileSelection ? "Sélection" : "Animation"));
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Revérifier la batterie à chaque retour sur l'app
        Log.d(TAG, "onResume - Revérification de la batterie");

        if (batteryMonitor.shouldActivateEcoMode() && !batteryMonitor.isEcoModeActive()) {
            checkBatteryAndActivateEcoMode();
        }

        // Mettre à jour l'affichage du bouton
        if (batteryMonitor.isEcoModeActive()) {
            btnDeactivateEcoMode.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Arrêter l'animation si l'activité passe en arrière-plan
        if (isAnimating) {
            stopAnimation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Nettoyer les handlers
        if (animationHandler != null) {
            animationHandler.removeCallbacksAndMessages(null);
        }

        // Désactiver le mode éco si l'app se ferme
        if (batteryMonitor.isEcoModeActive()) {
            Log.i(TAG, "Désactivation du mode éco à la fermeture de l'app");
            batteryMonitor.deactivateEcoMode();
        }
    }
}
