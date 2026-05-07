package com.xunyu.codenexus.backend.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunyu.codenexus.backend.aop.auth.Protector;
import com.xunyu.codenexus.backend.common.result.Result;
import com.xunyu.codenexus.backend.model.dto.request.admin.AdminArenaQueryRequest;
import com.xunyu.codenexus.backend.model.dto.response.admin.AdminArenaRoomVO;
import com.xunyu.codenexus.backend.model.enums.UserRoleEnum;
import com.xunyu.codenexus.backend.service.AdminService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端 - 对战记录管理控制器
 *
 * @author CodeNexusBuilder
 */
@RestController
@RequestMapping("/api/admin/arena")
public class AdminArenaController {

    @Resource
    private AdminService adminService;

    /**
     * 获取对战房间分页列表
     */
    @GetMapping("/rooms")
    @Protector(role = UserRoleEnum.ADMIN)
    public Result<Page<AdminArenaRoomVO>> getArenaRoomPage(AdminArenaQueryRequest request) {
        Page<AdminArenaRoomVO> page = adminService.getArenaRoomPage(request);
        return Result.success(page);
    }

    /**
     * 获取对战房间详情
     */
    @GetMapping("/rooms/{id}")
    @Protector(role = UserRoleEnum.ADMIN)
    public Result<AdminArenaRoomVO> getArenaRoomDetail(@PathVariable("id") Long roomId) {
        AdminArenaRoomVO detail = adminService.getArenaRoomDetail(roomId);
        return Result.success(detail);
    }

    /**
     * 获取异常对战房间列表
     */
    @GetMapping("/rooms/abnormal")
    @Protector(role = UserRoleEnum.ADMIN)
    public Result<Page<AdminArenaRoomVO>> getAbnormalArenaRooms(AdminArenaQueryRequest request) {
        Page<AdminArenaRoomVO> page = adminService.getAbnormalArenaRooms(request);
        return Result.success(page);
    }
}
