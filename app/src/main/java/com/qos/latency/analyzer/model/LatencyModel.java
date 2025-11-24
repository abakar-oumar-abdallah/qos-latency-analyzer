package com.qos.latency.analyzer.model;

import android.content.Context;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Modèle de données pour l'analyse de latence réseau.
 *
 * Cette classe gère le chargement et la structuration des données de paquets réseau
 * à partir de fichiers JSON au format "request_array". Elle calcule les temps relatifs
 * et détermine le statut de chaque paquet (reçu, perdu, dupliqué, inversé).
 *
 * @author Équipe QoS Gaming
 * @version 3.1
 */
public class LatencyModel {

    /** Conteneur principal des données de paquets */
    private RequestArrayData requestArrayData;

    /** Nom du fichier actuellement chargé */
    private String currentFileName = "";

    /**
     * Constructeur par défaut.
     * Initialise un nouveau conteneur de données vide.
     */
    public LatencyModel() {
        requestArrayData = new RequestArrayData();
    }

    /**
     * Énumération des différents statuts possibles d'un paquet réseau.
     */
    public enum PacketStatus {
        /** Paquet reçu normalement */
        RECEIVED,
        /** Paquet perdu (pas de réponse) */
        LOST,
        /** Paquet dupliqué (reçu plusieurs fois) */
        DUPLICATED,
        /** Paquet reçu dans le désordre */
        REVERSED
    }

    /**
     * Classe représentant les données d'un paquet réseau individuel.
     * Contient tous les timestamps et métriques associées à un paquet.
     */
    public static class RequestData {
        private long txTimestamp;           // Timestamp d'envoi
        private long rxTimestamp;           // Timestamp de réception
        private double rtt;                 // Round Trip Time en millisecondes
        private PacketStatus status;        // Statut du paquet
        private int sequenceNumber;         // Numéro de séquence
        private double txTimeRelative;      // Temps d'envoi relatif en secondes
        private double rxTimeRelative;      // Temps de réception relatif en secondes

        /**
         * Constructeur d'un paquet réseau.
         *
         * @param sequenceNumber Numéro de séquence du paquet
         * @param txTimestamp Timestamp d'envoi en microsecondes
         * @param rxTimestamp Timestamp de réception en microsecondes (0 si perdu)
         * @param rtt Round Trip Time en millisecondes
         * @param status Statut du paquet
         */
        public RequestData(int sequenceNumber, long txTimestamp, long rxTimestamp,
                           double rtt, PacketStatus status) {
            this.sequenceNumber = sequenceNumber;
            this.txTimestamp = txTimestamp;
            this.rxTimestamp = rxTimestamp;
            this.rtt = rtt;
            this.status = status;
        }

        /**
         * Calcule les temps relatifs par rapport au premier paquet envoyé.
         * Convertit les microsecondes en secondes pour l'affichage graphique.
         *
         * @param t0 Timestamp du premier paquet (temps de référence)
         */
        public void calculateRelativeTimes(long t0) {
            this.txTimeRelative = (txTimestamp - t0) / 1_000_000.0;
            if (status == PacketStatus.RECEIVED || status == PacketStatus.DUPLICATED || status == PacketStatus.REVERSED) {
                this.rxTimeRelative = (rxTimestamp - t0) / 1_000_000.0;
            } else {
                this.rxTimeRelative = this.txTimeRelative;
            }
        }

        // Méthodes d'accès aux données
        public PacketStatus getStatus() { return status; }
        public int getSequenceNumber() { return sequenceNumber; }
        public double getTxTimeRelative() { return txTimeRelative; }
        public double getRxTimeRelative() { return rxTimeRelative; }
        public double getRtt() { return rtt; }
    }

    /**
     * Conteneur pour toutes les données d'une mesure réseau.
     * Gère la liste des paquets et maintient le timestamp de référence.
     */
    public static class RequestArrayData {
        private List<RequestData> requestDataList = new ArrayList<>();
        private long baseTimestamp = 0;

        /**
         * Ajoute un nouveau paquet à la liste des données.
         * Détermine automatiquement le statut du paquet selon les paramètres.
         *
         * @param sequenceNumber Numéro de séquence
         * @param txTimestamp Timestamp d'envoi
         * @param rxTimestamp Timestamp de réception (0 si perdu)
         * @param rtt Round Trip Time
         * @param duplicated true si le paquet est dupliqué
         * @param reversed true si le paquet est reçu dans le désordre
         */
        public void addRequestData(int sequenceNumber, long txTimestamp, long rxTimestamp,
                                   double rtt, boolean duplicated, boolean reversed) {

            PacketStatus status;
            if (rxTimestamp == 0) {
                status = PacketStatus.LOST;
                rtt = 0.0;
            } else if (duplicated) {
                status = PacketStatus.DUPLICATED;
            } else if (reversed) {
                status = PacketStatus.REVERSED;
            } else {
                status = PacketStatus.RECEIVED;
            }

            if (baseTimestamp == 0) {
                baseTimestamp = txTimestamp;
            }

            RequestData request = new RequestData(sequenceNumber, txTimestamp, rxTimestamp, rtt, status);
            request.calculateRelativeTimes(baseTimestamp);
            requestDataList.add(request);
        }

        /**
         * Efface toutes les données chargées.
         */
        public void clear() {
            requestDataList.clear();
            baseTimestamp = 0;
        }

