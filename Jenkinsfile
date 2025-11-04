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

    }
}