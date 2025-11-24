pipeline {
    agent any

    options {
        timeout(time: 1, unit: 'HOURS')
        timestamps()
    }

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

        stage('Préparation Device pour Appium') {
            steps {
                echo 'Préparation du téléphone et des données de test'

                script {
                    sh '''
                        echo "========================================="
                        echo "CONNEXION AU TÉLÉPHONE"
                        echo "========================================="

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
                        echo "========================================="
                        echo "INSTALLATION APPLICATION"
                        echo "========================================="

                        echo "Désinstallation de l'ancienne version..."
                        adb uninstall com.qos.latency.analyzer 2>/dev/null || echo "   → Pas d'ancienne version (OK)"

                        echo ""
                        echo "Installation de la nouvelle APK..."
                        APK_PATH="app/build/outputs/apk/debug/app-debug.apk"

                        if [ ! -f "$APK_PATH" ]; then
                            echo "ERREUR: APK non trouvé à $APK_PATH"
                            exit 1
                        fi

                        adb install -r "$APK_PATH"
                        echo "APK installé avec succès"

                        echo ""
                        echo "========================================="
                        echo "ACCORD DES PERMISSIONS"
                        echo "========================================="

                        echo "Accord des permissions de stockage..."
                        adb shell pm grant com.qos.latency.analyzer android.permission.READ_EXTERNAL_STORAGE 2>/dev/null || echo "   → Permission non applicable (OK)"
                        adb shell pm grant com.qos.latency.analyzer android.permission.WRITE_EXTERNAL_STORAGE 2>/dev/null || echo "   → Permission non applicable (OK)"
                        echo "Permissions accordées"

                        echo ""
                        echo "========================================="
                        echo "COPIE AUTOMATIQUE DES ASSETS"
                        echo "========================================="

                        echo "Démarrage de l'application..."
                        adb shell am start -n com.qos.latency.analyzer/.MainActivity

                        echo "Attente 5 secondes (copie automatique des fichiers depuis assets)..."
                        sleep 5

                        echo "Arrêt de l'application..."
                        adb shell am force-stop com.qos.latency.analyzer

                        echo ""
                        echo "========================================="
                        echo "VÉRIFICATION FICHIERS COPIÉS"
                        echo "========================================="

                        echo "Contenu de /storage/emulated/0/QoS_Data/ :"
                        adb shell ls -lh /storage/emulated/0/QoS_Data/ 2>/dev/null || {
                            echo "ERREUR: Dossier /storage/emulated/0/QoS_Data/ introuvable"
                            echo ""
                            echo "=== LOGS APPLICATION ==="
                            adb logcat -d | grep "QoS_MainActivity" | tail -30
                            exit 1
                        }

                        FILE_COUNT=$(adb shell ls /storage/emulated/0/QoS_Data/*.json 2>/dev/null | wc -l)

                        echo ""
                        if [ "$FILE_COUNT" -gt 0 ]; then
                            echo "$FILE_COUNT fichier(s) JSON disponible(s)"
                            echo ""
                            echo "Liste des fichiers :"
                            adb shell ls /storage/emulated/0/QoS_Data/*.json 2>/dev/null | while read line; do
                                echo "   → $(basename $line)"
                            done
                        else
                            echo "ERREUR: Aucun fichier JSON trouvé dans /storage/emulated/0/QoS_Data/"
                            echo ""
                            echo "=== DEBUG - LOGS APPLICATION ==="
                            adb logcat -d | grep "QoS_MainActivity" | tail -30
                            exit 1
                        fi

                        echo ""
                        echo "========================================="
                        echo "PRÉPARATION TERMINÉE AVEC SUCCÈS"
                        echo "========================================="
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
            options {
                timeout(time: 20, unit: 'MINUTES')
            }

            steps {
                echo '📱 Exécution des tests Appium'

                script {
                    catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                        sh '''
                            echo "Lancement des tests Appium..."
                            echo "Les tests peuvent prendre 10-15 minutes (animations réelles)"
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
            options {
                timeout(time: 20, unit: 'MINUTES')
            }

            steps {
                echo 'Tests Instrumentés sur Téléphone'

                script {
                    try {
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
                            adb uninstall com.qos.latency.analyzer 2>/dev/null || echo "   → Pas d'ancienne version (OK)"
                        '''

                        echo ''
                        echo 'Lancement des tests instrumentés...'
                        echo 'Les tests peuvent prendre 10-15 minutes (animations réelles)'

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
                echo 'Archivage de l\'APK'
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