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
    }

    stages {
        stage('Nettoyage') {
            steps {
                echo 'Nettoyage de l\'espace de travail'
                sh '''
                    chmod +x gradlew
                    ./gradlew clean
                '''
            }
        }

        stage('Compilation') {
            steps {
                echo 'Compilation du projet Android'
                sh '''
                    chmod +x gradlew
                    ./gradlew assembleDebug
                '''
            }
        }

        stage('Tests Unitaires') {
            steps {
                echo 'Exécution des tests unitaires'
                catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                    sh '''
                        chmod +x gradlew
                        ./gradlew test --stacktrace
                    '''
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
                    echo "PRÉPARATION DEVICE"
                    echo "==================================="

                    # Connexion au téléphone
                    echo "📱 Connexion au téléphone ${DEVICE_UDID}..."
                    adb connect ${DEVICE_UDID}
                    sleep 3

                    # Vérification connexion
                    adb devices -l

                    # Installation de l'APK
                    echo ""
                    echo "📦 Installation de l'APK..."
                    adb -s ${DEVICE_UDID} install -r app/build/outputs/apk/debug/app-debug.apk

                    # Lancement de l'app pour copier les assets
                    echo ""
                    echo "🚀 Lancement de l'app (copie des assets)..."
                    adb -s ${DEVICE_UDID} shell am start -n com.qos.latency.analyzer/.MainActivity

                    # Attente copie assets (10 secondes pour être sûr)
                    echo "⏳ Attente 10 secondes pour copie des assets..."
                    sleep 10

                    # Arrêt de l'app
                    echo "🛑 Arrêt de l'application..."
                    adb -s ${DEVICE_UDID} shell am force-stop com.qos.latency.analyzer
                    sleep 2

                    # Vérification des fichiers copiés
                    echo ""
                    echo "==================================="
                    echo "VÉRIFICATION FICHIERS"
                    echo "==================================="
                    APP_DATA_DIR="/storage/emulated/0/Android/data/com.qos.latency.analyzer/files/QoS_Data"

                    echo "📂 Contenu de ${APP_DATA_DIR} :"
                    adb -s ${DEVICE_UDID} shell ls -lh "${APP_DATA_DIR}" || echo "❌ ERREUR: Dossier introuvable"

                    echo ""
                    echo "📊 Nombre de fichiers JSON :"
                    FILE_COUNT=$(adb -s ${DEVICE_UDID} shell "ls ${APP_DATA_DIR}/*.json 2>/dev/null | wc -l" | tr -d '\r')
                    echo "${FILE_COUNT} fichier(s)"

                    if [ "${FILE_COUNT}" -eq "0" ]; then
                        echo ""
                        echo "❌ ERREUR CRITIQUE: Aucun fichier JSON trouvé !"
                        echo ""
                        echo "📋 LOGS APPLICATION (dernières 50 lignes) :"
                        adb -s ${DEVICE_UDID} logcat -d | grep -i "QoS_MainActivity" | tail -50
                        echo ""
                        echo "📋 LOGS SYSTÈME (dernières 50 lignes) :"
                        adb -s ${DEVICE_UDID} logcat -d | grep -i "latency.analyzer" | tail -50
                        exit 1
                    else
                        echo "✅ ${FILE_COUNT} fichier(s) JSON disponible(s)"
                    fi

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
                        sh '''
                            chmod +x gradlew
                            ./gradlew appiumTest --stacktrace
                        '''
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
                        echo "Modèle     : $(adb shell getprop ro.product.model)"
                        echo "Fabricant  : $(adb shell getprop ro.product.manufacturer)"
                        echo "Android    : $(adb shell getprop ro.build.version.release)"
                        echo "API Level  : $(adb shell getprop ro.build.version.sdk)"

                        echo ""
                        echo "Désinstallation de l'ancienne version..."
                        adb uninstall com.qos.latency.analyzer || true
                    '''

                    echo ''
                    echo 'Lancement des tests instrumentés...'
                    echo 'Les tests peuvent prendre 10-15 minutes (animations réelles)'

                    catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                        sh '''
                            chmod +x gradlew
                            ./gradlew connectedAndroidTest --stacktrace
                        '''
                    }

                    echo ''
                    echo 'Tests instrumentés terminés'
                }
            }
        }

        stage('Analyse Lint') {
            steps {
                echo 'Analyse Lint Android'
                sh '''
                    chmod +x gradlew
                    ./gradlew lint
                '''
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