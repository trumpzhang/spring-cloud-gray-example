package com.kerwin.gray.config;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.kerwin.gray.filter.GrayGatewayAfterFilter;
import com.kerwin.gray.filter.GrayGatewayBeginFilter;
import com.kerwin.gray.handler.GrayGatewayExceptionHandler;
import com.kerwin.gray.interceptor.GrayFeignRequestInterceptor;
import com.kerwin.gray.interceptor.GrayMvcHandlerInterceptor;
import com.kerwin.gray.properties.GrayGatewayProperties;
import com.kerwin.gray.properties.GrayVersionProperties;
import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Configuration
// 可以通过@ConditionalOnProperty设置是否开启灰度自动配置 默认是不加载的
@ConditionalOnProperty(value = "kerwin.tool.gray.load", havingValue = "true")
@EnableConfigurationProperties(GrayVersionProperties.class)
public class GrayAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(value = GlobalFilter.class)
    @EnableConfigurationProperties(GrayGatewayProperties.class)
    static class GrayGatewayFilterAutoConfiguration {
        @Bean
        public GrayGatewayBeginFilter grayGatewayBeginFilter() {
            return new GrayGatewayBeginFilter();
        }

        @Bean
        public GrayGatewayAfterFilter grayGatewayAfterFilter() {
            return new GrayGatewayAfterFilter();
        }

        @Bean
        public GrayGatewayExceptionHandler grayGatewayExceptionHandler() {
            return new GrayGatewayExceptionHandler();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(value = WebMvcConfigurer.class)
    static class GrayWebMvcAutoConfiguration {
        /**
         * Spring MVC 请求拦截器
         * 
         * @return WebMvcConfigurer
         */
        @Bean
        public WebMvcConfigurer webMvcConfigurer() {
            return new WebMvcConfigurer() {
                @Override
                public void addInterceptors(InterceptorRegistry registry) {
                    registry.addInterceptor(new GrayMvcHandlerInterceptor());
                }
            };
        }
    }

    @Configuration
    @ConditionalOnClass(value = RequestInterceptor.class)
    static class GrayFeignInterceptorAutoConfiguration {
        /**
         * Feign拦截器
         * 
         * @return GrayFeignRequestInterceptor
         */
        @Bean
        public GrayFeignRequestInterceptor grayFeignRequestInterceptor() {
            return new GrayFeignRequestInterceptor();
        }
    }

    @Configuration
    static class NacosMetadata {
        @Bean
        @ConditionalOnProperty(value = "spring.cloud.nacos.discovery.enabled", matchIfMissing = true)
        public NacosDiscoveryProperties nacosProperties() {
            NacosDiscoveryProperties nacosDiscoveryProperties = new NacosDiscoveryProperties();
            Map<String, String> metadata = nacosDiscoveryProperties.getMetadata();
            metadata.put("startup.time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            // 写入版本
            String version = System.getenv("version");
            if (version != null) {
                metadata.put("version", version);
            }
            return nacosDiscoveryProperties;
        }
    }
}
