package com.xunyu.codenexus.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xunyu.codenexus.backend.model.entity.UserLoginLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户登录日志 Mapper
 *
 * @author CodeNexusBuilder
 */
@Mapper
public interface UserLoginLogMapper extends BaseMapper<UserLoginLog> {
}