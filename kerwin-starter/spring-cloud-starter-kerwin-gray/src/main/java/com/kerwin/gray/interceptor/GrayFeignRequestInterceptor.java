package com.kerwin.gray.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

/**
 * 灰度发布Feign请求拦截器
 * 用于透传灰度标记和Canary版本标记
 */
public class GrayFeignRequestInterceptor implements RequestInterceptor {
    // Canary版本请求头
    private static final String CANARY_VERSION_HEADER = "x-canary-version";

    @Override
    public void apply(RequestTemplate template) {
        // 检查当前请求中是否有x-canary-version请求头，如果有将灰度标记通过HttpHeader传递下去
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            // 检查并传递x-canary-version请求头
            String canaryVersion = request.getHeader(CANARY_VERSION_HEADER);
            if (canaryVersion != null && !canaryVersion.isEmpty()) {
                template.header(CANARY_VERSION_HEADER, canaryVersion);
            }
        }
    }

}