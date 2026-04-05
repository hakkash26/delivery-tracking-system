pipeline {
    agent any

    environment {
        DOCKER_IMAGE     = 'delivery-tracking-system'
        DOCKER_TAG       = "${BUILD_NUMBER}"
        DOCKER_REGISTRY  = 'your-dockerhub-username'   // ← CHANGE THIS
        KUBECONFIG_CRED  = 'kubeconfig-credentials'
        K8S_NAMESPACE    = 'delivery-app'
    }

    tools {
        maven 'Maven-3.9'
        jdk   'JDK-17'
    }

    stages {

        stage('Checkout') {
            steps {
                echo '📥 Checking out source code...'
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                echo '🔨 Building project and running JUnit tests...'
                bat 'mvn clean test -B'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
                failure {
                    echo '❌ Tests failed! Stopping pipeline.'
                }
            }
        }

        stage('Package') {
            steps {
                echo '📦 Packaging JAR...'
                bat 'mvn package -DskipTests -B'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Docker Build') {
            steps {
                echo '🐳 Building Docker image...'
                bat """
                docker build -t %DOCKER_REGISTRY%/%DOCKER_IMAGE%:%DOCKER_TAG% .
                docker tag %DOCKER_REGISTRY%/%DOCKER_IMAGE%:%DOCKER_TAG% %DOCKER_REGISTRY%/%DOCKER_IMAGE%:latest
                """
            }
        }

        stage('Docker Push') {
            steps {
                echo '⬆️ Pushing Docker image to registry...'
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    bat """
                    echo %DOCKER_PASS% | docker login -u %DOCKER_USER% --password-stdin
                    docker push %DOCKER_REGISTRY%/%DOCKER_IMAGE%:%DOCKER_TAG%
                    docker push %DOCKER_REGISTRY%/%DOCKER_IMAGE%:latest
                    """
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                echo '☸️ Deploying to Kubernetes...'
                withCredentials([file(credentialsId: "${KUBECONFIG_CRED}", variable: 'KUBECONFIG')]) {
                    bat """
                    kubectl apply -f k8s/namespace.yaml
                    kubectl apply -f k8s/deployment.yaml
                    kubectl apply -f k8s/service.yaml

                    kubectl set image deployment/delivery-tracking delivery-tracking=%DOCKER_REGISTRY%/%DOCKER_IMAGE%:%DOCKER_TAG% -n %K8S_NAMESPACE%

                    kubectl rollout status deployment/delivery-tracking -n %K8S_NAMESPACE% --timeout=120s
                    """
                }
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline completed successfully!'
        }
        failure {
            echo '🔴 Pipeline FAILED. Check logs above.'
        }
        always {
            bat """
            docker rmi %DOCKER_REGISTRY%/%DOCKER_IMAGE%:%DOCKER_TAG% || exit 0
            docker rmi %DOCKER_REGISTRY%/%DOCKER_IMAGE%:latest || exit 0
            """
        }
    }
}
