package com.xunyu.codenexus.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xunyu.codenexus.backend.model.dto.response.UserLoginVO;
import com.xunyu.codenexus.backend.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户业务逻辑接口
 *
 * @author xunyu
 */

public interface UserService extends IService<User> {
    long userRegister(String userAccount, String userPassword, String checkPassword);

    UserLoginVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    User getLoginUser(HttpServletRequest request);

    boolean isAdmin(User user);
}