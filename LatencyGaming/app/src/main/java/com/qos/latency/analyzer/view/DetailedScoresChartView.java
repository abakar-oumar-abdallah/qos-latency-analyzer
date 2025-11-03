package com.qos.latency.analyzer.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.qos.latency.analyzer.model.LatencyModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Vue des scores détaillés utilisant MPAndroidChart au lieu de Canvas.
 *
 * <p>Cette vue présente les scores Interact sous forme de barres horizontales colorées :</p>
 * <ul>
 *   <li><strong>Score principal IntAct</strong> : Barre principale en haut</li>
 *   <li><strong>Sous-scores</strong> : Il, DPDV, DDQ en barres secondaires</li>
 *   <li><strong>Code couleur</strong> : Vert (excellent) → Rouge (mauvais)</li>
 *   <li><strong>Valeurs affichées</strong> : Pourcentages sur les barres</li>
 * </ul>
 *
 * <p>Système de couleurs identique à la version Canvas :</p>
 * <ul>
 *   <li>🟢 Excellent (80-100%) - Vert</li>
 *   <li>🟡 Bon (60-79%) - Vert clair</li>
 *   <li>🟠 Moyen (40-59%) - Orange</li>
 *   <li>🟠 Faible (20-39%) - Orange-rouge</li>
 *   <li>🔴 Mauvais (0-19%) - Rouge</li>
 * </ul>
 *
 * @author QoS Gaming Team
 * @version 2.0 - MPAndroidChart
 */
public class DetailedScoresChartView extends HorizontalBarChart {

    /** Scores d'interaction à afficher */
    private LatencyModel.InteractScores interactScores;

    // === COULEURS PAR NIVEAU DE SCORE ===
    private static final int COLOR_EXCELLENT = 0xFF4CAF50;  // Vert
    private static final int COLOR_GOOD = 0xFF8BC34A;       // Vert clair
    private static final int COLOR_AVERAGE = 0xFFFF9800;    // Orange
    private static final int COLOR_POOR = 0xFFFF5722;       // Orange-rouge
    private static final int COLOR_BAD = 0xFFF44336;        // Rouge

    public DetailedScoresChartView(Context context) {
        super(context);
        initializeChart();
    }

    public DetailedScoresChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeChart();
    }

    /**
     * Initialise le graphique des scores.
     */
    private void initializeChart() {
        setupChart();
        setupAxes();
        setupLegend();
    }

    /**
     * Configure les paramètres généraux du graphique.
     */
    private void setupChart() {
        getDescription().setEnabled(true);
        getDescription().setText("Détail des Scores Interact");
        getDescription().setTextSize(16f);
        getDescription().setTextColor(Color.parseColor("#212121"));
        getDescription().setPosition(getWidth() / 2f, 30f);

        setTouchEnabled(false);
        setDragEnabled(false);
        setScaleEnabled(false);
        setPinchZoom(false);

        // Fond gris clair comme la version Canvas
        setBackgroundColor(Color.parseColor("#F5F5F5"));
        setDrawGridBackground(false);

        // Espacements pour ressembler aux cartes
        setExtraTopOffset(40f);
        setExtraBottomOffset(30f);
        setExtraLeftOffset(20f);
        setExtraRightOffset(50f);
    }

    /**
     * Configure les axes X et Y.
     */
    private void setupAxes() {
        // Axe X (valeurs des scores, horizontal)
        XAxis xAxis = getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(true);
        xAxis.setGridColor(Color.parseColor("#E0E0E0"));
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(100f);
        xAxis.setTextColor(Color.parseColor("#424242"));
        xAxis.setTextSize(12f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.0f%%", value);
            }
        });

        // Axe Y (noms des scores, vertical)
        YAxis leftAxis = getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setTextColor(Color.parseColor("#424242"));
        leftAxis.setTextSize(14f);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                switch (index) {
                    case 0: return "🎯 Score Global";
                    case 1: return "❌ DDQ (Erreur)";
                    case 2: return "📊 DPDV (Gigue)";
                    case 3: return "⚡ Il (Latence)";
                    case 4: return "🏆 IntAct (Principal)";
                    default: return "";
                }
            }
        });

        // Axe Y droit désactivé
        getAxisRight().setEnabled(false);
    }

    /**
     * Configure la légende.
     */
    private void setupLegend() {
        Legend legend = getLegend();
        legend.setEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextSize(12f);
        legend.setTextColor(Color.parseColor("#666666"));
        legend.setFormSize(12f);
        legend.setYOffset(10f);
    }

    /**
     * Met à jour les scores à afficher.
     */
    public void setInteractScores(LatencyModel.InteractScores scores) {
        this.interactScores = scores;
        updateChart();
    }

    /**
     * Met à jour l'affichage du graphique.
     */
    private void updateChart() {
        if (interactScores == null || !interactScores.isValid()) {
            clear();
            setNoDataText("Aucun score disponible");
            setNoDataTextColor(Color.parseColor("#757575"));
            // CORRECTION : Suppression de setNoDataTextSize() qui n'existe pas
            invalidate();
            return;
        }

        // Créer les entrées de données
        List<BarEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        // Score global calculé
        double globalScore = calculateGlobalHealth();
        entries.add(new BarEntry(0, (float) globalScore));
        colors.add(getScoreColor(globalScore));

        // DDQ (Score d'erreur)
        entries.add(new BarEntry(1, (float) interactScores.getDdq()));
        colors.add(getScoreColor(interactScores.getDdq()));

        // DPDV (Score de gigue)
        entries.add(new BarEntry(2, (float) interactScores.getDpdv()));
        colors.add(getScoreColor(interactScores.getDpdv()));

        // Il (Score de latence)
        entries.add(new BarEntry(3, (float) interactScores.getIl()));
        colors.add(getScoreColor(interactScores.getIl()));

        // IntAct (Score principal) - Plus gros
        entries.add(new BarEntry(4, (float) interactScores.getIntAct()));
        colors.add(getScoreColor(interactScores.getIntAct()));

        // Créer le dataset
        BarDataSet dataSet = new BarDataSet(entries, "Scores Interact");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1f%%", value);
            }
        });

        // Configurer les données
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f); // Largeur des barres

        setData(barData);

        // Mise à jour de la légende
        updateLegendWithDescription();

        invalidate();
    }

    /**
     * Met à jour la légende avec une description des scores.
     */
    private void updateLegendWithDescription() {
        String description = String.format(
                "Scores QoS Gaming - IntAct: %.1f%% | Il: %.1f%% | DPDV: %.1f%% | DDQ: %.1f%%",
                interactScores.getIntAct(),
                interactScores.getIl(),
                interactScores.getDpdv(),
                interactScores.getDdq()
        );

        getDescription().setText(description);
        getDescription().setTextSize(12f);
        getDescription().setPosition(getWidth() / 2f, 30f);
    }

    /**
     * Retourne la couleur appropriée selon le score.
     */
    private int getScoreColor(double score) {
        if (score >= 80) return COLOR_EXCELLENT;
        if (score >= 60) return COLOR_GOOD;
        if (score >= 40) return COLOR_AVERAGE;
        if (score >= 20) return COLOR_POOR;
        return COLOR_BAD;
    }

    /**
     * Calcule un score de santé global basé sur les sous-scores.
     * Formule identique à la version Canvas.
     */
    private double calculateGlobalHealth() {
        if (interactScores == null) return 0.0;

        return (interactScores.getIl() * 0.4 +
                interactScores.getDpdv() * 0.3 +
                interactScores.getDdq() * 0.3);
    }
}