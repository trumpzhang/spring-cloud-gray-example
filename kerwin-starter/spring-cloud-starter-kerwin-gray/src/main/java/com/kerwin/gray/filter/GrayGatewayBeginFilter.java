package com.kerwin.gray.filter;

import com.kerwin.gray.constant.GrayConstant;
import com.kerwin.gray.enums.GrayStatusEnum;
import com.kerwin.gray.holder.GrayFlagRequestHolder;
import com.kerwin.gray.properties.GrayGatewayProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;


public class GrayGatewayBeginFilter implements GlobalFilter, Ordered {
    @Autowired
    private GrayGatewayProperties grayGatewayProperties;
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        /*GrayStatusEnum grayStatusEnum = GrayStatusEnum.ALL;
        // 当灰度开关打开时才进行请求头判断
        if (grayGatewayProperties.getEnabled()) {
            grayStatusEnum = GrayStatusEnum.PROD;
            // 判断是否需要调用灰度版本
            if (checkGray(exchange.getRequest())) {
                grayStatusEnum = GrayStatusEnum.GRAY;
            }
        }*/
        // 开发/测试自己测试，已经携带灰度标记
        if (checkGray(exchange.getRequest())) {
            GrayStatusEnum grayStatusEnum = GrayStatusEnum.GRAY;
            GrayFlagRequestHolder.setGrayTag(GrayStatusEnum.GRAY);
            ServerHttpRequest newRequest = exchange.getRequest().mutate()
                    .header(GrayConstant.GRAY_HEADER, grayStatusEnum.getVal())
                    .build();
            ServerWebExchange newExchange = exchange.mutate()
                    .request(newRequest)
                    .build();
            return chain.filter(newExchange);
        }
        return chain.filter(exchange);
    }

    /**
     * 校验是否使用灰度版本
     */
    private boolean checkGray(ServerHttpRequest request) {
        if (checkGrayHeadKey(request)) {
            return true;
        }
        return false;
    }

    /**
     * 校验自定义灰度版本请求头判断是否需要调用灰度版本
     */
    private boolean checkGrayHeadKey(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        if (headers.containsKey(grayGatewayProperties.getGrayHeadKey())) {
            List<String> grayValues = headers.get(grayGatewayProperties.getGrayHeadKey());
            if (!Objects.isNull(grayValues)
                    && grayValues.size() > 0
                    && grayGatewayProperties.getGrayHeadValue().equals(grayValues.get(0))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        // 设置过滤器的执行顺序，值越小越先执行
        return Ordered.HIGHEST_PRECEDENCE;
    }
}