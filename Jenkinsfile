pipeline {
    agent any

    triggers {
        pollSCM('H */4 * * 1-5')
    }

    environment {
        ANDROID_HOME = '/opt/android-sdk'
        PATH = "${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${env.PATH}"
        PHONE_IP = "192.168.1.109"
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

        stage('Tests Instrumentés') {
            steps {
                echo '========================================='
                echo 'Tests Instrumentés sur Téléphone'
                echo '========================================='
                script {
                    try {
                        // Connexion au téléphone
                        sh '''
                            echo "🔌 Connexion au téléphone ${PHONE_IP}..."
                            adb connect ${PHONE_IP}:5555 || true
                            sleep 3

                            echo ""
                            echo "=== APPAREILS CONNECTÉS ==="
                            adb devices -l

                            # Vérifier qu'au moins un appareil est détecté
                            DEVICE_COUNT=$(adb devices | grep -w "device" | wc -l)

                            if [ $DEVICE_COUNT -eq 0 ]; then
                                echo ""
                                echo "ERREUR: Aucun appareil détecté"
                                echo ""
                                echo "Vérifications à faire :"
                                echo "1. Le téléphone est-il sur le même WiFi que le serveur ?"
                                echo "2. ADB WiFi activé ? Commande : adb tcpip 5555"
                                echo "3. Connexion établie ? Commande : adb connect ${PHONE_IP}:5555"
                                echo "4. IP correcte dans le Jenkinsfile : ${PHONE_IP}"
                                exit 1
                            fi

                            echo "${DEVICE_COUNT} appareil(s) connecté(s)"

                            echo ""
                            echo "=== INFORMATIONS APPAREIL ==="
                            echo "Modèle     : $(adb shell getprop ro.product.model)"
                            echo "Fabricant  : $(adb shell getprop ro.product.manufacturer)"
                            echo "Android    : $(adb shell getprop ro.build.version.release)"
                            echo "API Level  : $(adb shell getprop ro.build.version.sdk)"
                        '''

                        echo ''
                        echo 'Lancement des tests instrumentés...'
                        sh './gradlew connectedAndroidTest --stacktrace'

                        echo ''
                        echo 'Tests instrumentés terminés avec succès !'

                    } catch (Exception e) {
                        echo "ERREUR lors des tests: ${e.message}"

                        sh '''
                            echo ""
                            echo "=== DEBUG - État de la connexion ==="
                            adb devices -l
                        '''

                        throw e
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