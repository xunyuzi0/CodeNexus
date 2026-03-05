// src/main/java/com/xunyu/codenexus/backend/model/entity/UserProblemState.java
package com.xunyu.codenexus.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户题目状态表实体类 (记录用户解答进度)
 *
 * @author CodeNexusBuilder
 */
@Data
@TableName("user_problem_state")
public class UserProblemState {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 题目ID
     */
    private Long problemId;

    /**
     * 解答状态: 0-未开始(此状态通常不落库,查不到即为0), 1-尝试过未通过, 2-已通过(AC)
     */
    private Integer state;

    /**
     * 首次尝试时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 最后状态更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除(0-未删除, 1-已删除)
     */
    @TableLogic
    private Integer isDeleted;
}