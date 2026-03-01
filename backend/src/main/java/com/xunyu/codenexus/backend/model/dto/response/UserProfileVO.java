package com.xunyu.codenexus.backend.model.dto.response;

import lombok.Data;

/**
 * 用户完整档案出参 VO
 */
@Data
public class UserProfileVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String bio;
    private String email;
    private String phone;
    private String role;

    /**
     * 用户的全站全球排名
     */
    private Integer globalRank;

    /**
     * 用户的对战胜率
     */
    private Integer winRate;

    /**
     * 偏好设置对象
     */
    private UserPreferenceVO preferences;

    @Data
    public static class UserPreferenceVO {
        private String editorTheme;
        private Integer fontSize;
        private Integer systemNotifications;
        private Object extraSettings; // JSON字符串将转为Object供前端直接使用
    }
}