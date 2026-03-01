package com.xunyu.codenexus.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xunyu.codenexus.backend.model.entity.UserPreference;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户偏好设置 Mapper
 *
 * @author CodeNexusBuilder
 */
@Mapper
public interface UserPreferenceMapper extends BaseMapper<UserPreference> {
}