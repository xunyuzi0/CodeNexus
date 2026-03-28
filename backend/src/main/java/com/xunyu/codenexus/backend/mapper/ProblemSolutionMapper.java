// src/main/java/com/xunyu/codenexus/backend/mapper/ProblemSolutionMapper.java
package com.xunyu.codenexus.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xunyu.codenexus.backend.model.entity.ProblemSolution;
import org.apache.ibatis.annotations.Mapper;

/**
 * 官方题解表 Mapper
 *
 * @author CodeNexusBuilder
 */
@Mapper
public interface ProblemSolutionMapper extends BaseMapper<ProblemSolution> {
}