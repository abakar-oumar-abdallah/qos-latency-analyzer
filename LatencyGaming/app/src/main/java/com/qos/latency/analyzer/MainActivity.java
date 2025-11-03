package com.qos.latency.analyzer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.qos.latency.analyzer.controller.LatencyController;
import com.qos.latency.analyzer.model.LatencyModel;
import com.qos.latency.analyzer.view.BoxPlotChartView;
import com.qos.latency.analyzer.view.ChartView;
import com.qos.latency.analyzer.view.DetailedScoresChartView;

/**
 * Activité principale - Version 100% MPAndroidChart.
 *
 * <p>Cette version utilise exclusivement MPAndroidChart pour tous les graphiques :</p>
 * <ol>
 *   <li><strong>ChartView</strong> : Animation temps réel des courbes (LineChart)</li>
 *   <li><strong>BoxPlotChartView</strong> : Analyse statistique (CombinedChart)</li>
 *   <li><strong>DetailedScoresChartView</strong> : Scores détaillés (HorizontalBarChart)</li>
 * </ol>
 *
 * <p>Comportement identique à la version Canvas mais avec l'uniformité MPAndroidChart.</p>
 *
 * @author QoS Gaming Team
 * @version 2.0 - 100% MPAndroidChart
 */
public class MainActivity extends AppCompatActivity implements LatencyController.ControllerListener {

    /** Modèle de données contenant les séries de latence et scores */
    private LatencyModel model;
    /** Contrôleur gérant l'animation des données */
    private LatencyController controller;

    /** Tableau des 4 écrans principaux pour navigation simplifiée */
    private View[] screens = new View[4];

    /** TextView d'état principal affichant le progress de l'animation */
    private TextView tvStatus;
    /** TextView d'information détaillée sur la série courante */
    private TextView tvSeriesInfo;
    /** Bouton de lancement de l'analyse */
    private Button btnLaunch;

    /** Tableau des TextViews d'affichage des scores (IntAct, Il, DPDV, DDQ) */
    private TextView[] scoreTextViews = new TextView[4];

    /** Vues MPAndroidChart */
    private ChartView chartView;
    private BoxPlotChartView boxPlotChartView;
    private DetailedScoresChartView detailedScoresChartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initMVC();
        setupEvents();

