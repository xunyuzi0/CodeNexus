// src/main/java/com/xunyu/codenexus/backend/service/impl/ProblemSolutionServiceImpl.java
package com.xunyu.codenexus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xunyu.codenexus.backend.common.context.UserContext;
import com.xunyu.codenexus.backend.mapper.ProblemSolutionMapper;
import com.xunyu.codenexus.backend.mapper.UserMapper;
import com.xunyu.codenexus.backend.model.dto.request.problem.SolutionAddRequest;
import com.xunyu.codenexus.backend.model.dto.request.problem.SolutionUpdateRequest;
import com.xunyu.codenexus.backend.model.dto.response.problem.SolutionVO;
import com.xunyu.codenexus.backend.model.entity.ProblemSolution;
import com.xunyu.codenexus.backend.model.entity.User;
import com.xunyu.codenexus.backend.service.ProblemSolutionService;
import com.xunyu.codenexus.backend.utils.AssertUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProblemSolutionServiceImpl extends ServiceImpl<ProblemSolutionMapper, ProblemSolution> implements ProblemSolutionService {

    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;

    // 更新构造函数以注入 StringRedisTemplate
    public ProblemSolutionServiceImpl(UserMapper userMapper, StringRedisTemplate stringRedisTemplate) {
        this.userMapper = userMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public List<SolutionVO> getProblemSolutionList(Long problemId) {
        // 1. 获取该题目下的所有题解，官方题解置顶，再按最新时间排序
        List<ProblemSolution> solutions = this.list(new LambdaQueryWrapper<ProblemSolution>()
                .eq(ProblemSolution::getProblemId, problemId)
                .orderByDesc(ProblemSolution::getIsOfficial)
                .orderByDesc(ProblemSolution::getCreateTime));

        if (solutions.isEmpty()) {
            return List.of();
        }

        // 2. 提取所有的 authorId 进行批量查询，避免 N+1 查询问题 (性能优化)
        List<Long> authorIds = solutions.stream()
                .map(ProblemSolution::getAuthorId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> userMap = userMapper.selectBatchIds(authorIds).stream()
                .collect(Collectors.toMap(User::getId, user ->
                        user.getNickname() != null ? user.getNickname() : user.getUsername()
                ));

        // 3. 组装 VO
        return solutions.stream().map(sol -> {
            SolutionVO vo = new SolutionVO();
            BeanUtils.copyProperties(sol, vo);
            // 官方题解作者名显示为 CODENEXUS
            if (sol.getIsOfficial() != null && sol.getIsOfficial() == 1) {
                vo.setAuthorName("CODENEXUS");
            } else {
                vo.setAuthorName(userMap.getOrDefault(sol.getAuthorId(), "匿名极客"));
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishSolution(Long problemId, SolutionAddRequest request) {
        // 从 ThreadLocal 的 UserContext 获取当前无状态上下文的登录用户ID
        Long userId = UserContext.getUserId();
        AssertUtil.notNull(userId, "用户身份丢失，请重新登录");

        // 构造实体落库
        ProblemSolution solution = new ProblemSolution();
        solution.setProblemId(problemId);
        solution.setAuthorId(userId);
        solution.setTitle(request.getTitle());
        solution.setContent(request.getContent());
        solution.setCode(request.getCode());
        solution.setViewCount(0); // 初始阅读量为 0

        this.save(solution);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSolution(Long solutionId, SolutionUpdateRequest request) {
        Long userId = UserContext.getUserId();
        ProblemSolution solution = this.getById(solutionId);

        AssertUtil.notNull(solution, "题解不存在或已被删除");
        AssertUtil.isTrue(solution.getAuthorId().equals(userId), "越权警告：无法修改他人的题解");

        solution.setTitle(request.getTitle());
        solution.setContent(request.getContent());
        solution.setCode(request.getCode());
        this.updateById(solution);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSolution(Long solutionId) {
        Long userId = UserContext.getUserId();
        ProblemSolution solution = this.getById(solutionId);

        AssertUtil.notNull(solution, "题解不存在或已被删除");
        AssertUtil.isTrue(solution.getAuthorId().equals(userId), "越权警告：无法删除他人的题解");

        this.removeById(solutionId); // MyBatis-Plus 会自动执行逻辑删除 (is_deleted = 1)
    }

    @Override
    public void recordView(Long solutionId) {
        Long userId = UserContext.getUserId();
        if (userId == null) return; // 容错：未登录用户不记录（或可视情况分配游客标识）

        // 利用 Redis Set 数据结构实现 O(1) 的绝对去重
        String redisKey = "nexus:solution:viewed:" + solutionId;
        Long added = stringRedisTemplate.opsForSet().add(redisKey, String.valueOf(userId));

        // added == 1 表示该用户 ID 首次被加入 Set，即首次阅读
        if (added != null && added == 1L) {
            // 利用 MyBatis-Plus 的 setSql 执行原子的自增操作
            this.update(new LambdaUpdateWrapper<ProblemSolution>()
                    .eq(ProblemSolution::getId, solutionId)
                    .setSql("view_count = view_count + 1"));
        }
    }
}