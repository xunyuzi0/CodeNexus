package com.xunyu.codenexus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xunyu.codenexus.backend.common.context.UserContext;
import com.xunyu.codenexus.backend.common.result.ResultCode;
import com.xunyu.codenexus.backend.mapper.ProblemSubmissionMapper;
import com.xunyu.codenexus.backend.mapper.UserProblemStateMapper;
import com.xunyu.codenexus.backend.mapper.UserStatisticsMapper;
import com.xunyu.codenexus.backend.model.dto.request.problem.SubmissionQueryRequest;
import com.xunyu.codenexus.backend.model.dto.response.problem.SubmissionHistoryVO;
import com.xunyu.codenexus.backend.model.entity.ProblemSubmission;
import com.xunyu.codenexus.backend.model.entity.UserProblemState;
import com.xunyu.codenexus.backend.model.entity.UserStatistics;
import com.xunyu.codenexus.backend.service.DashboardService;
import com.xunyu.codenexus.backend.service.ProblemSubmissionService;
import com.xunyu.codenexus.backend.utils.AssertUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户提交记录业务实现类
 *
 * @author CodeNexusBuilder
 */
@Service
public class ProblemSubmissionServiceImpl extends ServiceImpl<ProblemSubmissionMapper, ProblemSubmission> implements ProblemSubmissionService {

    @Resource
    private UserProblemStateMapper userProblemStateMapper;

    @Resource
    private UserStatisticsMapper userStatisticsMapper;

    // 🎯 核心修复：引入仪表盘服务，使用 @Lazy 防止 Spring 启动时由于循环依赖报错
    @Resource
    @Lazy
    private DashboardService dashboardService;

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

    /**
     * 核心修复方法：供 JudgeDispatcher 沙箱判题引擎异步回调使用
     * 严禁在此处调用 UserContext.getUserId()，必须从 submission 反向寻址
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSubmissionStatus(Long submissionId, Integer status, Integer timeCost, Double memoryCost) {
        // 1. 获取原提交记录
        ProblemSubmission submission = this.getById(submissionId);
        if (submission == null || submission.getIsDeleted() == 1) {
            return false;
        }

        // 2. 更新流水表状态 (ProblemSubmission 中状态为 status)
        submission.setStatus(status);
        if (timeCost != null) submission.setTimeCost(timeCost);
        if (memoryCost != null) submission.setMemoryCost(memoryCost);
        // 更新时间交给 MyBatis-Plus 的自动填充 (@TableField) 或 DB 默认值处理
        boolean updated = this.updateById(submission);

        // 3. 状态机补偿机制：判题流水 AC 状态码为 1
        if (updated && status != null && status == 1) {
            Long userId = submission.getUserId();
            Long problemId = submission.getProblemId();

            // 🎯 核心修复：无论是否为首次 AC，只要今日 AC，都必须触发仪表盘热力图记录！
            // 该方法内部有 Redis Set 去重，同一天重复 AC 同一题只记一次。
            dashboardService.recordProblemAc(userId, problemId);

            // 联动更新 user_problem_state 表
            LambdaQueryWrapper<UserProblemState> stateQuery = new LambdaQueryWrapper<>();
            stateQuery.eq(UserProblemState::getUserId, userId)
                    .eq(UserProblemState::getProblemId, problemId)
                    .eq(UserProblemState::getIsDeleted, 0);

            UserProblemState userProblemState = userProblemStateMapper.selectOne(stateQuery);

            boolean isFirstAC = false;

            if (userProblemState == null) {
                // 之前连尝试都没有，直接 AC
                userProblemState = new UserProblemState();
                userProblemState.setUserId(userId);
                userProblemState.setProblemId(problemId);
                // 注意：UserProblemState 实体类中代表状态的字段是 state，且 AC 为 2
                userProblemState.setState(2);
                userProblemStateMapper.insert(userProblemState);
                isFirstAC = true;
            } else if (userProblemState.getState() == null || userProblemState.getState() != 2) {
                // 之前尝试过但没过，这次 AC 了
                userProblemState.setState(2);
                userProblemStateMapper.updateById(userProblemState);
                isFirstAC = true;
            }

            // 4. 高并发打卡与 AC 数统计更新
            if (isFirstAC) {
                LambdaQueryWrapper<UserStatistics> statsQuery = new LambdaQueryWrapper<>();
                statsQuery.eq(UserStatistics::getUserId, userId).eq(UserStatistics::getIsDeleted, 0);
                UserStatistics stats = userStatisticsMapper.selectOne(statsQuery);

                if (stats == null) {
                    stats = new UserStatistics();
                    stats.setUserId(userId);
                    stats.setSolvedCount(1);
                    stats.setContinuousCheckinDays(0);
                    userStatisticsMapper.insert(stats);
                } else {
                    LambdaUpdateWrapper<UserStatistics> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(UserStatistics::getId, stats.getId())
                            .setSql("solved_count = solved_count + 1");
                    userStatisticsMapper.update(null, updateWrapper);
                }
            }
        }

        return updated;
    }
}