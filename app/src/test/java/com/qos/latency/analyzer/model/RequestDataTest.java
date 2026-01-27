package com.qos.latency.analyzer.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.qos.latency.analyzer.model.LatencyModel.RequestData;
import com.qos.latency.analyzer.model.LatencyModel.PacketStatus;

/**
 * Tests unitaires pour la classe RequestData
 *
 * Cette classe représente un paquet réseau individuel avec toutes ses propriétés.
 * Ces tests vérifient que chaque type de paquet (reçu, perdu, dupliqué, inversé)
 * est correctement créé avec les bonnes propriétés et calculs de temps.
 *
 * @author ABAKAR Oumar
 */
public class RequestDataTest {

    private RequestData receivedPacket;
    private RequestData lostPacket;
    private RequestData duplicatedPacket;
    private RequestData reversedPacket;

    // Constantes pour rendre les tests plus lisibles
    private static final long BASE_TIMESTAMP = 1756192408770903L;
    private static final long TX_TIMESTAMP_1 = 1756192408770903L;
    private static final long RX_TIMESTAMP_1 = 1756192408803179L;
    private static final long TX_TIMESTAMP_2 = 1756192408831058L;
    private static final double RTT_1 = 32.276;

    @Before
    public void setUp() {
        // Créer des exemples de chaque type de paquet avant chaque test
        // Permet de tester isolément chaque type sans interférences

        // Paquet reçu normalement (cas standard)
        receivedPacket = new RequestData(
                0,                  // Numéro de séquence
                TX_TIMESTAMP_1,     // Temps d'envoi
                RX_TIMESTAMP_1,     // Temps de réception
                RTT_1,              // Round Trip Time
                PacketStatus.RECEIVED
        );
        receivedPacket.calculateRelativeTimes(BASE_TIMESTAMP);

        // Paquet perdu (rx_ts = 0, rtt = 0)
        lostPacket = new RequestData(
                1,
                TX_TIMESTAMP_2,
                0,                  // Pas de temps de réception
                0,                  // RTT nul
                PacketStatus.LOST
        );
        lostPacket.calculateRelativeTimes(BASE_TIMESTAMP);

        // Paquet dupliqué (reçu plusieurs fois)
        duplicatedPacket = new RequestData(
                2,
                TX_TIMESTAMP_1,
                RX_TIMESTAMP_1,
                RTT_1,
                PacketStatus.DUPLICATED
        );
        duplicatedPacket.calculateRelativeTimes(BASE_TIMESTAMP);

        // Paquet inversé (arrivé dans le désordre)
        reversedPacket = new RequestData(
                3,
                TX_TIMESTAMP_1,
                RX_TIMESTAMP_1,
                RTT_1,
                PacketStatus.REVERSED
        );
        reversedPacket.calculateRelativeTimes(BASE_TIMESTAMP);
    }

    @Test
    public void testReceivedPacket_HasCorrectStatus() {
        // Vérification de base : le paquet reçu a bien le statut RECEIVED
        assertEquals("Le paquet doit avoir le statut RECEIVED",
                PacketStatus.RECEIVED, receivedPacket.getStatus());
    }

    @Test
    public void testReceivedPacket_HasCorrectRtt() {
        // Vérifie que le RTT est correctement stocké
        // Le RTT (Round Trip Time) est le temps aller-retour du paquet
        assertEquals("Le RTT doit être correct",
                RTT_1, receivedPacket.getRtt(), 0.001);
    }

    @Test
    public void testReceivedPacket_RelativeTimesCalculated() {
        // Vérifie que les temps relatifs sont bien calculés
        // Important pour positionner correctement les points sur l'axe X du graphique

        // Le premier paquet (baseTimestamp) doit avoir un temps relatif de 0
        assertEquals("Le temps TX relatif doit être 0",
                0.0, receivedPacket.getTxTimeRelative(), 0.001);

        // Le temps RX doit être la différence en secondes
        // (RX_TIMESTAMP - BASE_TIMESTAMP) / 1_000_000 pour convertir en secondes
        double expectedRxTime = (RX_TIMESTAMP_1 - BASE_TIMESTAMP) / 1_000_000.0;
        assertEquals("Le temps RX relatif doit être correct",
                expectedRxTime, receivedPacket.getRxTimeRelative(), 0.001);
    }

