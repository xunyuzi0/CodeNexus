// src/main/java/com/xunyu/codenexus/backend/controller/DashboardController.java
package com.xunyu.codenexus.backend.controller;

import com.xunyu.codenexus.backend.aop.auth.Protector;
import com.xunyu.codenexus.backend.common.result.Result;
import com.xunyu.codenexus.backend.model.dto.response.CheckInVO;
import com.xunyu.codenexus.backend.model.dto.response.DashboardStatsVO;
import com.xunyu.codenexus.backend.model.dto.response.HeatmapItemVO;
import com.xunyu.codenexus.backend.model.enums.UserRoleEnum;
import com.xunyu.codenexus.backend.service.DashboardService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 仪表盘核心模块控制器
 *
 * @author CodeNexusBuilder
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    /**
     * 获取仪表盘核心统计数据
     */
    @GetMapping("/stats")
    @Protector(role = UserRoleEnum.USER)
    public Result<DashboardStatsVO> getDashboardStats() {
        DashboardStatsVO stats = dashboardService.getDashboardStats();
        return Result.success(stats);
    }

    /**
     * 用户每日打卡/充能
     */
    @PostMapping("/checkin")
    @Protector(role = UserRoleEnum.USER)
    public Result<CheckInVO> checkIn() {
        CheckInVO checkInVO = dashboardService.checkIn();
        return Result.success(checkInVO);
    }

    /**
     * 获取思维活跃度热力图数据
     *
     * @param year 查询的年份
     */
    @GetMapping("/heatmap")
    @Protector(role = UserRoleEnum.USER)
    public Result<List<HeatmapItemVO>> getActivityHeatmap(@RequestParam("year") Integer year) {
        // 参数校验 (年份不能为空)
        if (year == null) {
            return Result.error(400, "年份参数不能为空");
        }
        List<HeatmapItemVO> heatmapData = dashboardService.getActivityHeatmap(year);
        return Result.success(heatmapData);
    }
}