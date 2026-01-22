package com.xunyu.codenexus.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.xunyu.codenexus.backend.model.enums.UserRoleEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author xunyu
 * @date 2026/1/11 19:53
 * @description: 用户表实体类
 */
@TableName(value = "user")
@Data
public class User {

    /**
     * id
     * 使用 AUTO 自增策略，对应 MySQL 的 auto_increment
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色
     * 使用枚举映射：数据库存 "user"/"admin"，Java代码直接用枚举
     */
    private UserRoleEnum userRole;

    /**
     * 创建时间
     * 对应数据库 datetime 类型
     * fill = FieldFill.INSERT，插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     * fill = FieldFill.INSERT_UPDATE，插入和更新时自动填充
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 是否删除
     * 逻辑删除：0-未删除，1-已删除
     * 调用 deleteById 时，MyBatis Plus 会自动把该字段更新为 1
     */
    @TableLogic
    private Integer isDelete;
}