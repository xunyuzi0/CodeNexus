// src/main/java/com/xunyu/codenexus/backend/controller/ProblemController.java
package com.xunyu.codenexus.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunyu.codenexus.backend.common.result.Result;
import com.xunyu.codenexus.backend.model.dto.request.problem.ProblemQueryRequest;
import com.xunyu.codenexus.backend.model.dto.response.problem.ProblemDetailVO;
import com.xunyu.codenexus.backend.model.dto.response.problem.ProblemVO;
import com.xunyu.codenexus.backend.service.ProblemService;
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

    /**
     * 获取题目列表 (支持分页与多条件查询)
     * 无需 @Protector 鉴权，作为公开接口
     */
    @GetMapping
    public Result<Page<ProblemVO>> getProblemPage(ProblemQueryRequest request) {
        Page<ProblemVO> page = problemService.getProblemPage(request);
        return Result.success(page);
    }

    /**
     * 获取题目详情
     * 无需 @Protector 鉴权，作为公开接口
     */
    @GetMapping("/{id}")
    public Result<ProblemDetailVO> getProblemDetail(@PathVariable("id") Long id) {
        ProblemDetailVO detail = problemService.getProblemDetail(id);
        return Result.success(detail);
    }
}