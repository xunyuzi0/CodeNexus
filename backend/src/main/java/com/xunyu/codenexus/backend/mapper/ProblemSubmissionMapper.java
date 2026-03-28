// src/main/java/com/xunyu/codenexus/backend/mapper/ProblemSubmissionMapper.java
package com.xunyu.codenexus.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xunyu.codenexus.backend.model.entity.ProblemSubmission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户题目提交记录表 Mapper
 *
 * @author CodeNexusBuilder
 */
@Mapper
public interface ProblemSubmissionMapper extends BaseMapper<ProblemSubmission> {
}