package com.kerwin.user.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhengyibin
 * @date 2021/11/4 11:43
 */
@Configuration
@Slf4j
public class XxlJobConfig {
    @Value("${xxljob.addresses}")
    private String adminAddresses;

    @Value("${xxljob.appName}")
    private String appName;

    @Value("${xxljob.port}")
    private Integer port;

    @Value("${xxljob.accessToken}")
    private String accessToken;

    @Value("${xxljob.logPath}")
    private String logPath;

    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        log.info(">>>>>>>>>>> xxl-job config init.appName：{}",appName);
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(adminAddresses);
        xxlJobSpringExecutor.setAppname(appName);
        xxlJobSpringExecutor.setPort(port);
        xxlJobSpringExecutor.setAccessToken(accessToken);
        xxlJobSpringExecutor.setLogPath(logPath);
        return xxlJobSpringExecutor;
    }
}
