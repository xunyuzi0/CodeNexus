// src/main/java/com/xunyu/codenexus/backend/service/impl/ProblemServiceImpl.java
package com.xunyu.codenexus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xunyu.codenexus.backend.common.context.UserContext;
import com.xunyu.codenexus.backend.common.result.ResultCode;
import com.xunyu.codenexus.backend.mapper.ProblemMapper;
import com.xunyu.codenexus.backend.mapper.UserProblemStateMapper;
import com.xunyu.codenexus.backend.model.dto.request.problem.ProblemQueryRequest;
import com.xunyu.codenexus.backend.model.dto.response.problem.ProblemDetailVO;
import com.xunyu.codenexus.backend.model.dto.response.problem.ProblemVO;
import com.xunyu.codenexus.backend.model.entity.Problem;
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

    @Override
    public Page<ProblemVO> getProblemPage(ProblemQueryRequest request) {
        // 1. 构造基础分页参数
        Page<Problem> pageParam = new Page<>(request.getCurrent(), request.getPageSize());
        LambdaQueryWrapper<Problem> wrapper = new LambdaQueryWrapper<>();

        // 2. 动态拼装查询条件
        // 标题模糊查询
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.like(Problem::getTitle, request.getKeyword());
        }
        // 难度精确匹配
        if (request.getDifficulty() != null) {
            wrapper.eq(Problem::getDifficulty, request.getDifficulty());
        }
        // MySQL 8 JSON 数组过滤查询
        if (!CollectionUtils.isEmpty(request.getTags())) {
            for (String tag : request.getTags()) {
                // 使用占位符 {0} 防止 SQL 注入
                // 因为 MySQL 中 JSON_CONTAINS 查找字符串元素需要带双引号，所以拼接 "\"" + tag + "\""
                wrapper.apply("JSON_CONTAINS(tags, {0})", "\"" + tag + "\"");
            }
        }
        // 默认按题目 ID 升序排列
        wrapper.orderByAsc(Problem::getId);

        // 3. 执行数据库查询
        Page<Problem> problemPage = this.page(pageParam, wrapper);

        // 如果结果为空，直接返回空分页
        if (CollectionUtils.isEmpty(problemPage.getRecords())) {
            return new Page<>(problemPage.getCurrent(), problemPage.getSize(), problemPage.getTotal());
        }

        // 4. 获取当前页所有的题目 ID
        List<Long> problemIds = problemPage.getRecords().stream()
                .map(Problem::getId)
                .collect(Collectors.toList());

        // 5. 尝试获取当前登录用户 ID (公开接口可能未登录)
        Long userId = UserContext.getUserId();
        Map<Long, Integer> userStateMap = null;

        if (userId != null) {
            // 一次性通过 IN 查询该用户对当前页这些题目的解答状态，避免 N+1 查询问题
            LambdaQueryWrapper<UserProblemState> stateWrapper = new LambdaQueryWrapper<>();
            stateWrapper.eq(UserProblemState::getUserId, userId)
                    .in(UserProblemState::getProblemId, problemIds);

            List<UserProblemState> states = userProblemStateMapper.selectList(stateWrapper);
            // 转化为 Map 以便后续 O(1) 高速合并，Key=problemId, Value=state
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

            // 计算通过率并防零除异常
            if (problem.getSubmitNum() == null || problem.getSubmitNum() == 0) {
                vo.setPassRate(0.0);
            } else {
                double rate = (double) problem.getAcceptedNum() / problem.getSubmitNum();
                vo.setPassRate(rate);
            }

            // 合并用户答题状态 (未登录或没做过默认为 0)
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
        // 校验题目是否存在
        Problem problem = this.getById(id);
        AssertUtil.notNull(problem, ResultCode.NOT_FOUND, "题目不存在或已下架");

        // 转为详情 VO 返回，绝不暴漏多余的底层字段
        ProblemDetailVO vo = new ProblemDetailVO();
        BeanUtils.copyProperties(problem, vo);
        return vo;
    }
}