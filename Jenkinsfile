pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = ""
        IMAGE_NAME = "cloud-demo-order"
    }


    stages {
        stage('Build') {
            steps {
                echo "开始打包阶段"
                sh '''
                #!/bin/bash
                source /etc/profile
                pwd && ls -la
                mvn -v
                # mvn clean package -DskipTests -s $PWD/settings.xml
                '''

                script {
                    env.COMMIT_ID = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
                    echo "当前 Commit ID: ${env.COMMIT_ID}"
                }
            }

        }

        stage('Build Docker Image') {
            steps {
                echo "开始构建镜像阶段"
                dir('./kerwin-order/order-app') {
                    sh 'ls -la'
                    sh "docker build -t ${IMAGE_NAME}:${env.COMMIT_ID} ."
                }
            }
        }

        stage('Create New Deployment') {
            steps {
                echo "开始创建新的Deployment"
                dir('./kerwin-order/order-app') {
                    sh "sed -i 's/\${version}/${env.COMMIT_ID}/g' deploy.yaml"
                    echo '修改后的deploy.yaml内容：'
                    sh 'cat deploy.yaml'
                    sh 'kubectl apply -f deploy.yaml'
                }
            }
        }

    }

    post {
        failure {
            echo 'Pipeline failed'
        }
    }
}