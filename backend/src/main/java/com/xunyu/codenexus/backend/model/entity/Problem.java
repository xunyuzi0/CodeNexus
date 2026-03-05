// src/main/java/com/xunyu/codenexus/backend/model/entity/Problem.java
package com.xunyu.codenexus.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
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
     * 使用 JacksonTypeHandler 自动在 JSON 字符串和 List<String> 之间转换
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

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