package com.xunyu.codenexus.backend.model.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录成功后返回给前端的脱敏用户信息
 *
 * @author xunyu
 */
@Data
public class UserLoginVO {
    private Long id;
    private String userAccount;
    private String userName;
    private String userAvatar;
    private String userProfile;
    private String userRole;
    private String token;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}