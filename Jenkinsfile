pipeline {
    agent any

    triggers {
        pollSCM('H */4 * * 1-5')
    }

    environment {
        ANDROID_HOME = '/opt/android-sdk'
        PATH = "${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${env.PATH}"
        PHONE_IP = "192.168.1.109"
        ADB_PORT = "5555"
        APPIUM_PORT = "4723"
    }

    stages {

        stage('Nettoyage Préliminaire') {
            steps {
                echo '🧹 Nettoyage préliminaire - Arrêt des processus actifs'

                script {
                    sh '''
                        echo "═══════════════════════════════════════════════════════════════"
                        echo "  NETTOYAGE PRÉLIMINAIRE"
                        echo "═══════════════════════════════════════════════════════════════"

                        echo ""
                        echo "Arrêt de tous les processus Appium..."
                        pkill -f appium || echo "   ✓ Aucun processus Appium à arrêter"

                        echo ""
                        echo "Arrêt du daemon Gradle..."
                        ./gradlew --stop || echo "   ✓ Aucun daemon Gradle actif"

                        echo ""
                        echo "Attente de la libération des fichiers..."
                        sleep 3

                        echo ""
                        echo "✅ Nettoyage préliminaire terminé"
                        echo "═══════════════════════════════════════════════════════════════"
                    '''
                }
            }
        }

        stage('Informations Environnement') {
            steps {
                echo '📋 Vérification de l\'environnement'
                sh '''
                    echo "═══════════════════════════════════════════════════════════════"
                    echo "  INFORMATIONS ENVIRONNEMENT"
                    echo "═══════════════════════════════════════════════════════════════"

                    echo ""
                    echo "--- Java Version ---"
                    java -version

                    echo ""
                    echo "--- Android SDK ---"
                    echo "Location: ${ANDROID_HOME}"

                    echo ""
                    echo "--- Gradle ---"
                    ls -la gradlew

                    echo ""
                    echo "--- ADB ---"
                    adb version

                    echo ""
                    echo "═══════════════════════════════════════════════════════════════"
                '''
            }
        }

        stage('Clean') {
            steps {
                echo '🧹 Nettoyage du projet Gradle'

                script {
                    sh 'chmod +x gradlew'

                    // Tentative de clean normal
                    def cleanResult = sh(script: './gradlew clean --no-daemon', returnStatus: true)

                    if (cleanResult != 0) {
                        echo "⚠️  Clean standard échoué, nettoyage forcé en cours..."
                        sh '''
                            echo ""
                            echo "Arrêt des processus bloquants..."
                            pkill -f gradle || true
                            pkill -f appium || true
                            sleep 2

                            echo ""
                            echo "Suppression manuelle des répertoires build..."
                            rm -rf app/build || true
                            rm -rf build || true

                            echo ""
                            echo "✅ Nettoyage forcé terminé"
                        '''
                    } else {
                        echo "✅ Clean standard réussi"
                    }
                }
            }
        }

        stage('Build Application Android') {
            steps {
                echo '🔨 Compilation de l\'APK Debug'
                sh './gradlew assembleDebug'

                script {
                    sh '''
                        echo ""
                        echo "✅ APK créé avec succès:"
                        ls -lh app/build/outputs/apk/debug/app-debug.apk
                    '''
                }
            }
        }

        stage('Tests Unitaires') {
            steps {
                echo '🧪 Exécution des tests unitaires'
                sh './gradlew test --stacktrace'
            }
        }

        stage('Connexion ADB Wireless') {
            steps {
                echo '📱 Connexion au téléphone via ADB Wireless'

                script {
                    sh '''
                        echo "═══════════════════════════════════════════════════════════════"
                        echo "  CONNEXION ADB WIRELESS"
                        echo "═══════════════════════════════════════════════════════════════"

                        echo ""
                        echo "Arrêt du serveur ADB..."
                        adb kill-server || true
                        sleep 2

                        echo ""
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

                        echo ""
                        # Vérification qu'un appareil est bien connecté
                        DEVICE_COUNT=$(adb devices | grep -w "device" | wc -l)
                        if [ $DEVICE_COUNT -eq 0 ]; then
                            echo "❌ ERREUR: Aucun appareil détecté"
                            echo ""
                            echo "DIAGNOSTIC:"
                            echo "1. Vérifiez que le débogage sans fil est activé"
                            echo "2. Vérifiez que l'appareil est sur le réseau: ${PHONE_IP}"
                            echo "3. Si nécessaire, effectuez le pairing avec:"
                            echo "   adb pair ${PHONE_IP}:<PORT_PAIRING>"
                            echo "4. Puis reconnectez avec:"
                            echo "   adb connect ${PHONE_IP}:${ADB_PORT}"
                            echo ""
                            echo "═══════════════════════════════════════════════════════════════"
                            exit 1
                        fi

                        echo "✅ ${DEVICE_COUNT} appareil(s) connecté(s)"
                        echo "═══════════════════════════════════════════════════════════════"
                    '''
                }
            }
        }

        stage('Préparation Device pour Appium') {
            steps {
                echo '📱 Installation de l\'application et préparation du device'

                script {
                    sh '''
                        echo "═══════════════════════════════════════════════════════════════"
                        echo "  PRÉPARATION DEVICE"
                        echo "═══════════════════════════════════════════════════════════════"

                        echo ""
                        echo "=== INFORMATIONS APPAREIL ==="
                        echo "Modèle     : $(adb shell getprop ro.product.model)"
                        echo "Fabricant  : $(adb shell getprop ro.product.manufacturer)"
                        echo "Android    : $(adb shell getprop ro.build.version.release)"
                        echo "API Level  : $(adb shell getprop ro.build.version.sdk)"
                        echo "UDID       : ${PHONE_IP}:${ADB_PORT}"

                        echo ""
                        echo "Désinstallation de l'ancienne version..."
                        adb uninstall com.qos.latency.analyzer 2>/dev/null || echo "   ✓ Pas d'ancienne version"

                        echo ""
                        echo "=== INSTALLATION DE L'APP ==="
                        adb install -r app/build/outputs/apk/debug/app-debug.apk

                        echo ""
                        echo "✅ Application installée avec succès"
                        echo "   L'app lira les fichiers depuis les assets"
                        echo "═══════════════════════════════════════════════════════════════"
                    '''
                }
            }
        }

        stage('Démarrage Appium Server') {
            steps {
                echo '🚀 Démarrage du serveur Appium'

                script {
                    sh '''
                        echo "═══════════════════════════════════════════════════════════════"
                        echo "  DÉMARRAGE SERVEUR APPIUM"
                        echo "═══════════════════════════════════════════════════════════════"

                        echo ""
                        echo "Arrêt de tout processus Appium existant..."
                        pkill -f appium || echo "   ✓ Aucun processus Appium actif"
                        sleep 2

                        echo ""
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
                        if curl -s http://127.0.0.1:${APPIUM_PORT}/status > /dev/null 2>&1; then
                            echo "✅ Serveur Appium démarré et accessible"
                            echo ""
                            echo "Status:"
                            curl -s http://127.0.0.1:${APPIUM_PORT}/status | head -5
                        else
                            echo "❌ ERREUR: Le serveur Appium ne répond pas"
                            echo ""
                            echo "=== LOGS APPIUM STARTUP ==="
                            cat /tmp/appium-startup.log 2>/dev/null || echo "Pas de logs startup"
                            echo ""
                            echo "=== LOGS APPIUM ==="
                            cat /tmp/appium.log 2>/dev/null || echo "Pas de logs Appium"
                            echo ""
                            echo "═══════════════════════════════════════════════════════════════"
                            exit 1
                        fi

                        echo ""
                        echo "=== LOGS APPIUM (10 premières lignes) ==="
                        head -n 10 /tmp/appium.log || echo "Pas encore de logs"

                        echo ""
                        echo "═══════════════════════════════════════════════════════════════"
                    '''
                }
            }
        }

        stage('Tests Appium') {
            steps {
                echo '🧪 Exécution de tous les tests Appium'

                script {
                    catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                        sh '''
                            echo "═══════════════════════════════════════════════════════════════"
                            echo "  TESTS APPIUM"
                            echo "═══════════════════════════════════════════════════════════════"

                            echo ""
                            echo "Configuration:"
                            echo "  Serveur Appium : http://127.0.0.1:${APPIUM_PORT}"
                            echo "  Device UDID    : ${PHONE_IP}:${ADB_PORT}"
                            echo ""

                            # Vérifier la connexion ADB avant les tests
                            echo "Vérification de la connexion ADB..."
                            if ! adb devices | grep -q "${PHONE_IP}:${ADB_PORT}.*device"; then
                                echo "⚠️  Reconnexion ADB nécessaire..."
                                adb connect ${PHONE_IP}:${ADB_PORT}
                                sleep 3
                            else
                                echo "✓ Device connecté"
                            fi

                            echo ""
                            echo "Lancement des tests Appium (6 tests)..."
                            echo ""

                            ./gradlew appiumTest \
                                -Dappium.server=http://127.0.0.1:${APPIUM_PORT} \
                                --stacktrace \
                                --info

                            echo ""
                            echo "═══════════════════════════════════════════════════════════════"
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
                            echo "═══════════════════════════════════════════════════════════════"
                            echo "  TEST COMPLET AVEC VISUALISATION"
                            echo "═══════════════════════════════════════════════════════════════"

                            echo ""
                            echo "Ce test inclut:"
                            echo "  • 10 phases de test"
                            echo "  • Animation de 60 secondes"
                            echo "  • Vérifications toutes les 10 secondes"
                            echo ""
                            echo "Durée estimée: ~1 minute 45 secondes"
                            echo ""

                            ./gradlew appiumTest \
                                -Dappium.server=http://127.0.0.1:${APPIUM_PORT} \
                                -Pandroid.testInstrumentationRunnerArguments.class=com.qos.latency.analyzer.appium.CompleteFlowAppiumTest#testCompleteExecutionFlowWithVisualization \
                                --stacktrace \
                                --info

                            echo ""
                            echo "═══════════════════════════════════════════════════════════════"
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
                    pkill -f appium || echo "   ✓ Aucun processus Appium à arrêter"
                    sleep 2
                    echo "✅ Appium arrêté"
                '''
            }
        }

        stage('Tests Instrumentés Espresso') {
            steps {
                echo '📱 Tests instrumentés Espresso avec visualisation'

                script {
                    catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                        sh '''
                            echo "═══════════════════════════════════════════════════════════════"
                            echo "  TESTS INSTRUMENTÉS ESPRESSO"
                            echo "═══════════════════════════════════════════════════════════════"

                            echo ""
                            ./gradlew connectedAndroidTest \
                                -Pandroid.testInstrumentationRunnerArguments.class=com.qos.latency.analyzer.CompleteFlowTest#testCompleteExecutionFlowWithVisualization \
                                --stacktrace \
                                --info

                            echo ""
                            echo "═══════════════════════════════════════════════════════════════"
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

                script {
                    sh '''
                        echo ""
                        echo "✅ APK archivé:"
                        echo "   Nom: app-debug.apk"
                        echo "   Taille: $(du -h app/build/outputs/apk/debug/app-debug.apk | cut -f1)"
                    '''
                }
            }
        }

    }

    post {
        success {
            echo ''
            echo '═══════════════════════════════════════════════════════════════'
            echo '  ✅ BUILD RÉUSSI'
            echo '═══════════════════════════════════════════════════════════════'
            echo ''
            echo '📦 APK disponible dans les artifacts'
            echo '   Nom du fichier: app-debug.apk'
            echo ''

            script {
                try {
                    def testResults = junit testResults: '**/build/test-results/**/*.xml'
                    echo "📊 Résultats des tests:"
                    echo "   Tests exécutés : ${testResults.totalCount}"
                    echo "   Tests réussis  : ${testResults.passCount}"
                    echo "   Tests échoués  : ${testResults.failCount}"
                } catch (Exception e) {
                    echo "⚠️  Impossible de charger les résultats des tests"
                }
            }

            echo ''
            echo '═══════════════════════════════════════════════════════════════'
        }

        failure {
            echo ''
            echo '═══════════════════════════════════════════════════════════════'
            echo '  ❌ LA PIPELINE A ÉCHOUÉ'
            echo '═══════════════════════════════════════════════════════════════'
            echo ''
            echo 'Consultez les logs ci-dessus pour identifier les erreurs'
            echo ''

            script {
                sh '''
                    echo "=== DIAGNOSTIC ==="
                    echo ""
                    echo "État ADB:"
                    adb devices || echo "   ⚠️  ADB non disponible"

                    echo ""
                    echo "Processus actifs:"
                    ps aux | grep -E "appium|gradle" | grep -v grep || echo "   ✓ Aucun processus suspect"

                    echo ""
                    echo "Logs Appium (20 dernières lignes):"
                    tail -n 20 /tmp/appium.log 2>/dev/null || echo "   ⚠️  Pas de logs Appium"

                    echo ""
                    echo "═══════════════════════════════════════════════════════════════"
                '''
            }
        }

        always {
            echo ''
            echo '🧹 Nettoyage final'

            script {
                // Arrêter Appium si encore actif
                sh '''
                    pkill -f appium || echo "   ✓ Appium déjà arrêté"
                '''

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

            // Archiver les rapports de tests instrumentés
            publishHTML([
                allowMissing: true,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'app/build/reports/androidTests/connected',
                reportFiles: 'index.html',
                reportName: 'Espresso Test Report'
            ])
        }
    }
}
