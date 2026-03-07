// src/main/java/com/xunyu/codenexus/backend/model/entity/UserStatistics.java
package com.xunyu.codenexus.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户统计扩展宽表实体类
 * 作用: 存储无需频繁修改但经常被查询的聚合数据（如打卡状态、解题总数）
 *
 * @author CodeNexusBuilder
 */
@Data
@TableName("user_statistics")
public class UserStatistics {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联用户ID
     */
    private Long userId;

    /**
     * 已解决(AC)的题目总数
     */
    private Integer solvedCount;

    /**
     * 当前连续打卡天数
     */
    private Integer continuousCheckinDays;

    /**
     * 最后一次打卡日期
     */
    private LocalDate lastCheckinDate;

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