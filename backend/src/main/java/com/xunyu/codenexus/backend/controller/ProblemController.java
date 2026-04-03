// src/main/java/com/xunyu/codenexus/backend/controller/ProblemController.java
package com.xunyu.codenexus.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunyu.codenexus.backend.aop.auth.Protector;
import com.xunyu.codenexus.backend.common.result.Result;
import com.xunyu.codenexus.backend.model.dto.request.problem.ProblemQueryRequest;
import com.xunyu.codenexus.backend.model.dto.request.problem.SubmissionQueryRequest;
import com.xunyu.codenexus.backend.model.dto.response.problem.ProblemDetailVO;
import com.xunyu.codenexus.backend.model.dto.response.problem.ProblemVO;
import com.xunyu.codenexus.backend.model.dto.response.problem.SolutionVO;
import com.xunyu.codenexus.backend.model.dto.response.problem.SubmissionHistoryVO;
import com.xunyu.codenexus.backend.model.enums.UserRoleEnum;
import com.xunyu.codenexus.backend.service.ProblemService;
import com.xunyu.codenexus.backend.service.ProblemSolutionService;
import com.xunyu.codenexus.backend.service.ProblemSubmissionService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 题库中心接口控制器
 *
 * @author CodeNexusBuilder
 */
@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    @Resource
    private ProblemService problemService;

    @Resource
    private ProblemSolutionService problemSolutionService;

    @Resource
    private ProblemSubmissionService problemSubmissionService;

    /**
     * 获取题目列表 (支持分页与多条件查询)
     * 无需 @Protector 鉴权，作为公开接口
     *
     * @param request 列表查询条件
     * @return 分页题目列表
     */
    @GetMapping
    public Result<Page<ProblemVO>> getProblemPage(ProblemQueryRequest request) {
        Page<ProblemVO> page = problemService.getProblemPage(request);
        return Result.success(page);
    }

    /**
     * 获取题目详情 (Zen 模式/刷题主视图)
     * 无需 @Protector 鉴权，作为公开接口。
     * 业务层会自动探测 UserContext，如果已登录则映射做题状态，未登录则兜底为未开始。
     *
     * @param id 题目物理主键 ID
     * @return 题目详细信息
     */
    @GetMapping("/{id}")
    public Result<ProblemDetailVO> getProblemDetail(@PathVariable("id") Long id) {
        ProblemDetailVO detail = problemService.getProblemDetail(id);
        return Result.success(detail);
    }

    /**
     * 获取题目官方题解 (Solution Tab)
     * 无需 @Protector 鉴权，作为公开接口
     *
     * @param problemId 题目 ID
     * @return 题解详情
     */
    @GetMapping("/{problemId}/solution")
    public Result<SolutionVO> getProblemSolution(@PathVariable("problemId") Long problemId) {
        SolutionVO solution = problemSolutionService.getProblemSolution(problemId);
        return Result.success(solution);
    }

    /**
     * 获取我的历史提交记录 (Submissions Tab)
     * 强鉴权接口：必须登录方可访问，且只能获取自己的提交记录
     *
     * @param problemId 题目 ID
     * @param request   分页查询参数 (包含 current 和 pageSize)
     * @return 历史提交记录分页列表
     */
    @GetMapping("/{problemId}/submissions")
    @Protector(role = UserRoleEnum.USER)
    public Result<Page<SubmissionHistoryVO>> getMySubmissions(
            @PathVariable("problemId") Long problemId,
            SubmissionQueryRequest request) {
        Page<SubmissionHistoryVO> page = problemSubmissionService.getMySubmissions(problemId, request);
        return Result.success(page);
    }

    /**
     * 🎯 获取“每日一练”推荐题目 (Dashboard 专属)
     * 强鉴权接口：必须登录，基于用户的段位分进行难度匹配，并引入近 7 天防重机制
     *
     * @return 推荐题目 ID
     */
    @GetMapping("/daily-practice")
    @Protector(role = UserRoleEnum.USER)
    public Result<Long> getDailyPracticeProblem() {
        Long problemId = problemService.getDailyPracticeProblem();
        return Result.success(problemId);
    }
}