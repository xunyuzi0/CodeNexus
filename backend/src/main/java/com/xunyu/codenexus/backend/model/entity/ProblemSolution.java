// src/main/java/com/xunyu/codenexus/backend/model/entity/ProblemSolution.java
package com.xunyu.codenexus.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目官方题解表实体类
 *
 * @author CodeNexusBuilder
 */
@Data
@TableName("problem_solution")
public class ProblemSolution {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联题目ID
     */
    private Long problemId;

    /**
     * 题解作者ID (关联 user 表)
     */
    private Long authorId;

    /**
     * 题解标题
     */
    private String title;

    /**
     * 题解思路(富文本/Markdown)
     */
    private String content;

    /**
     * 阅读量
     */
    private Integer viewCount;

    // 新增：解题代码
    private String code;

    /**
     * 是否官方题解(0-否 1-是)
     */
    private Integer isOfficial;

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