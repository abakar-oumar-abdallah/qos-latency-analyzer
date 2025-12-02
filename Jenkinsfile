pipeline {
    agent any

    environment {
        ANDROID_HOME = '/opt/android-sdk'
        PATH = "${env.PATH}:${ANDROID_HOME}/tools:${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/cmdline-tools/latest/bin"
        GRADLE_OPTS = '-Dorg.gradle.daemon=false -Dorg.gradle.jvmargs="-Xmx2048m -XX:MaxMetaspaceSize=512m"'
    }

    stages {
        stage('Checkout SCM') {
            steps {
                script {
                    echo "Récupération du code source"
                    checkout scm
                }
            }
        }

        stage('Informations Environnement') {
            steps {
                script {
                    echo "=== INFORMATIONS ENVIRONNEMENT ==="
                    sh '''
                        echo "Java Version:"
                        java -version
                        echo "\nGradle Version:"
                        ./gradlew --version
                        echo "\nAndroid SDK Path:"
                        echo $ANDROID_HOME
                        echo "\nADB Devices:"
                        adb devices -l
                    '''
                }
            }
        }

        stage('Clean') {
            steps {
                script {
                    echo "Nettoyage du projet"
                    sh './gradlew clean'
                }
            }
        }

        stage('Build application android') {
            steps {
                script {
                    echo "Compilation de l'application Android"
                    sh './gradlew assembleDebug'
                }
            }
        }

        stage('Tests Unitaires') {
            steps {
                script {
                    echo "Exécution des tests unitaires"
                    sh './gradlew test'
                }
            }
        }

        stage('Préparation Device pour Appium') {
            steps {
                script {
                    echo "=== PRÉPARATION DEVICE POUR APPIUM ==="

                    // Connexion ADB
                    sh '''
                        echo "📱 Connexion ADB au device..."
                        adb connect 192.168.1.109:5555 || true
                        sleep 2
                        adb devices -l
                    '''

                    // Vérification du device
                    sh '''
                        echo "✅ Vérification du device..."
                        DEVICE_COUNT=$(adb devices | grep -v "List" | grep "device" | wc -l)
                        if [ "$DEVICE_COUNT" -eq "0" ]; then
                            echo "❌ ERREUR: Aucun device connecté"
                            exit 1
                        fi
                        echo "✅ Device connecté: $(adb devices | grep device | head -1)"
                    '''

                    // Désinstallation ancienne version
                    sh '''
                        echo "🗑️ Désinstallation ancienne version..."
                        adb uninstall com.qos.latency.analyzer || echo "App non installée"
                        sleep 1
                    '''

                    // Installation nouvelle version
                    sh '''
                        echo "📦 Installation nouvelle version..."
                        adb install -r app/build/outputs/apk/debug/app-debug.apk
                        if [ $? -ne 0 ]; then
                            echo "❌ ERREUR: Installation APK échouée"
                            exit 1
                        fi
                        echo "✅ APK installé avec succès"
                    '''

                    echo "✅ Préparation terminée - L'app lira les fichiers depuis les assets"
                }
            }
        }

        stage('Tests Instrumentés') {
            steps {
                script {
                    echo "Exécution des tests instrumentés Espresso"
                    sh '''
                        ./gradlew connectedDebugAndroidTest \
                            -Pandroid.testInstrumentationRunnerArguments.class=com.qos.latency.analyzer.CompleteFlowTest#testCompleteExecutionFlowWithVisualization \
                            --stacktrace --info
                    '''
                }
            }
        }

        stage('Démarrage Appium Server') {
            steps {
                script {
                    echo "Démarrage du serveur Appium"
                    sh '''
                        # Vérifier si Appium tourne déjà
                        if pgrep -f "appium" > /dev/null; then
                            echo "Appium est déjà en cours d'exécution"
                            pkill -f "appium" || true
                            sleep 2
                        fi

                        # Démarrer Appium en arrière-plan
                        nohup appium --address 127.0.0.1 --port 4723 > /tmp/appium.log 2>&1 &
                        APPIUM_PID=$!
                        echo "Appium PID: $APPIUM_PID"

                        # Attendre qu'Appium soit prêt
                        echo "Attente démarrage Appium..."
                        for i in {1..30}; do
                            if curl -s http://127.0.0.1:4723/status > /dev/null 2>&1; then
                                echo "✅ Appium Server prêt"
                                break
                            fi
                            echo "Attente... ($i/30)"
                            sleep 2
                        done

                        # Vérifier si Appium répond
                        if ! curl -s http://127.0.0.1:4723/status > /dev/null 2>&1; then
                            echo "❌ ERREUR: Appium Server ne répond pas"
                            cat /tmp/appium.log
                            exit 1
                        fi
                    '''
                }
            }
        }

        stage('Tests Appium') {
            steps {
                script {
                    echo "Exécution des tests Appium"
                    sh '''
                        ./gradlew appiumTest \
                            -Dappium.server=http://127.0.0.1:4723 \
                            -Pandroid.testInstrumentationRunnerArguments.class=com.qos.latency.analyzer.appium.CompleteFlowAppiumTest#testCompleteExecutionFlowWithVisualization \
                            --stacktrace --info
                    '''
                }
            }
        }

        stage('Arrêt Appium Server') {
            steps {
                script {
                    echo "Arrêt du serveur Appium"
                    sh '''
                        pkill -f "appium" || true
                        echo "✅ Appium Server arrêté"
                    '''
                }
            }
        }

        stage('Analyse Lint') {
            steps {
                script {
                    echo "Analyse Lint Android"
                    sh './gradlew lint'
                }
            }
        }

        stage('Archive APK') {
            steps {
                script {
                    echo "Archivage de APK"
                    archiveArtifacts artifacts: 'app/build/outputs/apk/debug/*.apk', fingerprint: true
                }
            }
        }
    }

    post {
        always {
            script {
                echo "Nettoyage final"

                // Arrêter Appium s'il tourne encore
                sh 'pkill -f "appium" || true'

                // Publier les résultats des tests
                junit allowEmptyResults: true, testResults: '**/build/test-results/**/*.xml'

                // Publier le rapport Lint
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
        success {
            script {
                echo "Build réussi"
                echo "APK disponible dans les artifacts"
                def apkPath = 'app/build/outputs/apk/debug/app-debug.apk'
                if (fileExists(apkPath)) {
                    def apkFile = new File(apkPath)
                    echo "Nom du fichier: ${apkFile.name}"
                }

                // Statistiques des tests
                def testResultAction = currentBuild.rawBuild.getAction(hudson.tasks.junit.TestResultAction.class)
                if (testResultAction != null) {
                    def total = testResultAction.totalCount
                    def failed = testResultAction.failCount
                    def skipped = testResultAction.skipCount
                    def passed = total - failed - skipped

                    echo "Tests exécutés : ${total}"
                    echo "Tests réussis : ${passed}"
                    echo "Tests échoués : ${failed}"
                }
            }
        }
        failure {
            script {
                echo "Build échoué"
                echo "Consultez les logs pour plus de détails"
            }
        }
        unstable {
            script {
                echo "Build instable"
                echo "Certains tests ont échoué"
            }
        }
    }
}