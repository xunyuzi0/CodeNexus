package com.xunyu.codenexus.backend.model.dto.response.admin;

import lombok.Data;

/**
 * 每日趋势 VO（提交量、AC量、新增用户）
 */
@Data
public class DailyTrendVO {
    private String date;
    private Long submissionCount;
    private Long acceptedCount;
    private Long newUserCount;
}
