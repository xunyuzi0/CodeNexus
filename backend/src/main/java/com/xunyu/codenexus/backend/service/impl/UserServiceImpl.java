package com.xunyu.codenexus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xunyu.codenexus.backend.common.context.UserContext;
import com.xunyu.codenexus.backend.common.result.ResultCode;
import com.xunyu.codenexus.backend.mapper.UserMapper;
import com.xunyu.codenexus.backend.model.dto.request.user.UpdatePasswordRequest;
import com.xunyu.codenexus.backend.model.dto.request.user.UpdateProfileRequest;
import com.xunyu.codenexus.backend.model.dto.response.UserLoginVO;
import com.xunyu.codenexus.backend.model.dto.response.UserProfileVO;
import com.xunyu.codenexus.backend.model.entity.User;
import com.xunyu.codenexus.backend.model.entity.UserPreference;
import com.xunyu.codenexus.backend.model.enums.UserRoleEnum;
import com.xunyu.codenexus.backend.service.UserPreferenceService;
import com.xunyu.codenexus.backend.service.UserService;
import com.xunyu.codenexus.backend.utils.AssertUtil;
import com.xunyu.codenexus.backend.utils.JwtUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.UUID;

/**
 * 用户业务逻辑实现类
 *
 * @author CodeNexusBuilder
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private UserPreferenceService userPreferenceService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        AssertUtil.notEmpty(userAccount, "账号不能为空");
        AssertUtil.notEmpty(userPassword, "密码不能为空");
        AssertUtil.notEmpty(checkPassword, "确认密码不能为空");
        AssertUtil.minLen(userAccount, 4, "账号长度不能少于 4 位");
        AssertUtil.minLen(userPassword, 8, "密码长度不能少于 8 位");
        AssertUtil.equals(userPassword, checkPassword, "两次输入的密码不一致");

        // 2. 检查账号是否重复
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, userAccount);
        long count = this.count(queryWrapper);
        AssertUtil.isFalse(count > 0, "账号已存在");

        // 3. 生成独立随机盐值并加密
        String salt = UUID.randomUUID().toString().replace("-", "");
        String encryptPassword = DigestUtils.md5DigestAsHex((salt + userPassword).getBytes());

        // 4. 插入数据
        User user = new User();
        user.setUsername(userAccount);
        user.setPasswordHash(encryptPassword);
        user.setSalt(salt);
        user.setNickname(userAccount); // 默认昵称=账号
        user.setRole(UserRoleEnum.USER.getValue());

        user.setGlobalRank(9999); // 默认初始排名设置到一个相对靠后的位置
        user.setWinRate(0);       // 初始胜率为 0

        boolean saveResult = this.save(user);
        AssertUtil.isTrue(saveResult, ResultCode.FAILED, "注册失败，数据库错误");

        return user.getId();
    }

    @Override
    public UserLoginVO userLogin(String userAccount, String userPassword) {
        // 1. 校验
        AssertUtil.notEmpty(userAccount, "账号不能为空");
        AssertUtil.notEmpty(userPassword, "密码不能为空");
        AssertUtil.minLen(userAccount, 4, "账号错误");
        AssertUtil.minLen(userPassword, 8, "密码错误");

        // 2. 查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, userAccount);
        User user = this.getOne(queryWrapper);

        // 校验用户是否存在
        AssertUtil.notNull(user, ResultCode.USER_LOGIN_ERROR, ResultCode.USER_LOGIN_ERROR.getMessage());

        // 校验密码（查出该用户的盐，重新加密对比）
        String encryptPassword = DigestUtils.md5DigestAsHex((user.getSalt() + userPassword).getBytes());
        AssertUtil.equals(encryptPassword, user.getPasswordHash(), ResultCode.USER_LOGIN_ERROR, ResultCode.USER_LOGIN_ERROR.getMessage());

        // 校验封号
        AssertUtil.notEquals(user.getRole(), UserRoleEnum.BAN.getValue(), ResultCode.USER_ACCOUNT_FORBIDDEN,
                ResultCode.USER_ACCOUNT_FORBIDDEN.getMessage());

        // 3. 生成 Token
        String token = jwtUtil.createToken(user.getId(), user.getRole());

        // 4. 返回脱敏信息
        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtils.copyProperties(user, userLoginVO);
        // 兼容旧版入参叫 userAccount 的情况
        userLoginVO.setUserAccount(user.getUsername());
        userLoginVO.setUserRole(user.getRole());
        userLoginVO.setToken(token);

        return userLoginVO;
    }

    @Override
    public User getLoginUser() {
        Long userId = UserContext.getUserId();
        // 校验登录状态
        AssertUtil.notNull(userId, ResultCode.UNAUTHORIZED, ResultCode.UNAUTHORIZED.getMessage());
        User user = this.getById(userId);
        AssertUtil.notNull(user, ResultCode.UNAUTHORIZED, ResultCode.UNAUTHORIZED.getMessage());
        return user;
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getRole());
    }

    @Override
    public UserProfileVO getUserProfile(Long userId) {
        // 1. 获取基础用户信息
        User user = this.getById(userId);
        AssertUtil.notNull(user, ResultCode.UNAUTHORIZED, "用户不存在");

        // 2. 获取用户偏好设置
        LambdaQueryWrapper<UserPreference> prefQuery = new LambdaQueryWrapper<>();
        prefQuery.eq(UserPreference::getUserId, userId);
        UserPreference preference = userPreferenceService.getOne(prefQuery);

        // 3. 组装 VO
        UserProfileVO profileVO = new UserProfileVO();
        BeanUtils.copyProperties(user, profileVO);

        // 组装偏好设置对象
        UserProfileVO.UserPreferenceVO prefVO = new UserProfileVO.UserPreferenceVO();
        if (preference != null) {
            BeanUtils.copyProperties(preference, prefVO);
        } else {
            // 提供默认值兜底
            prefVO.setEditorTheme("zeekr-dark");
            prefVO.setFontSize(14);
            prefVO.setSystemNotifications(1);
        }
        profileVO.setPreferences(prefVO);

        return profileVO;
    }

    @Override
    public boolean updateProfile(Long userId, UpdateProfileRequest request) {
        User userToUpdate = new User();
        userToUpdate.setId(userId);

        // Mybatis-Plus 会自动忽略 null 字段，只更新非 null 的字段 (PATCH 语义)
        if (request.getNickname() != null) userToUpdate.setNickname(request.getNickname());
        if (request.getAvatarUrl() != null) userToUpdate.setAvatarUrl(request.getAvatarUrl());
        if (request.getBio() != null) userToUpdate.setBio(request.getBio());
        if (request.getEmail() != null) userToUpdate.setEmail(request.getEmail());
        if (request.getPhone() != null) userToUpdate.setPhone(request.getPhone());

        return this.updateById(userToUpdate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePassword(Long userId, UpdatePasswordRequest request) {
        AssertUtil.minLen(request.getNewPassword(), 8, "新密码长度不能少于 8 位");

        User user = this.getById(userId);
        AssertUtil.notNull(user, ResultCode.UNAUTHORIZED, "用户不存在");

        // 1. 校验旧密码
        String oldEncryptPassword = DigestUtils.md5DigestAsHex((user.getSalt() + request.getOldPassword()).getBytes());
        AssertUtil.equals(oldEncryptPassword, user.getPasswordHash(), "旧密码错误");

        // 2. 生成新盐值并更新密码
        String newSalt = UUID.randomUUID().toString().replace("-", "");
        String newEncryptPassword = DigestUtils.md5DigestAsHex((newSalt + request.getNewPassword()).getBytes());

        User userToUpdate = new User();
        userToUpdate.setId(userId);
        userToUpdate.setPasswordHash(newEncryptPassword);
        userToUpdate.setSalt(newSalt);

        return this.updateById(userToUpdate);
    }

    @Override
    public boolean deleteAccount(Long userId) {
        User user = this.getById(userId);
        AssertUtil.notNull(user, ResultCode.UNAUTHORIZED, "用户不存在");

        // 你的 User 实体类中 isDeleted 字段已经加了 @TableLogic 注解
        // 所以调用 removeById 时，MyBatis-Plus 会自动将其转换为 UPDATE user SET is_deleted=1 WHERE id=?
        return this.removeById(userId);
    }
}