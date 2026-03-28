// src/main/java/com/xunyu/codenexus/backend/model/dto/response/problem/SolutionVO.java
package com.xunyu.codenexus.backend.model.dto.response.problem;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目官方题解出参 VO
 *
 * @author CodeNexusBuilder
 */
@Data
public class SolutionVO {

    private Long id;

    private Long problemId;

    private String title;

    private String content;

    /**
     * 作者名称 (联表查询 user 表得到)
     */
    private String authorName;

    private Integer viewCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}