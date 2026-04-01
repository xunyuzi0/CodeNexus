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

    /**
     * 异步更新提交记录状态并补偿状态机联动 (供沙箱引擎或 MQ 消费端调用)
     *
     * @param submissionId 提交记录 ID
     * @param status       状态码 (1 为 AC)
     * @param timeCost     耗时 (ms)
     * @param memoryCost   内存消耗 (MB)
     * @return 是否更新成功
     */
    boolean updateSubmissionStatus(Long submissionId, Integer status, Integer timeCost, Double memoryCost);
}