pipeline {
    agent any

    triggers {
        pollSCM('0 */4 * * 1-5')
    }

    stages {
        stage('Stage 1') {
            steps {
                echo 'Hello world!'
            }
        }
    }
}