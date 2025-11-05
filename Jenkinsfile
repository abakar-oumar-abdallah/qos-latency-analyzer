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

        stage('Checkout') {
            steps {
                echo 'Récupération du code depuis Bitbucket'
                checkout scm
            }
        }

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
                sh './gradlew test'
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
                reportDir: 'app/build/reports/lint-results-debug.html',
                reportFiles: 'lint-results-debug.html',
                reportName: 'Lint Report'
            ])
        }
    }
}