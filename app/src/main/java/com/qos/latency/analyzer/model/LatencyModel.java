package com.qos.latency.analyzer.model;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Modèle de données pour l'analyse de latence QoS Gaming.
 *
 * <p>Cette classe gère les données de latence pour 5 séries de mesures et les scores
 * d'interaction calculés. Elle utilise le pattern Iterator pour parcourir les séries
 * et charge les données depuis un format JSON.</p>
 *
 * <p>Structure des données :</p>
 * <ul>
 *   <li>5 séries de mesures de latence (rtt_array)</li>
 *   <li>Scores d'interaction : IntAct, Il, DPDV, DDQ</li>
 *   <li>Statistiques par série : gigue, perte de paquets</li>
 * </ul>
 *
 * @author QoS Gaming Team
 * @version 1.0
 * @since 1.0
 */
public class LatencyModel implements Iterable<LatencyModel.SeriesData> {

    /** Liste des 5 séries de données de latence */
    private List<SeriesData> allSeries = new ArrayList<>();

    /** Scores d'interaction calculés à partir des données */
    private InteractScores interactScores = new InteractScores();

    /** Données JSON par défaut contenant les mesures et scores */
// Dans LatencyModel.java, remplacer DEFAULT_JSON_DATA par :

// Dans LatencyModel.java, remplacer DEFAULT_JSON_DATA par :

