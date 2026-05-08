package com.xunyu.codenexus.backend.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunyu.codenexus.backend.aop.auth.Protector;
import com.xunyu.codenexus.backend.common.result.Result;
import com.xunyu.codenexus.backend.model.dto.request.admin.AdminProblemQueryRequest;
import com.xunyu.codenexus.backend.model.dto.request.admin.ProblemUpsertRequest;
import com.xunyu.codenexus.backend.model.dto.request.problem.SolutionAddRequest;
import com.xunyu.codenexus.backend.model.dto.response.admin.AdminProblemDetailVO;
import com.xunyu.codenexus.backend.model.dto.response.admin.AdminProblemVO;
import com.xunyu.codenexus.backend.model.dto.response.problem.SolutionVO;
import com.xunyu.codenexus.backend.model.entity.ProblemTestcase;
import com.xunyu.codenexus.backend.model.enums.UserRoleEnum;
import com.xunyu.codenexus.backend.service.AdminService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端 - 题库管理控制器
 *
 * @author CodeNexusBuilder
 */
@RestController
@RequestMapping("/api/admin/problems")
public class AdminProblemController {

    @Resource
    private AdminService adminService;

    /**
     * 获取题目分页列表
     */
    @GetMapping
    @Protector(role = UserRoleEnum.ADMIN)
    public Result<Page<AdminProblemVO>> getProblemPage(AdminProblemQueryRequest request) {
        Page<AdminProblemVO> page = adminService.getProblemPage(request);
        return Result.success(page);
    }

    /**
     * 获取题目详情（包含描述、示例等）
     */
    @GetMapping("/{id}/detail")
    @Protector(role = UserRoleEnum.ADMIN)
    public Result<AdminProblemDetailVO> getProblemDetail(@PathVariable("id") Long problemId) {
        AdminProblemDetailVO detail = adminService.getProblemDetail(problemId);
        return Result.success(detail);
    }

    /**
     * 创建题目
     */
    @PostMapping
    @Protector(role = UserRoleEnum.ADMIN)
    public Result<Long> createProblem(@RequestBody @Valid ProblemUpsertRequest request) {
        Long problemId = adminService.createProblem(request);
        return Result.success(problemId);
    }

    /**
     * 更新题目
     */
    @PutMapping("/{id}")
    @Protector(role = UserRoleEnum.ADMIN)
    public Result<Boolean> updateProblem(@PathVariable("id") Long problemId, @RequestBody @Valid ProblemUpsertRequest request) {
        adminService.updateProblem(problemId, request);
        return Result.success(true);
    }

    /**
     * 删除题目
     */
    @DeleteMapping("/{id}")
    @Protector(role = UserRoleEnum.ADMIN)
    public Result<Boolean> deleteProblem(@PathVariable("id") Long problemId) {
        adminService.deleteProblem(problemId);
        return Result.success(true);
    }

    /**
     * 发布题目（上线）
     */
    @PutMapping("/{id}/publish")
    @Protector(role = UserRoleEnum.ADMIN)
    public Result<Boolean> publishProblem(@PathVariable("id") Long problemId) {
        adminService.publishProblem(problemId);
        return Result.success(true);
    }

    /**
     * 下线题目
     */
    @PutMapping("/{id}/offline")
    @Protector(role = UserRoleEnum.ADMIN)
    public Result<Boolean> offlineProblem(@PathVariable("id") Long problemId) {
        adminService.offlineProblem(problemId);
        return Result.success(true);
    }

    /**
     * 获取题目测试用例列表
     */
    @GetMapping("/{id}/testcases")
    @Protector(role = UserRoleEnum.ADMIN)
    public Result<List<ProblemTestcase>> getTestcases(@PathVariable("id") Long problemId) {
        List<ProblemTestcase> testcases = adminService.getTestcases(problemId);
        return Result.success(testcases);
    }

    /**
     * 添加测试用例
     */
    @PostMapping("/{id}/testcases")
    @Protector(role = UserRoleEnum.ADMIN)
    public Result<Long> addTestcase(@PathVariable("id") Long problemId, @RequestBody ProblemTestcase testcase) {
        Long testcaseId = adminService.addTestcase(problemId, testcase);
        return Result.success(testcaseId);
    }

    /**
     * 更新测试用例
     */
    @PutMapping("/testcases/{id}")
    @Protector(role = UserRoleEnum.ADMIN)
    public Result<Boolean> updateTestcase(@PathVariable("id") Long testcaseId, @RequestBody ProblemTestcase testcase) {
        adminService.updateTestcase(testcaseId, testcase);
        return Result.success(true);
    }

    /**
     * 删除测试用例
     */
    @DeleteMapping("/testcases/{id}")
    @Protector(role = UserRoleEnum.ADMIN)
    public Result<Boolean> deleteTestcase(@PathVariable("id") Long testcaseId) {
        adminService.deleteTestcase(testcaseId);
        return Result.success(true);
    }

    // ==================== 题解管理 ====================

    /**
     * 获取题目的所有题解列表
     */
    @GetMapping("/{id}/solutions")
    @Protector(role = UserRoleEnum.ADMIN)
    public Result<List<SolutionVO>> getProblemSolutions(@PathVariable("id") Long problemId) {
        List<SolutionVO> solutions = adminService.getProblemSolutions(problemId);
        return Result.success(solutions);
    }

    /**
     * 发布官方题解
     */
    @PostMapping("/{id}/solutions")
    @Protector(role = UserRoleEnum.ADMIN)
    public Result<Boolean> publishOfficialSolution(
            @PathVariable("id") Long problemId,
            @RequestBody @Valid SolutionAddRequest request) {
        adminService.publishOfficialSolution(problemId, request);
        return Result.success(true);
    }

    /**
     * 删除题解（管理员可删除任意题解）
     */
    @DeleteMapping("/solutions/{id}")
    @Protector(role = UserRoleEnum.ADMIN)
    public Result<Boolean> deleteSolution(@PathVariable("id") Long solutionId) {
        adminService.deleteSolution(solutionId);
        return Result.success(true);
    }
}
