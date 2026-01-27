package com.qos.latency.analyzer.utils;

/**
 * Classe helper pour générer des données de test JSON
 * <p>
 * Cette classe fournit différents exemples de fichiers JSON pour tester
 * toutes les situations possibles : fichiers valides, formats alternatifs,
 * fichiers corrompus, paquets perdus, etc.
 * <p>
 * Avoir ces données en dur permet de tester sans dépendre de vrais fichiers
 * et garantit que les tests sont reproductibles et rapides.
 *
 * @author ABAKAR Oumar
 */
public class TestDataHelper {

    /**
     * JSON valide avec différents types de paquets
     * Contient 5 paquets représentant tous les cas possibles :
     * - Paquet 0 : Reçu normalement (RECEIVED)
     * - Paquet 1 : Reçu normalement (RECEIVED)
     * - Paquet 2 : Perdu (rx_ts = 0, LOST)
     * - Paquet 3 : Dupliqué (duplicated = 1, DUPLICATED)
     * - Paquet 4 : Inversé (reversed = 1, REVERSED)
     * Ce JSON couvre tous les cas de figure pour valider le parsing complet.
     *
     * @return JSON valide avec 5 paquets de test
     */
    public static String getValidJsonData() {
        return "{\n" +
                "  \"transmitted\": 10,\n" +
                "  \"received\": 8,\n" +
                "  \"request_array\": {\n" +
                "    \"0\": {\n" +
                "      \"tx_ts\": 1756192408770903,\n" +
                "      \"rx_ts\": 1756192408803179,\n" +
                "      \"rtt\": 32.276,\n" +
                "      \"duplicated\": 0,\n" +
                "      \"reversed\": 0\n" +
                "    },\n" +
                "    \"1\": {\n" +
                "      \"tx_ts\": 1756192408831058,\n" +
                "      \"rx_ts\": 1756192408860613,\n" +
                "      \"rtt\": 29.555,\n" +
                "      \"duplicated\": 0,\n" +
                "      \"reversed\": 0\n" +
                "    },\n" +
                "    \"2\": {\n" +
                "      \"tx_ts\": 1756192408891011,\n" +
                "      \"rx_ts\": 0,\n" +
                "      \"rtt\": 0,\n" +
                "      \"duplicated\": 0,\n" +
                "      \"reversed\": 0\n" +
                "    },\n" +
                "    \"3\": {\n" +
                "      \"tx_ts\": 1756192408950979,\n" +
                "      \"rx_ts\": 1756192408978011,\n" +
                "      \"rtt\": 27.032,\n" +
                "      \"duplicated\": 1,\n" +
                "      \"reversed\": 0\n" +
                "    },\n" +
                "    \"4\": {\n" +
                "      \"tx_ts\": 1756192409011025,\n" +
                "      \"rx_ts\": 1756192409059842,\n" +
                "      \"rtt\": 48.817,\n" +
                "      \"duplicated\": 0,\n" +
                "      \"reversed\": 1\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    /**
     * JSON avec format alternatif (dup/rev au lieu de duplicated/reversed)
     * Certains outils génèrent des JSONs avec des noms de champs abrégés.
     * L'application doit être capable de les gérer aussi pour être robuste.
     * Ce JSON teste la compatibilité avec :
     * - "dup" au lieu de "duplicated"
     * - "rev" au lieu de "reversed"
     * Cela garantit que l'application accepte plusieurs variantes du format.
     *
     * @return JSON avec format alternatif
     */
    public static String getAlternativeFormatJson() {
        return "{\n" +
                "  \"request_array\": {\n" +
                "    \"0\": {\n" +
                "      \"tx_ts\": 1756192408770903,\n" +
                "      \"rx_ts\": 1756192408803179,\n" +
                "      \"rtt\": 32.276,\n" +
                "      \"dup\": 0,\n" +
                "      \"rev\": 0\n" +
                "    },\n" +
                "    \"1\": {\n" +
                "      \"tx_ts\": 1756192408831058,\n" +
                "      \"rx_ts\": 1756192408860613,\n" +
                "      \"rtt\": 29.555,\n" +
                "      \"dup\": 1,\n" +
                "      \"rev\": 0\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

    /**
     * JSON invalide (pas de request_array)
     * Simule un fichier JSON corrompu ou mal formaté.
     * L'application doit détecter ce problème et lever une exception
     * plutôt que de crasher silencieusement.
     * Ce JSON manque le champ obligatoire "request_array".
     * Le test vérifie que l'application lève une RuntimeException claire
     * pour informer l'utilisateur du problème.
     *
     * @return JSON invalide sans request_array
     */
    public static String getInvalidJsonData() {
        return "{\n" +
                "  \"transmitted\": 10,\n" +
                "  \"received\": 8\n" +
                "}";
    }

    /**
     * JSON avec paquet perdu (rx_ts = 0)
     * Teste spécifiquement la détection des paquets perdus.
     * Un paquet perdu se caractérise par :
     * - rx_ts = 0 (aucun temps de réception)
     * - rtt = 0 (aucun temps de réponse)
     * Ces paquets doivent apparaître en rouge sur le graphique
     * et être positionnés selon leur temps d'envoi (tx_ts).
     * C'est un cas critique pour l'analyse QoS gaming car les paquets
     * perdus indiquent des problèmes de connexion sévères.
     *
     * @return JSON avec un paquet perdu
     */
    public static String getLostPacketJson() {
        return "{\n" +
                "  \"request_array\": {\n" +
                "    \"0\": {\n" +
                "      \"tx_ts\": 1756192408770903,\n" +
                "      \"rx_ts\": 0,\n" +
                "      \"rtt\": 0,\n" +
                "      \"duplicated\": 0,\n" +
                "      \"reversed\": 0\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }
}