    private static final String DEFAULT_JSON_DATA = "{\n" +
            "  \"series\": [\n" +
            "    {\n" +
            "      \"id\": 1,\n" +
            "      \"transmitted\": 125,\n" +
            "      \"received\": 86,\n" +
            "      \"packet_loss\": \"31%\",\n" +
            "      \"rtt_array\": [66.104, 57.988, 63.206, 55.436, 47.480, 45.876, 42.246, 55.969, 59.106, 51.559, 57.239, 49.338, 56.193, 52.055, 90.505, 105.012, 116.093, 111.949, 104.355, 96.128, 88.457, 80.227, 93.418, 85.702, 77.754, 90.931, 97.157, 109.245, 116.220, 176.352, 168.542, 160.863, 152.900, 144.801, 146.567, 138.565, 130.607, 122.777, 114.640, 106.785, 98.605, 90.807, 82.578, 74.869, 66.646, 155.148, 227.247, 219.570, 216.665, 208.734, 214.942, 212.271, 204.870, 196.883, 204.640, 196.817, 204.707, 196.872, 188.747, 180.880, 396.209, 388.805, 380.758, 372.816, 364.839, 484.545, 476.591, 482.209, 484.680, 483.787, 480.911, 472.915, 464.895, 468.414, 460.579, 444.947, 452.936, 442.812, 441.429, 438.138, 430.650, 489.243, 524.963, 493.031, 494.309, 525.376],\n" +
            "      \"has_error\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": 2,\n" +
            "      \"transmitted\": 375,\n" +
            "      \"received\": 33,\n" +
            "      \"packet_loss\": \"91%\",\n" +
            "      \"rtt_array\": [47.867, 50.155, 47.318, 52.911, 53.437, 58.117, 55.397, 52.851, 54.858, 52.267, 49.613, 86.588, 94.512, 97.434, 163.580, 179.946, 212.353, 256.666, 269.994, 267.477, 265.018, 262.172, 259.502, 355.352, 357.898, 364.274, 378.979, 400.530, 414.425, 414.450, 430.551, 427.987, 425.358],\n" +
            "      \"has_error\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": 3,\n" +
            "      \"error\": \"INIT_TIMEOUT\",\n" +
            "      \"packet_loss\": \"100%\",\n" +
            "      \"rtt_array\": [],\n" +
            "      \"has_error\": true\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": 4,\n" +
            "      \"error\": \"INIT_TIMEOUT\",\n" +
            "      \"packet_loss\": \"100%\",\n" +
            "      \"rtt_array\": [],\n" +
            "      \"has_error\": true\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": 5,\n" +
            "      \"transmitted\": 375,\n" +
            "      \"received\": 198,\n" +
            "      \"packet_loss\": \"47%\",\n" +
            "      \"rtt_array\": [68.574, 60.747, 67.988, 63.359, 61.113, 58.968, 51.125, 53.851, 51.190, 61.398, 63.112, 55.680, 47.613, 99.275, 123.146, 209.612, 221.386, 213.729, 205.758, 203.241, 211.552, 195.598, 217.997, 210.151, 234.012, 310.504, 346.390, 338.124, 330.560, 322.305, 314.623, 394.553, 386.925, 378.921, 439.117, 441.292, 453.175, 449.054, 441.178, 433.375, 425.299, 417.309, 409.645, 439.935, 457.115, 469.939, 462.356, 467.083, 516.727, 524.370, 516.671, 508.472, 500.424, 499.793, 510.008, 517.129, 497.156, 506.083, 495.265, 487.374, 479.258, 607.942, 612.046, 615.145, 607.651, 599.390, 591.467, 583.533, 575.739, 573.887, 566.421, 550.195, 558.276, 626.249, 675.740, 667.935, 662.764, 693.778, 704.374, 696.520, 706.080, 712.817, 696.812, 688.851, 704.886, 680.989, 727.953, 731.184, 723.213, 735.160, 743.520, 727.537, 751.274, 744.272, 736.331, 731.243, 723.356, 715.344, 722.853, 751.445, 821.993, 814.428, 806.817, 798.555, 790.896, 787.499, 779.854, 822.522, 892.228, 896.430, 896.761, 907.074, 899.270, 891.578, 883.289, 898.256, 919.404, 911.269, 928.008, 931.487, 929.517, 921.539, 923.320, 915.452, 917.449, 962.168, 965.447, 988.166, 980.374, 972.460, 964.554, 948.684, 940.722, 956.866, 932.855, 972.163, 997.390, 997.488, 989.950, 1042.129, 1048.682, 1040.723, 1032.575, 1024.681, 1043.084, 1095.968, 1116.531, 1151.971, 1144.077, 1146.849, 1144.897, 1129.392, 1137.181, 1140.117, 1132.299, 1179.186, 1171.450, 1163.025, 1196.755, 1204.477, 1188.613, 1238.610, 1298.152, 1290.322, 1282.692, 1274.481, 1271.778, 1270.142, 1292.743, 1285.308, 1277.103, 1269.425, 1261.187, 1371.412, 1363.659, 1355.383, 1346.861, 1339.776, 1332.081, 1324.191, 1368.545, 1378.988, 1371.410, 1428.185, 1425.402, 1417.488, 1409.913, 1401.854, 1446.401, 1443.414, 1451.407, 1443.653, 1450.200, 1442.641, 1464.578, 1479.316, 1505.209, 1565.772],\n" +
            "      \"has_error\": false,\n" +
            "      \"Interact_scores\": {\n" +
            "        \"series\": 5,\n" +
            "        \"total\": 5,\n" +
            "        \"use-case\": \"e-gaming\",\n" +
            "        \"median_pdvq\": 438.665,\n" +
            "        \"10pct_pdvq\": 20.18,\n" +
            "        \"99_9pct_pdvq\": 1504.388,\n" +
            "        \"IntAct\": 56.352,\n" +
            "        \"Il\": 8.148,\n" +
            "        \"DPDV\": 26.098,\n" +
            "        \"DDQ\": 26.502\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}";
    /**
     * Scores d'interaction calculés pour l'analyse QoS Gaming.
     *
     * <p>Cette classe encapsule les 4 scores principaux utilisés pour évaluer
     * la qualité de service pour le gaming :</p>
     * <ul>
     *   <li><strong>IntAct</strong> : Score e-gaming global (principal)</li>
     *   <li><strong>Il</strong> : Score de latence</li>
     *   <li><strong>DPDV</strong> : Score de variabilité de latence (gigue)</li>
     *   <li><strong>DDQ</strong> : Score d'erreur/perte de paquets</li>
     * </ul>
     *
     * @since 1.0
     */
    public static class InteractScores {
        /** Score e-gaming global (IntAct) */
        private double intAct;
        /** Score de latence (Il) */
        private double il;
        /** Score de variabilité de latence (DPDV) */
        private double dpdv;
        /** Score d'erreur/perte de paquets (DDQ) */
        private double ddq;

