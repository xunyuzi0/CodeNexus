// src/main/java/com/xunyu/codenexus/backend/model/dto/response/HeatmapItemVO.java
package com.xunyu.codenexus.backend.model.dto.response;

import lombok.Data;

/**
 * 活跃度热力图单日数据出参 VO
 *
 * @author CodeNexusBuilder
 */
@Data
public class HeatmapItemVO {
    /**
     * 日期，格式 YYYY-MM-DD
     */
    private String date;

    /**
     * 活跃度等级 (0 到 4)
     */
    private Integer level;

    /**
     * 当日实际提交次数
     */
    private Integer submissionCount;
}