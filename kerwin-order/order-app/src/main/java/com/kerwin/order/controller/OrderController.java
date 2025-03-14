package com.kerwin.order.controller;

import com.kerwin.common.api.ApiResult;
import com.kerwin.common.constants.Utils;
import com.kerwin.user.client.UserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author: kerwin
 * @CreateTime: 2023-07-15 08:51
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private UserClient userClient;

    @Value("${server.port}")
    private String port;

    @GetMapping("/{orderNo}")
    public ApiResult getOrderInfo(@PathVariable("orderNo") String orderNo, HttpServletRequest request) {
        // 打印请求头
        System.out.println("请求头信息：");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            System.out.println(headerName + ": " + headerValue);
        }

        Map<String, String> resultData = initData().get(orderNo);
        resultData.put("port", port);
        resultData.put("metaVersion", Utils.CURRENT_VERSION());
        if (resultData != null) {
            ApiResult<String> apiResult = userClient.getUserName(resultData.get("userNo"));
            resultData.put("userName", apiResult.getData());
        }
        return ApiResult.success(resultData);
    }

    private Map<String, Map<String, String>> initData() {
        Map<String, Map<String, String>> orderMap = new HashMap<>();
        Map<String, String> orderInfo1 = new LinkedHashMap<>();
        orderInfo1.put("orderNo", "N0001");
        orderInfo1.put("goodsName", "商品-G0001");
        orderInfo1.put("userNo", "U0001");
        orderMap.put("N0001", orderInfo1);
        Map<String, String> orderInfo2 = new LinkedHashMap<>();
        orderInfo2.put("orderNo", "N0002");
        orderInfo2.put("goodsName", "商品-G0002");
        orderInfo2.put("userNo", "U0002");
        orderMap.put("N0002", orderInfo2);
        return orderMap;
    }
}
