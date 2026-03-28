// src/main/java/com/xunyu/codenexus/backend/model/entity/ProblemTestcase.java
package com.xunyu.codenexus.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目测试用例表实体类 (沙箱评测点)
 */
@Data
@TableName("problem_testcase")
public class ProblemTestcase {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long problemId;
    private String inputData;
    private String expectedOutput;
    // 0-隐藏, 1-公开
    private Integer isPublic;
    private Integer sortOrder;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;
}