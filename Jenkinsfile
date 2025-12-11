pipeline {
    agent any

    triggers {
        pollSCM('H */4 * * 1-5')
    }

    environment {
        ANDROID_HOME = '/opt/android-sdk'
        PATH = "${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${env.PATH}"
        PHONE_IP = "192.168.1.109"
        APPIUM_PORT = "4723"
        PHONE_PORT = ""  // Sera détecté automatiquement
    }

    stages {

        stage('Informations Environement') {
            steps {
                echo 'Vérification de l\'environnement'
                sh '''
                    echo "Java version"
                    java -version
                    echo ""
                    echo "Android SDK Location"
                    echo ${ANDROID_HOME}
                    echo ""
                    echo "Gradle wrapper"
                    ls -la gradlew
                '''
            }
        }

        stage('Clean') {
            steps {
                echo 'Nettoyage du projet'
                sh 'chmod +x gradlew'
                sh './gradlew clean'
            }
        }

        stage('Build application android') {
            steps {
                echo 'Compilation de l\'APK Debug'
                sh './gradlew assembleDebug'
            }
        }

        stage('Tests Unitaires') {
            steps {
                echo 'Exécution des tests unitaires'
                sh './gradlew test --stacktrace'
            }
        }

        stage('Préparation Device pour Tests') {
            steps {
                echo '📱 Connexion et préparation du téléphone'

                script {
                    // Détecter automatiquement le port ADB
                    def detectedPort = sh(
                        script: '''
                            # Essayer de se connecter avec différents ports
                            for PORT in 5555 32773 36505 5557 37717; do
                                echo "Tentative ${PHONE_IP}:${PORT}..." >&2
                                adb connect ${PHONE_IP}:${PORT} >/dev/null 2>&1
                                sleep 1
                            done

                            # Récupérer le port effectivement connecté
                            CONNECTED=$(adb devices | grep ${PHONE_IP} | grep device | awk '{print $1}')
                            if [ ! -z "$CONNECTED" ]; then
                                echo "$CONNECTED" | cut -d':' -f2
                            else
                                echo "5555"
                            fi
                        ''',
                        returnStdout: true
                    ).trim()

                    env.PHONE_PORT = detectedPort
                    echo "✅ Port détecté : ${env.PHONE_PORT}"

                    sh '''
                        echo "=========================================="
                        echo "CONNEXION AU TÉLÉPHONE"
                        echo "=========================================="
                        echo "IP : ${PHONE_IP}"
                        echo "Port : ${PHONE_PORT}"

                        # Assurer la connexion sur le bon port
                        adb connect ${PHONE_IP}:${PHONE_PORT}
                        sleep 3

                        echo ""
                        echo "=== APPAREILS CONNECTÉS ==="
                        adb devices -l

                        DEVICE_COUNT=$(adb devices | grep -w "device" | wc -l)
                        if [ $DEVICE_COUNT -eq 0 ]; then
                            echo "❌ ERREUR: Aucun appareil détecté"
                            exit 1
                        fi
                        echo "✓ ${DEVICE_COUNT} appareil(s) connecté(s)"

                        echo ""
                        echo "=== INFORMATIONS APPAREIL ==="
                        echo "Modèle     : $(adb shell getprop ro.product.model)"
                        echo "Fabricant  : $(adb shell getprop ro.product.manufacturer)"
                        echo "Android    : $(adb shell getprop ro.build.version.release)"
                        echo "API Level  : $(adb shell getprop ro.build.version.sdk)"

                        echo ""
                        echo "=== DÉSINSTALLATION ANCIENNE VERSION ==="
                        adb uninstall com.qos.latency.analyzer 2>/dev/null || echo "   Pas d'ancienne version (OK)"

                        echo ""
                        echo "=== INSTALLATION DE L'APK ==="
                        adb install -r app/build/outputs/apk/debug/app-debug.apk

                        echo ""
                        echo "=== ATTRIBUTION DES PERMISSIONS SYSTÈME ==="
                        echo "Attribution WRITE_SETTINGS..."
                        adb shell pm grant com.qos.latency.analyzer android.permission.WRITE_SETTINGS || echo "   Déjà accordée"

                        echo "Attribution WRITE_SECURE_SETTINGS..."
                        adb shell pm grant com.qos.latency.analyzer android.permission.WRITE_SECURE_SETTINGS || echo "   Déjà accordée"

                        echo "Attribution ACCESS_FINE_LOCATION..."
                        adb shell pm grant com.qos.latency.analyzer android.permission.ACCESS_FINE_LOCATION || echo "   Déjà accordée"

                        echo "Attribution ACCESS_COARSE_LOCATION..."
                        adb shell pm grant com.qos.latency.analyzer android.permission.ACCESS_COARSE_LOCATION || echo "   Déjà accordée"

                        echo ""
                        echo "=== VÉRIFICATION DES PERMISSIONS ==="
                        adb shell dumpsys package com.qos.latency.analyzer | grep "permission granted=true" || echo "Vérification terminée"

                        echo ""
                        echo "=== RÉINITIALISATION DES PARAMÈTRES SYSTÈME ==="
                        echo "Réinitialisation batterie..."
                        adb shell dumpsys battery reset

                        echo "Désactivation mode avion..."
                        adb shell settings put global airplane_mode_on 0
                        adb shell am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false

                        echo ""
                        echo "✅ Préparation terminée avec succès"
                    '''
                }
            }
        }

        stage('Démarrage Appium Server') {
            steps {
                echo '🚀 Démarrage du serveur Appium'

                script {
                    sh '''
                        echo "Arrêt de tout processus Appium existant..."
                        pkill -f appium || true
                        sleep 2

                        echo "Démarrage d'Appium sur le port ${APPIUM_PORT} avec adb_shell..."
                        nohup appium server \
                            --address 127.0.0.1 \
                            --port ${APPIUM_PORT} \
                            --log /tmp/appium.log \
                            --log-level info \
                            --use-drivers uiautomator2 \
                            --allow-insecure adb_shell \
                            --relaxed-security &

                        echo "Attente du démarrage d'Appium..."
                        sleep 10

                        echo "Appium démarré avec succès"

                        echo ""
                        echo "=== LOGS APPIUM (10 premières lignes) ==="
                        head -n 10 /tmp/appium.log || echo "Logs non encore disponibles"
                    '''
                }
            }
        }

        stage('Tests Appium - Mode Éco Batterie') {
            steps {
                echo '🔋 Tests du mode économie d\'énergie basé sur la batterie'
                echo ''
                echo '=========================================='
                echo 'SCÉNARIOS TESTÉS:'
                echo '1. Activation auto si batterie < 60%'
                echo '2. Non-activation si batterie >= 60%'
                echo '3. Désactivation manuelle'
                echo '4. Vérification états système'
                echo '=========================================='

                script {
                    catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                        sh '''
                            echo "Exécution des tests de mode éco batterie..."
                            echo "Port ADB utilisé : ${PHONE_PORT}"

                            ./gradlew appiumTest \
                                -Dappium.server=http://127.0.0.1:${APPIUM_PORT} \
                                -Dphone.udid=${PHONE_IP}:${PHONE_PORT} \
                                -Pandroid.testInstrumentationRunnerArguments.class=com.qos.latency.analyzer.appium.BatteryEcoModeTest \
                                --stacktrace \
                                --info

                            echo ""
                            echo "=== RÉSULTATS DES TESTS ==="
                            if [ -f "app/build/reports/tests/appiumTest/index.html" ]; then
                                echo "✓ Rapport HTML généré avec succès"
                                echo "  Emplacement: app/build/reports/tests/appiumTest/index.html"
                            else
                                echo "⚠ Rapport HTML non trouvé"
                            fi

                            if [ -d "app/build/test-results/appiumTest" ]; then
                                echo "✓ Résultats XML générés"
                                echo "  Nombre de fichiers: $(ls app/build/test-results/appiumTest/*.xml 2>/dev/null | wc -l)"
                            fi
                        '''
                    }
                }
            }
        }

        stage('Arrêt Appium Server') {
            steps {
                echo '🛑 Arrêt du serveur Appium'

                sh '''
                    echo "Arrêt d'Appium..."
                    pkill -f appium || true
                    sleep 2
                    echo "✓ Appium arrêté"
                '''
            }
        }

        stage('Tests Instrumentés Espresso') {
            steps {
                echo '📱 Tests Espresso avec visualisation (105 secondes)'

                script {
                    catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                        sh '''
                            ./gradlew connectedAndroidTest \
                                -Pandroid.testInstrumentationRunnerArguments.class=com.qos.latency.analyzer.CompleteFlowTest#testCompleteExecutionFlowWithVisualization \
                                --stacktrace \
                                --info
                        '''
                    }
                }
            }
        }

        stage('Analyse Lint') {
            steps {
                echo '🔍 Analyse Lint Android'
                sh './gradlew lint'
            }
        }

        stage('Archive APK') {
            steps {
                echo '📦 Archivage de l\'APK'
                archiveArtifacts artifacts: '**/build/outputs/apk/debug/*.apk', allowEmptyArchive: false, fingerprint: true
            }
        }

    }

    post {
        success {
            echo '=========================================='
            echo '✅ BUILD RÉUSSI'
            echo '=========================================='
            echo 'APK disponible dans les artifacts'
            echo 'Nom du fichier: app-debug.apk'
            echo ''

            script {
                // Publier les résultats des tests unitaires
                def testResults = junit testResults: '**/build/test-results/**/*.xml'
                echo "Tests unitaires exécutés : ${testResults.totalCount}"
                echo "Tests réussis : ${testResults.passCount}"
                echo "Tests échoués : ${testResults.failCount}"

                // Résumé des tests Appium
                if (fileExists('app/build/test-results/appiumTest')) {
                    echo ""
                    echo "=== TESTS APPIUM (MODE ÉCO) ==="
                    sh 'ls -lh app/build/test-results/appiumTest/*.xml 2>/dev/null || echo "Résultats non trouvés"'
                }
            }
        }

        failure {
            echo '=========================================='
            echo '❌ LA PIPELINE A ÉCHOUÉ'
            echo '=========================================='
            echo 'Consultez les logs ci-dessus pour identifier les erreurs'
            echo ''
            echo 'Points de vérification:'
            echo '1. Connexion au téléphone'
            echo '2. Installation de l\'APK'
            echo '3. Permissions système accordées'
            echo '4. Serveur Appium démarré'
            echo '5. Tests exécutés'
        }

        unstable {
            echo '=========================================='
            echo '⚠️  BUILD INSTABLE (certains tests ont échoué)'
            echo '=========================================='
            echo 'L\'APK est construit mais certains tests ne sont pas passés'
        }

        always {
            echo '🧹 Nettoyage final'

            script {
                // Archiver tous les rapports de tests
                junit allowEmptyResults: true, testResults: '**/build/test-results/**/*.xml'

                // Archiver les rapports Lint
                publishHTML([
                    allowMissing: true,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'app/build/reports',
                    reportFiles: 'lint-results-debug.html',
                    reportName: 'Rapport Lint'
                ])

                // Archiver les rapports de tests Appium
                publishHTML([
                    allowMissing: true,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'app/build/reports/tests/appiumTest',
                    reportFiles: 'index.html',
                    reportName: 'Rapport Tests Appium (Mode Éco)'
                ])

                // Réinitialiser l'appareil
                sh '''
                    echo "Réinitialisation de l'appareil de test..."
                    adb shell dumpsys battery reset 2>/dev/null || true
                    adb shell settings put global airplane_mode_on 0 2>/dev/null || true
                    echo "✓ Appareil réinitialisé"
                '''
            }
        }
    }
}