    @Test
    public void testLostPacket_HasCorrectStatus() {
        // Vérification du statut LOST pour un paquet perdu
        assertEquals("Le paquet perdu doit avoir le statut LOST",
                PacketStatus.LOST, lostPacket.getStatus());
    }

    @Test
    public void testLostPacket_HasZeroRtt() {
        // Un paquet perdu n'a jamais reçu de réponse
        // donc son RTT est forcément 0
        assertEquals("Un paquet perdu doit avoir un RTT de 0",
                0.0, lostPacket.getRtt(), 0.001);
    }

    @Test
    public void testLostPacket_RxTimeEqualsTxTime() {
        // Pour un paquet perdu, on n'a pas de temps de réception (rx_ts = 0)
        // Dans ce cas, on utilise le temps d'envoi (TX) pour les deux valeurs
        // Ça permet de positionner le point rouge sur le graphique
        // à l'endroit où le paquet a été envoyé
        assertEquals("Pour un paquet perdu, RX time = TX time",
                lostPacket.getTxTimeRelative(),
                lostPacket.getRxTimeRelative(),
                0.001);
    }

    @Test
    public void testDuplicatedPacket_HasCorrectStatus() {
        // Vérification du statut DUPLICATED
        assertEquals("Le paquet dupliqué doit avoir le statut DUPLICATED",
                PacketStatus.DUPLICATED, duplicatedPacket.getStatus());
    }

    @Test
    public void testDuplicatedPacket_HasValidRtt() {
        // Un paquet dupliqué a quand même un RTT valide (> 0)
        // Contrairement aux paquets perdus qui ont RTT = 0
        // On vérifie juste qu'il est positif ici
        assertTrue("Un paquet dupliqué doit avoir un RTT > 0",
                duplicatedPacket.getRtt() > 0);
    }

    @Test
    public void testReversedPacket_HasCorrectStatus() {
        // Vérification du statut REVERSED pour un paquet arrivé dans le désordre
        assertEquals("Le paquet inversé doit avoir le statut REVERSED",
                PacketStatus.REVERSED, reversedPacket.getStatus());
    }

    @Test
    public void testSequenceNumber() {
        // Vérifie que les numéros de séquence sont correctement assignés
        // Important pour identifier chaque paquet de manière unique
        assertEquals("Le numéro de séquence doit être correct",
                0, receivedPacket.getSequenceNumber());
        assertEquals("Le numéro de séquence doit être correct",
                1, lostPacket.getSequenceNumber());
    }

    @Test
    public void testRelativeTimeConversion() {
        // Vérifie que la conversion microsecondes → secondes est correcte
        // Les timestamps JSON sont en microsecondes (millionièmes de seconde)
        // On les convertit en secondes pour l'affichage lisible sur l'axe X

        // Créer un paquet avec des timestamps faciles à calculer
        RequestData packet = new RequestData(
                0,
                BASE_TIMESTAMP + 1_000_000,  // +1 seconde (1 million de µs)
                BASE_TIMESTAMP + 2_000_000,  // +2 secondes (2 millions de µs)
                1000.0,
                PacketStatus.RECEIVED
        );
        packet.calculateRelativeTimes(BASE_TIMESTAMP);

        // Vérifications : 1 million de microsecondes = 1 seconde
        assertEquals("1 million de microsecondes = 1 seconde",
                1.0, packet.getTxTimeRelative(), 0.001);
        assertEquals("2 millions de microsecondes = 2 secondes",
                2.0, packet.getRxTimeRelative(), 0.001);
    }
}