// src/main/java/com/xunyu/codenexus/backend/model/entity/UserActivityLog.java
package com.xunyu.codenexus.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户活跃度日志表实体类
 *
 * @author CodeNexusBuilder
 */
@Data
@TableName("user_activity_log")
public class UserActivityLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private LocalDate activityDate;

    /**
     * 当日去重后的 AC 题目总数 (原 submissionCount)
     */
    private Integer acCount;

    /**
     * 当日是否已打卡 (0-否, 1-是)
     */
    private Integer isCheckin;

    private Integer scoreChange;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}