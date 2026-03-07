// src/main/java/com/xunyu/codenexus/backend/service/DashboardService.java
package com.xunyu.codenexus.backend.service;

import com.xunyu.codenexus.backend.model.dto.response.CheckInVO;
import com.xunyu.codenexus.backend.model.dto.response.DashboardStatsVO;
import com.xunyu.codenexus.backend.model.dto.response.HeatmapItemVO;

import java.util.List;

/**
 * 仪表盘核心业务接口
 *
 * @author CodeNexusBuilder
 */
public interface DashboardService {

    /**
     * 获取仪表盘核心统计数据 (包含打卡状态、连续天数、战力分、周变化等)
     *
     * @return DashboardStatsVO
     */
    DashboardStatsVO getDashboardStats();

    /**
     * 用户每日打卡/充能
     *
     * @return CheckInVO (包含连续打卡天数和奖励信息)
     */
    CheckInVO checkIn();

    /**
     * 获取指定年份的思维活跃度热力图数据
     *
     * @param year 查询的年份 (如 2026)
     * @return 包含日期和活跃度等级的列表
     */
    List<HeatmapItemVO> getActivityHeatmap(Integer year);

    /**
     * 记录用户成功 AC 了一道题目 (防作弊：同日同题只加1分)
     * 该接口供判题沙箱 Callback 成功时调用
     *
     * @param userId    用户 ID
     * @param problemId 题目 ID
     */
    void recordProblemAc(Long userId, Long problemId);
}