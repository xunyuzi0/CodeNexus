package com.xunyu.codenexus.backend.config.websocket;

import com.xunyu.codenexus.backend.engine.arena.ArenaWebSocketHandler;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 全局路由配置
 *
 * @author CodeNexusBuilder
 */
@Configuration
@EnableWebSocket // 开启 WebSocket 支持
public class WebSocketConfig implements WebSocketConfigurer {

    @Resource
    private ArenaWsAuthInterceptor arenaWsAuthInterceptor;

    @Resource
    private ArenaWebSocketHandler arenaWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(arenaWebSocketHandler, "/api/ws/arena/*") // 必须是 /*，不能写 {roomCode}
                .setAllowedOriginPatterns("*") // 确保允许跨域握手
                .addInterceptors(arenaWsAuthInterceptor);
    }
}