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
    }

    stages {

        stage('Informations Environement') {
            steps {
                echo 'Vérification de l/environment'
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

        stage('Préparation Device pour Appium') {
            steps {
                echo '📱 Connexion au téléphone pour Appium'

                script {
                    sh '''
                        echo "Connexion au téléphone ${PHONE_IP}..."
                        adb connect ${PHONE_IP}:5555 || true
                        sleep 3

                        echo ""
                        echo "=== APPAREILS CONNECTÉS ==="
                        adb devices -l

                        DEVICE_COUNT=$(adb devices | grep -w "device" | wc -l)
                        if [ $DEVICE_COUNT -eq 0 ]; then
                            echo "ERREUR: Aucun appareil détecté"
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

                echo 'Démarrage du serveur Appium'

                script {
                    sh '''
                        echo "Arrêt de tout processus Appium existant..."
                        pkill -f appium || true
                        sleep 2

                        echo "Démarrage d'Appium sur le port ${APPIUM_PORT}..."
                        nohup appium server \
                            --address 127.0.0.1 \
                            --port ${APPIUM_PORT} \
                            --log /tmp/appium.log \
                            --log-level info \
                            --use-drivers uiautomator2 \
                            --relaxed-security &

                        echo "Attente du démarrage d'Appium..."
                        sleep 10

                        echo "Appium démarré"

                        echo ""
                        echo "=== LOGS APPIUM (10 premières lignes) ==="
                        head -n 10 /tmp/appium.log || echo "Pas encore de logs"
                    '''
                }
            }
        }

        stage('Tests Appium') {
            steps {
                echo '📱 Test Appium avec visualisation (105 secondes)'

                script {
                    catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                        sh '''
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
            steps {
                echo '📱 Test Espresso avec visualisation (105 secondes)'

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
                echo 'Analyse Lint Android'
                sh './gradlew lint'
            }
        }

        stage('Archive APK') {
            steps {
                echo 'Archivage de APK'
                archiveArtifacts artifacts: '**/build/outputs/apk/debug/*.apk', allowEmptyArchive: false, fingerprint: true
            }
        }

    }

    post {
        success {
            echo 'Build réussi'
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
            echo 'La pipeline a échoué'
            echo 'Consultez les logs ci-dessus pour identifier les erreurs'
        }

        always {
            echo 'Nettoyage final'

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
        }
    }
}