# SpringCloud服务迁移到Kubernetes环境指南

## 1. 迁移准备
- 1.1 评估现有SpringCloud架构
  - 服务依赖关系梳理
  - 配置中心使用情况
  - 服务注册与发现机制
  - 负载均衡策略
  - 熔断降级机制
- 1.2 K8s环境准备
  - K8s集群搭建/接入说明
  - 命名空间规划
  - 资源限制规划
- 1.3 技术栈评估与选型
  - 容器化工具选择(Docker/Podman等)
  - 服务网格考虑(Istio/Linkerd等)
  - CI/CD流水线规划
  - 监控与日志方案

## 2. 应用容器化
- 2.1 编写Dockerfile
  - 基础镜像选择
  - 多阶段构建优化
- 2.2 健康检查与生命周期管理
  - 就绪探针(Readiness Probe)配置
  - 存活探针(Liveness Probe)配置
- 2.3 资源需求评估
  - CPU/内存需求分析

## 3. SpringCloud组件替代方案
- 3.1 服务发现与负载均衡
  - Eureka/Nacos替换为K8s Service
  
  改造前：
    ```java
    @FeignClient(name = "user")
    public interface UserClient {
        @GetMapping("/user/{userNo}")
        ApiResult getUserName(@PathVariable("userNo") String userNo);
    }
    ```
  改造后（添加url，服务域名是下游服务的Service名，端口是Pod暴露的端口）：
    ```java
    @FeignClient(name = "user", url = "http://user:8080")
    public interface UserClient {
        @GetMapping("/user/{userNo}")
        ApiResult getUserName(@PathVariable("userNo") String userNo);
    }
    ```
    修改后，请求将使用K8s集群的Service资源实现服务发现与负载均衡

- 3.2 熔断与限流
  - 考虑取消Hystrix/Sentinel限流、熔断策略
  - 基于服务网格实现流量控制

## 4. K8s资源编排
- 4.1 Deployment配置
  - 副本数设置
  - 更新策略(RollingUpdate)
  - Pod亲和性/反亲和性配置
- 4.2 Service配置
  - 服务暴露类型选择（基本都是ClusterIP）
  - 端口映射

## 5. 网络配置检查
- 5.1 服务间通信
  - DNS解析策略
- 5.2 外部服务访问
  - 外部服务对接
- 5.3 网络策略
  - 微服务间访问控制
  - 外部访问控制

## 6. 可观测性建设
- 6.1 日志收集
  - 容器日志收集方案
  - EFK/ELK方案集成
- 6.2 监控指标
  - Prometheus集成
  - 自定义指标暴露
- 6.3 链路追踪
  - Skywalking/Jaeger集成

## 7. CI/CD流水线构建
- 7.1 持续集成
  - 代码构建
  - 镜像构建与推送
- 7.2 持续部署
  - 金丝雀部署策略
  - 回滚机制

## 8. 性能优化
- 8.1 资源配置优化
  - 资源请求与限制调优
  - HPA自动扩缩容配置
- 8.2 JVM调优
  - 容器环境JVM参数优化
  - 内存限制考量
- 8.3 启动时间优化
  - Spring应用启动优化
  - 镜像精简

## 9. 迁移路线与策略
- 9.1 渐进式迁移策略
  - 双模式并行运行
- 9.2 切换计划
  - 流量切换策略
  - 回滚预案
- 9.3 团队技能培养
  - K8s技能培训
  - DevOps文化建设

## 附录
- A. K8s常用命令参考
- B. 常见问题(FAQ)
- C. 配置模板示例
  - 1 Dockerfile模板
  - 2 Kubernetes资源配置模板
  - 3 CI/CD配置模板
- D. 参考资源与工具

## C. 配置模板示例

### 1. Dockerfile模板

#### 1.1 Java应用Dockerfile基本示例

```dockerfile
# 基础镜像选择
FROM openjdk:8-jre-alpine
# 工作目录
WORKDIR /opt
# 添加应用jar包
COPY target/*.jar app.jar
# 暴露端口
EXPOSE 8080
# 设置JVM参数
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs/"
# 设置时区
ENV TZ=Asia/Shanghai \
    LANG=en_US.UTF-8

# 启动命令
ENTRYPOINT java $JAVA_OPTS -jar app.jar
```

#### 1.2 Dockerfile最佳实践

1. **使用适当的基础镜像**：
   - 生产环境推荐使用slim版本减小镜像体积
   - 考虑使用官方的Eclipse Temurin或Amazon Corretto等JDK镜像
   - 考虑使用Alpine基础镜像（注意JVM兼容性问题）

2. **优化JVM参数**：
   - 启用容器感知（`-XX:+UseContainerSupport`）
   - 使用百分比设置内存（`-XX:MaxRAMPercentage=75.0`）,使用-Xms512m -Xmx1024m不是很优雅
   - 选择适合容器环境的GC（如G1GC）