        /**
         * Obtient le score e-gaming global.
         * @return Score IntAct (0-100)
         */
        public double getIntAct() { return intAct; }

        /**
         * Obtient le score de latence.
         * @return Score Il (0-100)
         */
        public double getIl() { return il; }

        /**
         * Obtient le score de variabilité de latence.
         * @return Score DPDV (0-100)
         */
        public double getDpdv() { return dpdv; }

        /**
         * Obtient le score d'erreur/perte de paquets.
         * @return Score DDQ (0-100)
         */
        public double getDdq() { return ddq; }

        /**
         * Définit le score e-gaming global.
         * @param intAct Score IntAct (0-100)
         */
        void setIntAct(double intAct) { this.intAct = intAct; }

        /**
         * Définit le score de latence.
         * @param il Score Il (0-100)
         */
        void setIl(double il) { this.il = il; }

        /**
         * Définit le score de variabilité de latence.
         * @param dpdv Score DPDV (0-100)
         */
        void setDpdv(double dpdv) { this.dpdv = dpdv; }

        /**
         * Définit le score d'erreur/perte de paquets.
         * @param ddq Score DDQ (0-100)
         */
        void setDdq(double ddq) { this.ddq = ddq; }

        /**
         * Vérifie si les scores sont valides (au moins un score > 0).
         * @return true si au moins un score est positif
         */
        public boolean isValid() { return intAct > 0 || il > 0 || dpdv > 0 || ddq > 0; }

        /**
         * Formate le score IntAct pour l'affichage.
         * @return Chaîne formatée "XX.X %"
         */
        public String getFormattedIntAct() { return String.format("%.1f %%", intAct); }

        /**
         * Formate le score Il pour l'affichage.
         * @return Chaîne formatée "XX.X %"
         */
        public String getFormattedIl() { return String.format("%.1f %%", il); }

        /**
         * Formate le score DPDV pour l'affichage.
         * @return Chaîne formatée "XX.X %"
         */
        public String getFormattedDpdv() { return String.format("%.1f %%", dpdv); }

        /**
         * Formate le score DDQ pour l'affichage.
         * @return Chaîne formatée "XX.X %"
         */
        public String getFormattedDdq() { return String.format("%.1f %%", ddq); }
    }

    /**
     * Données d'une série de mesures de latence.
     *
     * <p>Chaque série contient :</p>
     * <ul>
     *   <li>Un numéro d'identification (1-5)</li>
     *   <li>Un tableau de valeurs RTT en millisecondes</li>
     *   <li>La gigue calculée (variabilité entre mesures consécutives)</li>
     *   <li>Le pourcentage de perte de paquets</li>
     *   <li>Un indicateur d'erreur</li>
     * </ul>
     *
     * @since 1.0
     */
    public static class SeriesData {
        /** Numéro de la série (1-5) */
        private int seriesNumber;
        /** Tableau des valeurs RTT en millisecondes */
        private List<Double> rttArray = new ArrayList<>();
        /** Gigue calculée en millisecondes */
        private double jitterMs;
        /** Pourcentage de perte de paquets */
        private double packetLossPercent;
        /** Indicateur d'erreur pour cette série */
        private boolean hasError;

        /**
         * Constructeur d'une série de données.
         * @param seriesNumber Numéro de la série (1-5)
         */
        public SeriesData(int seriesNumber) { this.seriesNumber = seriesNumber; }

