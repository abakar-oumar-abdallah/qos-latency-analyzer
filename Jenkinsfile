pipeline {
    agent any

    triggers {
        pollSCM('H */4 * * 1-5')
    }

    environment {
        ANDROID_HOME = '/opt/android-sdk'
        PATH = "${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${env.PATH}"
        PHONE_IP = "192.168.1.109"
        ADB_PORT = "5555"  // Port TCP/IP standard après pairing wireless
        APPIUM_PORT = "4723"
    }

    stages {

        stage('Informations Environnement') {
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
                    echo ""
                    echo "ADB version"
                    adb version
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
                echo 'Compilation de APK Debug'
                sh './gradlew assembleDebug'
            }
        }

        stage('Tests Unitaires') {
            steps {
                echo 'Exécution des tests unitaires'
                sh './gradlew test --stacktrace'
            }
        }

        stage('Connexion ADB Wireless') {
            steps {
                echo '📱 Connexion au téléphone via ADB Wireless'

                script {
                    sh '''
                        echo "=== CONNEXION ADB WIRELESS ==="
                        echo "Arrêt du serveur ADB..."
                        adb kill-server || true
                        sleep 2

                        echo "Démarrage du serveur ADB..."
                        adb start-server
                        sleep 2

                        echo ""
                        echo "Tentative de connexion à ${PHONE_IP}:${ADB_PORT}..."
                        adb connect ${PHONE_IP}:${ADB_PORT} || true
                        sleep 3

                        echo ""
                        echo "=== APPAREILS CONNECTÉS ==="
                        adb devices -l

                        # Vérification qu'un appareil est bien connecté
                        DEVICE_COUNT=$(adb devices | grep -w "device" | wc -l)
                        if [ $DEVICE_COUNT -eq 0 ]; then
                            echo ""
                            echo "❌ ERREUR: Aucun appareil détecté"
                            echo ""
                            echo "DIAGNOSTIC :"
                            echo "1. Vérifiez que le débogage sans fil est activé sur l'appareil"
                            echo "2. Vérifiez que l'appareil est sur le même réseau (${PHONE_IP})"
                            echo "3. Si nécessaire, effectuez le pairing manuel avec:"
                            echo "   adb pair ${PHONE_IP}:<PORT_PAIRING>"
                            echo "4. Puis reconnectez avec:"
                            echo "   adb connect ${PHONE_IP}:${ADB_PORT}"
                            exit 1
                        fi

                        echo ""
                        echo "✅ ${DEVICE_COUNT} appareil(s) connecté(s)"
                    '''
                }
            }
        }

        stage('Préparation Device pour Appium') {
            steps {
                echo '📱 Installation de l\'application et préparation'

                script {
                    sh '''
                        echo "=== INFORMATIONS APPAREIL ==="
                        echo "Modèle     : $(adb shell getprop ro.product.model)"
                        echo "Fabricant  : $(adb shell getprop ro.product.manufacturer)"
                        echo "Android    : $(adb shell getprop ro.build.version.release)"
                        echo "API Level  : $(adb shell getprop ro.build.version.sdk)"
                        echo "UDID       : ${PHONE_IP}:${ADB_PORT}"

                        echo ""
                        echo "Désinstallation de l'ancienne version..."
                        adb uninstall com.qos.latency.analyzer 2>/dev/null || echo "   Pas d'ancienne version (OK)"

                        echo ""
                        echo "=== INSTALLATION DE L'APP ==="
                        adb install -r app/build/outputs/apk/debug/app-debug.apk

                        echo ""
                        echo "✅ Préparation terminée - L'app lira les fichiers depuis les assets"
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

                        echo "Démarrage d'Appium sur le port ${APPIUM_PORT}..."
                        nohup appium \
                            --address 127.0.0.1 \
                            --port ${APPIUM_PORT} \
                            --log /tmp/appium.log \
                            --log-level info \
                            --relaxed-security > /tmp/appium-startup.log 2>&1 &

                        echo "Attente du démarrage d'Appium (10 secondes)..."
                        sleep 10

                        echo ""
                        echo "=== VÉRIFICATION DU SERVEUR APPIUM ==="
                        if curl -s http://127.0.0.1:${APPIUM_PORT}/status > /dev/null; then
                            echo "✅ Serveur Appium démarré et accessible"
                            curl -s http://127.0.0.1:${APPIUM_PORT}/status | head -5
                        else
                            echo "❌ ERREUR: Le serveur Appium ne répond pas"
                            echo ""
                            echo "=== LOGS APPIUM ==="
                            cat /tmp/appium.log 2>/dev/null || echo "Pas de logs disponibles"
                            exit 1
                        fi

                        echo ""
                        echo "=== LOGS APPIUM (10 premières lignes) ==="
                        head -n 10 /tmp/appium.log || echo "Pas encore de logs"
                    '''
                }
            }
        }

        stage('Tests Appium') {
            steps {
                echo '🧪 Exécution des tests Appium'

                script {
                    // On exécute tous les tests Appium
                    catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                        sh '''
                            echo "=== EXÉCUTION DES TESTS APPIUM ==="
                            echo "Serveur Appium : http://127.0.0.1:${APPIUM_PORT}"
                            echo "Device UDID    : ${PHONE_IP}:${ADB_PORT}"
                            echo ""

                            # Vérifier à nouveau la connexion ADB avant les tests
                            echo "Vérification de la connexion ADB..."
                            adb devices | grep "${PHONE_IP}:${ADB_PORT}.*device" || {
                                echo "⚠️  Reconnexion ADB nécessaire..."
                                adb connect ${PHONE_IP}:${ADB_PORT}
                                sleep 3
                            }

                            echo ""
                            echo "Lancement des tests..."
                            ./gradlew appiumTest \
                                -Dappium.server=http://127.0.0.1:${APPIUM_PORT} \
                                --stacktrace \
                                --info
                        '''
                    }
                }
            }
        }

        stage('Test Complet avec Visualisation') {
            steps {
                echo '📱 Test Appium avec visualisation complète (~2 minutes)'

                script {
                    catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                        sh '''
                            echo "=== TEST COMPLET AVEC VISUALISATION ==="
                            echo "Ce test inclut 10 phases avec animation de 60 secondes"
                            echo "Durée estimée : ~1 minute 45 secondes"
                            echo ""

                            ./gradlew appiumTest \
                                -Dappium.server=http://127.0.0.1:${APPIUM_PORT} \
                                -Pandroid.testInstrumentationRunnerArguments.class=com.qos.latency.analyzer.appium.CompleteFlowAppiumTest#testCompleteExecutionFlowWithVisualization \
                                --stacktrace \
                                --info
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
                    echo "✅ Appium arrêté"
                '''
            }
        }

        stage('Tests Instrumentés Espresso') {
            steps {
                echo '📱 Test Espresso avec visualisation (105 secondes)'

                script {
                    catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                        sh '''
                            echo "=== TESTS INSTRUMENTÉS ESPRESSO ==="
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
                echo '📦 Archivage de APK'
                archiveArtifacts artifacts: '**/build/outputs/apk/debug/*.apk', allowEmptyArchive: false, fingerprint: true
            }
        }

    }

    post {
        success {
            echo '✅ Build réussi'
            echo 'APK disponible dans les artifacts'
            echo 'Nom du fichier: app-debug.apk'

            script {
                def testResults = junit testResults: '**/build/test-results/**/*.xml'
                echo "Tests exécutés : ${testResults.totalCount}"
                echo "Tests réussis : ${testResults.passCount}"
                echo "Tests échoués : ${testResults.failCount}"
            }
        }

        failure {
            echo '❌ La pipeline a échoué'
            echo 'Consultez les logs ci-dessus pour identifier les erreurs'

            script {
                sh '''
                    echo ""
                    echo "=== DIAGNOSTIC ==="
                    echo "État ADB:"
                    adb devices || true

                    echo ""
                    echo "Logs Appium (20 dernières lignes):"
                    tail -n 20 /tmp/appium.log 2>/dev/null || echo "Pas de logs Appium"
                '''
            }
        }

        always {
            echo '🧹 Nettoyage final'

            script {
                // Arrêter Appium si encore actif
                sh 'pkill -f appium || true'

                // Optionnel : déconnecter ADB
                // sh 'adb disconnect ${PHONE_IP}:${ADB_PORT} || true'
            }

            // Archiver les rapports de tests
            junit allowEmptyResults: true, testResults: '**/build/test-results/**/*.xml'

            // Archiver les rapports Lint
            publishHTML([
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'app/build/reports',
                reportFiles: 'lint-results-debug.html',
                reportName: 'Lint Report'
            ])

            // Archiver les rapports de tests Appium
            publishHTML([
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'app/build/reports/tests/appiumTest',
                reportFiles: 'index.html',
                reportName: 'Appium Test Report'
            ])
        }
    }
}