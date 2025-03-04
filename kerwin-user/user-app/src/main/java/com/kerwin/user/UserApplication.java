package com.kerwin.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Kerwin
 * @date 2023/7/14
 */
@SpringBootApplication
//@ComponentScan({ "com.kerwin.user", "com.kerwin.gray" })
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class);
    }
}
