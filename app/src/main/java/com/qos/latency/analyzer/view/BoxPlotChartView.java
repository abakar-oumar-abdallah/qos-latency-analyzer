package com.qos.latency.analyzer.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.qos.latency.analyzer.model.LatencyModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Vue BoxPlot utilisant MPAndroidChart pour l'affichage des statistiques de latence.
 *
 * <p>Cette vue remplace la version Canvas par une implementation MPAndroidChart qui affiche :</p>
 * <ul>
 *   <li><strong>Barres BoxPlot</strong> : Min, Q1, Médiane, Q3, Max pour chaque série</li>
 *   <li><strong>Lignes de moustaches</strong> : Connexions entre les quartiles</li>
 *   <li><strong>Gestion d'erreurs</strong> : Barres spéciales pour les séries invalides</li>
 *   <li><strong>Légende enrichie</strong> : Gigue et perte de paquets affichées</li>
 * </ul>
 *
 * @author QoS Gaming Team
 * @version 2.0 - MPAndroidChart
 */
public class BoxPlotChartView extends CombinedChart {

    /** Modèle de données contenant les séries de latence */
    private LatencyModel model;

    /** Couleurs pour les différents éléments des BoxPlots */
    private static final int BOX_COLOR = Color.parseColor("#E3F2FD");
    private static final int BORDER_COLOR = Color.parseColor("#424242");
    private static final int ERROR_COLOR = Color.parseColor("#FFEBEE");
    private static final int MEDIAN_COLOR = Color.parseColor("#1976D2");

    public BoxPlotChartView(Context context) {
        super(context);
        initializeChart();
    }

