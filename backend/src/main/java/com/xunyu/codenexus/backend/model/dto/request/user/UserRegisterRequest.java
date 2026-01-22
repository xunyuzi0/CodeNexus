package com.xunyu.codenexus.backend.model.dto.request.user;

import lombok.Data;

/**
 * 用户注册请求dto类
 *
 * @author xunyu
 */

@Data
public class UserRegisterRequest {
    private String userAccount;
    private String userPassword;
    private String checkPassword;
}