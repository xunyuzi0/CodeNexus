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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
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

    @Resource
    private UserMapper userMapper;

    @Override
    public Page<ProblemVO> getProblemPage(ProblemQueryRequest request) {
        // 1. 构造基础分页参数
        Page<Problem> pageParam = new Page<>(request.getCurrent(), request.getPageSize());
        LambdaQueryWrapper<Problem> wrapper = new LambdaQueryWrapper<>();

        // 2. 动态拼装查询条件
        // 公开接口只展示已上架题目
        wrapper.eq(Problem::getStatus, 1);
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

            // 统一列表页 displayId 的兜底与 1000 偏移量对齐
            if (!StringUtils.hasText(vo.getDisplayId())) {
                vo.setDisplayId("P-" + (problem.getId() + 1000));
            }

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
        // 1. 获取物理记录
        Problem problem = this.getById(id);
        AssertUtil.notNull(problem, ResultCode.NOT_FOUND, "题目不存在或已下架");

        ProblemDetailVO vo = new ProblemDetailVO();
        BeanUtils.copyProperties(problem, vo);

        // 2. 字段桥接处理
        if (!StringUtils.hasText(vo.getDisplayId())) {
            vo.setDisplayId("P-" + (problem.getId() + 1000));
        }
        vo.setContent(problem.getDescription());

        // 🐛 核心修复：统一前后端契约，由前端统一进行 *100 的格式化。
        // 这里剥离掉之前的 * 100，直接输出 0~1 的小数。彻底解决详情页通过率 5000% 的双重放大 Bug。
        if (problem.getSubmitNum() == null || problem.getSubmitNum() == 0) {
            vo.setPassRate(0.0);
        } else {
            double rate = (double) problem.getAcceptedNum() / problem.getSubmitNum();
            vo.setPassRate(rate);
        }

        // 4. 基于 UserContext 探测用户做题状态
        Long userId = UserContext.getUserId();
        if (userId != null) {
            LambdaQueryWrapper<UserProblemState> stateWrapper = new LambdaQueryWrapper<>();
            stateWrapper.eq(UserProblemState::getUserId, userId)
                    .eq(UserProblemState::getProblemId, id);
            UserProblemState stateObj = userProblemStateMapper.selectOne(stateWrapper);
            vo.setUserState(stateObj != null ? stateObj.getState() : 0);
        } else {
            vo.setUserState(0);
        }

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

        // 2. 查出该用户在【近 7 天内】已经 AC (状态为2) 的所有题目 ID
        // 精准时间戳计算：当前时间减去 7 天毫秒数
        Date sevenDaysAgo = new Date(System.currentTimeMillis() - 7L * 24 * 3600 * 1000);

        LambdaQueryWrapper<UserProblemState> stateWrapper = new LambdaQueryWrapper<>();
        stateWrapper.select(UserProblemState::getProblemId)
                .eq(UserProblemState::getUserId, userId)
                .eq(UserProblemState::getState, 2)
                .ge(UserProblemState::getUpdateTime, sevenDaysAgo); // 【核心改造】：设置 7 天时间屏障

        List<Object> acIdObjs = userProblemStateMapper.selectObjs(stateWrapper);
        List<Long> recentAcIds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(acIdObjs)) {
            recentAcIds = acIdObjs.stream().map(obj -> ((Number) obj).longValue()).collect(Collectors.toList());
        }

        // 3. 执行高性能的【内存级随机抽取算法】
        Long recommendProblemId = pickRandomProblemId(targetDifficulty, recentAcIds);

        // 4. 兜底策略1：如果该难度的题全在最近 7 天被刷完了，取消难度限制
        if (recommendProblemId == null) {
            recommendProblemId = pickRandomProblemId(null, recentAcIds);
        }

        // 5. 极端兜底策略2：如果全库所有的题目都在最近 7 天内被刷完了，全库无限制随机抽取
        if (recommendProblemId == null) {
            recommendProblemId = pickRandomProblemId(null, null);
        }

        AssertUtil.notNull(recommendProblemId, "当前题库为空，无法获取推荐题目");

        return recommendProblemId;
    }

    /**
     * 高性能内存级 O(1) 随机抽取算法 (摒弃 ORDER BY RAND)
     */
    private Long pickRandomProblemId(Integer difficulty, List<Long> excludeIds) {
        LambdaQueryWrapper<Problem> wrapper = new LambdaQueryWrapper<>();
        // 核心优化：只查询 ID 列，避免把 description 等长文本拉进内存
        wrapper.select(Problem::getId);

        if (difficulty != null) {
            wrapper.eq(Problem::getDifficulty, difficulty);
        }
        if (!CollectionUtils.isEmpty(excludeIds)) {
            wrapper.notIn(Problem::getId, excludeIds);
        }

        List<Object> idObjs = this.listObjs(wrapper);
        if (CollectionUtils.isEmpty(idObjs)) {
            return null;
        }

        // 线程安全的极速乱序选取
        int randomIndex = ThreadLocalRandom.current().nextInt(idObjs.size());
        return ((Number) idObjs.get(randomIndex)).longValue();
    }
}