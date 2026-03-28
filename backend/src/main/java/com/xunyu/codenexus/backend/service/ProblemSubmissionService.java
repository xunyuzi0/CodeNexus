// src/main/java/com/xunyu/codenexus/backend/service/ProblemSubmissionService.java
package com.xunyu.codenexus.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xunyu.codenexus.backend.model.dto.request.problem.SubmissionQueryRequest;
import com.xunyu.codenexus.backend.model.dto.response.problem.SubmissionHistoryVO;
import com.xunyu.codenexus.backend.model.entity.ProblemSubmission;

/**
 * 用户提交记录业务逻辑接口
 *
 * @author CodeNexusBuilder
 */
public interface ProblemSubmissionService extends IService<ProblemSubmission> {

    /**
     * 分页获取当前登录用户的历史提交记录
     *
     * @param problemId 题目 ID
     * @param request   分页请求参数
     * @return 提交记录分页列表
     */
    Page<SubmissionHistoryVO> getMySubmissions(Long problemId, SubmissionQueryRequest request);
}