3. **安全性考虑**：
   - 使用非root用户运行应用
   - 移除不必要的工具和库
   
4. **其他最佳实践**：
   - 添加健康检查
   - 设置适当的时区
   - 使用LABEL添加元数据（维护者、版本等）

### 2. Kubernetes资源配置模板

#### 2.1 Spring Boot Actuator配置

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true
  health:
    livenessState:
      enabled: true
    readinessState:
      enabled: true
```

#### 2.2 Deployment配置模板

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: spring-service
  namespace: spring-apps
  labels:
    app: spring-service
    version: ${TAG}
spec:
  replicas: 2
  selector:
    matchLabels:
      app: spring-service
      version: ${TAG}
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  template:
    metadata:
      labels:
        app: spring-service
        version: ${TAG}
    spec:
      containers:
      - name: spring-service
        image: registry.example.com/spring-service:${TAG}
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 8080
          name: http
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: TZ
          value: "Asia/Shanghai"
        - name: version
          value: "${TAG}"
        resources:
          requests:
            memory: "512Mi"
            cpu: "200m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        # 就绪探针配置
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 3
          failureThreshold: 3
        # 存活探针配置
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 20
          timeoutSeconds: 3
          failureThreshold: 3
        # 启动探针配置
        startupProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 15
          periodSeconds: 10
          timeoutSeconds: 3
          failureThreshold: 12
        volumeMounts:
        - name: config-volume
          mountPath: /opt/config
      volumes:
      - name: config-volume
        configMap:
          name: spring-service-config
```
关于resources中request、limit设置的最佳实践，参考腾讯云提供的一个设置规则：

在启动时设置JAVA_OPTS = -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0

```sh
Request >= 1.25 * JVM 最大堆内存 ；
Limit >= 2 * JVM 最大堆内存；
```

#### 2.3 Service配置模板

```yaml
apiVersion: v1
kind: Service
metadata:
  name: spring-service
  namespace: spring-apps
spec:
  selector:
    app: spring-service
  type: ClusterIP
  ports:
  - port: 80
    targetPort: 8080
    protocol: TCP
    name: http
```
### 3. Istio资源配置模板

#### Gateway:
```yaml
apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: order-gateway
  namespace: default
spec:
  selector:
    istio: ingressgateway  # 确保与 Istio Ingress Gateway 的标签匹配
  servers:
    - port:
        number: 80
        name: http
        protocol: HTTP
      hosts:
        - "*"  # 替换为你希望访问的域名
```

#### DestinationRule:
```yaml
apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: order
spec:
  host: "*"
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
```

#### VirtureService:
```yaml
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: order-virtualservice
  namespace: default
spec:
  hosts:
    - "*"  # 与 Gateway 中的 hosts 一致
  gateways:
    - order-gateway  # 引用 Gateway 的名称
  http:
    - match:
      - headers:
          uid:
            exact: "1"
      route:
      - destination:
            host: order  # 目标 Service 的名称
            subset: v2
    - route:
      - destination:
          host: order
          subset: v1
        weight: 100
      - destination:
          host: order
          subset: v2
        weight: 0
        headers:
          request:
            set:
              x-canary-version: "true"
  
```

### 4. CI/CD配置模板

#### 4.1 Jenkins Pipeline示例

```groovy
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
        
        stage('Docker Build & Push') {
            steps {
                sh "docker build -t ${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG} ."
                sh "docker push ${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}"
            }
        }
        
        /* 测试环境直接滚动 */
        stage('Deploy to K8s') {
            steps {
                sh "kubectl --kubeconfig=${KUBE_CONFIG} set image deployment/spring-service spring-service=${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG} -n test"
            }
        }

        /* 生产环境金丝雀方式，创建新的Deployment */
        stage('Create Deployment') {
            steps {
                sh "sed -i 's/\${TAG}/${IMAGE_TAG}/g' Deployment.yaml"
                // sh "sed -i 's//${env.BRANCH_NAME}/' Deployment.yaml"
                sh "kubectl apply -f Deployment.yaml"
            }
        }
        
        /* 创建新的DestinationRule标识新版本 */
        stage('Create New DestinationRule') {
            steps {
                sh "sed -i 's/\${TAG}/${IMAGE_TAG}/g' dest-rule.yaml"
                sh "kubectl apply -f dest-rule.yaml"
            }
        }
        
        /* 更新VirtureService，更新灰度版本标识 destination.subset */
        stage('Update VirtureService') {
            steps {
                sh "sed -i 's/\${TAG}/${IMAGE_TAG}/g' VirtureService.yaml"
                sh "kubectl apply -f VirtureService.yaml"
            }
        }
        
        /* 研发在kiali进行流量染色和手动切量 */

    }
    
    post {
        failure {
            echo 'Pipeline failed'
        }
    }
}
```
