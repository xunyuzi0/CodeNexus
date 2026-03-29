package com.xunyu.codenexus.backend.config.websocket;

import com.xunyu.codenexus.backend.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * 竞技场 WebSocket 握手鉴权拦截器
 * 作用：在建立 WS 长连接前，拦截提取 URL 中的 token 并验证身份。
 *
 * @author CodeNexusBuilder
 */
@Slf4j
@Component
public class ArenaWsAuthInterceptor implements HandshakeInterceptor {

    @Resource
    private JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
                                   @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) {

        // 1. 使用 Spring 原生的 UriComponentsBuilder 优雅提取 ?token=xxxx 参数
        MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams();
        String token = queryParams.getFirst("token");

        if (!StringUtils.hasText(token)) {
            log.warn("[WS鉴权] 拒绝连接: token为空");
            return false;
        }

        // 2. 校验 Token 并提取 userId
        try {
            Claims claims = jwtUtil.parseToken(token);
            if (claims == null) {
                log.warn("[WS鉴权] 拒绝连接: token解析失败或已过期");
                return false;
            }

            Long userId = claims.get("userId", Long.class);
            if (userId == null) {
                return false;
            }

            // 3. 将 userId 塞入 attributes 中，传递给 WebSocketSession
            attributes.put("userId", userId);
            return true;

        } catch (Exception e) {
            log.error("[WS鉴权] 拒绝连接: 系统验证异常", e);
            return false;
        }
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
                               @NonNull WebSocketHandler wsHandler, Exception exception) {
        // 握手后的收尾工作，暂不需要处理
    }
}