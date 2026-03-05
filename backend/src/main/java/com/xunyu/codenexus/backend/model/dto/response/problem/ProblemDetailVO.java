// src/main/java/com/xunyu/codenexus/backend/model/dto/response/problem/ProblemDetailVO.java
package com.xunyu.codenexus.backend.model.dto.response.problem;

import lombok.Data;

import java.util.List;

/**
 * 题目详情出参 VO
 *
 * @author CodeNexusBuilder
 */
@Data
public class ProblemDetailVO {

    private Long id;

    private String title;

    /**
     * Markdown 格式的题目描述
     */
    private String description;

    private Integer difficulty;

    private List<String> tags;

    /**
     * 时间限制 (ms)
     */
    private Integer timeLimit;

    /**
     * 内存限制 (MB)
     */
    private Integer memoryLimit;

    private Integer submitNum;

    private Integer acceptedNum;
}