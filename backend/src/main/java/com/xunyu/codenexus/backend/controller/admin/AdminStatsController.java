package com.xunyu.codenexus.backend.controller.admin;

import com.xunyu.codenexus.backend.aop.auth.Protector;
import com.xunyu.codenexus.backend.common.result.Result;
import com.xunyu.codenexus.backend.model.dto.response.admin.ActivityAnalysisVO;
import com.xunyu.codenexus.backend.model.dto.response.admin.AdminStatsOverviewVO;
import com.xunyu.codenexus.backend.model.dto.response.admin.DailyTrendVO;
import com.xunyu.codenexus.backend.model.enums.UserRoleEnum;
import com.xunyu.codenexus.backend.service.AdminService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端 - 数据统计控制器
 *
 * @author CodeNexusBuilder
 */
@RestController
@RequestMapping("/api/admin/stats")
public class AdminStatsController {

    @Resource
    private AdminService adminService;

    /**
     * 获取统计概览数据
     */
    @GetMapping("/overview")
    @Protector(role = UserRoleEnum.ADMIN)
    public Result<AdminStatsOverviewVO> getStatsOverview() {
        AdminStatsOverviewVO overview = adminService.getStatsOverview();
        return Result.success(overview);
    }

    /**
     * 获取趋势数据（按天）
     */
    @GetMapping("/trends")
    @Protector(role = UserRoleEnum.ADMIN)
    public Result<List<DailyTrendVO>> getStatsTrends(@RequestParam(defaultValue = "30") int days) {
        List<DailyTrendVO> trends = adminService.getStatsTrends(days);
        return Result.success(trends);
    }

    /**
     * 获取活跃度分析数据
     */
    @GetMapping("/activity")
    @Protector(role = UserRoleEnum.ADMIN)
    public Result<ActivityAnalysisVO> getStatsActivity() {
        ActivityAnalysisVO activity = adminService.getStatsActivity();
        return Result.success(activity);
    }
}
