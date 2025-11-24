pipeline {
    agent any

    options {
        timeout(time: 2, unit: 'HOURS')
        timestamps()
    }

    environment {
        ANDROID_HOME = '/opt/android-sdk'
        JAVA_HOME = '/opt/java/openjdk'
        PATH = "${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/cmdline-tools/latest/bin:${JAVA_HOME}/bin:${PATH}"

        PHONE_IP = '192.168.1.109'
        PHONE_PORT = '5555'
        DEVICE_UDID = "${PHONE_IP}:${PHONE_PORT}"
        APPIUM_PORT = '4723'

        WORKSPACE_DIR = "${WORKSPACE}"
        APK_PATH = "${WORKSPACE}/app/build/outputs/apk/debug/app-debug.apk"
    }

    stages {
        stage('Fix Permissions') {
            steps {
                echo 'Correction des permissions gradlew'
                sh '''
                    echo "📝 Permissions avant:"
                    ls -la gradlew || echo "gradlew introuvable"

                    echo ""
                    echo "🔧 Application chmod +x..."
                    chmod +x gradlew

                    echo ""
                    echo "✅ Permissions après:"
                    ls -la gradlew
                '''
            }
        }

        stage('Nettoyage') {
            steps {
                echo 'Nettoyage de l\'espace de travail'
                sh './gradlew clean'
            }
        }

        stage('Compilation') {
            steps {
                echo 'Compilation du projet Android'
                sh './gradlew assembleDebug'

                sh '''
                    if [ ! -f "${APK_PATH}" ]; then
                        echo "❌ ERREUR: APK introuvable à ${APK_PATH}"
                        exit 1
                    fi
                    echo "✅ APK trouvé: ${APK_PATH}"
                    ls -lh "${APK_PATH}"
                '''
            }
        }

        stage('Tests Unitaires') {
            steps {
                echo 'Exécution des tests unitaires'
                catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                    sh './gradlew test --stacktrace'
                }
            }
        }

        stage('Démarrage Appium Server') {
            steps {
                echo 'Démarrage du serveur Appium'
                sh '''
                    echo "Arrêt des instances Appium existantes..."
                    pkill -f appium || true
                    sleep 2

                    echo "Démarrage d'Appium sur le port ${APPIUM_PORT}..."
                    nohup appium --port ${APPIUM_PORT} > appium.log 2>&1 &
                    sleep 5

                    echo "Vérification du serveur Appium..."
                    curl -s http://127.0.0.1:${APPIUM_PORT}/status || echo "Appium non prêt"
                '''
            }
        }

        stage('Préparation Device pour Appium') {
            steps {
                echo 'Préparation du téléphone pour les tests'
                sh '''
                    echo "==================================="
                    echo "PRÉPARATION DEVICE POUR APPIUM"
                    echo "==================================="
                    echo "Workspace: ${WORKSPACE_DIR}"
                    echo "APK Path: ${APK_PATH}"

                    echo ""
                    echo "📱 Connexion au téléphone ${DEVICE_UDID}..."
                    adb connect ${DEVICE_UDID}
                    sleep 3

                    echo ""
                    echo "📋 Appareils connectés:"
                    adb devices -l

                    echo ""
                    echo "🗑️ Désinstallation complète..."
                    adb -s ${DEVICE_UDID} uninstall com.qos.latency.analyzer || echo "App non installée"
                    sleep 2

                    echo ""
                    echo "📦 Installation de l'APK..."
                    echo "Chemin: ${APK_PATH}"
                    adb -s ${DEVICE_UDID} install "${APK_PATH}"

                    if [ $? -ne 0 ]; then
                        echo "❌ ERREUR: Échec de l'installation"
                        exit 1
                    fi
                    echo "✅ APK installé avec succès"

                    echo ""
                    echo "🔍 Vérification installation..."
                    adb -s ${DEVICE_UDID} shell pm list packages | grep latency

                    echo ""
                    echo "🧹 Effacer les logs..."
                    adb -s ${DEVICE_UDID} logcat -c

                    echo ""
                    echo "🚀 Lancement de l'app (copie des assets)..."
                    adb -s ${DEVICE_UDID} shell am start -n com.qos.latency.analyzer/.MainActivity

                    echo "⏳ Attente 10 secondes pour copie des assets..."
                    sleep 10

                    echo ""
                    echo "🛑 Arrêt de l'application..."
                    adb -s ${DEVICE_UDID} shell am force-stop com.qos.latency.analyzer
                    sleep 2

                    echo ""
                    echo "==================================="
                    echo "VÉRIFICATION FICHIERS"
                    echo "==================================="
                    APP_DATA_DIR="/storage/emulated/0/Android/data/com.qos.latency.analyzer/files/QoS_Data"

                    echo "📂 Contenu de ${APP_DATA_DIR} :"
                    adb -s ${DEVICE_UDID} shell ls -lh "${APP_DATA_DIR}"

                    if [ $? -ne 0 ]; then
                        echo ""
                        echo "❌ ERREUR: Dossier QoS_Data introuvable !"
                        echo ""
                        echo "📋 LOGS APPLICATION (copie assets) :"
                        adb -s ${DEVICE_UDID} logcat -d | grep -i "QoS_MainActivity"
                        exit 1
                    fi

                    echo ""
                    echo "📊 Nombre de fichiers JSON :"
                    FILE_COUNT=$(adb -s ${DEVICE_UDID} shell "ls ${APP_DATA_DIR}/*.json 2>/dev/null | wc -l" | tr -d '\r')
                    echo "${FILE_COUNT} fichier(s)"

                    if [ "${FILE_COUNT}" -eq "0" ]; then
                        echo ""
                        echo "❌ ERREUR CRITIQUE: Aucun fichier JSON trouvé !"
                        echo ""
                        echo "📋 LOGS APPLICATION :"
                        adb -s ${DEVICE_UDID} logcat -d | grep -i "QoS_MainActivity"
                        exit 1
                    else
                        echo ""
                        echo "✅ ${FILE_COUNT} fichier(s) JSON disponible(s)"
                        echo ""
                        echo "📋 LOGS COPIE (confirmation) :"
                        adb -s ${DEVICE_UDID} logcat -d | grep "QoS_MainActivity" | grep "COPIE TERMINÉE"
                    fi

                    echo "==================================="
                    echo "✅ PRÉPARATION TERMINÉE"
                    echo "==================================="
                '''
            }
        }

        stage('Tests Appium') {
            options {
                timeout(time: 20, unit: 'MINUTES')
            }
            steps {
                echo 'Tests Appium sur Téléphone'
                script {
                    catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                        sh './gradlew appiumTest --stacktrace'
                    }
                    echo 'Tests Appium terminés'
                }
            }
        }

        stage('Arrêt Appium Server') {
            steps {
                echo 'Arrêt du serveur Appium'
                sh '''
                    echo "Arrêt d'Appium..."
                    pkill -f appium || true
                    sleep 2
                    echo "Appium arrêté"
                '''
            }
        }

        stage('Tests Instrumentés') {
            options {
                timeout(time: 20, unit: 'MINUTES')
            }
            steps {
                echo 'Tests Instrumentés sur Téléphone'
                script {
                    sh '''
                        echo "Connexion au téléphone ${DEVICE_UDID}..."
                        adb connect ${DEVICE_UDID}
                        sleep 3

                        echo ""
                        echo "=== APPAREILS CONNECTÉS ==="
                        adb devices -l

                        DEVICE_COUNT=$(adb devices | grep -w device | wc -l)
                        if [ $DEVICE_COUNT -eq 0 ]; then
                            echo "❌ Aucun appareil connecté"
                            exit 1
                        fi
                        echo "${DEVICE_COUNT} appareil(s) connecté(s)"

                        echo ""
                        echo "=== INFORMATIONS APPAREIL ==="
                        echo "Modèle     : $(adb -s ${DEVICE_UDID} shell getprop ro.product.model)"
                        echo "Fabricant  : $(adb -s ${DEVICE_UDID} shell getprop ro.product.manufacturer)"
                        echo "Android    : $(adb -s ${DEVICE_UDID} shell getprop ro.build.version.release)"
                        echo "API Level  : $(adb -s ${DEVICE_UDID} shell getprop ro.build.version.sdk)"

                        echo ""
                        echo "⚠️ L'app est déjà installée avec les fichiers copiés"
                        echo "Pas besoin de désinstaller pour les tests instrumentés"
                    '''

                    echo ''
                    echo 'Lancement des tests instrumentés...'
                    echo 'Les tests peuvent prendre 10-15 minutes (animations réelles)'

                    catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                        sh './gradlew connectedAndroidTest --stacktrace'
                    }

                    echo ''
                    echo 'Tests instrumentés terminés'
                }
            }
        }

        stage('Analyse Lint') {
            steps {
                echo 'Analyse Lint Android'
                sh './gradlew lint'
            }
        }

        stage('Archive APK') {
            steps {
                echo 'Archivage de l\'APK'
                archiveArtifacts artifacts: 'app/build/outputs/apk/debug/*.apk', fingerprint: true
            }
        }
    }

    post {
        always {
            echo 'Nettoyage final'

            junit allowEmptyResults: true, testResults: '**/build/test-results/**/*.xml, **/build/outputs/androidTest-results/**/*.xml'

            publishHTML([
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'app/build/reports',
                reportFiles: 'lint-results-debug.html',
                reportName: 'Lint Report'
            ])
        }
    }
}