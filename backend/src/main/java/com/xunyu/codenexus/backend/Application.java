// src/main/java/com/xunyu/codenexus/backend/Application.java
package com.xunyu.codenexus.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * CodeNexus 后端启动类
 *
 * @author xunyu
 */
@SpringBootApplication
@EnableScheduling // [新增] 开启 Spring Boot 定时任务调度功能
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}