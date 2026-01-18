package com.xunyu.codenexus.backend.config;

import com.xunyu.codenexus.backend.interceptor.JwtInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 拦截器配置类
 *
 * @author xunyu
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    public InterceptorConfig(JwtInterceptor jwtInterceptor) {
        this.jwtInterceptor = jwtInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                // 拦截所有路径
                .addPathPatterns("/**")
                // 白名单 (路径不需要包含 context-path)
                .excludePathPatterns(
                        "/user/login", "/user/register", "/doc.html",
                        "/webjars/**", "/v3/api-docs/**",
                        "/swagger-resources/**");
    }
}