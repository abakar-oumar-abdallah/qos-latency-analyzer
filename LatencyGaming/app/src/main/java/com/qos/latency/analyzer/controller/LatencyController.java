package com.qos.latency.analyzer.controller;

import android.os.Handler;
import android.os.Looper;
import com.qos.latency.analyzer.model.LatencyModel;
import com.qos.latency.analyzer.view.ChartView;

import java.util.Iterator;
import java.util.List;

/**
 * Contrôleur pour l'animation des données de latence dans l'architecture MVC.
 *
 * <p>Ce contrôleur gère le cycle complet d'animation des données de latence :</p>
 * <ol>
 *   <li><strong>Animation des courbes</strong> : Affichage point par point des rtt_array (10ms par point)</li>
 *   <li><strong>Pause de visualisation</strong> : 7 secondes pour analyser les courbes</li>
 *   <li><strong>Affichage des scores</strong> : Transition vers l'écran des résultats</li>
 * </ol>
 *
 * <p>Utilise le pattern Observer pour notifier l'interface utilisateur des événements
 * via {@link ControllerListener}.</p>
 *
 * <p><strong>Exemple d'utilisation :</strong></p>
 * <pre>
 * LatencyController controller = new LatencyController(model, chartView);
 * controller.setListener(this);
 * controller.initializeData();
 * controller.startAnimation();
 * </pre>
 *
 * @author QoS Gaming Team
 * @version 1.0
 * @since 1.0
 */
public class LatencyController {

    /** Modèle de données contenant les séries de latence */
    private LatencyModel model;
    /** Vue graphique pour l'affichage des courbes */
    private ChartView chartView;
    /** Handler pour les animations sur le thread UI */
    private Handler animationHandler = new Handler(Looper.getMainLooper());
    /** Listener pour les callbacks vers l'interface */
    private ControllerListener listener;

    /** État de l'animation des courbes */
    private boolean isAnimating;
    /** État de la pause de visualisation */
    private boolean isInVisualizationPause;
    /** Iterator pour parcourir les séries valides */
    private Iterator<LatencyModel.SeriesData> seriesIterator;
    /** Série actuellement en cours d'animation */
    private LatencyModel.SeriesData currentSeries;
    /** Index du point actuel dans la série courante */
    private int currentPointIndex;

    /**
     * Interface de callback pour notifier l'interface utilisateur des événements d'animation.
     *
     * <p>Implémentez cette interface pour recevoir les notifications du cycle d'animation.</p>
     *
     * @since 1.0
     */
    public interface ControllerListener {
        /**
         * Notifie le début de l'animation des courbes.
         */
        void onAnimationStarted();

        /**
         * Notifie la fin de l'animation des courbes (avant la pause).
         */
        void onAnimationFinished();

        /**
         * Notifie le début de la pause de visualisation.
         * @param remainingSeconds Durée de la pause en secondes
         */
        void onVisualizationPauseStarted(int remainingSeconds);

        /**
         * Notifie chaque seconde du compte à rebours de la pause.
         * @param remainingSeconds Secondes restantes
         */
        void onVisualizationPauseCountdown(int remainingSeconds);

        /**
         * Notifie la fin de la pause de visualisation.
         */
        void onVisualizationPauseFinished();

        /**
         * Notifie le début de l'animation d'une nouvelle série.
         * @param seriesNumber Numéro de la série (1-5)
         * @param seriesName Nom affiché de la série
         */
        void onSeriesStarted(int seriesNumber, String seriesName);

        /**
         * Notifie l'ajout d'un nouveau point de latence.
         * @param seriesNumber Numéro de la série
         * @param latencyMs Valeur de latence en millisecondes
         * @param pointIndex Index du point (1-based)
         * @param totalPoints Nombre total de points dans cette série
         */
        void onPointAdded(int seriesNumber, double latencyMs, int pointIndex, int totalPoints);
    }

    /**
     * Constructeur du contrôleur de latence.
     *
     * @param model Modèle contenant les données de latence
     * @param chartView Vue graphique pour l'affichage
     * @throws IllegalArgumentException Si model ou chartView est null
     */
    public LatencyController(LatencyModel model, ChartView chartView) {
        this.model = model;
        this.chartView = chartView;
    }

    /**
     * Définit le listener pour recevoir les callbacks d'animation.
     * @param listener Interface de callback ou null pour désactiver
     */
    public void setListener(ControllerListener listener) { this.listener = listener; }

    /**
     * Initialise les données en chargeant le JSON par défaut.
     *
     * <p>Cette méthode doit être appelée avant {@link #startAnimation()}.</p>
     */
    public void initializeData() { model.loadData(); }

