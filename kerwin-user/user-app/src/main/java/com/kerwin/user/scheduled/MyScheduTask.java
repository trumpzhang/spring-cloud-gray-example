package com.kerwin.user.scheduled;

import com.kerwin.user.utils.XXlJobUtils;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @Author: zhangzhiyu
 * @Date: 2025/3/12 13:34
 */

@Slf4j
@Component
public class MyScheduTask {

    @XxlJob("firstQuery")
    public void firstQuery() {
        try {
            log.info("firstQuery定时任务开始执行");
            String now = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            now = XXlJobUtils.getParam("now", String.class, now);
            log.info("firstQuery获取到传参：{}", now);

        } catch (Exception e) {
            log.error("firstQuery定时任务执行异常", e);
        }
    }
}