        showScreen(0); // Animation screen
        controller.initializeData();
    }

    /**
     * Initialise toutes les vues de l'interface utilisateur.
     */
    private void initViews() {
        // Écrans principaux (indexes : 0=Animation, 1=Scores, 2=BoxPlot, 3=Détail)
        screens[0] = findViewById(R.id.screen_animation);
        screens[1] = findViewById(R.id.screen_scores);
        screens[2] = findViewById(R.id.screen_boxplot);
        screens[3] = findViewById(R.id.screen_detail);

        // Éléments de l'écran d'animation
        tvStatus = findViewById(R.id.tv_status);
        tvSeriesInfo = findViewById(R.id.tv_series_info);
        btnLaunch = findViewById(R.id.btn_launch);

        // TextViews des scores (indexes : 0=IntAct, 1=Il, 2=DPDV, 3=DDQ)
        scoreTextViews[0] = findViewById(R.id.tv_score_principal_simple);
        scoreTextViews[1] = findViewById(R.id.tv_score_latence_simple);
        scoreTextViews[2] = findViewById(R.id.tv_score_gigue_simple);
        scoreTextViews[3] = findViewById(R.id.tv_score_erreur_simple);
    }

    /**
     * Initialise l'architecture MVC avec vues MPAndroidChart uniquement.
     */
    private void initMVC() {
        model = new LatencyModel();

        // ChartView principale (déjà MPAndroidChart dans le layout)
        chartView = findViewById(R.id.chart_view);

        // NOUVEAU : Créer les vues MPAndroidChart pour BoxPlot et Scores
        boxPlotChartView = new BoxPlotChartView(this);
        detailedScoresChartView = new DetailedScoresChartView(this);

        // Ajouter les nouvelles vues aux containers
        ((FrameLayout) findViewById(R.id.boxplot_container)).addView(boxPlotChartView);
        ((FrameLayout) findViewById(R.id.detail_container)).addView(detailedScoresChartView);

        // Configuration du contrôleur (inchangé)
        controller = new LatencyController(model, chartView);
        controller.setListener(this);
    }

    /**
     * Configure tous les événements de l'interface utilisateur.
     */
    private void setupEvents() {
        // Bouton de lancement de l'animation
        btnLaunch.setOnClickListener(v -> {
            if (!controller.isAnimating()) {
                controller.startAnimation();
            }
        });

        // Navigation entre écrans
        findViewById(R.id.btn_detail_circular).setOnClickListener(v -> showScreen(2));

        // Bouton relancer - Version MPAndroidChart
        findViewById(R.id.btn_nouvelle_mesure).setOnClickListener(v -> {
            // Nettoyer tous les graphiques
            chartView.clearAllSeries();
            boxPlotChartView.setModel(null);
            detailedScoresChartView.setInteractScores(null);

            // Réinitialiser le modèle
            model = new LatencyModel();
            controller.initializeData();

            // Retour à l'écran d'animation
            showScreen(0);
        });

        findViewById(R.id.btn_retour_scores).setOnClickListener(v -> showScreen(1));
        findViewById(R.id.btn_detail_sous_scores).setOnClickListener(v -> showScreen(3));
        findViewById(R.id.btn_retour_boxplot).setOnClickListener(v -> showScreen(2));
    }

    /**
     * Affiche l'écran spécifié et met à jour son contenu.
     */
    private void showScreen(int screenIndex) {
        // Gestion de la visibilité des écrans
        for (int i = 0; i < screens.length; i++) {
            screens[i].setVisibility(i == screenIndex ? View.VISIBLE : View.GONE);
        }

        // Mise à jour spécifique selon l'écran
        if (screenIndex == 0) {
            // Écran d'animation - État initial
            tvStatus.setText("Prêt à démarrer l'analyse");
            tvSeriesInfo.setText("Chaque série affichera ses latences");
            btnLaunch.setEnabled(true);
            btnLaunch.setText("Lancer");
            btnLaunch.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF4CAF50));
        } else if (screenIndex == 1) {
            // Écran des scores - Mise à jour des valeurs
            updateScoreTextViews();
        } else if (screenIndex == 2) {
            // Écran BoxPlot - Mise à jour du modèle MPAndroidChart
            boxPlotChartView.setModel(model);
        } else if (screenIndex == 3) {
            // Écran de détail - Mise à jour des scores MPAndroidChart
            detailedScoresChartView.setInteractScores(model.getInteractScores());
        }
    }

    /**
     * Met à jour les TextViews des scores dans l'écran simplifié.
     */
    private void updateScoreTextViews() {
        LatencyModel.InteractScores scores = model.getInteractScores();
        if (scores != null && scores.isValid()) {
            scoreTextViews[0].setText(String.format("%.0f %%", scores.getIntAct()));
            scoreTextViews[1].setText(String.format("%.0f %%", scores.getIl()));
            scoreTextViews[2].setText(String.format("%.0f %%", scores.getDpdv()));
            scoreTextViews[3].setText(String.format("%.0f %%", scores.getDdq()));
        } else {
            // Valeurs par défaut si pas de scores
            for (TextView tv : scoreTextViews) {
                tv.setText("-- %");
            }
        }
    }

    // === IMPLÉMENTATION DE ControllerListener ===

    @Override
    public void onAnimationStarted() {
        btnLaunch.setEnabled(false);
        btnLaunch.setText("⏳ Animation en cours...");
        btnLaunch.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFF9800));
        tvStatus.setText("🎬 Animation démarrée - Affichage des rtt_array");
    }

    @Override
    public void onAnimationFinished() {
        tvStatus.setText("✅ Animation terminée - Traitement des Interact_scores...");
    }

    @Override
    public void onVisualizationPauseStarted(int remainingSeconds) {
        btnLaunch.setText("👁️ Visualisation");
        btnLaunch.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF2196F3));
        tvStatus.setText("📊 Analyse des courbes rtt_array - IntAct dans " + remainingSeconds + " sec");
    }

    @Override
    public void onVisualizationPauseCountdown(int remainingSeconds) {
        tvStatus.setText("🧮 Calcul Interact_scores en cours - Résultats dans " + remainingSeconds + " sec");
        btnLaunch.setText(remainingSeconds <= 3 ? "⏰ " + remainingSeconds + " sec" : "⏳ " + remainingSeconds + " sec");
        if (remainingSeconds <= 3) {
            btnLaunch.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFF5722));
        }
    }

    @Override
    public void onVisualizationPauseFinished() {
        tvStatus.setText("🎯 Calcul des scores IntAct, Il, DPDV, DDQ terminé !");
        new android.os.Handler().postDelayed(() -> showScreen(1), 500);
    }

    @Override
    public void onSeriesStarted(int seriesNumber, String seriesName) {
        tvStatus.setText("📈 " + seriesName + " - Tracé rtt_array en cours...");
        tvSeriesInfo.setText("Affichage latence par latence de la série " + seriesNumber);
    }

    @Override
    public void onPointAdded(int seriesNumber, double latencyMs, int pointIndex, int totalPoints) {
        // Throttling : mise à jour tous les 5 points seulement
        if (pointIndex % 5 == 0) {
            tvStatus.setText(String.format("📊 Série %d : %d/%d latences (%.1f ms)", seriesNumber, pointIndex, totalPoints, latencyMs));
            tvSeriesInfo.setText(String.format("rtt_array[%d] = %.1f ms", pointIndex - 1, latencyMs));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (controller != null) {
            controller.cleanup();
        }
    }
}