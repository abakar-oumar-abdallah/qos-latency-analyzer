package com.qos.latency.analyzer.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.qos.latency.analyzer.model.LatencyModel.RequestArrayData;
import com.qos.latency.analyzer.model.LatencyModel.RequestData;
import com.qos.latency.analyzer.model.LatencyModel.PacketStatus;

/**
 * Tests unitaires pour la classe RequestArrayData
 *
 * Cette classe gère la liste complète de tous les paquets réseau.
 * Ces tests vérifient le bon fonctionnement de l'ajout de paquets
 * et le calcul correct de leurs temps relatifs par rapport au premier paquet (t0).
 *
 * @author ABAKAR Oumar
 */
public class RequestArrayDataTest {

    private RequestArrayData data;

    @Before
    public void setUp() {
        // Créer une nouvelle instance vide avant chaque test
        // Garantit l'indépendance des tests entre eux
        data = new RequestArrayData();
    }

    @Test
    public void testInitialState_IsEmpty() {
        // Vérifie l'état initial d'un RequestArrayData vide
        // C'est le point de départ : aucune donnée, liste vide
        assertFalse("Les données initiales doivent être vides", data.hasData());
        assertEquals("La liste doit être vide", 0, data.getRequestDataList().size());
    }

    @Test
    public void testAddRequestData_ReceivedPacket() {
        // Test basique : ajout d'un paquet reçu normalement
        // Vérifie que le statut RECEIVED est bien attribué
        // et que toutes les propriétés sont correctement stockées

        // Paramètres : seq, tx_ts, rx_ts, rtt, duplicated, reversed
        data.addRequestData(0, 1756192408770903L, 1756192408803179L, 32.276, false, false);

        assertTrue("Les données ne doivent plus être vides", data.hasData());
        assertEquals("La liste doit contenir 1 élément", 1, data.getRequestDataList().size());

        RequestData packet = data.getRequestDataList().get(0);
        assertEquals("Le statut doit être RECEIVED", PacketStatus.RECEIVED, packet.getStatus());
        assertEquals("Le RTT doit être correct", 32.276, packet.getRtt(), 0.001);
    }

    @Test
    public void testAddRequestData_LostPacket() {
        // Teste la détection d'un paquet perdu
        // Caractéristique principale : rx_ts = 0 (pas de temps de réception)
        // Dans ce cas, le RTT est automatiquement mis à 0

        // rx_ts = 0 indique un paquet perdu
        data.addRequestData(0, 1756192408770903L, 0, 50.0, false, false);

        RequestData packet = data.getRequestDataList().get(0);
        assertEquals("Le statut doit être LOST", PacketStatus.LOST, packet.getStatus());
        assertEquals("Le RTT d'un paquet perdu doit être 0", 0.0, packet.getRtt(), 0.001);
    }

    @Test
    public void testAddRequestData_DuplicatedPacket() {
        // Teste la détection d'un paquet dupliqué
        // Un paquet dupliqué a été reçu plusieurs fois (duplicated = true)
        // Cela peut indiquer des problèmes de réseau ou de retransmission

        data.addRequestData(0, 1756192408770903L, 1756192408803179L, 32.276, true, false);

        RequestData packet = data.getRequestDataList().get(0);
        assertEquals("Le statut doit être DUPLICATED", PacketStatus.DUPLICATED, packet.getStatus());
    }

    @Test
    public void testAddRequestData_ReversedPacket() {
        // Teste la détection d'un paquet inversé (out-of-order)
        // Un paquet inversé est arrivé après des paquets envoyés plus tard
        // Indique un problème de routage réseau

        data.addRequestData(0, 1756192408770903L, 1756192408803179L, 32.276, false, true);

        RequestData packet = data.getRequestDataList().get(0);
        assertEquals("Le statut doit être REVERSED", PacketStatus.REVERSED, packet.getStatus());
    }

    @Test
    public void testAddRequestData_PriorityOfStatuses() {
        // Teste la priorité des statuts quand plusieurs flags sont activés
        // Si rx_ts = 0 (perdu), ça prime sur duplicated/reversed
        // C'est logique : un paquet perdu ne peut pas être dupliqué ou inversé
        // car il n'est jamais arrivé !

        data.addRequestData(0, 1756192408770903L, 0, 0, true, true);

        RequestData packet = data.getRequestDataList().get(0);
        assertEquals("LOST a la priorité absolue sur les autres statuts",
                PacketStatus.LOST, packet.getStatus());
    }

    @Test
    public void testAddRequestData_DuplicatedOverReversed() {
        // Si un paquet est à la fois dupliqué ET inversé,
        // on considère qu'il est dupliqué (priorité plus haute)
        // Ce choix a été fait car un dupliqué est généralement
        // plus critique qu'un simple désordre dans les arrivées

        data.addRequestData(0, 1756192408770903L, 1756192408803179L, 32.276, true, true);

        RequestData packet = data.getRequestDataList().get(0);
        assertEquals("DUPLICATED a la priorité sur REVERSED",
                PacketStatus.DUPLICATED, packet.getStatus());
    }

    @Test
    public void testMultiplePackets_RelativeTimesAreCorrect() {
        // Vérifie que les temps relatifs sont bien calculés pour plusieurs paquets
        // Le premier paquet définit t0 (temps de base)
        // Tous les autres paquets ont leur temps calculé relativement à t0
        // C'est essentiel pour l'affichage chronologique correct sur le graphique

        long baseTime = 1756192408770903L;

        // Ajouter 3 paquets avec 1 seconde d'écart (1_000_000 microsecondes)
        data.addRequestData(0, baseTime, baseTime + 500_000, 0.5, false, false);
        data.addRequestData(1, baseTime + 1_000_000, baseTime + 1_500_000, 0.5, false, false);
        data.addRequestData(2, baseTime + 2_000_000, baseTime + 2_500_000, 0.5, false, false);

        assertEquals("Doit contenir 3 paquets", 3, data.getRequestDataList().size());

        RequestData packet0 = data.getRequestDataList().get(0);
        RequestData packet1 = data.getRequestDataList().get(1);
        RequestData packet2 = data.getRequestDataList().get(2);

        // Le premier paquet est toujours à temps 0 (référence)
        assertEquals("Le premier paquet doit avoir un temps TX de 0",
                0.0, packet0.getTxTimeRelative(), 0.001);
        assertEquals("Le deuxième paquet doit avoir un temps TX de 1s",
                1.0, packet1.getTxTimeRelative(), 0.001);
        assertEquals("Le troisième paquet doit avoir un temps TX de 2s",
                2.0, packet2.getTxTimeRelative(), 0.001);
    }

    @Test
    public void testClear_RemovesAllData() {
        // Teste la fonction de nettoyage des données
        // Important quand l'utilisateur charge un nouveau fichier :
        // il faut s'assurer que les anciennes données sont bien effacées
        // pour éviter des mélanges de données de différents fichiers

        // Ajouter quelques paquets
        data.addRequestData(0, 1756192408770903L, 1756192408803179L, 32.276, false, false);
        data.addRequestData(1, 1756192408831058L, 1756192408860613L, 29.555, false, false);

        assertTrue("Les données doivent exister avant clear", data.hasData());

        // Nettoyer
        data.clear();

        // Vérifier que tout est bien vide
        assertFalse("Les données doivent être vides après clear", data.hasData());
        assertEquals("La liste doit être vide", 0, data.getRequestDataList().size());
    }
}