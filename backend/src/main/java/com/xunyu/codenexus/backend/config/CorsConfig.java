package com.xunyu.codenexus.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置类
 * 必须加上 @Configuration 注解才能生效
 *
 * @author xunyu
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 覆盖所有请求路径
        // 所有的接口都开放
        registry.addMapping("/**")
                // 允许发送 Cookie
                .allowCredentials(true)
                // 允许所有域名
                .allowedOriginPatterns("*")
                // 允许的方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许所有的请求头
                .allowedHeaders("*")
                // 允许所有的响应头
                .exposedHeaders("*")
                // 3600 秒内，不需要再发送预检验请求，可以缓存该结果
                .maxAge(3600);
    }
}