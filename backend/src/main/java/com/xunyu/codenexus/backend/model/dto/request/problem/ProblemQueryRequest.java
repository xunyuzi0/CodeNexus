// src/main/java/com/xunyu/codenexus/backend/model/dto/request/problem/ProblemQueryRequest.java
package com.xunyu.codenexus.backend.model.dto.request.problem;

import com.xunyu.codenexus.backend.model.dto.request.BaseQueryRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 获取题目列表查询入参 DTO
 * 继承 BaseQueryRequest 获取基础的分页参数
 *
 * @author CodeNexusBuilder
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProblemQueryRequest extends BaseQueryRequest {

    /**
     * 搜索关键词 (模糊匹配标题)
     */
    private String keyword;

    /**
     * 难度: 1-Easy, 2-Medium, 3-Hard
     */
    private Integer difficulty;

    /**
     * 标签数组 (用于 MySQL 8 JSON_CONTAINS 精确过滤)
     */
    private List<String> tags;
}