package com.xunyu.codenexus.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunyu.codenexus.backend.aop.auth.Protector;
import com.xunyu.codenexus.backend.common.context.UserContext;
import com.xunyu.codenexus.backend.common.result.Result;
import com.xunyu.codenexus.backend.model.dto.request.user.*;
import com.xunyu.codenexus.backend.model.dto.response.LoginLogVO;
import com.xunyu.codenexus.backend.model.dto.response.UserLoginVO;
import com.xunyu.codenexus.backend.model.dto.response.UserProfileVO;
import com.xunyu.codenexus.backend.model.entity.User;
import com.xunyu.codenexus.backend.model.enums.UserRoleEnum;
import com.xunyu.codenexus.backend.service.UserLoginLogService;
import com.xunyu.codenexus.backend.service.UserPreferenceService;
import com.xunyu.codenexus.backend.service.UserService;
import com.xunyu.codenexus.backend.utils.AssertUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器类
 *
 * @author CodeNexusBuilder
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private UserPreferenceService userPreferenceService;

    @Resource
    private UserLoginLogService userLoginLogService;

    // ==========================================
    // 1. 基础登录注册模块 (放行接口)
    // ==========================================

    @PostMapping("/register")
    public Result<Long> userRegister(@RequestBody UserRegisterRequest request) {
        AssertUtil.notNull(request, "请求参数不能为空");
        long result = userService.userRegister(request.getUserAccount(), request.getUserPassword(), request.getCheckPassword());
        return Result.success(result);
    }

    @PostMapping("/login")
    public Result<UserLoginVO> userLogin(@RequestBody UserLoginRequest request) {
        AssertUtil.notNull(request, "请求参数不能为空");
        UserLoginVO userLoginVO = userService.userLogin(request.getUserAccount(), request.getUserPassword());
        return Result.success(userLoginVO);
    }

    @GetMapping("/get/login")
    @Protector(role = UserRoleEnum.USER)
    public Result<UserLoginVO> getLoginUser() {
        User user = userService.getLoginUser();
        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtils.copyProperties(user, userLoginVO);
        userLoginVO.setUserAccount(user.getUsername());
        userLoginVO.setUserRole(user.getRole());
        return Result.success(userLoginVO);
    }

    // ==========================================
    // 2. 个人中心模块 (严格鉴权接口)
    // ==========================================

    /**
     * 获取当前用户完整档案 (供全局 Store 初始化使用)
     */
    @GetMapping("/profile/me")
    @Protector(role = UserRoleEnum.USER)
    public Result<UserProfileVO> getMyProfile() {
        // 绝对红线：只能从 ThreadLocal 上下文获取当前用户ID
        Long userId = UserContext.getUserId();
        UserProfileVO profileVO = userService.getUserProfile(userId);
        return Result.success(profileVO);
    }

    /**
     * 更新基础档案信息 (ProfileTab 提交)
     */
    @PatchMapping("/profile")
    @Protector(role = UserRoleEnum.USER)
    public Result<Boolean> updateProfile(@RequestBody UpdateProfileRequest request) {
        Long userId = UserContext.getUserId();
        boolean result = userService.updateProfile(userId, request);
        return Result.success(result);
    }

    /**
     * 更新偏好设置 (PreferencesTab 提交)
     */
    @PutMapping("/preferences")
    @Protector(role = UserRoleEnum.USER)
    public Result<Boolean> updatePreferences(@RequestBody UpdatePreferenceRequest request) {
        Long userId = UserContext.getUserId();
        boolean result = userPreferenceService.upsertUserPreference(userId, request);
        return Result.success(result);
    }

    /**
     * 修改密码 (SecurityTab 高权限操作)
     * 使用 @Validated 触发 DTO 中的 @NotBlank 校验
     */
    @PutMapping("/security/password")
    @Protector(role = UserRoleEnum.USER)
    public Result<Boolean> updatePassword(@RequestBody @Validated UpdatePasswordRequest request) {
        Long userId = UserContext.getUserId();
        boolean result = userService.updatePassword(userId, request);
        return Result.success(result);
    }

    /**
     * 获取登录日志列表 (SecurityTab 审计展示)
     */
    @PostMapping("/security/login-logs")
    @Protector(role = UserRoleEnum.USER)
    public Result<Page<LoginLogVO>> queryLoginLogs(@RequestBody LoginLogQueryRequest request) {
        Long userId = UserContext.getUserId();
        Page<LoginLogVO> result = userLoginLogService.queryLoginLogs(userId, request);
        return Result.success(result);
    }

    /**
     * 注销账号 (危险操作)
     */
    @DeleteMapping("/security/account")
    @Protector(role = UserRoleEnum.USER)
    public Result<Boolean> deleteAccount() {
        Long userId = UserContext.getUserId();
        boolean result = userService.deleteAccount(userId);
        return Result.success(result);
    }
}