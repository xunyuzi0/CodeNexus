// src/main/java/com/xunyu/codenexus/backend/model/dto/response/problem/SubmissionHistoryVO.java
package com.xunyu.codenexus.backend.model.dto.response.problem;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户提交记录列表出参 VO
 *
 * @author CodeNexusBuilder
 */
@Data
public class SubmissionHistoryVO {

    private Long id;

    /**
     * 状态枚举值 (供前端翻译为 AC/WA/TLE)
     */
    private Integer status;

    /**
     * 运行耗时(ms)
     */
    private Integer timeCost;

    /**
     * 内存消耗(MB)
     */
    private Double memoryCost;

    /**
     * 编程语言
     */
    private String language;

    /**
     * 提交时间 (映射数据库 createTime)
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;
}