// src/main/java/com/xunyu/codenexus/backend/service/impl/ProblemSubmissionServiceImpl.java
package com.xunyu.codenexus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xunyu.codenexus.backend.common.context.UserContext;
import com.xunyu.codenexus.backend.common.result.ResultCode;
import com.xunyu.codenexus.backend.mapper.ProblemSubmissionMapper;
import com.xunyu.codenexus.backend.model.dto.request.problem.SubmissionQueryRequest;
import com.xunyu.codenexus.backend.model.dto.response.problem.SubmissionHistoryVO;
import com.xunyu.codenexus.backend.model.entity.ProblemSubmission;
import com.xunyu.codenexus.backend.service.ProblemSubmissionService;
import com.xunyu.codenexus.backend.utils.AssertUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户提交记录业务实现类
 *
 * @author CodeNexusBuilder
 */
@Service
public class ProblemSubmissionServiceImpl extends ServiceImpl<ProblemSubmissionMapper, ProblemSubmission> implements ProblemSubmissionService {

    @Override
    public Page<SubmissionHistoryVO> getMySubmissions(Long problemId, SubmissionQueryRequest request) {
        // 1. 获取上下文用户ID
        Long userId = UserContext.getUserId();
        AssertUtil.notNull(userId, ResultCode.UNAUTHORIZED, "请先登录");

        // 2. 构建分页查询条件
        Page<ProblemSubmission> pageParam = new Page<>(request.getCurrent(), request.getPageSize());
        LambdaQueryWrapper<ProblemSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProblemSubmission::getUserId, userId)
                .eq(ProblemSubmission::getProblemId, problemId)
                .orderByDesc(ProblemSubmission::getCreateTime);

        Page<ProblemSubmission> submissionPage = this.page(pageParam, wrapper);

        // 3. 结果集转换：Entity -> VO
        Page<SubmissionHistoryVO> voPage = new Page<>(submissionPage.getCurrent(), submissionPage.getSize(), submissionPage.getTotal());
        List<SubmissionHistoryVO> voList = new ArrayList<>();

        for (ProblemSubmission submission : submissionPage.getRecords()) {
            SubmissionHistoryVO vo = new SubmissionHistoryVO();
            BeanUtils.copyProperties(submission, vo);
            // 将 createTime 映射为契约要求的 submitTime
            vo.setSubmitTime(submission.getCreateTime());
            voList.add(vo);
        }

        voPage.setRecords(voList);
        return voPage;
    }
}