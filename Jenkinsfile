pipeline {
    agent any
    
    environment {
        DOCKER_REGISTRY = "registry.example.com"
        IMAGE_NAME = "spring-service"
        KUBE_CONFIG = credentials('kubeconfig')
    }

    
    stages {
        stage('Clone') {
            git url: "https://github.com/willemswang/jenkins-demo.git"
            script {
                IMAGE_TAG = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }
        
        stage('Docker Build') {
            steps {
                sh "docker build -t ${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG} ."
            }
        }
        
        stage('Docker Push') {
            steps {
                sh "docker push ${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}"
            }
        }
        
        /* 测试环境直接滚动 */
        stage('Deploy to K8s') {
            steps {
                sh "kubectl --kubeconfig=${KUBE_CONFIG} set image deployment/spring-service spring-service=${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG} -n spring-apps"
            }
        }

        /* 生产环境金丝雀方式 */
        stage('Update Yaml & Deploy') {
            steps {
                sh "sed -i 's/\${TAG}/${IMAGE_TAG}/g' k8s.yaml"
                // sh "sed -i 's//${env.BRANCH_NAME}/' k8s.yaml"
                sh "kubectl apply -f k8s.yaml"
            }
        }

        /* 自动更新DestinationRule，加入新版本说明 */
        /* 自动更新VirtureService，更新灰度版本标识 destination.subset */
        /* 研发手动切量 */

    }
    
    post {
        failure {
            echo 'Pipeline failed'
        }
    }
}