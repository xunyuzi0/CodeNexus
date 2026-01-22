package com.xunyu.codenexus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xunyu.codenexus.backend.common.context.UserContext;
import com.xunyu.codenexus.backend.common.result.ResultCode;
import com.xunyu.codenexus.backend.exception.BusinessException;
import com.xunyu.codenexus.backend.mapper.UserMapper;
import com.xunyu.codenexus.backend.model.dto.response.UserLoginVO;
import com.xunyu.codenexus.backend.model.entity.User;
import com.xunyu.codenexus.backend.model.enums.UserRoleEnum;
import com.xunyu.codenexus.backend.service.UserService;
import com.xunyu.codenexus.backend.utils.AssertUtil;
import com.xunyu.codenexus.backend.utils.JwtUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * 用户业务逻辑实现类
 *
 * @author xunyu
 */

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    // 盐值，混淆密码
    private static final String SALT = "codenexus";
    @Resource
    private JwtUtil jwtUtil;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        AssertUtil.notEmpty(userAccount, "账号不能为空");
        AssertUtil.notEmpty(userPassword, "密码不能为空");
        AssertUtil.notEmpty(checkPassword, "确认密码不能为空");
        AssertUtil.minLen(userAccount, 4, "账号长度不能少于 4 位");
        AssertUtil.minLen(userPassword, 8, "密码长度不能少于 8 位");
        AssertUtil.equals(userPassword, checkPassword, "两次输入的密码不一致");

        // 2. 检查账号是否重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        long count = this.count(queryWrapper);
        AssertUtil.isFalse(count > 0, "账号已存在");

        // 3. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 4. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        // 默认昵称=账号
        user.setUserName(userAccount);
        user.setUserRole(UserRoleEnum.USER);

        boolean saveResult = this.save(user);
        AssertUtil.isTrue(saveResult, "注册失败，数据库错误");

        return user.getId();
    }

    @Override
    public UserLoginVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        AssertUtil.notEmpty(userAccount, "账号不能为空");
        AssertUtil.notEmpty(userPassword, "密码不能为空");
        AssertUtil.minLen(userAccount, 4, "账号错误");
        AssertUtil.minLen(userPassword, 8, "密码错误");

        // 2. 查询用户
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        queryWrapper.eq("user_password", encryptPassword);
        User user = this.getOne(queryWrapper);

        // 用户不存在
        if (user == null) {
            log.info("user login failed, account cannot match password");
            throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "账号或密码错误");
        }

        // 校验封号
        if (user.getUserRole() == UserRoleEnum.BAN) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "该账号已被封禁");
        }

        // 3. 生成 Token
        String token = jwtUtil.createToken(user.getId(), user.getUserRole().getValue());

        // 4. 返回脱敏信息
        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtils.copyProperties(user, userLoginVO);
        userLoginVO.setUserRole(user.getUserRole().getValue());
        userLoginVO.setToken(token);

        return userLoginVO;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return user;
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.equals(user.getUserRole());
    }
}