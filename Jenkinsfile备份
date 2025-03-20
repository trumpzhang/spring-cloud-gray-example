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

        stage('Update DestinationRule') {
            steps {
                echo "更新Istio DestinationRule添加新版本子集: ${env.COMMIT_ID}"
                script {
                    // 检查子集是否存在
                    def subsetExists = sh(
                            script: "kubectl get destinationrule order -o jsonpath='{.spec.subsets[?(@.name==\"${env.COMMIT_ID}\")]}' | grep -q '${env.COMMIT_ID}'",
                            returnStatus: true
                    ) == 0

                    if (!subsetExists) {
                        // 准备patch内容
                        def jsonContent = """
                        [
                          {
                            "op": "add",
                            "path": "/spec/subsets/-",
                            "value": {
                              "name": "${env.COMMIT_ID}",
                              "labels": {
                                "version": "${env.COMMIT_ID}"
                              }
                            }
                          }
                        ]
                        """

                        sh "echo '${jsonContent}' > patch-dr.json"
                        sh "cat patch-dr.json"
                        sh "kubectl patch destinationrule order --type=json --patch-file=patch-dr.json"
                        echo "DestinationRule已更新，新版本子集 ${env.COMMIT_ID} 已添加"
                    } else {
                        echo "子集版本 ${env.COMMIT_ID} 已存在，无需更新"
                    }
                }
            }
        }

        stage('Update VirtualService') {
            steps {
                echo "更新Istio VirtualService添加新版本路由，默认权重0: ${env.COMMIT_ID}"

                script {
                    // 检查VirtualService中是否已存在该版本的路由
                    def routeExists = sh(
                            script: "kubectl get virtualservice order -o jsonpath='{.spec.http[0].route[?(@.destination.subset==\"${env.COMMIT_ID}\")]}' | grep -q '${env.COMMIT_ID}'",
                            returnStatus: true
                    ) == 0

                    if (!routeExists) {
                        // 准备JSON Patch内容
                        def jsonContent = """
                        [
                          {
                            "op": "add",
                            "path": "/spec/http/0/route/-",
                            "value": {
                              "destination": {
                                "host": "order",
                                "subset": "${env.COMMIT_ID}"
                              },
                              "weight": 0
                            }
                          }
                        ]
                        """

                        sh "echo '${jsonContent}' > patch-vs.json"
                        sh "cat patch-vs.json"
                        sh "kubectl patch vs order --type=json --patch-file=patch-vs.json"

                        echo "VirtualService已更新，添加了新版本 ${env.COMMIT_ID} 的路由，权重为0"
                    } else {
                        echo "VirtualService中已存在版本 ${env.COMMIT_ID} 的路由，无需更新"
                    }
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