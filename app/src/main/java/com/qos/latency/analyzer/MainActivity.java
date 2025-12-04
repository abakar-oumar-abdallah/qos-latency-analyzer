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
 */
public class MainActivity extends AppCompatActivity implements LatencyController.ControllerListener {

    private LatencyModel model;
    private LatencyController controller;
    private ChartView chartView;

    private View[] screens = new View[2];
    private String selectedFileName = "";

    private TextView tvStatus;
    private TextView tvSeriesInfo;
    private Button btnLaunch;
    private TextView tvSelectedFile;
    private LinearLayout fileSelectionContainer;

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

    private void initViews() {
        screens[0] = findViewById(R.id.screen_file_selection);
        screens[1] = findViewById(R.id.screen_animation);

        tvSelectedFile = findViewById(R.id.tv_selected_file);
        tvSelectedFile.setContentDescription("Fichier sélectionné"); // <-- Ajouté
        fileSelectionContainer = findViewById(R.id.file_selection_container);
        tvStatus = findViewById(R.id.tv_status);
        tvStatus.setContentDescription("Statut de l'application"); // <-- Ajouté
        tvSeriesInfo = findViewById(R.id.tv_series_info);
        tvSeriesInfo.setContentDescription("Informations détaillées sur les séries"); // <-- Ajouté
        btnLaunch = findViewById(R.id.btn_launch);
        btnLaunch.setContentDescription("Bouton lancer l'analyse"); // <-- Ajouté
        chartView = findViewById(R.id.chart_view);
        chartView.setContentDescription("Graphique de latence"); // <-- Ajouté
    }

    private void initMVC() {
        model = new LatencyModel();
        controller = new LatencyController(model, chartView, this);
        controller.setListener(this);
    }

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

    private void loadAvailableFiles() {
        fileSelectionContainer.removeAllViews();
        List<String> assetFiles = LatencyModel.getAvailableDataFiles(this);

        if (assetFiles.isEmpty()) {
            TextView noFilesText = new TextView(this);
            noFilesText.setText("Aucun fichier JSON trouvé dans les assets");
            noFilesText.setTextSize(16);
            noFilesText.setTextColor(0xFFE53935);
            noFilesText.setContentDescription("Aucun fichier JSON trouvé"); // <-- Ajouté
            fileSelectionContainer.addView(noFilesText);
            return;
        }

        for (String fileName : assetFiles) {
            Button fileButton = createSimpleFileButton(fileName);
            fileSelectionContainer.addView(fileButton);
        }
    }

    private Button createSimpleFileButton(String fileName) {
        Button button = new Button(this);
        String displayName = fileName.replace(".json", "");
        button.setText(displayName);
        button.setContentDescription(displayName); // <-- Ajouté
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

    private void selectFile(String fileName) {
        selectedFileName = fileName;
        tvSelectedFile.setText("Fichier : " + fileName.replace(".json", ""));

        model = new LatencyModel();
        controller = new LatencyController(model, chartView, this);
        controller.setListener(this);

        try {
            model.loadData(this, fileName);
            tvStatus.setText("Prêt pour l'analyse");
            showScreen(1);
        } catch (Exception e) {
            tvStatus.setText("Erreur : " + e.getMessage());
        }
    }

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

    private String getStatusText(LatencyModel.RequestData packet) {
        switch (packet.getStatus()) {
            case RECEIVED: return "Reçu";
            case LOST: return "PERDU";
            case DUPLICATED: return "Dupliqué";
            case REVERSED: return "Inversé";
            default: return "Inconnu";
        }
    }

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
