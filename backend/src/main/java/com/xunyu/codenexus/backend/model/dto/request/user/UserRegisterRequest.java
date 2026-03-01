package com.xunyu.codenexus.backend.model.dto.request.user;

import lombok.Data;

@Data
public class UserRegisterRequest {
    private String userAccount;
    private String userPassword;
    private String checkPassword;
}