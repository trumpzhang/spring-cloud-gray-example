package com.kerwin.order;

import com.sun.management.OperatingSystemMXBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/**
 * @author Kerwin
 * @date 2023/7/14
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "com.kerwin.*.client")
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class);

        // 获取当前JVM进程的Xmx
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        long maxHeapSize = memoryMXBean.getHeapMemoryUsage().getMax() / 1024 / 1024;
        // 获取当前主机的物理总内存
        OperatingSystemMXBean osBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        // 获取总物理内存（字节）
        long totalPhysicalMemorySize = osBean.getTotalPhysicalMemorySize();

        // 转换单位
        long totalMemoryGB = totalPhysicalMemorySize / 1024 / 1024 / 1024;
        long totalMemoryMB = totalPhysicalMemorySize / 1024 / 1024;

        System.out.println("maxHeapSize(MB)(Xmx): " + maxHeapSize);
        System.out.println("主机总内存: " + totalMemoryGB + " GB (" + totalMemoryMB + " MB)");

    }
}
