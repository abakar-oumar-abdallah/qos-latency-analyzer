pipeline {
    agent any

    triggers {
        pollSCM('H */4 * * 1-5')
    }

    environment {
        ANDROID_HOME = '/opt/android-sdk'
        PATH = "${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${env.PATH}"
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
                echo 'Lancement des tests sur émulateur'
                script {
                    try {
                        // 1. Lancer l'émulateur en arrière-plan
                        sh '''
                            nohup emulator -avd test_emulator -no-window -no-audio -gpu swiftshader_indirect > emulator.log 2>&1 &
                            echo $! > emulator.pid
                        '''

                        // 2. Attendre que l'émulateur soit prêt (max 5 min)
                        timeout(time: 5, unit: 'MINUTES') {
                            sh '''
                                adb wait-for-device
                                while [ "$(adb shell getprop sys.boot_completed | tr -d '\\r')" != "1" ]; do
                                    sleep 5
                                done
                                echo "Émulateur prêt"
                            '''
                        }

                        // 3. Exécuter les tests
                        sh './gradlew connectedAndroidTest'

                    } finally {
                        // 4. Arrêter l'émulateur (toujours exécuté)
                        sh '''
                            adb emu kill || true
                            pkill -9 emulator || true
                            rm -f emulator.pid emulator.log
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