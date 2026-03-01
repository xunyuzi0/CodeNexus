package com.xunyu.codenexus.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户登录日志实体类
 *
 * @author CodeNexusBuilder
 */
@Data
@TableName("user_login_log")
public class UserLoginLog {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 登录的用户ID
     */
    private Long userId;

    /**
     * 登录IP地址
     */
    private String loginIp;

    /**
     * 登录设备/User-Agent
     */
    private String loginDevice;

    /**
     * 登录状态(1-成功, 0-失败)
     */
    private Integer loginStatus;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * 登录时间(即创建时间)
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