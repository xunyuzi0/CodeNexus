// src/main/java/com/xunyu/codenexus/backend/service/impl/ProblemServiceImpl.java
package com.xunyu.codenexus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xunyu.codenexus.backend.common.context.UserContext;
import com.xunyu.codenexus.backend.common.result.ResultCode;
import com.xunyu.codenexus.backend.mapper.ProblemMapper;
import com.xunyu.codenexus.backend.mapper.UserMapper;
import com.xunyu.codenexus.backend.mapper.UserProblemStateMapper;
import com.xunyu.codenexus.backend.model.dto.request.problem.ProblemQueryRequest;
import com.xunyu.codenexus.backend.model.dto.response.problem.ProblemDetailVO;
import com.xunyu.codenexus.backend.model.dto.response.problem.ProblemVO;
import com.xunyu.codenexus.backend.model.entity.Problem;
import com.xunyu.codenexus.backend.model.entity.User;
import com.xunyu.codenexus.backend.model.entity.UserProblemState;
import com.xunyu.codenexus.backend.service.ProblemService;
import com.xunyu.codenexus.backend.utils.AssertUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 题库中心业务实现类
 *
 * @author CodeNexusBuilder
 */
@Service
public class ProblemServiceImpl extends ServiceImpl<ProblemMapper, Problem> implements ProblemService {

    @Resource
    private UserProblemStateMapper userProblemStateMapper;

    // 修复：新增注入 UserMapper，供推荐算法使用
    @Resource
    private UserMapper userMapper;

    @Override
    public Page<ProblemVO> getProblemPage(ProblemQueryRequest request) {
        // 1. 构造基础分页参数
        Page<Problem> pageParam = new Page<>(request.getCurrent(), request.getPageSize());
        LambdaQueryWrapper<Problem> wrapper = new LambdaQueryWrapper<>();

        // 2. 动态拼装查询条件
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.like(Problem::getTitle, request.getKeyword());
        }
        if (request.getDifficulty() != null) {
            wrapper.eq(Problem::getDifficulty, request.getDifficulty());
        }
        if (!CollectionUtils.isEmpty(request.getTags())) {
            for (String tag : request.getTags()) {
                wrapper.apply("JSON_CONTAINS(tags, {0})", "\"" + tag + "\"");
            }
        }
        wrapper.orderByAsc(Problem::getId);

        // 3. 执行数据库查询
        Page<Problem> problemPage = this.page(pageParam, wrapper);

        if (CollectionUtils.isEmpty(problemPage.getRecords())) {
            return new Page<>(problemPage.getCurrent(), problemPage.getSize(), problemPage.getTotal());
        }

        // 4. 获取当前页所有的题目 ID
        List<Long> problemIds = problemPage.getRecords().stream()
                .map(Problem::getId)
                .collect(Collectors.toList());

        // 5. 获取当前登录用户解答状态
        Long userId = UserContext.getUserId();
        Map<Long, Integer> userStateMap = null;

        if (userId != null) {
            LambdaQueryWrapper<UserProblemState> stateWrapper = new LambdaQueryWrapper<>();
            stateWrapper.eq(UserProblemState::getUserId, userId)
                    .in(UserProblemState::getProblemId, problemIds);

            List<UserProblemState> states = userProblemStateMapper.selectList(stateWrapper);
            userStateMap = states.stream().collect(Collectors.toMap(
                    UserProblemState::getProblemId,
                    UserProblemState::getState
            ));
        }

        // 6. 转化为 VO 并合并数据
        List<ProblemVO> voList = new ArrayList<>();
        for (Problem problem : problemPage.getRecords()) {
            ProblemVO vo = new ProblemVO();
            BeanUtils.copyProperties(problem, vo);

            if (problem.getSubmitNum() == null || problem.getSubmitNum() == 0) {
                vo.setPassRate(0.0);
            } else {
                double rate = (double) problem.getAcceptedNum() / problem.getSubmitNum();
                vo.setPassRate(rate);
            }

            if (userStateMap != null && userStateMap.containsKey(problem.getId())) {
                vo.setUserState(userStateMap.get(problem.getId()));
            } else {
                vo.setUserState(0);
            }

            voList.add(vo);
        }

        Page<ProblemVO> voPage = new Page<>(problemPage.getCurrent(), problemPage.getSize(), problemPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public ProblemDetailVO getProblemDetail(Long id) {
        Problem problem = this.getById(id);
        AssertUtil.notNull(problem, ResultCode.NOT_FOUND, "题目不存在或已下架");

        ProblemDetailVO vo = new ProblemDetailVO();
        BeanUtils.copyProperties(problem, vo);
        return vo;
    }

    @Override
    public Long getDailyPracticeProblem() {
        Long userId = UserContext.getUserId();

        // 1. 查询当前用户段位，动态映射难度
        User user = userMapper.selectById(userId);
        AssertUtil.notNull(user, ResultCode.UNAUTHORIZED, "用户状态异常");

        int score = user.getRatingScore();
        int targetDifficulty;
        if (score < 1800) {
            targetDifficulty = 1; // 坚韧黑铁~英勇黄铜 推 Easy
        } else if (score < 3000) {
            targetDifficulty = 2; // 不屈白银~荣耀黄金 推 Medium
        } else {
            targetDifficulty = 3; // 天梯传说 推 Hard
        }

        // 2. 查出该用户已经 AC(状态为2) 的所有题目 ID
        LambdaQueryWrapper<UserProblemState> stateWrapper = new LambdaQueryWrapper<>();
        stateWrapper.select(UserProblemState::getProblemId)
                .eq(UserProblemState::getUserId, userId)
                .eq(UserProblemState::getState, 2);
        List<UserProblemState> acStates = userProblemStateMapper.selectList(stateWrapper);
        List<Long> acProblemIds = acStates.stream().map(UserProblemState::getProblemId).collect(Collectors.toList());

        // 3. 构造智能推荐查询
        LambdaQueryWrapper<Problem> problemWrapper = new LambdaQueryWrapper<>();
        problemWrapper.select(Problem::getId)
                .eq(Problem::getDifficulty, targetDifficulty);
        if (!acProblemIds.isEmpty()) {
            problemWrapper.notIn(Problem::getId, acProblemIds);
        }
        problemWrapper.last("ORDER BY RAND() LIMIT 1");

        Problem recommendProblem = this.getOne(problemWrapper);

        // 4. 兜底策略：如果符合该难度的题全做完了，取消难度限制
        if (recommendProblem == null) {
            LambdaQueryWrapper<Problem> fallbackWrapper = new LambdaQueryWrapper<>();
            fallbackWrapper.select(Problem::getId);
            if (!acProblemIds.isEmpty()) {
                fallbackWrapper.notIn(Problem::getId, acProblemIds);
            }
            fallbackWrapper.last("ORDER BY RAND() LIMIT 1");
            recommendProblem = this.getOne(fallbackWrapper);
        }

        // 5. 极端兜底：如果所有题都刷完了，全表随机抽
        if (recommendProblem == null) {
            LambdaQueryWrapper<Problem> extremeFallback = new LambdaQueryWrapper<>();
            extremeFallback.select(Problem::getId).last("ORDER BY RAND() LIMIT 1");
            recommendProblem = this.getOne(extremeFallback);
        }

        AssertUtil.notNull(recommendProblem, "当前题库为空，无法获取推荐题目");

        return recommendProblem.getId();
    }
}