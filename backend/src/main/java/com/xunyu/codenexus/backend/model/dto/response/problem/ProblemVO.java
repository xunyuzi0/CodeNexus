// src/main/java/com/xunyu/codenexus/backend/model/dto/response/problem/ProblemVO.java
package com.xunyu.codenexus.backend.model.dto.response.problem;

import lombok.Data;

import java.util.List;

/**
 * 题目列表单条记录出参 VO
 *
 * @author CodeNexusBuilder
 */
@Data
public class ProblemVO {

    private Long id;

    private String title;

    private Integer difficulty;

    private List<String> tags;

    /**
     * 通过率 (例如 55.5 表示 55.5%)
     */
    private Double passRate;

    /**
     * 当前用户状态 (0-未开始, 1-尝试过, 2-已通过)
     * 未登录用户默认为 0
     */
    private Integer userState;
}