    /**
     * Vérifie si une animation ou pause est en cours.
     * @return true si le contrôleur est actif
     */
    public boolean isAnimating() { return isAnimating || isInVisualizationPause; }

    /**
     * Nettoie les ressources et arrête toute animation en cours.
     *
     * <p>À appeler dans onDestroy() de l'Activity.</p>
     */
    public void cleanup() { stopAnimation(); listener = null; }

    /**
     * Démarre le cycle complet d'animation.
     *
     * <p>Le cycle comprend :</p>
     * <ol>
     *   <li>Animation des séries valides (10ms par point)</li>
     *   <li>Pause de visualisation (7 secondes)</li>
     *   <li>Transition vers l'écran des scores</li>
     * </ol>
     *
     * <p>Ne fait rien si une animation est déjà en cours ou si aucune donnée n'est disponible.</p>
     *
     * @see #stopAnimation()
     * @see #isAnimating()
     */
    public void startAnimation() {
        if (isAnimating() || !model.hasData()) return;

        isAnimating = true;
        chartView.clearAllSeries();
        seriesIterator = model.validSeriesIterator();

        if (listener != null) listener.onAnimationStarted();
        moveToNextSeries();
    }

    /**
     * Arrête immédiatement toute animation en cours.
     *
     * <p>Annule tous les callbacks programmés et remet l'état à zéro.</p>
     */
    public void stopAnimation() {
        isAnimating = false;
        isInVisualizationPause = false;
        animationHandler.removeCallbacksAndMessages(null);
        if (listener != null) listener.onAnimationFinished();
    }

    /**
     * Passe à la série suivante ou démarre la pause si toutes sont terminées.
     *
     * <p>Méthode privée qui gère la logique de progression entre les séries.</p>
     */
    private void moveToNextSeries() {
        if (!isAnimating) return;

        if (seriesIterator.hasNext()) {
            currentSeries = seriesIterator.next();
            currentPointIndex = 0;
            chartView.startNewSeries(currentSeries.getSeriesNumber());

            if (listener != null) {
                listener.onSeriesStarted(currentSeries.getSeriesNumber(), "Série " + currentSeries.getSeriesNumber());
            }

            displayNextPoint();
        } else {
            startVisualizationPause();
        }
    }

    /**
     * Affiche le point suivant de la série courante.
     *
     * <p>Cette méthode s'auto-programme toutes les 10ms pour créer l'animation fluide.
     * Notifie le listener tous les 3 points pour éviter le spam d'événements.</p>
     */
    private void displayNextPoint() {
        if (!isAnimating || currentSeries == null) return;

        List<Double> rttValues = currentSeries.getRttValues();
        if (currentPointIndex < rttValues.size()) {
            double latencyMs = rttValues.get(currentPointIndex);
            chartView.addPoint(currentSeries.getSeriesNumber(), (float) latencyMs);

            // Notification avec throttling (tous les 3 points)
            if (listener != null && currentPointIndex % 3 == 0) {
                listener.onPointAdded(currentSeries.getSeriesNumber(), latencyMs, currentPointIndex + 1, rttValues.size());
            }

            currentPointIndex++;
            // Programmer le point suivant dans 10ms
            animationHandler.postDelayed(this::displayNextPoint, 10);
        } else {
            moveToNextSeries();
        }
    }

    /**
     * Démarre la pause de visualisation de 7 secondes.
     *
     * <p>Transition entre la fin de l'animation et l'affichage des scores.
     * Permet à l'utilisateur d'analyser visuellement les courbes avant les résultats.</p>
     */
    private void startVisualizationPause() {
        isAnimating = false;
        isInVisualizationPause = true;

        if (listener != null) {
            listener.onAnimationFinished();
            listener.onVisualizationPauseStarted(7);
        }

        startCountdown(7);
    }

    /**
     * Gère le compte à rebours de la pause de visualisation.
     *
     * <p>Notifie le listener chaque seconde du temps restant.</p>
     *
     * @param remainingSeconds Secondes restantes dans le compte à rebours
     */
    private void startCountdown(int remainingSeconds) {
        if (!isInVisualizationPause) return;

        if (remainingSeconds > 0) {
            if (listener != null) listener.onVisualizationPauseCountdown(remainingSeconds);
            // Programmer la seconde suivante
            animationHandler.postDelayed(() -> startCountdown(remainingSeconds - 1), 1000);
        } else {
            // Fin du compte à rebours
            isInVisualizationPause = false;
            if (listener != null) listener.onVisualizationPauseFinished();
        }
    }
}