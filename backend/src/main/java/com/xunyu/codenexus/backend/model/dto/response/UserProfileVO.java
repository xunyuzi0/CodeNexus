package com.xunyu.codenexus.backend.model.dto.response;

import lombok.Data;

import java.util.Date;

/**
 * 用户个人档案 VO (修复字段映射丢失问题，全量对齐底层 User 实体)
 */
@Data
public class UserProfileVO {
    private Long id;

    // === 修复区：与底层 User 实体及前端 api.ts 绝对对齐的字段名 ===
    private String username;   // 登录账号
    private String nickname;   // 用户昵称
    private String avatarUrl;  // 头像链接
    private String bio;        // 个人简介
    private String role;       // 角色权限
    private String phone;      // 找回丢失的电话号码字段
    private String email;      // 邮箱

    // === 积分与时间 ===
    private Integer ratingScore;
    private Integer globalRank;
    private Date createTime;

    // === 核心新增：天梯竞技数据 ===
    private Integer arenaScore;    // 排位分
    private Integer arenaMatches;  // 总场数
    private Integer arenaWins;     // 胜场数
    private Double winRate;        // 胜率
    private Integer solvedCount;   // 解题数

    // === 偏好设置对象 ===
    private UserPreferenceVO preferences;

    /**
     * 内部静态类：用户偏好设置 VO
     */
    @Data
    public static class UserPreferenceVO {
        private String editorTheme;
        private Integer fontSize;
        private Integer systemNotifications;
        private String extraSettings;
    }
}