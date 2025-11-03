package com.qos.latency.analyzer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.qos.latency.analyzer.controller.LatencyController;
import com.qos.latency.analyzer.model.LatencyModel;
import com.qos.latency.analyzer.view.ChartView;

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
 * @version 3.0
 */
public class MainActivity extends AppCompatActivity implements LatencyController.ControllerListener {

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

        initViews();
        initMVC();
        setupEvents();

        showScreen(0);
        loadAvailableFiles();
    }

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