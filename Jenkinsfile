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
                        echo "UDID       : $(adb devices | grep -w "device" | awk '{print $1}' | head -n 1)"

                        echo ""
                        echo "Désinstallation de l'ancienne version..."
                        adb uninstall com.qos.latency.analyzer 2>/dev/null || echo "   Pas d'ancienne version (OK)"

                        echo ""
                        echo "Préparation des fichiers de test sur le téléphone..."

                        # Créer le répertoire sur le téléphone (CORRIGÉ: bon chemin + QoS_Data)
                        adb shell mkdir -p /storage/emulated/0/Android/data/com.qos.latency.analyzer/files/QoS_Data/

                        # Copier les fichiers JSON depuis les assets du projet vers le téléphone (CORRIGÉ: bon chemin)
                        echo "Copie de test_data.json..."
                        adb push app/src/main/assets/test_data.json /storage/emulated/0/Android/data/com.qos.latency.analyzer/files/QoS_Data/test_data.json

                        echo "Copie de data_high_variable_latency.json..."
                        adb push app/src/main/assets/data_high_variable_latency.json /storage/emulated/0/Android/data/com.qos.latency.analyzer/files/QoS_Data/high_variable_latency.json

                        echo "Copie de new_data.json..."
                        adb push app/src/main/assets/new_data.json /storage/emulated/0/Android/data/com.qos.latency.analyzer/files/QoS_Data/new_data.json

                        echo ""
                        echo "Vérification des fichiers copiés :"
                        adb shell ls -la /storage/emulated/0/Android/data/com.qos.latency.analyzer/files/QoS_Data/
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

                echo '📱 Exécution des tests Appium'

                script {
                    catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                        sh '''
                            echo "Lancement des tests Appium..."
                            ./gradlew appiumTest \
                                -Dappium.server=http://127.0.0.1:${APPIUM_PORT} \
                                --stacktrace \
                                --info
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
            steps {

                echo 'Tests Instrumentés sur Téléphone'

                script {
                    try {
                        // Connexion au téléphone
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
                        '''

                        echo ''
                        echo 'Lancement des tests instrumentés...'

                        // Utiliser catchError pour ne pas faire échouer le build
                        catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                            sh './gradlew connectedAndroidTest --stacktrace'
                        }

                        echo ''
                        echo 'Tests instrumentés terminés'

                    } catch (Exception e) {
                        echo "Erreur lors des tests: ${e.message}"

                        sh '''
                            echo ""
                            echo "=== DEBUG ==="
                            adb devices -l
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
            echo 'Consultez les logs ci-dessus pour identier les erreurs qui ont fait échoué la pipeline'
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