package com.xunyu.codenexus.backend.model.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改密码入参 DTO
 */
@Data
public class UpdatePasswordRequest {
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}