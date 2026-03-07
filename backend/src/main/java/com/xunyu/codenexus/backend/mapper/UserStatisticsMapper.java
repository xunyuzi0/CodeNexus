// src/main/java/com/xunyu/codenexus/backend/mapper/UserStatisticsMapper.java
package com.xunyu.codenexus.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xunyu.codenexus.backend.model.entity.UserStatistics;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户统计扩展宽表 Mapper
 *
 * @author CodeNexusBuilder
 */
@Mapper
public interface UserStatisticsMapper extends BaseMapper<UserStatistics> {
}