        /**
         * Obtient le numéro de la série.
         * @return Numéro de série (1-5)
         */
        public int getSeriesNumber() { return seriesNumber; }

        /**
         * Obtient les valeurs RTT de la série.
         * @return Liste des valeurs RTT en millisecondes
         */
        public List<Double> getRttValues() { return rttArray; }

        /**
         * Obtient la gigue calculée.
         * @return Gigue en millisecondes
         */
        public double getJitterMs() { return jitterMs; }

        /**
         * Obtient le pourcentage de perte de paquets.
         * @return Pourcentage de perte (0-100)
         */
        public double getPacketLossPercent() { return packetLossPercent; }

        /**
         * Vérifie si la série a une erreur.
         * @return true si la série est en erreur
         */
        public boolean hasError() { return hasError; }

        /**
         * Vérifie si la série est valide (pas d'erreur et contient des données).
         * @return true si la série est valide
         */
        public boolean isValid() { return !hasError && !rttArray.isEmpty(); }

        /**
         * Définit la gigue calculée.
         * @param jitterMs Gigue en millisecondes
         */
        void setJitterMs(double jitterMs) { this.jitterMs = jitterMs; }

        /**
         * Définit le pourcentage de perte de paquets.
         * @param packetLossPercent Pourcentage de perte (0-100)
         */
        void setPacketLossPercent(double packetLossPercent) { this.packetLossPercent = packetLossPercent; }

        /**
         * Définit l'état d'erreur de la série.
         * @param hasError true si la série est en erreur
         */
        void setHasError(boolean hasError) { this.hasError = hasError; }

        /**
         * Ajoute une valeur RTT à la série.
         * @param value Valeur RTT en millisecondes
         */
        void addRttValue(double value) { this.rttArray.add(value); }

        /**
         * Calcule la gigue (variabilité) de la série.
         *
         * <p>La gigue est calculée comme la moyenne des différences absolues
         * entre les valeurs RTT consécutives.</p>
         *
         * @return Gigue en millisecondes
         */
        public double calculateJitter() {
            if (rttArray.size() < 2) return 0.0;
            double sum = 0.0;
            for (int i = 1; i < rttArray.size(); i++) {
                sum += Math.abs(rttArray.get(i) - rttArray.get(i - 1));
            }
            return sum / (rttArray.size() - 1);
        }

        /**
         * Calcule les statistiques BoxPlot de la série.
         *
         * <p>Retourne un tableau contenant dans l'ordre :</p>
         * <ol>
         *   <li>Minimum</li>
         *   <li>Premier quartile (Q1)</li>
         *   <li>Médiane (Q2)</li>
         *   <li>Troisième quartile (Q3)</li>
         *   <li>Maximum</li>
         * </ol>
         *
         * @return Tableau des 5 valeurs BoxPlot
         */
        public double[] getBoxPlotStats() {
            if (rttArray.isEmpty()) return new double[5];

            rttArray.sort(Double::compareTo);
            int size = rttArray.size();

            return new double[]{
                    rttArray.get(0),                    // min
                    rttArray.get(size / 4),             // q1
                    rttArray.get(size / 2),             // median
                    rttArray.get(size * 3 / 4),         // q3
                    rttArray.get(size - 1)              // max
            };
        }
    }

    /**
     * Constructeur du modèle de latence.
     * Initialise les 5 séries vides et les scores à zéro.
     */
    public LatencyModel() {
        for (int i = 1; i <= 5; i++) {
            allSeries.add(new SeriesData(i));
        }
    }

    /**
     * Implémentation de l'interface Iterable pour parcourir toutes les séries.
     * @return Iterator sur toutes les séries (valides et en erreur)
     */
    @Override
    public Iterator<SeriesData> iterator() { return allSeries.iterator(); }

