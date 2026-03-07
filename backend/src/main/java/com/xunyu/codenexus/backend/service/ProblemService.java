// src/main/java/com/xunyu/codenexus/backend/service/ProblemService.java
package com.xunyu.codenexus.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xunyu.codenexus.backend.model.dto.request.problem.ProblemQueryRequest;
import com.xunyu.codenexus.backend.model.dto.response.problem.ProblemDetailVO;
import com.xunyu.codenexus.backend.model.dto.response.problem.ProblemVO;
import com.xunyu.codenexus.backend.model.entity.Problem;

/**
 * 题库中心业务接口
 *
 * @author CodeNexusBuilder
 */
public interface ProblemService extends IService<Problem> {

    /**
     * 分页查询题目列表 (支持复杂过滤与状态合并)
     *
     * @param request 查询请求参数
     * @return 分页题目列表 VO
     */
    Page<ProblemVO> getProblemPage(ProblemQueryRequest request);

    /**
     * 获取题目详情
     *
     * @param id 题目 ID
     * @return 题目详情 VO
     */
    ProblemDetailVO getProblemDetail(Long id);

    /**
     * 获取“每日一练”推荐题目
     * 智能推荐逻辑：基于当前用户的段位分映射难度，并过滤掉已 AC 的题目
     *
     * @return 推荐题目的 ID
     */
    Long getDailyPracticeProblem();
}