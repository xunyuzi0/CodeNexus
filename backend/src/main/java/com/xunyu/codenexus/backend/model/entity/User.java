package com.xunyu.codenexus.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户核心基础表实体类
 *
 * @author CodeNexusBuilder
 */
@Data
@TableName("user")
public class User {

    /**
     * 主键, 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 登录账号
     */
    private String username;

    /**
     * 密码哈希值 (绝对禁止明文)
     */
    private String passwordHash;

    /**
     * 密码加密盐值
     */
    private String salt;

    /**
     * 角色权限 (USER/ADMIN)
     */
    private String role;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 绑定邮箱
     */
    private String email;

    /**
     * 绑定手机号
     */
    private String phone;

    /**
     * 全站全球排名
     */
    private Integer globalRank;

    /**
     * 对战胜率 (例如 88 代表 88%)
     */
    private Integer winRate;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除(0-未删除, 1-已删除)
     */
    @TableLogic
    private Integer isDeleted;
}