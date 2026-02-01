package com.xunyu.codenexus.backend.config;

import com.xunyu.codenexus.backend.interceptor.JwtInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 拦截器配置类
 * 配置JWT拦截器具体要拦截的路径和排除的路径,让拦截器真正生效
 *
 * @author xunyu
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {


    /**
     * 构造器注入JWT拦截器
     * 构造器注入的好处是可以确保依赖在对象创建时就被注入，避免了空指针异常的问题
     * 比起字段注入,也就是直接在字段上使用 @Autowired 注解,构造器注入更有利于单元测试
     * 因为可以通过构造器参数传入模拟对象，从而更容易地进行依赖注入和隔离测试
     */
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