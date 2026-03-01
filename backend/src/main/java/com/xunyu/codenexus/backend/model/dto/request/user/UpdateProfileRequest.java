package com.xunyu.codenexus.backend.model.dto.request.user;

import lombok.Data;

/**
 * 更新基础档案信息入参 DTO
 */
@Data
public class UpdateProfileRequest {
    private String nickname;
    private String avatarUrl;
    private String bio;
    private String email;
    private String phone;
}