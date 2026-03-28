// src/main/java/com/xunyu/codenexus/backend/model/entity/ProblemSubmission.java
package com.xunyu.codenexus.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户题目提交记录表实体类
 *
 * @author CodeNexusBuilder
 */
@Data
@TableName("problem_submission")
public class ProblemSubmission {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 提交用户ID
     */
    private Long userId;

    /**
     * 关联题目ID
     */
    private Long problemId;

    /**
     * 编程语言(如 java, cpp, typescript)
     */
    private String language;

    /**
     * 提交的完整代码
     */
    private String code;

    /**
     * 评测状态: 0-PENDING, 1-AC, 2-WA, 3-TLE, 4-MLE, 5-RE, 6-CE
     */
    private Integer status;

    /**
     * 运行耗时(单位: ms)
     */
    private Integer timeCost;

    /**
     * 内存消耗(单位: MB)
     */
    private Double memoryCost;

    /**
     * 创建时间(即提交时间)
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