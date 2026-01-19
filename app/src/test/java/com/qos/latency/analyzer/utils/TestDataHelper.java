package com.qos.latency.analyzer.utils;

/**
 * Classe helper pour générer des données de test JSON
 */
public class TestDataHelper {

    /**
     * JSON valide avec différents types de paquets
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
     */
    public static String getInvalidJsonData() {
        return "{\n" +
                "  \"transmitted\": 10,\n" +
                "  \"received\": 8\n" +
                "}";
    }

    /**
     * JSON avec paquet perdu (rx_ts = 0)
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