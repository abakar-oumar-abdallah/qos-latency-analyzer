pipeline {
    agent any
    triggers {
        pollSCM('H */4 * * 1-5')
    }
    stages {
        stage('Stage 1') {
            steps {
                echo 'Hello world!'
                echo 'ABAKAR Oumar Abdallah'
            }
        }
    }
}