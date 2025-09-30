package com.qos.latency.analyzer.controller;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.qos.latency.analyzer.model.LatencyModel;
import com.qos.latency.analyzer.view.ChartView;

import java.util.List;

/**
 * Contrôleur gérant l'animation chronologique des données de latence réseau.
 *
 * Cette classe implémente la partie "Controller" du pattern MVC et orchestre
 * l'affichage progressif des paquets réseau sur le graphique. Elle gère :
 * - Le timing de l'animation (100ms entre chaque paquet)
 * - La communication entre le modèle de données et la vue graphique
 * - Les callbacks vers l'interface utilisateur pour les mises à jour
 * - La pause de visualisation en fin d'animation
 *
 * Le contrôleur utilise un Handler Android pour gérer l'animation de manière
 * asynchrone sans bloquer l'interface utilisateur.
 *
 * @author Équipe QoS Gaming
 * @version 3.0
 */
public class LatencyController {

    private static final String TAG = "LatencyController";

    // Références vers les autres composants MVC
    private LatencyModel model;
    private ChartView chartView;
    private Handler animationHandler = new Handler(Looper.getMainLooper());
    private ControllerListener listener;

    // Variables d'état de l'animation
    private boolean isAnimating;
    private boolean isInVisualizationPause;
    private int currentPacketIndex = 0;
    private List<LatencyModel.RequestData> currentPacketList;

    /** Délai en millisecondes entre l'affichage de chaque paquet */
    private static final int PACKET_ANIMATION_DELAY = 100;

    /**
     * Interface définissant les callbacks que le contrôleur peut envoyer
     * vers l'interface utilisateur pour notifier des changements d'état.
     */
    public interface ControllerListener {
        /** Appelé quand l'animation démarre */
        void onAnimationStarted();

        /** Appelé quand l'animation se termine */
        void onAnimationFinished();

        /** Appelé au début d'une nouvelle analyse */
        void onAnalysisStarted(String analysisName);

        /** Appelé à chaque nouveau paquet affiché */
        void onPacketDisplayed(LatencyModel.RequestData packet, int displayedCount, int totalCount);

        /** Appelé à chaque seconde pendant la pause de visualisation */
        void onVisualizationPauseCountdown(int remainingSeconds);

        /** Appelé quand la pause de visualisation se termine */
        void onVisualizationPauseFinished();
    }

    /**
     * Constructeur du contrôleur.
     *
     * @param model Modèle contenant les données à animer
     * @param chartView Vue graphique où afficher l'animation
     * @param context Contexte Android (requis mais non utilisé actuellement)
     * @throws IllegalArgumentException Si l'un des paramètres est null
     */
    public LatencyController(LatencyModel model, ChartView chartView, Context context) {
        if (model == null || chartView == null || context == null) {
            throw new IllegalArgumentException("Paramètres requis");
        }
        this.model = model;
        this.chartView = chartView;
    }

    /**
     * Définit le listener qui recevra les callbacks d'état.
     *
     * @param listener Interface implémentée par l'activité principale
     */
    public void setListener(ControllerListener listener) {
        this.listener = listener;
    }

    /**
     * Vérifie si une animation est actuellement en cours ou si on est en pause.
     *
     * @return true si le contrôleur est occupé, false sinon
     */
    public boolean isAnimating() {
        return isAnimating || isInVisualizationPause;
    }

    /**
     * Nettoie les ressources utilisées par le contrôleur.
     * Doit être appelé quand le contrôleur n'est plus utilisé.
     */
    public void cleanup() {
        stopAnimation();
        listener = null;
    }

    /**
     * Démarre une nouvelle animation des données chargées dans le modèle.
     *
     * Vérifie d'abord que :
     * - Aucune animation n'est déjà en cours
     * - Le modèle contient des données à afficher
     *
     * Prépare ensuite la vue graphique et lance l'animation séquentielle.
     */
    public void startAnimation() {
        try {
            if (isAnimating() || !model.hasData()) {
                return;
            }

            // Initialisation de l'animation
            isAnimating = true;
            chartView.clearAllSeries();
            chartView.setTimestampMode(true);

            // Récupération des données à animer
            LatencyModel.RequestArrayData requestArrayData = model.getRequestArrayData();
            currentPacketList = requestArrayData.getRequestDataList();
            currentPacketIndex = 0;

            // Notification du début d'animation
            if (listener != null) {
                listener.onAnimationStarted();
                listener.onAnalysisStarted("Analyse chronologique - " + currentPacketList.size() + " paquets");
            }

            // Démarre l'animation du premier paquet
            animateNextPacket();

        } catch (Exception e) {
            Log.e(TAG, "Erreur animation: " + e.getMessage());
            isAnimating = false;
        }
    }

    /**
     * Méthode récursive qui anime le paquet suivant dans la séquence.
     *
     * Cette méthode :
     * 1. Vérifie qu'on n'a pas été interrompu
     * 2. Affiche le paquet courant sur le graphique
     * 3. Notifie l'interface utilisateur
     * 4. Programme l'affichage du paquet suivant après un délai
     * 5. Ou lance la pause de visualisation si tous les paquets sont affichés
     */
    private void animateNextPacket() {
        if (!isAnimating) return;

        try {
            if (currentPacketIndex < currentPacketList.size()) {
                LatencyModel.RequestData packet = currentPacketList.get(currentPacketIndex);

                if (packet != null) {
                    // Ajout du paquet au graphique
                    chartView.addPacketPoint(packet);

                    // Notification vers l'interface utilisateur
                    if (listener != null) {
                        listener.onPacketDisplayed(packet, currentPacketIndex + 1, currentPacketList.size());
                    }
                }

                // Préparation du paquet suivant
                currentPacketIndex++;
                animationHandler.postDelayed(this::animateNextPacket, PACKET_ANIMATION_DELAY);

            } else {
                // Tous les paquets sont affichés, commence la pause
                startVisualizationPause();
            }

        } catch (Exception e) {
            Log.e(TAG, "Erreur paquet: " + e.getMessage());
            stopAnimation();
        }
    }

    /**
     * Arrête immédiatement l'animation en cours.
     * Annule tous les callbacks programmés et remet les états à zéro.
     */
    public void stopAnimation() {
        isAnimating = false;
        isInVisualizationPause = false;
        animationHandler.removeCallbacksAndMessages(null);

        if (listener != null) {
            listener.onAnimationFinished();
        }
    }

    /**
     * Démarre la phase de pause de visualisation après l'animation.
     * Cette pause de 30 secondes permet à l'utilisateur d'examiner le graphique complet.
     */
    private void startVisualizationPause() {
        isAnimating = false;
        isInVisualizationPause = true;

        if (listener != null) {
            listener.onAnimationFinished();
        }

        startCountdown(30);
    }

    /**
     * Gère le compte à rebours de la pause de visualisation.
     *
     * @param remainingSeconds Nombre de secondes restantes dans la pause
     */
    private void startCountdown(int remainingSeconds) {
        if (!isInVisualizationPause) return;

        if (remainingSeconds > 0) {
            // Notifie l'interface du temps restant
            if (listener != null) {
                listener.onVisualizationPauseCountdown(remainingSeconds);
            }
            // Programme la prochaine seconde
            animationHandler.postDelayed(() -> startCountdown(remainingSeconds - 1), 1000);
        } else {
            // Fin de la pause
            isInVisualizationPause = false;
            if (listener != null) {
                listener.onVisualizationPauseFinished();
            }
        }
    }
}