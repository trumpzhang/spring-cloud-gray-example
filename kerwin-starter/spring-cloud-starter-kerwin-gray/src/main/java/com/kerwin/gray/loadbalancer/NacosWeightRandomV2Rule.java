package com.kerwin.gray.loadbalancer;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.ribbon.NacosServer;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.kerwin.gray.enums.GrayStatusEnum;
import com.kerwin.gray.holder.GrayFlagRequestHolder;
import com.kerwin.gray.properties.GrayVersionProperties;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.DynamicServerListLoadBalancer;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.kerwin.gray.enums.GrayStatusEnum.GRAY;

/**
 * @Author: zhangzhiyu
 * @Date: 2025/3/3 11:28
 */
@Slf4j
public class NacosWeightRandomV2Rule extends AbstractGrayLoadBalancerRule {

    private AtomicInteger nextServerCyclicCounter;
    @Autowired
    private NacosDiscoveryProperties discoveryProperties;

    @Autowired
    private GrayVersionProperties grayVersionProperties;

    public NacosWeightRandomV2Rule() {
        nextServerCyclicCounter = new AtomicInteger(0);
    }

    @Override
    public Server choose(Object key) {
        GrayStatusEnum grayStatusEnum = GrayFlagRequestHolder.getGrayTag();
        // 灰度请求(测试环境携带/网关自动染色后选择下一跳)
        if (grayStatusEnum != null && grayStatusEnum == GRAY) {
            return chooseGrayPreference(key);
        } else {
            // 尚未染色，正常请求
            return chooseInsByWeight(key);
        }
    }

    public Server chooseInsByWeight(Object key) {
        DynamicServerListLoadBalancer loadBalancer = (DynamicServerListLoadBalancer) getLoadBalancer();
        String name = loadBalancer.getName();
        try {
            Instance instance = discoveryProperties.namingServiceInstance()
                    .selectOneHealthyInstance(name);
            // 对流量进行染色
            // TODO 需要配置好不同微服务的灰度版本
            String version = instance.getMetadata().get("version");
            if (version.equals(grayVersionProperties.getGrayVersion())) {
                GrayFlagRequestHolder.setGrayTag(GrayStatusEnum.GRAY);
            }
            return new NacosServer(instance);
        } catch (NacosException e) {
            log.info("异常", e);
            return null;
        }
    }

    /*
    * 只选择灰度版本
    * */
    public Server chooseGrayPreference(Object key) {
        ILoadBalancer lb = getLoadBalancer();
        if (lb == null) {
            log.warn("no load balancer");
            return null;
        }

        Server server = null;
        int count = 0;
        while (server == null && count++ < 10) {
            List<Server> reachableServers = getReachableServers();
            List<Server> allServers = getAllServers();
            int upCount = reachableServers.size();
            int serverCount = allServers.size();

            if ((upCount == 0) || (serverCount == 0)) {
                log.warn("No up servers available from load balancer: " + lb);
                return null;
            }
            int nextServerIndex = incrementAndGetModulo(serverCount);
            // select a server from allServers
            server = allServers.get(nextServerIndex);
            if (server == null) {
                /* Transient. */
                Thread.yield();
                continue;
            }
            if (server.isAlive() && (server.isReadyToServe())) {
                return (server);
            }
            // Next.
            server = null;
        }
        if (count >= 10) {
            log.warn("No available alive servers after 10 tries from load balancer: "
                    + lb);
        }
        return server;
    }

    private int incrementAndGetModulo(int modulo) {
        for (;;) {
            int current = nextServerCyclicCounter.get();
            int next = (current + 1) % modulo;
            if (nextServerCyclicCounter.compareAndSet(current, next)) {
                return next;
            }
        }
    }

    @Override
    public void initWithNiwsConfig(IClientConfig iClientConfig) {}
}
