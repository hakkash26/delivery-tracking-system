pipeline {
    agent any

    environment {
        DOCKER_IMAGE     = 'delivery-tracking-system'
        DOCKER_TAG       = "${BUILD_NUMBER}"
        DOCKER_REGISTRY  = 'your-dockerhub-username'   // ← CHANGE THIS
        KUBECONFIG_CRED  = 'kubeconfig-credentials'    // Jenkins credential ID
        K8S_NAMESPACE    = 'delivery-app'
    }

    tools {
        maven 'Maven-3.9'   // Must match name configured in Jenkins Global Tools
        jdk   'JDK-17'      // Must match name configured in Jenkins Global Tools
    }

    stages {

        // ── 1. Checkout ──────────────────────────────────────────────────────
        stage('Checkout') {
            steps {
                echo '📥 Checking out source code...'
                checkout scm
            }
        }

        // ── 2. Build & Unit Tests ────────────────────────────────────────────
        stage('Build & Test') {
            steps {
                echo '🔨 Building project and running JUnit tests...'
                sh 'mvn clean test -B'
            }
            post {
                always {
                    // Publish JUnit test results
                    junit '**/target/surefire-reports/*.xml'
                    // Publish test coverage (optional — requires jacoco plugin in pom)
                    // jacoco()
                }
                failure {
                    echo '❌ Tests failed! Stopping pipeline.'
                }
            }
        }

        // ── 3. Package ───────────────────────────────────────────────────────
        stage('Package') {
            steps {
                echo '📦 Packaging JAR...'
                sh 'mvn package -DskipTests -B'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        // ── 4. Docker Build ──────────────────────────────────────────────────
        stage('Docker Build') {
            steps {
                echo '🐳 Building Docker image...'
                sh """
                    docker build -t ${DOCKER_REGISTRY}/${DOCKER_IMAGE}:${DOCKER_TAG} .
                    docker tag  ${DOCKER_REGISTRY}/${DOCKER_IMAGE}:${DOCKER_TAG} \
                                ${DOCKER_REGISTRY}/${DOCKER_IMAGE}:latest
                """
            }
        }

        // ── 5. Docker Push ───────────────────────────────────────────────────
        stage('Docker Push') {
            steps {
                echo '⬆️  Pushing Docker image to registry...'
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh """
                        echo "${DOCKER_PASS}" | docker login -u "${DOCKER_USER}" --password-stdin
                        docker push ${DOCKER_REGISTRY}/${DOCKER_IMAGE}:${DOCKER_TAG}
                        docker push ${DOCKER_REGISTRY}/${DOCKER_IMAGE}:latest
                    """
                }
            }
        }

        // ── 6. Deploy to Kubernetes ───────────────────────────────────────────
        stage('Deploy to Kubernetes') {
            steps {
                echo '☸️  Deploying to Kubernetes...'
                withCredentials([file(credentialsId: "${KUBECONFIG_CRED}", variable: 'KUBECONFIG')]) {
                    sh """
                        kubectl apply -f k8s/namespace.yaml    --kubeconfig=$KUBECONFIG
                        kubectl apply -f k8s/deployment.yaml   --kubeconfig=$KUBECONFIG
                        kubectl apply -f k8s/service.yaml      --kubeconfig=$KUBECONFIG

                        # Update image to the current build
                        kubectl set image deployment/delivery-tracking \
                            delivery-tracking=${DOCKER_REGISTRY}/${DOCKER_IMAGE}:${DOCKER_TAG} \
                            -n ${K8S_NAMESPACE} --kubeconfig=$KUBECONFIG

                        # Wait for rollout
                        kubectl rollout status deployment/delivery-tracking \
                            -n ${K8S_NAMESPACE} --kubeconfig=$KUBECONFIG --timeout=120s
                    """
                }
            }
        }
    }

    // ── Post-pipeline actions ─────────────────────────────────────────────────
    post {
        success {
            echo '✅ Pipeline completed successfully!'
        }
        failure {
            echo '🔴 Pipeline FAILED. Check logs above.'
        }
        always {
            // Clean up local Docker images to save space
            sh "docker rmi ${DOCKER_REGISTRY}/${DOCKER_IMAGE}:${DOCKER_TAG} || true"
            sh "docker rmi ${DOCKER_REGISTRY}/${DOCKER_IMAGE}:latest || true"
        }
    }
}
