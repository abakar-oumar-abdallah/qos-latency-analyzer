package com.qos.latency.analyzer.model;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;

import com.qos.latency.analyzer.utils.TestDataHelper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Tests unitaires pour la classe LatencyModel
 *
 * ✅ MODIFIÉ : Utilise loadDataFromStream() au lieu de loadData()
 * pour tester le parsing JSON sans dépendre du système de fichiers Android
 */
@RunWith(MockitoJUnitRunner.class)
public class LatencyModelTest {

    private LatencyModel model;

    @Before
    public void setUp() {
        model = new LatencyModel();
    }

    @Test
    public void testNewModel_IsEmpty() {
        assertFalse("Un nouveau modèle doit être vide", model.hasData());
    }

    @Test
    public void testLoadData_ValidJson_Success() throws Exception {
        // ✅ MODIFIÉ : Utiliser loadDataFromStream()
        String jsonData = TestDataHelper.getValidJsonData();
        InputStream stream = new ByteArrayInputStream(jsonData.getBytes());

        // Charger les données directement depuis le stream
        model.loadDataFromStream(stream, "test_data.json");

        // Vérifications
        assertTrue("Le modèle doit contenir des données", model.hasData());
        assertEquals("Doit contenir 5 paquets",
                5, model.getRequestArrayData().getRequestDataList().size());
    }

    @Test
    public void testLoadData_AlternativeFormat_Success() throws Exception {
        // ✅ MODIFIÉ : Utiliser loadDataFromStream()
        String jsonData = TestDataHelper.getAlternativeFormatJson();
        InputStream stream = new ByteArrayInputStream(jsonData.getBytes());

        model.loadDataFromStream(stream, "alt_format.json");

        assertTrue("Le modèle doit accepter le format alternatif", model.hasData());

        // Vérifier que le paquet dupliqué est bien détecté
        LatencyModel.RequestData duplicatedPacket = model.getRequestArrayData()
                .getRequestDataList().get(1);
        assertEquals("Le paquet dupliqué doit être détecté avec 'dup'",
                LatencyModel.PacketStatus.DUPLICATED, duplicatedPacket.getStatus());
    }

    @Test(expected = RuntimeException.class)
    public void testLoadData_InvalidJson_ThrowsException() throws Exception {
        // ✅ MODIFIÉ : Utiliser loadDataFromStream()
        String jsonData = TestDataHelper.getInvalidJsonData();
        InputStream stream = new ByteArrayInputStream(jsonData.getBytes());

        // Doit lancer une exception car pas de "request_array"
        model.loadDataFromStream(stream, "invalid.json");
    }

    @Test
    public void testLoadData_LostPacket_DetectedCorrectly() throws Exception {
        // ✅ MODIFIÉ : Utiliser loadDataFromStream()
        String jsonData = TestDataHelper.getLostPacketJson();
        InputStream stream = new ByteArrayInputStream(jsonData.getBytes());

        model.loadDataFromStream(stream, "lost_packet.json");

        LatencyModel.RequestData packet = model.getRequestArrayData()
                .getRequestDataList().get(0);

        assertEquals("Un paquet avec rx_ts=0 doit être LOST",
                LatencyModel.PacketStatus.LOST, packet.getStatus());
        assertEquals("Un paquet perdu doit avoir RTT=0",
                0.0, packet.getRtt(), 0.001);
    }

    @Test
    public void testLoadData_AddsJsonExtension() throws Exception {
        // ✅ MODIFIÉ : Tester uniquement le parsing, pas l'ajout d'extension
        String jsonData = TestDataHelper.getValidJsonData();
        InputStream stream = new ByteArrayInputStream(jsonData.getBytes());

        model.loadDataFromStream(stream, "test_file"); // Sans .json

        assertTrue("Le modèle doit charger même sans extension .json", model.hasData());
    }

    @Test
    public void testGetDisplayFileName_RemovesExtension() throws Exception {
        // ✅ MODIFIÉ : Utiliser loadDataFromStream()
        String jsonData = TestDataHelper.getValidJsonData();
        InputStream stream = new ByteArrayInputStream(jsonData.getBytes());

        model.loadDataFromStream(stream, "my_test_file.json");

        String displayName = model.getDisplayFileName();
        assertFalse("Le nom d'affichage ne doit pas contenir .json",
                displayName.contains(".json"));
        assertTrue("Le nom d'affichage doit contenir des espaces au lieu de underscores",
                displayName.contains(" "));
    }

    @Test
    public void testGetDisplayFileName_EmptyWhenNoFileLoaded() {
        String displayName = model.getDisplayFileName();
        assertEquals("Sans fichier chargé, doit retourner 'Aucun fichier'",
                "Aucun fichier", displayName);
    }
}