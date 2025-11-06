package com.qos.latency.analyzer.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.qos.latency.analyzer.model.LatencyModel.RequestArrayData;
import com.qos.latency.analyzer.model.LatencyModel.RequestData;
import com.qos.latency.analyzer.model.LatencyModel.PacketStatus;

/**
 * Tests unitaires pour la classe RequestArrayData
 */
public class RequestArrayDataTest {

    private RequestArrayData data;

    @Before
    public void setUp() {
        data = new RequestArrayData();
    }

    @Test
    public void testInitialState_IsEmpty() {
        assertFalse("Les données initiales doivent être vides", data.hasData());
        assertEquals("La liste doit être vide", 0, data.getRequestDataList().size());
    }

    @Test
    public void testAddRequestData_ReceivedPacket() {
        data.addRequestData(0, 1756192408770903L, 1756192408803179L, 32.276, false, false);

        assertTrue("Les données ne doivent plus être vides", data.hasData());
        assertEquals("La liste doit contenir 1 élément", 1, data.getRequestDataList().size());

        RequestData packet = data.getRequestDataList().get(0);
        assertEquals("Le statut doit être RECEIVED", PacketStatus.RECEIVED, packet.getStatus());
        assertEquals("Le RTT doit être correct", 32.276, packet.getRtt(), 0.001);
    }

    @Test
    public void testAddRequestData_LostPacket() {
        // rx_ts = 0 indique un paquet perdu
        data.addRequestData(0, 1756192408770903L, 0, 50.0, false, false);

        RequestData packet = data.getRequestDataList().get(0);
        assertEquals("Le statut doit être LOST", PacketStatus.LOST, packet.getStatus());
        assertEquals("Le RTT d'un paquet perdu doit être 0", 0.0, packet.getRtt(), 0.001);
    }

    @Test
    public void testAddRequestData_DuplicatedPacket() {
        data.addRequestData(0, 1756192408770903L, 1756192408803179L, 32.276, true, false);

        RequestData packet = data.getRequestDataList().get(0);
        assertEquals("Le statut doit être DUPLICATED", PacketStatus.DUPLICATED, packet.getStatus());
    }

    @Test
    public void testAddRequestData_ReversedPacket() {
        data.addRequestData(0, 1756192408770903L, 1756192408803179L, 32.276, false, true);

        RequestData packet = data.getRequestDataList().get(0);
        assertEquals("Le statut doit être REVERSED", PacketStatus.REVERSED, packet.getStatus());
    }

    @Test
    public void testAddRequestData_PriorityOfStatuses() {
        // Si rx_ts = 0, c'est LOST même si duplicated/reversed sont à true
        data.addRequestData(0, 1756192408770903L, 0, 0, true, true);

        RequestData packet = data.getRequestDataList().get(0);
        assertEquals("LOST a la priorité sur les autres statuts",
                PacketStatus.LOST, packet.getStatus());
    }

    @Test
    public void testAddRequestData_DuplicatedOverReversed() {
        // Si duplicated ET reversed = true, duplicated a la priorité
        data.addRequestData(0, 1756192408770903L, 1756192408803179L, 32.276, true, true);

        RequestData packet = data.getRequestDataList().get(0);
        assertEquals("DUPLICATED a la priorité sur REVERSED",
                PacketStatus.DUPLICATED, packet.getStatus());
    }

    @Test
    public void testMultiplePackets_RelativeTimesAreCorrect() {
        long baseTime = 1756192408770903L;

        // Ajouter 3 paquets avec 1 seconde d'écart
        data.addRequestData(0, baseTime, baseTime + 500_000, 0.5, false, false);
        data.addRequestData(1, baseTime + 1_000_000, baseTime + 1_500_000, 0.5, false, false);
        data.addRequestData(2, baseTime + 2_000_000, baseTime + 2_500_000, 0.5, false, false);

        assertEquals("Doit contenir 3 paquets", 3, data.getRequestDataList().size());

        RequestData packet0 = data.getRequestDataList().get(0);
        RequestData packet1 = data.getRequestDataList().get(1);
        RequestData packet2 = data.getRequestDataList().get(2);

        assertEquals("Le premier paquet doit avoir un temps TX de 0",
                0.0, packet0.getTxTimeRelative(), 0.001);
        assertEquals("Le deuxième paquet doit avoir un temps TX de 1s",
                1.0, packet1.getTxTimeRelative(), 0.001);
        assertEquals("Le troisième paquet doit avoir un temps TX de 2s",
                2.0, packet2.getTxTimeRelative(), 0.001);
    }

    @Test
    public void testClear_RemovesAllData() {
        data.addRequestData(0, 1756192408770903L, 1756192408803179L, 32.276, false, false);
        data.addRequestData(1, 1756192408831058L, 1756192408860613L, 29.555, false, false);

        assertTrue("Les données doivent exister avant clear", data.hasData());

        data.clear();

        assertFalse("Les données doivent être vides après clear", data.hasData());
        assertEquals("La liste doit être vide", 0, data.getRequestDataList().size());
    }
}