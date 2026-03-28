package com.xunyu.codenexus.backend.controller;

import com.xunyu.codenexus.backend.aop.auth.Protector;
import com.xunyu.codenexus.backend.common.result.Result;
import com.xunyu.codenexus.backend.model.dto.request.problem.RunCodeRequest;
import com.xunyu.codenexus.backend.model.dto.request.problem.SubmitCodeRequest;
import com.xunyu.codenexus.backend.model.dto.response.problem.RunResultVO;
import com.xunyu.codenexus.backend.model.dto.response.problem.SubmitResponseVO;
import com.xunyu.codenexus.backend.model.enums.UserRoleEnum;
import com.xunyu.codenexus.backend.service.ProblemRunService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 代码执行控制层
 * 用于处理用户在线自测运行与异步判题
 */
@RestController
@RequestMapping("/api")
public class ProblemRunController {

    @Resource
    private ProblemRunService problemRunService;

    /**
     * 1. 运行/自测代码 (Run Code - 同步阻塞架构)
     * 权限要求：必须登录
     */
    @PostMapping("/problems/{id}/run")
    @Protector(role = UserRoleEnum.USER)
    public Result<List<RunResultVO>> runCode(
            @PathVariable("id") Long problemId,
            @RequestBody @Validated RunCodeRequest request) {

        List<RunResultVO> result = problemRunService.runCode(problemId, request);
        return Result.success(result);
    }

    /**
     * 2. 提交判题 (Submit Code - 异步派发)
     */
    @PostMapping("/problems/{id}/submit")
    @Protector(role = UserRoleEnum.USER)
    public Result<Long> submitCode(
            @PathVariable("id") Long problemId,
            @RequestBody @Validated SubmitCodeRequest request) {
        // 投递任务到 Redis MQ，并立刻返回 submissionId
        Long submissionId = problemRunService.submitCode(problemId, request);
        return Result.success(submissionId);
    }

    /**
     * 3. 轮询判题进度 (Poll Submission Status)
     */
    @GetMapping("/submissions/{submissionId}/status")
    @Protector(role = UserRoleEnum.USER)
    public Result<SubmitResponseVO> pollSubmissionStatus(@PathVariable("submissionId") Long submissionId) {
        // 从 Redis 高速缓存中读取判题进度
        SubmitResponseVO status = problemRunService.pollStatus(submissionId);
        return Result.success(status);
    }
}