package com.xunyu.codenexus.backend.model.dto.request.user;

import lombok.Data;

/**
 * 更新用户偏好设置入参 DTO
 */
@Data
public class UpdatePreferenceRequest {
    private String editorTheme;
    private Integer fontSize;
    private Integer systemNotifications;
    private String extraSettings;
}
