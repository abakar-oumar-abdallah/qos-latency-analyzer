package com.qos.latency.analyzer.model;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.content.res.AssetManager;

import com.qos.latency.analyzer.utils.TestDataHelper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Tests unitaires pour la classe LatencyModel
 *
 * Note: Ces tests utilisent Mockito pour mocker le Context Android
 * car LatencyModel.loadData() nécessite un Context pour accéder aux assets
 */
@RunWith(MockitoJUnitRunner.class)
public class LatencyModelTest {

    @Mock
    private Context mockContext;

    @Mock
    private AssetManager mockAssetManager;

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
        // Préparer le mock pour retourner notre JSON de test
        String jsonData = TestDataHelper.getValidJsonData();
        InputStream stream = new ByteArrayInputStream(jsonData.getBytes());

        when(mockContext.getAssets()).thenReturn(mockAssetManager);
        when(mockAssetManager.open("test_data.json")).thenReturn(stream);

        // Charger les données
        model.loadData(mockContext, "test_data.json");

        // Vérifications
        assertTrue("Le modèle doit contenir des données", model.hasData());
        assertEquals("Doit contenir 5 paquets",
                5, model.getRequestArrayData().getRequestDataList().size());
    }

    @Test
    public void testLoadData_AlternativeFormat_Success() throws Exception {
        // Tester le format alternatif avec "dup" et "rev" au lieu de "duplicated" et "reversed"
        String jsonData = TestDataHelper.getAlternativeFormatJson();
        InputStream stream = new ByteArrayInputStream(jsonData.getBytes());

        when(mockContext.getAssets()).thenReturn(mockAssetManager);
        when(mockAssetManager.open("alt_format.json")).thenReturn(stream);

        model.loadData(mockContext, "alt_format.json");

        assertTrue("Le modèle doit accepter le format alternatif", model.hasData());

        // Vérifier que le paquet dupliqué est bien détecté
        LatencyModel.RequestData duplicatedPacket = model.getRequestArrayData()
                .getRequestDataList().get(1);
        assertEquals("Le paquet dupliqué doit être détecté avec 'dup'",
                LatencyModel.PacketStatus.DUPLICATED, duplicatedPacket.getStatus());
    }

    @Test(expected = RuntimeException.class)
    public void testLoadData_InvalidJson_ThrowsException() throws Exception {
        String jsonData = TestDataHelper.getInvalidJsonData();
        InputStream stream = new ByteArrayInputStream(jsonData.getBytes());

        when(mockContext.getAssets()).thenReturn(mockAssetManager);
        when(mockAssetManager.open("invalid.json")).thenReturn(stream);

        // Doit lancer une exception car pas de "request_array"
        model.loadData(mockContext, "invalid.json");
    }

    @Test
    public void testLoadData_LostPacket_DetectedCorrectly() throws Exception {
        String jsonData = TestDataHelper.getLostPacketJson();
        InputStream stream = new ByteArrayInputStream(jsonData.getBytes());

        when(mockContext.getAssets()).thenReturn(mockAssetManager);
        when(mockAssetManager.open("lost_packet.json")).thenReturn(stream);

        model.loadData(mockContext, "lost_packet.json");

        LatencyModel.RequestData packet = model.getRequestArrayData()
                .getRequestDataList().get(0);

        assertEquals("Un paquet avec rx_ts=0 doit être LOST",
                LatencyModel.PacketStatus.LOST, packet.getStatus());
        assertEquals("Un paquet perdu doit avoir RTT=0",
                0.0, packet.getRtt(), 0.001);
    }

    @Test
    public void testLoadData_AddsJsonExtension() throws Exception {
        String jsonData = TestDataHelper.getValidJsonData();
        InputStream stream = new ByteArrayInputStream(jsonData.getBytes());

        when(mockContext.getAssets()).thenReturn(mockAssetManager);
        // Le modèle doit ajouter .json si absent
        when(mockAssetManager.open("test_file.json")).thenReturn(stream);

        model.loadData(mockContext, "test_file"); // Sans .json

        assertTrue("Le modèle doit charger même sans extension .json", model.hasData());
    }

    @Test
    public void testGetDisplayFileName_RemovesExtension() throws Exception {
        String jsonData = TestDataHelper.getValidJsonData();
        InputStream stream = new ByteArrayInputStream(jsonData.getBytes());

        when(mockContext.getAssets()).thenReturn(mockAssetManager);
        when(mockAssetManager.open("my_test_file.json")).thenReturn(stream);

        model.loadData(mockContext, "my_test_file.json");

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