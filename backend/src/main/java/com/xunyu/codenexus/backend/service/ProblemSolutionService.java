// src/main/java/com/xunyu/codenexus/backend/service/ProblemSolutionService.java
package com.xunyu.codenexus.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xunyu.codenexus.backend.model.dto.response.problem.SolutionVO;
import com.xunyu.codenexus.backend.model.entity.ProblemSolution;

/**
 * 官方题解业务逻辑接口
 *
 * @author CodeNexusBuilder
 */
public interface ProblemSolutionService extends IService<ProblemSolution> {

    /**
     * 获取题目官方题解
     *
     * @param problemId 题目 ID
     * @return 题解详情 VO
     */
    SolutionVO getProblemSolution(Long problemId);
}