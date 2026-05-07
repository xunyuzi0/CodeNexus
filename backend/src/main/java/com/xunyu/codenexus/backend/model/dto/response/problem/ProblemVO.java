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

    // 【修复】：新增展示用ID，解决列表页与详情页数据不一致的问题
    private String displayId;

    private String title;

    private Integer difficulty;

    private List<String> tags;

    private Integer status;

    private Double passRate;

    private Integer userState;
}