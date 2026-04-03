// src/main/java/com/xunyu/codenexus/backend/service/ProblemSolutionService.java
package com.xunyu.codenexus.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xunyu.codenexus.backend.model.dto.request.problem.SolutionAddRequest;
import com.xunyu.codenexus.backend.model.dto.request.problem.SolutionUpdateRequest;
import com.xunyu.codenexus.backend.model.dto.response.problem.SolutionVO;
import com.xunyu.codenexus.backend.model.entity.ProblemSolution;

import java.util.List;

/**
 * 题目官方题解服务类
 *
 * @author CodeNexusBuilder
 */
public interface ProblemSolutionService extends IService<ProblemSolution> {

    /**
     * 获取题目的所有题解列表
     *
     * @param problemId 题目ID
     * @return 题解VO列表
     */
    List<SolutionVO> getProblemSolutionList(Long problemId);

    /**
     * 发布新题解
     *
     * @param problemId 题目ID
     * @param request   题解发布请求体
     */
    void publishSolution(Long problemId, SolutionAddRequest request);

    // 修改题解
    void updateSolution(Long solutionId, SolutionUpdateRequest request);

    // 删除题解
    void deleteSolution(Long solutionId);

    // 增加阅读量(去重)
    void recordView(Long solutionId);
}