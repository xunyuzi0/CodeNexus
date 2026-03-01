package com.xunyu.codenexus.backend.model.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录成功后返回给前端的脱敏用户信息 VO
 *
 * @author CodeNexusBuilder
 */
@Data
public class UserLoginVO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 登录账号 (对应新 DDL 的 username)
     */
    private String username;

    /**
     * 用户昵称 (对应新 DDL 的 nickname)
     */
    private String nickname;

    /**
     * 头像URL (对应新 DDL 的 avatar_url)
     */
    private String avatarUrl;

    /**
     * 个人简介 (对应新 DDL 的 bio)
     */
    private String bio;

    /**
     * 角色权限 (对应新 DDL 的 role)
     */
    private String role;

    // ==========================================
    // 兼容旧版前端对接字段 (与 UserServiceImpl 对应)
    // ==========================================
    private String userAccount;
    private String userRole;

    /**
     * 鉴权 Token
     */
    private String token;

    /**
     * 注册时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}