    /**
     * Retourne un Iterator sur les séries valides uniquement.
     *
     * <p>Filtre automatiquement les séries en erreur ou vides.</p>
     *
     * @return Iterator sur les séries valides seulement
     */
    public Iterator<SeriesData> validSeriesIterator() {
        return allSeries.stream().filter(SeriesData::isValid).iterator();
    }

    /**
     * Charge les données depuis le JSON par défaut.
     *
     * <p>Cette méthode :</p>
     * <ol>
     *   <li>Vide toutes les séries existantes</li>
     *   <li>Parse le JSON des données par défaut</li>
     *   <li>Remplit les séries avec les rtt_array</li>
     *   <li>Calcule automatiquement la gigue pour chaque série</li>
     *   <li>Charge les scores d'interaction</li>
     * </ol>
     *
     * @throws RuntimeException Si le parsing JSON échoue
     */
// Dans LatencyModel.java, remplacer la méthode loadData() par :

// Dans LatencyModel.java, remplacer la méthode loadData() par :

    public void loadData() {
        try {
            JSONObject root = new JSONObject(DEFAULT_JSON_DATA);

            // Clear data
            allSeries.forEach(series -> {
                series.rttArray.clear();
                series.setHasError(false);
            });

            // Load series
            JSONArray seriesArray = root.getJSONArray("series");
            for (int i = 0; i < seriesArray.length(); i++) {
                JSONObject seriesObj = seriesArray.getJSONObject(i);
                int seriesId = seriesObj.getInt("id");

                if (seriesId >= 1 && seriesId <= 5) {
                    SeriesData series = allSeries.get(seriesId - 1);

                    // Vérifier s'il y a une erreur
                    if (seriesObj.has("error") || seriesObj.optBoolean("has_error", false)) {
                        series.setHasError(true);
                    } else {
                        // Charger les données RTT
                        if (seriesObj.has("rtt_array")) {
                            JSONArray rttArray = seriesObj.getJSONArray("rtt_array");
                            for (int j = 0; j < rttArray.length(); j++) {
                                series.addRttValue(rttArray.getDouble(j));
                            }
                            series.setJitterMs(series.calculateJitter());
                        }

                        // Charger packet_loss
                        if (seriesObj.has("packet_loss")) {
                            String packetLossStr = seriesObj.getString("packet_loss");
                            if (packetLossStr.endsWith("%")) {
                                double packetLoss = Double.parseDouble(packetLossStr.replace("%", ""));
                                series.setPacketLossPercent(packetLoss);
                            } else {
                                series.setPacketLossPercent(seriesObj.getDouble("packet_loss"));
                            }
                        }

                        // NOUVEAU : Lire les scores depuis la série 5 SEULEMENT
                        if (seriesId == 5 && seriesObj.has("Interact_scores")) {
                            JSONObject scoresObj = seriesObj.getJSONObject("Interact_scores");
                            interactScores.setIntAct(scoresObj.getDouble("IntAct"));
                            interactScores.setIl(scoresObj.getDouble("Il"));
                            interactScores.setDpdv(scoresObj.getDouble("DPDV"));
                            interactScores.setDdq(scoresObj.getDouble("DDQ"));
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Obtient les scores d'interaction.
     * @return Objet InteractScores contenant les 4 scores
     */
    public InteractScores getInteractScores() { return interactScores; }

    /**
     * Obtient une série spécifique par son numéro.
     * @param seriesNumber Numéro de série (1-5)
     * @return Série correspondante ou null si numéro invalide
     */
    public SeriesData getSeries(int seriesNumber) {
        return (seriesNumber >= 1 && seriesNumber <= 5) ? allSeries.get(seriesNumber - 1) : null;
    }

    /**
     * Vérifie si le modèle contient des données valides.
     * @return true si au moins une série est valide
     */
    public boolean hasData() {
        return allSeries.stream().anyMatch(SeriesData::isValid);
    }
}