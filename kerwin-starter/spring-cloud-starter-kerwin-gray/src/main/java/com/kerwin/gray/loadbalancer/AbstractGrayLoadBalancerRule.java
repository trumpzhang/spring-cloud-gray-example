package com.kerwin.gray.loadbalancer;

import com.alibaba.cloud.nacos.ribbon.NacosServer;
import com.kerwin.gray.constant.GrayConstant;
import com.kerwin.gray.enums.GrayStatusEnum;
import com.kerwin.gray.holder.GrayFlagRequestHolder;
import com.kerwin.gray.properties.GrayGatewayProperties;
import com.kerwin.gray.properties.GrayVersionProperties;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.kerwin.gray.enums.GrayStatusEnum.GRAY;


public abstract class AbstractGrayLoadBalancerRule extends AbstractLoadBalancerRule {
    @Autowired
    private GrayVersionProperties grayVersionProperties;

    /**
     * 只有已启动且可访问的服务器，并对灰度标识进行判断
     */
    public List<Server> getReachableServers() {
        ILoadBalancer lb = getLoadBalancer();
        if (lb == null) {
            return new ArrayList<>();
        }
        List<Server> reachableServers = lb.getReachableServers();

        return getGrayServers(reachableServers);
    }

    /**
     * 所有已知的服务器，可访问和不可访问，并对灰度标识进行判断
     */
    public List<Server> getAllServers() {
        ILoadBalancer lb = getLoadBalancer();
        if (lb == null) {
            return new ArrayList<>();
        }
        List<Server> allServers = lb.getAllServers();
        return getGrayServers(allServers);
    }

    /**
     * 获取灰度版本服务列表
     */
    protected List<Server> getGrayServers(List<Server> servers) {
        List<Server> result = new ArrayList<>();
        if (servers == null) {
            return result;
        }
        String currentVersion = "";
        GrayStatusEnum grayStatusEnum = GrayFlagRequestHolder.getGrayTag();
        if (grayStatusEnum != null && grayStatusEnum == GRAY) {
            currentVersion = grayVersionProperties.getGrayVersion();
        } else {
            return servers;
        }

        for (Server server : servers) {
            NacosServer nacosServer = (NacosServer) server;
            Map<String, String> metadata = nacosServer.getMetadata();
            String version = metadata.get("version");
            // 判断服务metadata下的version是否于设置的请求版本一致
            if (version != null && version.equals(currentVersion)) {
                result.add(server);
            }
        }
        // TODO 有可能无灰度实例
        return result;
    }
}
