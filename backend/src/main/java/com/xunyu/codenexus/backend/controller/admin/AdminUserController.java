package com.xunyu.codenexus.backend.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunyu.codenexus.backend.aop.auth.Protector;
import com.xunyu.codenexus.backend.common.result.Result;
import com.xunyu.codenexus.backend.model.dto.request.admin.AdminUserQueryRequest;
import com.xunyu.codenexus.backend.model.dto.response.admin.AdminUserDetailVO;
import com.xunyu.codenexus.backend.model.dto.response.admin.AdminUserVO;
import com.xunyu.codenexus.backend.model.enums.UserRoleEnum;
import com.xunyu.codenexus.backend.service.AdminService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理端 - 用户管理控制器
 *
 * @author CodeNexusBuilder
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Resource
    private AdminService adminService;

    /**
     * 获取用户分页列表
     */
    @GetMapping
    @Protector(role = UserRoleEnum.ADMIN)
    public Result<Page<AdminUserVO>> getUserPage(AdminUserQueryRequest request) {
        Page<AdminUserVO> page = adminService.getUserPage(request);
        return Result.success(page);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    @Protector(role = UserRoleEnum.ADMIN)
    public Result<AdminUserDetailVO> getUserDetail(@PathVariable("id") Long userId) {
        AdminUserDetailVO detail = adminService.getUserDetail(userId);
        return Result.success(detail);
    }

    /**
     * 更新用户角色
     */
    @PutMapping("/{id}/role")
    @Protector(role = UserRoleEnum.ADMIN)
    public Result<Boolean> updateUserRole(@PathVariable("id") Long userId, @RequestBody Map<String, String> body) {
        String role = body.get("role");
        adminService.updateUserRole(userId, role);
        return Result.success(true);
    }

    /**
     * 封禁用户
     */
    @PutMapping("/{id}/ban")
    @Protector(role = UserRoleEnum.ADMIN)
    public Result<Boolean> banUser(@PathVariable("id") Long userId) {
        adminService.banUser(userId);
        return Result.success(true);
    }

    /**
     * 解封用户
     */
    @PutMapping("/{id}/unban")
    @Protector(role = UserRoleEnum.ADMIN)
    public Result<Boolean> unbanUser(@PathVariable("id") Long userId) {
        adminService.unbanUser(userId);
        return Result.success(true);
    }
}
