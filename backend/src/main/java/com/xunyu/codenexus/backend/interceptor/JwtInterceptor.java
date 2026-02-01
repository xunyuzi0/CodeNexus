package com.xunyu.codenexus.backend.interceptor;

import com.xunyu.codenexus.backend.common.context.UserContext;
import com.xunyu.codenexus.backend.common.result.ResultCode;
import com.xunyu.codenexus.backend.utils.AssertUtil;
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

/**
 * JWT 身份拦截器
 *
 * @author xunyu
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final String TOKEN_HEADER = "Authorization";
    // 注意Bearer的后面有个空格,格式所需,必须保留
    private static final String TOKEN_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    /**
     * 构造器注入
     * Spring 会自动将容器中的 JwtUtil 实例传进来
     */
    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {

        // 放行 OPTIONS 请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 获取 Token
        String token = request.getHeader(TOKEN_HEADER);

        // 1. 校验 Token 格式并提取
        // 如果 token 为空或者不以 Bearer 开头，视为未登录
        boolean isValidFormat = StringUtils.hasText(token) && token.startsWith(TOKEN_PREFIX);

        // 使用 AssertUtil 断言：如果格式不对，抛出 401 未授权异常
        AssertUtil.isTrue(isValidFormat, ResultCode.UNAUTHORIZED, "未提供有效的身份令牌");

        // 截取 Token
        token = token.substring(TOKEN_PREFIX.length());

        // 2. 解析 Token
        Claims claims = null;
        try {
            // 调用实例方法 jwtUtil.parseToken
            claims = jwtUtil.parseToken(token);
        } catch (Exception e) {
            log.warn("JWT 解析失败: {}", e.getMessage());
            // 捕获解析过程中的异常（如过期、签名错误），统一返回 401
        }

        // 3. 断言 Token 有效性
        // 如果 claims 为 null，抛出 401 异常
        AssertUtil.notNull(claims, ResultCode.UNAUTHORIZED, "身份令牌无效或已过期");

        // 提取信息
        Long userId = claims != null ? claims.get("userId", Long.class) : null;
        String userRole = claims != null ? claims.get("userRole", String.class) : null;

        // 这里的 userId 为空属于 Token 数据损坏，也可以报 401
        AssertUtil.notNull(userId, ResultCode.UNAUTHORIZED, "无效的 Token 载荷");

        // 5. 存入上下文
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
}