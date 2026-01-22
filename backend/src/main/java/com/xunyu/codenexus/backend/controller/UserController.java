package com.xunyu.codenexus.backend.controller;

import com.xunyu.codenexus.backend.common.result.Result;
import com.xunyu.codenexus.backend.model.dto.request.user.UserLoginRequest;
import com.xunyu.codenexus.backend.model.dto.request.user.UserRegisterRequest;
import com.xunyu.codenexus.backend.model.dto.response.UserLoginVO;
import com.xunyu.codenexus.backend.model.entity.User;
import com.xunyu.codenexus.backend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器类
 *
 * @author xunyu
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            return Result.error("请求参数为空");
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return Result.success(result);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<UserLoginVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return Result.error("请求参数为空");
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        UserLoginVO userLoginVO = userService.userLogin(userAccount, userPassword, request);
        return Result.success(userLoginVO);
    }

    /**
     * 获取当前登录用户
     */
    @GetMapping("/get/login")
    public Result<UserLoginVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtils.copyProperties(user, userLoginVO);
        userLoginVO.setUserRole(user.getUserRole().getValue());
        return Result.success(userLoginVO);
    }
}