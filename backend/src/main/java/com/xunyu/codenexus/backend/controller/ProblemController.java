// src/main/java/com/xunyu/codenexus/backend/controller/ProblemController.java
package com.xunyu.codenexus.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunyu.codenexus.backend.aop.auth.Protector;
import com.xunyu.codenexus.backend.common.result.Result;
import com.xunyu.codenexus.backend.model.dto.request.problem.ProblemQueryRequest;
import com.xunyu.codenexus.backend.model.dto.request.problem.SolutionAddRequest;
import com.xunyu.codenexus.backend.model.dto.request.problem.SolutionUpdateRequest;
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
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * 获取指定题目的所有题解列表 (按时间倒序)
     * 规范化 RESTful 路径: 变更为复数 /solutions
     */
    @GetMapping("/{problemId}/solutions")
    public Result<List<SolutionVO>> getProblemSolutions(@PathVariable Long problemId) {
        List<SolutionVO> solutions = problemSolutionService.getProblemSolutionList(problemId);
        return Result.success(solutions);
    }

    /**
     * 发布新的题解
     */
    @Protector(role = UserRoleEnum.USER)
    @PostMapping("/{problemId}/solutions")
    public Result<Void> publishSolution(
            @PathVariable Long problemId,
            @RequestBody @Valid SolutionAddRequest request) {
        problemSolutionService.publishSolution(problemId, request);
        return Result.success();
    }

    /**
     * 更新我的题解
     */
    @Protector(role = UserRoleEnum.USER)
    @PutMapping("/solutions/{solutionId}")
    public Result<Void> updateSolution(
            @PathVariable Long solutionId,
            @RequestBody @Valid SolutionUpdateRequest request) {
        problemSolutionService.updateSolution(solutionId, request);
        return Result.success();
    }

    /**
     * 删除我的题解
     */
    @Protector(role = UserRoleEnum.USER)
    @DeleteMapping("/solutions/{solutionId}")
    public Result<Void> deleteSolution(@PathVariable Long solutionId) {
        problemSolutionService.deleteSolution(solutionId);
        return Result.success();
    }

    /**
     * 记录题解阅读 (防刷去重)
     */
    @Protector(role = UserRoleEnum.USER)
    @PostMapping("/solutions/{solutionId}/view")
    public Result<Void> recordSolutionView(@PathVariable Long solutionId) {
        problemSolutionService.recordView(solutionId);
        return Result.success();
    }
}