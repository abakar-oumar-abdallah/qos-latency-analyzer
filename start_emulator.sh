#!/bin/bash

###############################################################################
# Script de démarrage émulateur Android pour tests Appium
# Usage: ./start_emulator.sh
###############################################################################

echo "========================================="
echo "🚀 DÉMARRAGE ÉMULATEUR ANDROID"
echo "========================================="

# Configuration
AVD_NAME="TestEmulator"
ANDROID_SDK="$HOME/Android/Sdk"

# Vérifier SDK Android
if [ ! -d "$ANDROID_SDK" ]; then
    echo "❌ Android SDK non trouvé dans $ANDROID_SDK"
    echo "Modifiez la variable ANDROID_SDK dans ce script"
    exit 1
fi

# Ajouter SDK au PATH
export ANDROID_HOME="$ANDROID_SDK"
export PATH="$ANDROID_SDK/emulator:$ANDROID_SDK/platform-tools:$ANDROID_SDK/cmdline-tools/latest/bin:$PATH"

echo "✅ Android SDK: $ANDROID_SDK"

# Vérifier si émulateur existe
echo ""
echo "📋 Vérification émulateurs disponibles..."
avdmanager list avd

if ! avdmanager list avd | grep -q "$AVD_NAME"; then
    echo ""
    echo "❌ Émulateur '$AVD_NAME' non trouvé"
    echo "Création de l'émulateur..."

    # Installer image système si nécessaire
    echo "📦 Installation image système Android 34..."
    sdkmanager "system-images;android-34;google_apis;x86_64"

    # Créer émulateur
    avdmanager create avd \
        -n "$AVD_NAME" \
        -k "system-images;android-34;google_apis;x86_64" \
        -d "pixel_6" \
        --force

    echo "✅ Émulateur créé"
fi

# Arrêter tout émulateur existant
echo ""
echo "🛑 Arrêt émulateurs existants..."
adb devices | grep emulator | cut -f1 | xargs -I {} adb -s {} emu kill 2>/dev/null || true
pkill -9 qemu-system-x86_64 2>/dev/null || true
sleep 3

# Démarrer émulateur
echo ""
echo "🚀 Démarrage de l'émulateur..."
echo "   Nom: $AVD_NAME"
echo "   Mode: Avec interface graphique"
echo ""

emulator -avd "$AVD_NAME" \
    -gpu host \
    -memory 2048 \
    -cores 2 \
    > /tmp/emulator.log 2>&1 &

EMULATOR_PID=$!
echo "   PID: $EMULATOR_PID"

# Attendre démarrage
echo ""
echo "⏳ Attente détection émulateur (max 2 minutes)..."
timeout 120 adb wait-for-device || {
    echo "❌ ERREUR: Émulateur non détecté"
    echo "Logs:"
    cat /tmp/emulator.log
    exit 1
}

echo "✅ Émulateur détecté"

# Attendre boot complet
echo "⏳ Attente boot complet (max 3 minutes)..."
timeout 180 sh -c 'while [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d \r)" != "1" ]; do sleep 2; done' || {
    echo "❌ ERREUR: Boot non terminé"
    exit 1
}

# Attendre stabilisation
echo "⏳ Stabilisation..."
sleep 10

# Afficher infos
echo ""
echo "========================================="
echo "✅ ÉMULATEUR PRÊT"
echo "========================================="
adb devices -l
echo ""
echo "Modèle  : $(adb shell getprop ro.product.model)"
echo "Android : $(adb shell getprop ro.build.version.release)"
echo "API     : $(adb shell getprop ro.build.version.sdk)"
echo "UDID    : emulator-5554"
echo ""
echo "========================================="
echo "Vous pouvez maintenant lancer les tests"
echo "========================================="
echo ""
echo "Pour arrêter l'émulateur:"
echo "  adb emu kill"
echo ""