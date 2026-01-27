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
 * Ces tests vérifient le bon fonctionnement du chargement des données JSON
 * et la gestion des différents formats de fichiers.
 *
 * J'utilise Mockito pour simuler le Context Android car on ne peut pas
 * accéder aux vrais assets pendant les tests unitaires (pas d'émulateur).
 *
 * @author ABAKAR Oumar
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
        // Initialisation avant chaque test
        // On part toujours d'un modèle vide pour éviter les interférences
        model = new LatencyModel();
    }

    @Test
    public void testNewModel_IsEmpty() {
        // Vérifie qu'un modèle fraîchement créé ne contient aucune donnée
        // C'est important pour s'assurer qu'on part d'un état propre
        // sans résidus d'anciennes données
        assertFalse("Un nouveau modèle doit être vide", model.hasData());
    }

    @Test
    public void testLoadData_ValidJson_Success() throws Exception {
        // Test du cas nominal : chargement d'un fichier JSON valide
        // Ce test simule le chargement depuis les assets Android
        // et vérifie que toutes les données sont correctement parsées

        // Préparer le mock pour retourner notre JSON de test
        String jsonData = TestDataHelper.getValidJsonData();
        InputStream stream = new ByteArrayInputStream(jsonData.getBytes());

        // Configuration du mock : quand on demande les assets,
        // on retourne notre AssetManager mocké
        when(mockContext.getAssets()).thenReturn(mockAssetManager);
        when(mockAssetManager.open("test_data.json")).thenReturn(stream);

        // Charger les données depuis le fichier mocké
        model.loadData(mockContext, "test_data.json");

        // Vérifications : le modèle doit maintenant contenir des données
        assertTrue("Le modèle doit contenir des données", model.hasData());

        // Notre JSON de test contient exactement 5 paquets (0 à 4)
        // On vérifie qu'ils ont tous été chargés
        assertEquals("Doit contenir 5 paquets",
                5, model.getRequestArrayData().getRequestDataList().size());
    }

    @Test
    public void testLoadData_AlternativeFormat_Success() throws Exception {
        // Teste la compatibilité avec le format alternatif du JSON
        // Certains fichiers utilisent "dup" et "rev" au lieu de
        // "duplicated" et "reversed" (versions abrégées)
        // L'application doit gérer les deux formats pour être robuste

        String jsonData = TestDataHelper.getAlternativeFormatJson();
        InputStream stream = new ByteArrayInputStream(jsonData.getBytes());

        when(mockContext.getAssets()).thenReturn(mockAssetManager);
        when(mockAssetManager.open("alt_format.json")).thenReturn(stream);

        model.loadData(mockContext, "alt_format.json");

        assertTrue("Le modèle doit accepter le format alternatif", model.hasData());

        // Vérifier que le paquet dupliqué est bien détecté
        // même avec la notation courte "dup" au lieu de "duplicated"
        LatencyModel.RequestData duplicatedPacket = model.getRequestArrayData()
                .getRequestDataList().get(1);
        assertEquals("Le paquet dupliqué doit être détecté avec 'dup'",
                LatencyModel.PacketStatus.DUPLICATED, duplicatedPacket.getStatus());
    }

    @Test(expected = RuntimeException.class)
    public void testLoadData_InvalidJson_ThrowsException() throws Exception {
        // Teste le comportement en cas de JSON invalide ou mal formaté
        // L'application doit lever une exception claire plutôt que crasher
        // silencieusement. C'est important pour donner un message d'erreur
        // utile à l'utilisateur (ex: "Fichier JSON corrompu")

        String jsonData = TestDataHelper.getInvalidJsonData();
        InputStream stream = new ByteArrayInputStream(jsonData.getBytes());

        when(mockContext.getAssets()).thenReturn(mockAssetManager);
        when(mockAssetManager.open("invalid.json")).thenReturn(stream);

        // Doit lancer une RuntimeException car pas de "request_array"
        model.loadData(mockContext, "invalid.json");
    }

    @Test
    public void testLoadData_LostPacket_DetectedCorrectly() throws Exception {
        // Vérifie la détection correcte des paquets perdus
        // Un paquet perdu a toujours rx_ts = 0 (pas de temps de réception)
        // et RTT = 0 (temps de réponse nul car pas de réponse)
        // C'est crucial pour l'affichage correct des points rouges sur le graphique

        String jsonData = TestDataHelper.getLostPacketJson();
        InputStream stream = new ByteArrayInputStream(jsonData.getBytes());

        when(mockContext.getAssets()).thenReturn(mockAssetManager);
        when(mockAssetManager.open("lost_packet.json")).thenReturn(stream);

        model.loadData(mockContext, "lost_packet.json");

        LatencyModel.RequestData packet = model.getRequestArrayData()
                .getRequestDataList().get(0);

        // Double vérification : statut ET valeurs numériques
        assertEquals("Un paquet avec rx_ts=0 doit être LOST",
                LatencyModel.PacketStatus.LOST, packet.getStatus());
        assertEquals("Un paquet perdu doit avoir RTT=0",
                0.0, packet.getRtt(), 0.001);
    }

    @Test
    public void testLoadData_AddsJsonExtension() throws Exception {
        // Teste que l'application ajoute automatiquement .json si oublié
        // Améliore l'expérience utilisateur en étant tolérant avec le nom du fichier
        // L'utilisateur peut taper "test_file" au lieu de "test_file.json"

        String jsonData = TestDataHelper.getValidJsonData();
        InputStream stream = new ByteArrayInputStream(jsonData.getBytes());

        when(mockContext.getAssets()).thenReturn(mockAssetManager);
        // Le modèle doit ajouter .json automatiquement
        when(mockAssetManager.open("test_file.json")).thenReturn(stream);

        model.loadData(mockContext, "test_file"); // Sans .json

        assertTrue("Le modèle doit charger même sans extension .json", model.hasData());
    }

    @Test
    public void testGetDisplayFileName_RemovesExtension() throws Exception {
        // Vérifie que le nom affiché est user-friendly
        // On enlève .json et on remplace les underscores par des espaces
        // Exemple: "my_test_file.json" devient "my test file"
        // C'est plus agréable à lire dans l'interface

        String jsonData = TestDataHelper.getValidJsonData();
        InputStream stream = new ByteArrayInputStream(jsonData.getBytes());

        when(mockContext.getAssets()).thenReturn(mockAssetManager);
        when(mockAssetManager.open("my_test_file.json")).thenReturn(stream);

        model.loadData(mockContext, "my_test_file.json");

        String displayName = model.getDisplayFileName();

        // Vérifications multiples pour un affichage propre
        assertFalse("Le nom d'affichage ne doit pas contenir .json",
                displayName.contains(".json"));
        assertTrue("Le nom d'affichage doit contenir des espaces au lieu de underscores",
                displayName.contains(" "));
    }

    @Test
    public void testGetDisplayFileName_EmptyWhenNoFileLoaded() {
        // Teste le comportement quand aucun fichier n'a été chargé
        // Important pour éviter des NullPointerException dans l'interface
        // On affiche "Aucun fichier" plutôt qu'une chaîne vide ou null

        String displayName = model.getDisplayFileName();
        assertEquals("Sans fichier chargé, doit retourner 'Aucun fichier'",
                "Aucun fichier", displayName);
    }
}