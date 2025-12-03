pipeline {
    agent any

    triggers {
        pollSCM('H */4 * * 1-5')
    }

    environment {
        ANDROID_HOME = '/opt/android-sdk'
        PATH = "${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/emulator:${env.PATH}"
        AVD_NAME = 'TestEmulator'
        APPIUM_PORT = '4723'
    }

    stages {

        stage('Informations Environnement') {
            steps {
                echo '📋 Vérification environnement'
                sh '''
                    java -version
                    echo "Android SDK: ${ANDROID_HOME}"
                    avdmanager list avd || echo "Aucun émulateur"
                '''
            }
        }

        stage('Clean') {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew clean'
            }
        }

        stage('Build APK') {
            steps {
                sh './gradlew assembleDebug'
            }
        }

        stage('Tests Unitaires') {
            steps {
                sh './gradlew test --stacktrace'
            }
        }

        stage('Création Émulateur') {
            steps {
                script {
                    sh '''
                        if avdmanager list avd | grep -q "${AVD_NAME}"; then
                            echo "✅ Émulateur existe"
                        else
                            avdmanager create avd -n ${AVD_NAME} -k "system-images;android-34;google_apis;x86_64" -d "pixel_6" --force
                        fi
                    '''
                }
            }
        }

        stage('Démarrage Émulateur') {
            steps {
                script {
                    sh '''
                        pkill -9 emulator || true
                        sleep 3

                        emulator -avd ${AVD_NAME} -no-window -no-audio -no-boot-anim -gpu swiftshader_indirect -memory 2048 > /tmp/emulator.log 2>&1 &

                        timeout 120 adb wait-for-device
                        timeout 180 sh -c 'while [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d \\r)" != "1" ]; do sleep 2; done'
                        sleep 10

                        adb devices
                    '''
                }
            }
        }

        stage('Installation APK') {
            steps {
                sh '''
                    adb uninstall com.qos.latency.analyzer 2>/dev/null || true
                    adb install -r app/build/outputs/apk/debug/app-debug.apk
                '''
            }
        }

        stage('Démarrage Appium') {
            steps {
                sh '''
                    pkill -f appium || true
                    sleep 2
                    nohup appium server --address 127.0.0.1 --port ${APPIUM_PORT} --log /tmp/appium.log --use-drivers uiautomator2 --relaxed-security &
                    sleep 15
                '''
            }
        }

        stage('Tests Appium') {
            steps {
                sh '''
                    ./gradlew appiumTest -Dappium.server=http://127.0.0.1:4723 --stacktrace --info
                '''
            }
        }

        stage('Arrêt Appium') {
            steps {
                sh 'pkill -f appium || true'
            }
        }

        stage('Tests Instrumentés') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    sh './gradlew connectedAndroidTest --stacktrace'
                }
            }
        }

        stage('Arrêt Émulateur') {
            steps {
                sh '''
                    adb emu kill || true
                    pkill -9 emulator || true
                '''
            }
        }

        stage('Analyse Lint') {
            steps {
                sh './gradlew lint'
            }
        }

        stage('Archive APK') {
            steps {
                archiveArtifacts artifacts: '**/build/outputs/apk/debug/*.apk'
            }
        }
    }

    post {
        always {
            sh '''
                adb emu kill || true
                pkill -9 emulator || true
                pkill -f appium || true
            ''' || true

            junit allowEmptyResults: true, testResults: '**/build/test-results/**/*.xml'
        }
    }
}