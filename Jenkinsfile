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
                    echo "Node.js et Appium"
                    node --version || echo "Node non trouvé"
                    appium --version || echo "Appium non trouvé"
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

        stage('Build Application Android') {
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

        stage('Préparation Device pour Appium') {
            steps {
                echo '📱 Connexion au téléphone via ADB Wireless'

                script {
                    sh '''
                        echo "=== CONNEXION ADB WIRELESS ==="
                        echo "Tentative de connexion à ${PHONE_IP}:${ADB_PORT}..."

                        # Déconnecter tous les appareils précédents
                        adb disconnect || true
                        sleep 2

                        # Connexion au téléphone sur le port wireless
                        adb connect ${PHONE_IP}:${ADB_PORT}
                        sleep 3

                        echo ""
                        echo "=== APPAREILS CONNECTÉS ==="
                        adb devices -l

                        # Vérifier qu'au moins un appareil est connecté
                        DEVICE_COUNT=$(adb devices | grep -w "device" | wc -l)
                        if [ $DEVICE_COUNT -eq 0 ]; then
                            echo "❌ ERREUR: Aucun appareil détecté"
                            echo ""
                            echo "💡 SOLUTION:"
                            echo "1. Vérifiez que le débogage sans fil est activé sur le téléphone"
                            echo "2. Vérifiez que le téléphone est sur le même réseau (${PHONE_IP})"
                            echo "3. Si nécessaire, reconnectez manuellement avec:"
                            echo "   adb connect ${PHONE_IP}:${ADB_PORT}"
                            exit 1
                        fi
                        echo "✅ ${DEVICE_COUNT} appareil(s) connecté(s)"

                        echo ""
                        echo "=== INFORMATIONS APPAREIL ==="
                        echo "Modèle     : $(adb shell getprop ro.product.model)"
                        echo "Fabricant  : $(adb shell getprop ro.product.manufacturer)"
                        echo "Android    : $(adb shell getprop ro.build.version.release)"
                        echo "API Level  : $(adb shell getprop ro.build.version.sdk)"
                        echo "Adresse IP : ${PHONE_IP}:${ADB_PORT}"

                        echo ""
                        echo "Désinstallation de l'ancienne version..."
                        adb uninstall com.qos.latency.analyzer 2>/dev/null || echo "   Pas d'ancienne version (OK)"

                        echo ""
                        echo "=== INSTALLATION DE L'APK ==="
                        adb install -r app/build/outputs/apk/debug/app-debug.apk

                        echo ""
                        echo "✅ Préparation terminée - L'app est installée et prête pour les tests"
                    '''
                }
            }
        }

        stage('Démarrage Appium Server') {
            steps {
                echo '🚀 Démarrage du serveur Appium'

                script {
                    sh '''
                        echo "=== VÉRIFICATION APPIUM EXISTANT ==="

                        APP_LOG="$WORKSPACE/appium.log"

                        # Vérifier si Appium est déjà en cours d'exécution
                        if curl -s http://127.0.0.1:${APPIUM_PORT}/status > /dev/null 2>&1; then
                            echo "✅ Appium est déjà en cours d'exécution"
                            curl -s http://127.0.0.1:${APPIUM_PORT}/status | python3 -m json.tool || echo "Statut Appium OK"
                        else
                            echo "Appium n'est pas en cours, démarrage..."

                            echo "Arrêt de tout processus Appium existant..."
                            pkill -f appium || true
                            sleep 2

                            echo "Création du fichier de log Appium dans le workspace"
                            touch "$APP_LOG"

                            echo "Démarrage d'Appium sur le port ${APPIUM_PORT}..."
                            nohup appium server \
                                --address 0.0.0.0 \
                                --port ${APPIUM_PORT} \
                                --log "$APP_LOG" \
                                --log-level info \
                                --use-drivers uiautomator2 \
                                --relaxed-security > /dev/null 2>&1 &

                            echo "Attente du démarrage d'Appium (15 secondes)..."
                            sleep 15
                        fi

                        echo ""
                        echo "=== VÉRIFICATION DU SERVEUR APPIUM ==="

                        MAX_ATTEMPTS=5
                        ATTEMPT=0
                        APPIUM_READY=false

                        while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
                            ATTEMPT=$((ATTEMPT + 1))
                            echo "Tentative $ATTEMPT/$MAX_ATTEMPTS..."

                            if curl -s http://127.0.0.1:${APPIUM_PORT}/status > /dev/null 2>&1; then
                                APPIUM_READY=true
                                echo "✅ Appium répond !"
                                break
                            fi

                            echo "⏳ Appium ne répond pas encore, attente de 3 secondes..."
                            sleep 3
                        done

                        if [ "$APPIUM_READY" = false ]; then
                            echo "❌ ERREUR: Le serveur Appium ne répond pas"
                            echo ""
                            echo "=== LOGS APPIUM ==="
                            tail -n 50 "$APP_LOG" || echo "Pas de logs disponibles"
                            exit 1
                        fi

                        echo "✅ Serveur Appium prêt sur http://127.0.0.1:${APPIUM_PORT}"
                    '''
                }
            }
        }


        stage('Tests Appium') {
            steps {
                echo '🧪 Exécution des tests Appium (tous les tests)'

                script {
                    catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                        sh '''
                            echo "=== LANCEMENT DES TESTS APPIUM ==="
                            echo "Serveur Appium : http://127.0.0.1:${APPIUM_PORT}"
                            echo "Appareil : ${PHONE_IP}:${ADB_PORT}"
                            echo ""

                            # Vérifier la connexion ADB avant de lancer les tests
                            if ! adb devices | grep -q "${PHONE_IP}:${ADB_PORT}.*device"; then
                                echo "⚠️ Reconnexion ADB nécessaire..."
                                adb connect ${PHONE_IP}:${ADB_PORT}
                                sleep 2
                            fi

                            # Exécuter tous les tests Appium
                            ./gradlew appiumTest \
                                -Dappium.server=http://127.0.0.1:${APPIUM_PORT} \
                                --stacktrace

                            echo ""
                            echo "✅ Tests Appium terminés"
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
                echo '📱 Tests Espresso avec visualisation (105 secondes)'

                script {
                    catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                        sh '''
                            echo "=== LANCEMENT DES TESTS ESPRESSO ==="

                            # Vérifier la connexion ADB
                            if ! adb devices | grep -q "${PHONE_IP}:${ADB_PORT}.*device"; then
                                echo "⚠️ Reconnexion ADB nécessaire..."
                                adb connect ${PHONE_IP}:${ADB_PORT}
                                sleep 2
                            fi

                            ./gradlew connectedAndroidTest \
                                -Pandroid.testInstrumentationRunnerArguments.class=com.qos.latency.analyzer.CompleteFlowTest#testCompleteExecutionFlowWithVisualization \
                                --stacktrace \
                                --info

                            echo ""
                            echo "✅ Tests Espresso terminés"
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
            echo '✅ Build réussi !'
            echo '📦 APK disponible dans les artifacts'
            echo '📄 Nom du fichier: app-debug.apk'

            script {
                try {
                    def testResults = junit testResults: '**/build/test-results/**/*.xml'
                    echo "📊 RÉSULTATS DES TESTS:"
                    echo "   Tests exécutés : ${testResults.totalCount}"
                    echo "   Tests réussis  : ${testResults.passCount}"
                    echo "   Tests échoués  : ${testResults.failCount}"
                } catch (Exception e) {
                    echo "⚠️ Impossible de parser les résultats des tests"
                }
            }
        }

        failure {
            echo '❌ La pipeline a échoué'
            echo '📋 Consultez les logs ci-dessus pour identifier les erreurs'
            echo ''
            echo '💡 POINTS À VÉRIFIER:'
            echo '   1. La connexion ADB au téléphone (${PHONE_IP}:${ADB_PORT})'
            echo '   2. Le serveur Appium est bien démarré'
            echo '   3. Les tests compilent correctement'
        }

        unstable {
            echo '⚠️ Build instable - Certains tests ont échoué'
            echo '📊 Consultez les rapports de tests pour plus de détails'
        }

        always {
            echo '🧹 Nettoyage final'

            script {
                // Arrêter Appium si toujours en cours
                sh 'pkill -f appium || true'

                // Archiver les rapports de tests
                junit allowEmptyResults: true, testResults: '**/build/test-results/**/*.xml'

                // Archiver les rapports de tests Appium
                junit allowEmptyResults: true, testResults: '**/build/test-results/appiumTest/**/*.xml'

                // Archiver les rapports Lint
                publishHTML([
                    allowMissing: true,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'app/build/reports',
                    reportFiles: 'lint-results-debug.html',
                    reportName: 'Lint Report'
                ])

                // Archiver les rapports de tests
                publishHTML([
                    allowMissing: true,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'app/build/reports/tests',
                    reportFiles: 'appiumTest/index.html',
                    reportName: 'Appium Test Report'
                ])
            }
        }
    }
}
