package com.qos.latency.analyzer.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.qos.latency.analyzer.model.LatencyModel.RequestData;
import com.qos.latency.analyzer.model.LatencyModel.PacketStatus;

/**
 * Tests unitaires pour la classe RequestData
 */
public class RequestDataTest {

    private RequestData receivedPacket;
    private RequestData lostPacket;
    private RequestData duplicatedPacket;
    private RequestData reversedPacket;

    private static final long BASE_TIMESTAMP = 1756192408770903L;
    private static final long TX_TIMESTAMP_1 = 1756192408770903L;
    private static final long RX_TIMESTAMP_1 = 1756192408803179L;
    private static final long TX_TIMESTAMP_2 = 1756192408831058L;
    private static final double RTT_1 = 32.276;

    @Before
    public void setUp() {
        // Paquet reçu normalement
        receivedPacket = new RequestData(
                0,
                TX_TIMESTAMP_1,
                RX_TIMESTAMP_1,
                RTT_1,
                PacketStatus.RECEIVED
        );
        receivedPacket.calculateRelativeTimes(BASE_TIMESTAMP);

        // Paquet perdu
        lostPacket = new RequestData(
                1,
                TX_TIMESTAMP_2,
                0,
                0,
                PacketStatus.LOST
        );
        lostPacket.calculateRelativeTimes(BASE_TIMESTAMP);

        // Paquet dupliqué
        duplicatedPacket = new RequestData(
                2,
                TX_TIMESTAMP_1,
                RX_TIMESTAMP_1,
                RTT_1,
                PacketStatus.DUPLICATED
        );
        duplicatedPacket.calculateRelativeTimes(BASE_TIMESTAMP);

        // Paquet inversé
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
        assertEquals("Le paquet doit avoir le statut RECEIVED",
                PacketStatus.RECEIVED, receivedPacket.getStatus());
    }

    @Test
    public void testReceivedPacket_HasCorrectRtt() {
        assertEquals("Le RTT doit être correct",
                RTT_1, receivedPacket.getRtt(), 0.001);
    }

    @Test
    public void testReceivedPacket_RelativeTimesCalculated() {
        // Le premier paquet doit avoir un temps relatif de 0
        assertEquals("Le temps TX relatif doit être 0",
                0.0, receivedPacket.getTxTimeRelative(), 0.001);

        // Le temps RX doit être la différence en secondes
        double expectedRxTime = (RX_TIMESTAMP_1 - BASE_TIMESTAMP) / 1_000_000.0;
        assertEquals("Le temps RX relatif doit être correct",
                expectedRxTime, receivedPacket.getRxTimeRelative(), 0.001);
    }

    @Test
    public void testLostPacket_HasCorrectStatus() {
        assertEquals("Le paquet perdu doit avoir le statut LOST",
                PacketStatus.LOST, lostPacket.getStatus());
    }

    @Test
    public void testLostPacket_HasZeroRtt() {
        assertEquals("Un paquet perdu doit avoir un RTT de 0",
                0.0, lostPacket.getRtt(), 0.001);
    }

    @Test
    public void testLostPacket_RxTimeEqualseTxTime() {
        // Pour un paquet perdu, rxTimeRelative doit être égal à txTimeRelative
        assertEquals("Pour un paquet perdu, RX time = TX time",
                lostPacket.getTxTimeRelative(),
                lostPacket.getRxTimeRelative(),
                0.001);
    }

    @Test
    public void testDuplicatedPacket_HasCorrectStatus() {
        assertEquals("Le paquet dupliqué doit avoir le statut DUPLICATED",
                PacketStatus.DUPLICATED, duplicatedPacket.getStatus());
    }

    @Test
    public void testDuplicatedPacket_HasValidRtt() {
        assertTrue("Un paquet dupliqué doit avoir un RTT > 0",
                duplicatedPacket.getRtt() > 0);
    }

    @Test
    public void testReversedPacket_HasCorrectStatus() {
        assertEquals("Le paquet inversé doit avoir le statut REVERSED",
                PacketStatus.REVERSED, reversedPacket.getStatus());
    }

    @Test
    public void testSequenceNumber() {
        assertEquals("Le numéro de séquence doit être correct",
                0, receivedPacket.getSequenceNumber());
        assertEquals("Le numéro de séquence doit être correct",
                1, lostPacket.getSequenceNumber());
    }

    @Test
    public void testRelativeTimeConversion() {
        // Vérifier que la conversion microsecondes -> secondes est correcte
        RequestData packet = new RequestData(
                0,
                BASE_TIMESTAMP + 1_000_000, // +1 seconde
                BASE_TIMESTAMP + 2_000_000, // +2 secondes
                1000.0,
                PacketStatus.RECEIVED
        );
        packet.calculateRelativeTimes(BASE_TIMESTAMP);

        assertEquals("1 million de microsecondes = 1 seconde",
                1.0, packet.getTxTimeRelative(), 0.001);
        assertEquals("2 millions de microsecondes = 2 secondes",
                2.0, packet.getRxTimeRelative(), 0.001);
    }
}