    public BoxPlotChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeChart();
    }

    /**
     * Initialise le graphique BoxPlot.
     * RENOMMÉ de init() vers initializeChart() pour éviter le conflit avec CombinedChart
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
        getDescription().setEnabled(false);
        setTouchEnabled(true);
        setDragEnabled(true);
        setScaleEnabled(false);
        setPinchZoom(false);
        setDrawOrder(new DrawOrder[]{
                DrawOrder.BAR, DrawOrder.LINE
        });

        // Fond blanc
        setBackgroundColor(Color.WHITE);
        setGridBackgroundColor(Color.WHITE);
        setDrawGridBackground(true);

        // Marges
        setExtraTopOffset(20f);
        setExtraBottomOffset(60f);
        setExtraLeftOffset(40f);
        setExtraRightOffset(20f);
    }

    /**
     * Configure les axes X et Y.
     */
    private void setupAxes() {
        // Axe X
        XAxis xAxis = getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < 5) {
                    return "Série " + (index + 1);
                }
                return "";
            }
        });
        xAxis.setTextColor(Color.parseColor("#424242"));
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(12f);

        // Axe Y gauche
        YAxis leftAxis = getAxisLeft();
        leftAxis.setTextColor(Color.parseColor("#424242"));
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#E0E0E0"));
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.0f ms", value);
            }
        });
        leftAxis.setTextSize(10f);

        // Axe Y droit désactivé
        getAxisRight().setEnabled(false);
    }

    /**
     * Configure la légende pour afficher les statistiques.
     */
    private void setupLegend() {
        Legend legend = getLegend();
        legend.setEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setTextSize(10f);
        legend.setTextColor(Color.parseColor("#666666"));
        legend.setYOffset(10f);
    }

    /**
     * Met à jour le modèle et redessine le graphique.
     */
    public void setModel(LatencyModel model) {
        this.model = model;
        updateChart();
    }

    /**
     * Met à jour l'affichage du graphique avec les données du modèle.
     */
    private void updateChart() {
        if (model == null) {
            clear();
            setNoDataText("Aucune donnée de latence disponible");
            invalidate();
            return;
        }

        CombinedData combinedData = new CombinedData();

        // Ajouter les données de barres (BoxPlots)
        BarData barData = createBoxPlotBarData();
        if (barData.getDataSetCount() > 0) {
            combinedData.setData(barData);
        }

        // Ajouter les lignes de médiane
        LineData lineData = createMedianLineData();
        if (lineData.getDataSetCount() > 0) {
            combinedData.setData(lineData);
        }

        if (combinedData.getDataSetCount() > 0) {
            setData(combinedData);

            // Mise à jour de la légende avec les statistiques
            updateLegendWithStats();

            // Ajustement automatique de l'échelle
            fitScreen();
        } else {
            clear();
            setNoDataText("Toutes les séries sont en erreur");
        }

        invalidate();
    }

    /**
     * Crée les données de barres pour représenter les BoxPlots.
     * Version optimisée avec gestion automatique de l'échelle.
     */
    private BarData createBoxPlotBarData() {
        List<BarEntry> entriesValid = new ArrayList<>();
        List<BarEntry> entriesError = new ArrayList<>();

        // Trouver l'échelle globale pour normaliser les barres
        double globalMin = Double.MAX_VALUE;
        double globalMax = Double.MIN_VALUE;

        for (int i = 0; i < 5; i++) {
            LatencyModel.SeriesData series = model.getSeries(i + 1);
            if (series != null && series.isValid()) {
                double[] stats = series.getBoxPlotStats();
                globalMin = Math.min(globalMin, stats[0]);
                globalMax = Math.max(globalMax, stats[4]);
            }
        }

        // Si pas de données valides, échelle par défaut
        if (globalMin == Double.MAX_VALUE) {
            globalMin = 0;
            globalMax = 1000;
        }

        for (int i = 0; i < 5; i++) {
            LatencyModel.SeriesData series = model.getSeries(i + 1);

            if (series != null && series.isValid()) {
                double[] stats = series.getBoxPlotStats();
                // Utiliser les valeurs absolues pour un rendu correct
                float[] values = {
                        (float)stats[0], // min (base)
                        (float)(stats[1] - stats[0]), // min to Q1
                        (float)(stats[3] - stats[1]), // Q1 to Q3 (boîte principale)
                        (float)(stats[4] - stats[3])  // Q3 to max
                };
                entriesValid.add(new BarEntry(i, values));
            } else {
                // Série en erreur - barre rouge à 50% de l'échelle
                float errorHeight = (float)((globalMax - globalMin) * 0.5);
                entriesError.add(new BarEntry(i, errorHeight));
            }
        }

        // Créer les datasets séparément pour un meilleur contrôle
        BarData barData = new BarData();

        if (!entriesValid.isEmpty()) {
            BarDataSet validDataSet = new BarDataSet(entriesValid, "Séries Valides");
            validDataSet.setColors(new int[]{
                    Color.TRANSPARENT,           // Base invisible
                    Color.parseColor("#FFCDD2"), // min-Q1 (moustache)
                    BOX_COLOR,                   // Q1-Q3 (boîte principale)
                    Color.parseColor("#FFCDD2")  // Q3-max (moustache)
            });
            validDataSet.setStackLabels(new String[]{"Base", "Min-Q1", "Q1-Q3", "Q3-Max"});
            validDataSet.setDrawValues(false);
            barData.addDataSet(validDataSet);
        }

        if (!entriesError.isEmpty()) {
            BarDataSet errorDataSet = new BarDataSet(entriesError, "Séries en Erreur");
            errorDataSet.setColor(ERROR_COLOR);
            errorDataSet.setDrawValues(false);
            barData.addDataSet(errorDataSet);
        }

        return barData;
    }

    /**
     * Crée les données de lignes pour les médianes.
     */
    private LineData createMedianLineData() {
        List<Entry> medianEntries = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            LatencyModel.SeriesData series = model.getSeries(i + 1);

            if (series != null && series.isValid()) {
                double[] stats = series.getBoxPlotStats();
                medianEntries.add(new Entry(i, (float)stats[2])); // médiane
            }
        }

        if (medianEntries.isEmpty()) {
            return new LineData();
        }

        LineDataSet medianDataSet = new LineDataSet(medianEntries, "Médiane");
        medianDataSet.setColor(MEDIAN_COLOR);
        medianDataSet.setLineWidth(3f);
        medianDataSet.setDrawCircles(true);
        medianDataSet.setCircleColor(MEDIAN_COLOR);
        medianDataSet.setCircleRadius(5f);
        medianDataSet.setDrawValues(false);
        medianDataSet.setMode(LineDataSet.Mode.LINEAR);

        return new LineData(medianDataSet);
    }

    /**
     * Met à jour la légende avec les statistiques de gigue et de perte.
     */
    private void updateLegendWithStats() {
        // Construire le texte des statistiques
        StringBuilder stats = new StringBuilder();
        stats.append("Statistiques détaillées:\n");

        // Gigue
        stats.append("Gigue (ms): ");
        for (int i = 1; i <= 5; i++) {
            LatencyModel.SeriesData series = model.getSeries(i);
            if (series != null && series.isValid()) {
                stats.append(String.format("%.1f", series.getJitterMs()));
            } else {
                stats.append("--");
            }
            if (i < 5) stats.append(" | ");
        }

        stats.append("\nPerte (%): ");
        for (int i = 1; i <= 5; i++) {
            LatencyModel.SeriesData series = model.getSeries(i);
            if (series != null && series.isValid()) {
                stats.append(String.format("%.0f", series.getPacketLossPercent()));
            } else {
                stats.append("--");
            }
            if (i < 5) stats.append(" | ");
        }

        // Note: MPAndroidChart ne permet pas de texte libre dans la légende
        // On utilise la description à la place
        getDescription().setEnabled(true);
        getDescription().setText(stats.toString());
        getDescription().setTextSize(10f);
        getDescription().setTextColor(Color.parseColor("#666666"));
        getDescription().setPosition(50f, getHeight() - 100f);
    }
}