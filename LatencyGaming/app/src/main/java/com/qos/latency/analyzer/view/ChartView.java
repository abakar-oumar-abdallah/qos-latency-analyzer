package com.qos.latency.analyzer.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Vue graphique pour les courbes de latence en fonction du temps avec MPAndroidChart.
 */
public class ChartView extends LineChart {

    /** Données des séries */
    private Map<Integer, LineDataSet> seriesDataSets;
    /** Compteurs de points par série */
    private Map<Integer, Integer> pointCounts;

    /** Temps de début de l'animation (pour temps relatif) */
    private long startTime = 0;

    /** Couleurs des séries */
    private static final int[] SERIES_COLORS = {
            Color.parseColor("#F44336"),    // Série 1 - Rouge
            Color.parseColor("#2196F3"),    // Série 2 - Bleu
            Color.parseColor("#4CAF50"),    // Série 3 - Vert
            Color.parseColor("#FF9800"),    // Série 4 - Orange
            Color.parseColor("#9C27B0")     // Série 5 - Violet
    };

    public ChartView(Context context) {
        super(context);
        initializeChart();
    }

    public ChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeChart();
    }

    /**
     * Initialise le graphique.
     */
    private void initializeChart() {
        seriesDataSets = new HashMap<>();
        pointCounts = new HashMap<>();

        setupChart();
        setupAxes();
        setupLegend();

        // Préparer les séries valides selon le modèle
        int[] validSeries = {1, 2, 5};
        for (int series : validSeries) {
            pointCounts.put(series, 0);
        }
    }

    /**
     * Configure les paramètres généraux du graphique.
     */
    private void setupChart() {
        getDescription().setEnabled(true);
        getDescription().setText("Courbes de Latence vs Temps");
        getDescription().setTextSize(14f);
        getDescription().setTextColor(Color.parseColor("#616161"));

        setTouchEnabled(true);
        setDragEnabled(true);
        setScaleEnabled(true);
        setPinchZoom(true);

        // Fond et style
        setBackgroundColor(Color.parseColor("#FAFAFA"));
        setGridBackgroundColor(Color.WHITE);
        setDrawGridBackground(true);

        // Marges
        setExtraTopOffset(20f);
        setExtraBottomOffset(20f);
        setExtraLeftOffset(20f);
        setExtraRightOffset(20f);
    }

    /**
     * Configure les axes X et Y.
     */
    private void setupAxes() {
        // Axe X (Temps en secondes)
        XAxis xAxis = getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(0.1f); // 100ms de granularité
        xAxis.setDrawGridLines(true);
        xAxis.setGridColor(Color.parseColor("#E0E0E0"));
        xAxis.setTextColor(Color.parseColor("#424242"));
        xAxis.setTextSize(12f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1fs", value); // Affiche en secondes
            }
        });

        // Axe Y gauche (Latence en ms)
        YAxis leftAxis = getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#E0E0E0"));
        leftAxis.setTextColor(Color.parseColor("#424242"));
        leftAxis.setTextSize(12f);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.0f ms", value);
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
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(true);
        legend.setTextSize(12f);
        legend.setTextColor(Color.parseColor("#424242"));
        legend.setFormSize(12f);
        legend.setXEntrySpace(10f);
        legend.setYEntrySpace(5f);
    }

    /**
     * Efface toutes les séries.
     */
    public void clearAllSeries() {
        seriesDataSets.clear();
        for (int series : pointCounts.keySet()) {
            pointCounts.put(series, 0);
        }
        startTime = 0; // Reset du temps de début
        clear();
        invalidate();
    }

    /**
     * Démarre une nouvelle série.
     */
    public void startNewSeries(int seriesNumber) {
        // Initialiser le temps de début lors de la première série
        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }

        // Créer un nouveau dataset pour cette série
        LineDataSet dataSet = new LineDataSet(new ArrayList<Entry>(), "Série " + seriesNumber);

        // Configuration du style
        dataSet.setColor(getSeriesColor(seriesNumber));
        dataSet.setLineWidth(3f);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.LINEAR);
        dataSet.setDrawFilled(false);

        // Effets visuels
        dataSet.setHighlightEnabled(true);
        dataSet.setHighLightColor(Color.parseColor("#FF5722"));

        seriesDataSets.put(seriesNumber, dataSet);
        pointCounts.put(seriesNumber, 0);
    }

    /**
     * Ajoute un point à une série.
     */
    public void addPoint(int seriesNumber, float latencyMs) {
        LineDataSet dataSet = seriesDataSets.get(seriesNumber);
        if (dataSet == null) return;

        // Calculer le temps écoulé depuis le début en secondes
        long currentTime = System.currentTimeMillis();
        float timeInSeconds = (currentTime - startTime) / 1000.0f;

        // Ajouter le point avec le temps en X et latence en Y
        dataSet.addEntry(new Entry(timeInSeconds, latencyMs));

        int pointIndex = pointCounts.get(seriesNumber);
        pointCounts.put(seriesNumber, pointIndex + 1);

        // Mettre à jour les données du graphique
        updateChart();
    }

    /**
     * Met à jour l'affichage du graphique.
     */
    private void updateChart() {
        List<LineDataSet> activeSeries = new ArrayList<>();
        for (LineDataSet dataSet : seriesDataSets.values()) {
            if (dataSet.getEntryCount() > 0) {
                activeSeries.add(dataSet);
            }
        }

        if (!activeSeries.isEmpty()) {
            LineData lineData = new LineData(activeSeries.toArray(new LineDataSet[0]));
            setData(lineData);
            notifyDataSetChanged();

            // Auto-ajustement de la vue pour suivre l'animation
            if (activeSeries.size() > 0) {
                LineDataSet lastSeries = activeSeries.get(activeSeries.size() - 1);
                if (lastSeries.getEntryCount() > 10) {
                    // Suivre les 5 dernières secondes
                    float currentTime = (System.currentTimeMillis() - startTime) / 1000.0f;
                    moveViewToX(currentTime - 2.5f);
                }
            }

            invalidate(); // Redessiner
        }
    }

    /**
     * Retourne la couleur pour une série.
     */
    private int getSeriesColor(int seriesNumber) {
        switch (seriesNumber) {
            case 1: return SERIES_COLORS[0]; // Rouge
            case 2: return SERIES_COLORS[1]; // Bleu
            case 3: return SERIES_COLORS[2]; // Vert
            case 4: return SERIES_COLORS[3]; // Orange
            case 5: return SERIES_COLORS[4]; // Violet
            default: return SERIES_COLORS[0];
        }
    }

    /**
     * Retourne le nombre total de points affichés.
     */
    public int getTotalPoints() {
        int total = 0;
        for (int count : pointCounts.values()) {
            total += count;
        }
        return total;
    }

    /**
     * Retourne le nombre de séries actives.
     */
    public int getActiveSeriesCount() {
        return seriesDataSets.size();
    }

    /**
     * Ajuste automatiquement la vue pour voir toutes les données.
     */
    public void fitAllData() {
        fitScreen();
        invalidate();
    }
}