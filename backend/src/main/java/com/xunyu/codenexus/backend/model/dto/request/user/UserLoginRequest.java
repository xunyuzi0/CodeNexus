package com.xunyu.codenexus.backend.model.dto.request.user;

import lombok.Data;

/**
 * 用户登录请求dto类
 *
 * @author xunyu
 */

@Data
public class UserLoginRequest {
    private String userAccount;
    private String userPassword;
}