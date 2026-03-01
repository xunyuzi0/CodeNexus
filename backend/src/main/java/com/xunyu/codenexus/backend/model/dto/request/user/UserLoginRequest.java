package com.xunyu.codenexus.backend.model.dto.request.user;

import lombok.Data;

@Data
public class UserLoginRequest {
    private String userAccount;
    private String userPassword;
}