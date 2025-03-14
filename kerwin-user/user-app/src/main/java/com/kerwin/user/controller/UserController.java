package com.kerwin.user.controller;

import com.kerwin.common.api.ApiResult;
import com.kerwin.common.constants.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: kerwin
 * @CreateTime: 2023-07-15 08:57
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Value("${server.port}")
    private String port;

    private static final Map<String, String> userMap = new HashMap();
    static {
        userMap.put("U0001", "用户1号");
        userMap.put("U0002", "用户2号");
        userMap.put("U0003", "用户3号");
    }

    @GetMapping("/{userNo}")
    public ApiResult<String> getUserName(@PathVariable("userNo") String userNo, HttpServletRequest request) {
        // 打印请求头
        System.out.println("请求头信息：");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            System.out.println(headerName + ": " + headerValue);
        }

        return ApiResult.success(userMap.get(userNo) + " - port=" + port + " - mateVersion=" + Utils.CURRENT_VERSION());
    }

    @GetMapping("/cpu/stress")
    public ApiResult<String> cpuStress() {
        long startTime = System.currentTimeMillis();
        // 执行密集计算
        double result = 0;
        for (int i = 0; i < 100000; i++) {
            result += Math.sqrt(Math.pow(i, 3)) * Math.sin(i) * Math.cos(i);
            for (int j = 0; j < 100; j++) {
                result += Math.sqrt(Math.pow(j, 2)) * Math.sin(j) * Math.cos(j);
            }
        }
        long endTime = System.currentTimeMillis();
        return ApiResult.success("CPU压力测试完成，耗时: " + (endTime - startTime) + "ms, 计算结果: " + result);
    }
}
