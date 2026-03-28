package com.xunyu.codenexus.backend.service;

import com.xunyu.codenexus.backend.model.dto.request.problem.RunCodeRequest;
import com.xunyu.codenexus.backend.model.dto.request.problem.SubmitCodeRequest;
import com.xunyu.codenexus.backend.model.dto.response.problem.RunResultVO;
import com.xunyu.codenexus.backend.model.dto.response.problem.SubmitResponseVO;

import java.util.List;

/**
 * 核心代码执行与判题业务逻辑接口
 *
 * @author CodeNexusBuilder (The Core Specialist)
 */
public interface ProblemRunService {

    /**
     * 第 1 阶段：同步运行/自测代码 (阻塞式)
     * 作用：用户在界面输入自定义测试用例，实时看运行结果，不计入数据库。
     *
     * @param problemId 题目 ID
     * @param request   代码与自定义输入入参
     * @return 运行结果列表
     */
    List<RunResultVO> runCode(Long problemId, RunCodeRequest request);

    /**
     * 第 2 阶段：异步提交判题 (MQ 派发式)
     * 作用：用户正式提交代码，系统生成记录并打入 Redis 队列，立即返回。
     *
     * @param problemId 题目 ID
     * @param request   代码入参
     * @return submissionId 提交记录的物理主键 ID，供前端轮询
     */
    Long submitCode(Long problemId, SubmitCodeRequest request);

    /**
     * 第 3 阶段：高频轮询判题进度 (Cache 高速读取)
     * 作用：前端拿到 submissionId 后，每隔 1-2 秒请求该接口获取最新沙箱进度。
     *
     * @param submissionId 提交记录 ID
     * @return 判题进度 VO (包含通过状态与各个检查点的耗时)
     */
    SubmitResponseVO pollStatus(Long submissionId);
}