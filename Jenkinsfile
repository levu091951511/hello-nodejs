pipeline {
    agent any

    stages {
        stage('Clone Git Repo') {
            steps {
                git url: 'https://github.com/levu091951511/hello-nodejs.git', branch: 'master'
            }
        }

        stage('Build image') {
            steps {
                withDockerRegistry(credentialsId: 'docker-hub', url: 'https://index.docker.io/v1/') {
                    sh 'docker build -t lqvu/pipeline-test:1.0.0 .'
                    sh 'docker push lqvu/pipeline-test:1.0.0'
                }
            }
        }
    }
}
