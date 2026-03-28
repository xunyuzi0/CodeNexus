// src/main/java/com/xunyu/codenexus/backend/model/dto/response/problem/ProblemDetailVO.java
package com.xunyu.codenexus.backend.model.dto.response.problem;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目详情出参 VO
 *
 * @author CodeNexusBuilder
 */
@Data
public class ProblemDetailVO {

    /**
     * 物理主键 ID
     */
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
     * 难度: 1-Easy, 2-Medium, 3-Hard
     */
    private Integer difficulty;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 题目通过率 (%)
     */
    private Double passRate;

    /**
     * 当前登录用户的解答状态: 0-未开始, 1-尝试过, 2-已通过
     */
    private Integer userState;

    /**
     * 题目最后更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 题干正文 (映射数据库的 description 字段)
     */
    private String content;

    /**
     * 时间限制 (ms)
     */
    private Integer timeLimit;

    /**
     * 内存限制 (MB)
     */
    private Integer memoryLimit;

    /**
     * 测试用例示例列表
     */
    private List<ExampleVO> examples;
}