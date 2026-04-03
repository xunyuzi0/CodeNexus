// src/main/java/com/xunyu/codenexus/backend/mapper/ProblemMapper.java
package com.xunyu.codenexus.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xunyu.codenexus.backend.model.entity.Problem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 题库持久层接口
 * 保持纯净，复杂的聚合与随机逻辑已上浮至 Service 层利用内存计算完成
 */
@Mapper
public interface ProblemMapper extends BaseMapper<Problem> {
}