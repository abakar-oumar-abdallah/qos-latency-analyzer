package com.qos.latency.analyzer;

import android.os.Bundle;
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

    // État de l'application
    private boolean isAnimating = false;

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

        // Ajouter le bouton à l'écran de sélection
        if (screenFileSelection instanceof LinearLayout) {
            ((LinearLayout) screenFileSelection).addView(btnDeactivateEcoMode, 1); // Position après le titre
        }
    }

    /**
     * Vérifie le niveau de batterie et active le mode éco si nécessaire
     */
    private void checkBatteryAndActivateEcoMode() {
        Log.d(TAG, "Vérification du niveau de batterie...");

        int batteryLevel = batteryMonitor.getBatteryLevel();
        Log.i(TAG, "Niveau de batterie actuel: " + batteryLevel + "%");

        if (batteryMonitor.shouldActivateEcoMode()) {
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
            Log.i(TAG, "Batterie suffisante - Mode éco non nécessaire");
            btnDeactivateEcoMode.setVisibility(View.GONE);
        }

        // Afficher le résumé dans les logs
        Log.d(TAG, batteryMonitor.getSystemStatusSummary());
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
                        "• Mode avion activé\n" +
                        "• Localisation désactivée\n" +
                        "• Mode économie d'énergie\n\n" +
                        "Vous pouvez désactiver manuellement ce mode avec le bouton orange.",
                batteryMonitor.getBatteryLevel()
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
        Log.i(TAG, "Désactivation manuelle du mode éco demandée");

        batteryMonitor.deactivateEcoMode();
        btnDeactivateEcoMode.setVisibility(View.GONE);

        Toast.makeText(this, "Mode éco désactivé", Toast.LENGTH_SHORT).show();

        Log.d(TAG, batteryMonitor.getSystemStatusSummary());
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

                chartView.clearChart();
                chartView.invalidate();

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

        Log.i(TAG, "Démarrage de l'animation");
        isAnimating = true;
        btnLaunch.setEnabled(false);
        btnLaunch.setText("Animation...");

        chartView.startAnimation(
                model.getRequestArrayData(),
                () -> {
                    // Callback fin d'animation
                    runOnUiThread(() -> {
                        isAnimating = false;
                        btnLaunch.setEnabled(true);
                        btnLaunch.setText("Terminé");
                        tvStatus.setText("Analyse terminée");
                        Log.i(TAG, "Animation terminée");
                    });
                }
        );
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Désactiver le mode éco si l'app se ferme
        if (batteryMonitor.isEcoModeActive()) {
            Log.i(TAG, "Désactivation du mode éco à la fermeture de l'app");
            batteryMonitor.deactivateEcoMode();
        }
    }
}