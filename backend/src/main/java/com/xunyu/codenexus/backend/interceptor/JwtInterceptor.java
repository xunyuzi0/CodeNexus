package com.xunyu.codenexus.backend.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xunyu.codenexus.backend.common.context.UserContext;
import com.xunyu.codenexus.backend.common.result.Result;
import com.xunyu.codenexus.backend.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * JWT 身份拦截器
 *
 * @author xunyu
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final String TOKEN_HEADER = "Authorization";
    // 注意Bearer的后面有个空格,且必须保留
    private static final String TOKEN_PREFIX = "Bearer ";

    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;

    /**
     * 构造器注入
     * Spring 会自动将容器中的 ObjectMapper 和 JwtUtil 实例传进来
     */
    public JwtInterceptor(ObjectMapper objectMapper, JwtUtil jwtUtil) {
        this.objectMapper = objectMapper;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {

        // 放行 OPTIONS 请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 获取 Token
        String token = request.getHeader(TOKEN_HEADER);

        // 处理 Bearer 前缀标准
        if (StringUtils.hasText(token) && token.startsWith(TOKEN_PREFIX)) {
            token = token.substring(TOKEN_PREFIX.length());
        } else {
            // 如果没有 Bearer 前缀或者为空，视为未登录
            token = null;
        }

        if (!StringUtils.hasText(token)) {
            return returnJsonError(response, "未提供身份令牌 (Token)，请先登录");
        }

        Claims claims = null;
        try {
            // 改动点：调用实例方法 jwtUtil.parseToken
            claims = jwtUtil.parseToken(token);
        } catch (Exception e) {
            log.warn("JWT 解析失败: {}", e.getMessage());
            // 捕获解析过程中的异常（如过期、签名错误），统一返回 401
        }

        if (claims == null) {
            return returnJsonError(response, "身份令牌无效或已过期，请重新登录");
        }

        // 提取信息
        Long userId = claims.get("userId", Long.class);
        String userRole = claims.get("userRole", String.class);

        if (userId == null) {
            return returnJsonError(response, "无效的 Token 载荷");
        }

        // 存入上下文
        UserContext.set(userId, userRole);

        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                @Nullable Exception ex) {
        // 清理 ThreadLocal
        UserContext.remove();
    }

    private boolean returnJsonError(HttpServletResponse response, String msg) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        Result<?> errorResult = Result.error(401, msg);
        try (PrintWriter writer = response.getWriter()) {
            writer.print(objectMapper.writeValueAsString(errorResult));
        }
        return false;
    }
}