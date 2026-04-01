package com.xunyu.codenexus.backend.model.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 仪表盘统计数据输出对象 (全量字段对齐)
 */
@Data
public class DashboardStatsVO {
    // 基础信息
    private Integer rankScore;
    private Integer solvedCount;
    private Integer continuousCheckInDays; // 注意：前端要求的是 CheckIn (大写 I)
    private Boolean isCheckedInToday;
    private Integer totalProblems;
    private Integer globalRank;
    private Integer weeklyScoreChange;

    // 天梯竞技字段
    private Integer arenaScore;
    private Integer arenaMatches;
    private Integer arenaWins;
    private Double winRate; // 胜率百分比，如 65.5

    // 热力图数据
    private List<HeatmapItemVO> checkinHeatmap;
}