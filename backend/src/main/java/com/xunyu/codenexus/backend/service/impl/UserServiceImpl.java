package com.xunyu.codenexus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xunyu.codenexus.backend.common.context.UserContext;
import com.xunyu.codenexus.backend.common.result.ResultCode;
import com.xunyu.codenexus.backend.mapper.UserMapper;
import com.xunyu.codenexus.backend.mapper.UserStatisticsMapper;
import com.xunyu.codenexus.backend.model.dto.request.user.UpdatePasswordRequest;
import com.xunyu.codenexus.backend.model.dto.request.user.UpdateProfileRequest;
import com.xunyu.codenexus.backend.model.dto.response.UserLoginVO;
import com.xunyu.codenexus.backend.model.dto.response.UserProfileVO;
import com.xunyu.codenexus.backend.model.entity.User;
import com.xunyu.codenexus.backend.model.entity.UserPreference;
import com.xunyu.codenexus.backend.model.entity.UserStatistics;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * 用户业务逻辑实现类 (修复实体类编译报错版)
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

    @Resource
    private UserStatisticsMapper userStatisticsMapper;

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
        user.setNickname(userAccount);
        user.setRole(UserRoleEnum.USER.getValue());
        user.setGlobalRank(9999);
        // 【已修复】：删除了会导致报错的 user.setWinRate(0.0)

        boolean saveResult = this.save(user);
        AssertUtil.isTrue(saveResult, ResultCode.FAILED, "注册失败，数据库错误");

        return user.getId();
    }

    @Override
    public UserLoginVO userLogin(String userAccount, String userPassword) {
        AssertUtil.notEmpty(userAccount, "账号不能为空");
        AssertUtil.notEmpty(userPassword, "密码不能为空");
        AssertUtil.minLen(userAccount, 4, "账号错误");
        AssertUtil.minLen(userPassword, 8, "密码错误");

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, userAccount);
        User user = this.getOne(queryWrapper);

        AssertUtil.notNull(user, ResultCode.USER_LOGIN_ERROR, ResultCode.USER_LOGIN_ERROR.getMessage());

        String encryptPassword = DigestUtils.md5DigestAsHex((user.getSalt() + userPassword).getBytes());
        AssertUtil.equals(encryptPassword, user.getPasswordHash(), ResultCode.USER_LOGIN_ERROR, ResultCode.USER_LOGIN_ERROR.getMessage());

        AssertUtil.notEquals(user.getRole(), UserRoleEnum.BAN.getValue(), ResultCode.USER_ACCOUNT_FORBIDDEN,
                ResultCode.USER_ACCOUNT_FORBIDDEN.getMessage());

        String token = jwtUtil.createToken(user.getId(), user.getRole());

        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtils.copyProperties(user, userLoginVO);
        userLoginVO.setUserAccount(user.getUsername());
        userLoginVO.setUserRole(user.getRole());
        userLoginVO.setToken(token);

        return userLoginVO;
    }

    @Override
    public User getLoginUser() {
        Long userId = UserContext.getUserId();
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
        User user = this.getById(userId);
        AssertUtil.notNull(user, ResultCode.UNAUTHORIZED, "用户不存在");

        LambdaQueryWrapper<UserPreference> prefQuery = new LambdaQueryWrapper<>();
        prefQuery.eq(UserPreference::getUserId, userId);
        UserPreference preference = userPreferenceService.getOne(prefQuery);

        UserProfileVO profileVO = new UserProfileVO();
        BeanUtils.copyProperties(user, profileVO);

        UserProfileVO.UserPreferenceVO prefVO = new UserProfileVO.UserPreferenceVO();
        if (preference != null) {
            BeanUtils.copyProperties(preference, prefVO);
        } else {
            prefVO.setEditorTheme("zeekr-dark");
            prefVO.setFontSize(14);
            prefVO.setSystemNotifications(1);
        }
        profileVO.setPreferences(prefVO);

        LambdaQueryWrapper<UserStatistics> statsQw = new LambdaQueryWrapper<>();
        statsQw.eq(UserStatistics::getUserId, userId);
        UserStatistics stats = userStatisticsMapper.selectOne(statsQw);

        if (stats != null) {
            profileVO.setArenaScore(stats.getArenaScore() != null ? stats.getArenaScore() : 1000);
            profileVO.setArenaMatches(stats.getArenaMatches() != null ? stats.getArenaMatches() : 0);
            profileVO.setArenaWins(stats.getArenaWins() != null ? stats.getArenaWins() : 0);
            profileVO.setSolvedCount(stats.getSolvedCount() != null ? stats.getSolvedCount() : 0);

            int matches = profileVO.getArenaMatches();
            if (matches > 0) {
                double rate = ((double) profileVO.getArenaWins() / matches) * 100;
                profileVO.setWinRate(new BigDecimal(rate).setScale(1, RoundingMode.HALF_UP).doubleValue());
            } else {
                profileVO.setWinRate(0.0);
            }
        } else {
            profileVO.setArenaScore(1000);
            profileVO.setArenaMatches(0);
            profileVO.setArenaWins(0);
            profileVO.setWinRate(0.0);
            profileVO.setSolvedCount(0);
        }

        return profileVO;
    }

    @Override
    public boolean updateProfile(Long userId, UpdateProfileRequest request) {
        User userToUpdate = new User();
        userToUpdate.setId(userId);

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

        String oldEncryptPassword = DigestUtils.md5DigestAsHex((user.getSalt() + request.getOldPassword()).getBytes());
        AssertUtil.equals(oldEncryptPassword, user.getPasswordHash(), "旧密码错误");

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
        return this.removeById(userId);
    }
}