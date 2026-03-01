package com.xunyu.codenexus.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xunyu.codenexus.backend.model.dto.request.user.LoginLogQueryRequest;
import com.xunyu.codenexus.backend.model.dto.response.LoginLogVO;
import com.xunyu.codenexus.backend.model.entity.UserLoginLog;

public interface UserLoginLogService extends IService<UserLoginLog> {
    /**
     * 分页查询当前用户的登录日志
     */
    Page<LoginLogVO> queryLoginLogs(Long userId, LoginLogQueryRequest request);
}