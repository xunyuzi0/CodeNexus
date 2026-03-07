// src/main/java/com/xunyu/codenexus/backend/model/dto/response/DashboardStatsVO.java
package com.xunyu.codenexus.backend.model.dto.response;

import lombok.Data;

/**
 * 仪表盘核心统计数据出参 VO
 *
 * @author CodeNexusBuilder
 */
@Data
public class DashboardStatsVO {
    /**
     * 今日是否已打卡充能
     */
    private Boolean isCheckedInToday;

    /**
     * 当前连续打卡天数
     */
    private Integer continuousCheckInDays;

    /**
     * 当前排位分
     */
    private Integer rankScore;

    /**
     * 本周排位分变化量 (带正负号)
     */
    private Integer weeklyScoreChange;

    /**
     * 全站排名
     */
    private Integer globalRank;

    /**
     * 已解决(AC)的题目总数
     */
    private Integer solvedCount;

    /**
     * 全站可用题目总数
     */
    private Integer totalProblems;
}