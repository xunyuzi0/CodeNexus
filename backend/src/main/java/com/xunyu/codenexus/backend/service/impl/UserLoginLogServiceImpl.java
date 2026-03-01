package com.xunyu.codenexus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xunyu.codenexus.backend.mapper.UserLoginLogMapper;
import com.xunyu.codenexus.backend.model.dto.request.user.LoginLogQueryRequest;
import com.xunyu.codenexus.backend.model.dto.response.LoginLogVO;
import com.xunyu.codenexus.backend.model.entity.UserLoginLog;
import com.xunyu.codenexus.backend.service.UserLoginLogService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserLoginLogServiceImpl extends ServiceImpl<UserLoginLogMapper, UserLoginLog> implements UserLoginLogService {

    @Override
    public Page<LoginLogVO> queryLoginLogs(Long userId, LoginLogQueryRequest request) {
        Page<UserLoginLog> page = new Page<>(request.getCurrent(), request.getPageSize());

        LambdaQueryWrapper<UserLoginLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserLoginLog::getUserId, userId)
                .orderByDesc(UserLoginLog::getCreateTime);

        Page<UserLoginLog> logPage = this.page(page, queryWrapper);

        // 实体转换为 VO
        Page<LoginLogVO> voPage = new Page<>(logPage.getCurrent(), logPage.getSize(), logPage.getTotal());
        List<LoginLogVO> voList = logPage.getRecords().stream().map(log -> {
            LoginLogVO vo = new LoginLogVO();
            BeanUtils.copyProperties(log, vo);
            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }
}