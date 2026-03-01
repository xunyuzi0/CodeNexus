package com.xunyu.codenexus.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xunyu.codenexus.backend.model.dto.request.user.UpdatePasswordRequest;
import com.xunyu.codenexus.backend.model.dto.request.user.UpdateProfileRequest;
import com.xunyu.codenexus.backend.model.dto.response.UserLoginVO;
import com.xunyu.codenexus.backend.model.dto.response.UserProfileVO;
import com.xunyu.codenexus.backend.model.entity.User;

/**
 * 用户业务逻辑接口
 *
 * @author CodeNexusBuilder
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     */
    UserLoginVO userLogin(String userAccount, String userPassword);

    /**
     * 获取当前登录的底层 User 实体
     */
    User getLoginUser();

    /**
     * 判断是否为管理员
     */
    boolean isAdmin(User user);

    /**
     * 获取当前用户的完整档案 (包含偏好设置)
     */
    UserProfileVO getUserProfile(Long userId);

    /**
     * 更新基础档案信息
     */
    boolean updateProfile(Long userId, UpdateProfileRequest request);

    /**
     * 修改密码
     */
    boolean updatePassword(Long userId, UpdatePasswordRequest request);

    /**
     * 注销账号 (逻辑删除)
     */
    boolean deleteAccount(Long userId);
}