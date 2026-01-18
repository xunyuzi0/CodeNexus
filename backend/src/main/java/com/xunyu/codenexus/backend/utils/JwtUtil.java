package com.xunyu.codenexus.backend.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JWT 工具类
 * 作用：生成和解析 Token
 *
 * @author xunyu
 */
@Slf4j
@Component
public class JwtUtil {

    /**
     * 从 application.properties 中注入密钥
     */
    @Value("${codenexus.jwt.secret}")
    private String secretString;

    /**
     * 从 application.properties 中注入过期时间
     */
    @Value("${codenexus.jwt.expiration}")
    private Long expireTime;

    /**
     * 真正的密钥对象
     */
    private SecretKey secretKey;

    /**
     * 初始化方法：在 Bean 创建且属性注入完成后自动执行
     */
    @PostConstruct
    public void init() {
        log.info("正在初始化 JWT 工具类，加载密钥...");
        // 将字符串转换成 HMAC-SHA 算法需要的 Key 对象
        this.secretKey = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT Token
     *
     * @param userId   用户ID
     * @param userRole 用户角色
     * @return Token 字符串
     */
    public String createToken(Long userId, String userRole) {

        // 创建并设置载荷信息
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userRole", userRole);

        // 生成 Token
        return Jwts.builder()
                // 设置载荷
                .setClaims(claims)
                // 设置主题
                .setSubject("CodeNexus-User-Auth")
                // 设置签发时间,这里使用new Date()是因为jjwt较老,兼顾兼容性
                .setIssuedAt(new Date())
                // 设置过期时间
                .setExpiration(new Date(System.currentTimeMillis() + expireTime))
                // 设置唯一标识
                .setId(UUID.randomUUID().toString())
                // 设置密钥
                .signWith(secretKey)
                // 构建并压缩成字符串
                .compact();
    }

    /**
     * 解析 Token
     *
     * @param token Token 字符串
     * @return Claims 载荷
     */
    // 忽略S1168警告信息注解
    @SuppressWarnings("java:S1168")
    public Claims parseToken(String token) {
        if (!StringUtils.hasLength(token)) {
            // 创建一个实现了 Claims 接口的空对象并返回
            // 这样调用处就不会因为空指针异常而崩溃
            return Jwts.claims();
        }
        try {
            JwtParser parser = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build();
            return parser.parseClaimsJws(token).getBody();
        } catch (Exception e) {
            // 解析失败（过期、签名不对、格式错误等）
            // 在这里我们直接返回null,虽然不符合规范,但是在这里是必要的
            // 因为若是返回空值的对象,调用处可能会认为解析成功,只是没有数据
            return null;
        }
    }
}