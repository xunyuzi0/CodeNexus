// src/main/java/com/xunyu/codenexus/backend/model/entity/Problem.java
package com.xunyu.codenexus.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.xunyu.codenexus.backend.model.dto.response.problem.ExampleVO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 题库基础信息表实体类
 *
 * @author CodeNexusBuilder
 */
@Data
// 开启 autoResultMap 以支持 JSON 字段的自动映射
@TableName(value = "problem", autoResultMap = true)
public class Problem {

    /**
     * 主键, 题目ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 展示用ID(如 P-1001)
     */
    private String displayId;

    /**
     * 题目标题
     */
    private String title;

    /**
     * 题目描述 (Markdown格式)
     */
    private String description;

    /**
     * 难度: 1-Easy, 2-Medium, 3-Hard
     */
    private Integer difficulty;

    /**
     * 标签列表 (JSON数组)
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    /**
     * 测试用例示例(JSON数组) - 映射为 ExampleVO
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<ExampleVO> examples;

    /**
     * 时间限制 (单位: ms)
     */
    private Integer timeLimit;

    /**
     * 空间限制 (单位: MB)
     */
    private Integer memoryLimit;

    /**
     * 总提交次数
     */
    private Integer submitNum;

    /**
     * 总通过次数
     */
    private Integer acceptedNum;

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