        public List<RequestData> getRequestDataList() { return requestDataList; }
        public boolean hasData() { return !requestDataList.isEmpty(); }
    }

    /**
     * Récupère la liste des fichiers JSON disponibles dans le stockage de l'app.
     * ✅ MODIFIÉ : Lit depuis getExternalFilesDir() au lieu des assets
     *
     * @param context Contexte Android pour accéder aux fichiers
     * @return Liste des noms de fichiers JSON trouvés
     */
    public static List<String> getAvailableDataFiles(Context context) {
        List<String> jsonFiles = new ArrayList<>();
        try {
            // ✅ NOUVEAU : Lire depuis getExternalFilesDir()
            File appExternalDir = context.getExternalFilesDir(null);
            if (appExternalDir == null) {
                android.util.Log.e("LatencyModel", "getExternalFilesDir() retourne null");
                return jsonFiles;
            }

            File qosDataDir = new File(appExternalDir, "QoS_Data");
            android.util.Log.d("LatencyModel", "Recherche fichiers dans: " + qosDataDir.getAbsolutePath());

            if (!qosDataDir.exists() || !qosDataDir.isDirectory()) {
                android.util.Log.w("LatencyModel", "Dossier QoS_Data n'existe pas ou n'est pas un dossier");
                return jsonFiles;
            }

            File[] files = qosDataDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (files != null) {
                android.util.Log.d("LatencyModel", "Fichiers trouvés: " + files.length);
                for (File file : files) {
                    jsonFiles.add(file.getName());
                    android.util.Log.d("LatencyModel", "  - " + file.getName());
                }
            } else {
                android.util.Log.w("LatencyModel", "listFiles() retourne null");
            }
        } catch (Exception e) {
            android.util.Log.e("LatencyModel", "Erreur lecture fichiers: " + e.getMessage(), e);
            e.printStackTrace();
        }
        return jsonFiles;
    }

    /**
     * Retourne le nom du fichier actuel formaté pour l'affichage.
     * Supprime l'extension .json et remplace les underscores par des espaces.
     */
    public String getDisplayFileName() {
        if (currentFileName.isEmpty()) return "Aucun fichier";
        return currentFileName.replace(".json", "").replace("_", " ");
    }

    /**
     * Charge les données depuis un fichier JSON au format request_array.
     * Compatible avec deux formats de champs : dup/duplicated et rev/reversed.
     * ✅ MODIFIÉ : Lit depuis getExternalFilesDir() au lieu des assets
     *
     * @param context Contexte Android pour accéder aux fichiers
     * @param fileName Nom du fichier JSON à charger
     * @throws RuntimeException Si le fichier n'existe pas ou a un format invalide
     */
    public void loadData(Context context, String fileName) {
        try {
            if (!fileName.endsWith(".json")) {
                fileName += ".json";
            }

            // ✅ NOUVEAU : Lire depuis getExternalFilesDir()
            File appExternalDir = context.getExternalFilesDir(null);
            if (appExternalDir == null) {
                throw new RuntimeException("Impossible d'accéder au stockage de l'app");
            }

            File qosDataDir = new File(appExternalDir, "QoS_Data");
            File file = new File(qosDataDir, fileName);

            android.util.Log.d("LatencyModel", "Tentative de chargement: " + file.getAbsolutePath());

            if (!file.exists()) {
                throw new RuntimeException("Fichier introuvable: " + fileName + " dans " + qosDataDir.getAbsolutePath());
            }

            // Lire le fichier
            InputStream inputStream = new FileInputStream(file);
            Scanner scanner = new Scanner(inputStream, "UTF-8");
            String jsonString = scanner.useDelimiter("\\A").next();
            scanner.close();
            inputStream.close();

            this.currentFileName = fileName;
            JSONObject root = new JSONObject(jsonString);
            requestArrayData.clear();

            if (root.has("request_array")) {
                JSONObject requestArray = root.getJSONObject("request_array");

                for (int i = 0; i < requestArray.length(); i++) {
                    String key = String.valueOf(i);
                    if (requestArray.has(key)) {
                        JSONObject request = requestArray.getJSONObject(key);

                        long txTimestamp = request.getLong("tx_ts");
                        long rxTimestamp = request.optLong("rx_ts", 0);
                        double rtt = request.getDouble("rtt");

                        boolean duplicated = request.optInt("duplicated", 0) == 1 ||
                                request.optInt("dup", 0) == 1;
                        boolean reversed = request.optInt("reversed", 0) == 1 ||
                                request.optInt("rev", 0) == 1;

                        requestArrayData.addRequestData(i, txTimestamp, rxTimestamp, rtt, duplicated, reversed);
                    }
                }

                android.util.Log.d("LatencyModel", "✅ Fichier chargé avec succès: " + requestArrayData.getRequestDataList().size() + " paquets");
            } else {
                throw new RuntimeException("Format non supporté : 'request_array' requis");
            }

        } catch (Exception e) {
            android.util.Log.e("LatencyModel", "❌ Erreur chargement " + fileName + ": " + e.getMessage(), e);
            throw new RuntimeException("Erreur chargement " + fileName + ": " + e.getMessage());
        }
    }

    public RequestArrayData getRequestArrayData() { return requestArrayData; }
    public boolean hasData() { return requestArrayData.hasData(); }
}