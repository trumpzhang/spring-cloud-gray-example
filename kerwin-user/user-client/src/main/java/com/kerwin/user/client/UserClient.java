package com.kerwin.user.client;

import com.kerwin.common.api.ApiResult;
import com.kerwin.common.constants.ServiceNames;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Author: kerwin
 * @CreateTime: 2023-07-15  09:01
 */
@FeignClient(name = "${spring.application.name}", url = "http://user:7200")
public interface UserClient {
    @GetMapping("/user/{userNo}")
    ApiResult getUserName(@PathVariable("userNo") String userNo);
}
