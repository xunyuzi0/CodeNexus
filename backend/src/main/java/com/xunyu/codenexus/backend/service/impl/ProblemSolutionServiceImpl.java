// src/main/java/com/xunyu/codenexus/backend/service/impl/ProblemSolutionServiceImpl.java
package com.xunyu.codenexus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xunyu.codenexus.backend.common.result.ResultCode;
import com.xunyu.codenexus.backend.mapper.ProblemSolutionMapper;
import com.xunyu.codenexus.backend.mapper.UserMapper;
import com.xunyu.codenexus.backend.model.dto.response.problem.SolutionVO;
import com.xunyu.codenexus.backend.model.entity.ProblemSolution;
import com.xunyu.codenexus.backend.model.entity.User;
import com.xunyu.codenexus.backend.service.ProblemSolutionService;
import com.xunyu.codenexus.backend.utils.AssertUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * 官方题解业务实现类
 *
 * @author CodeNexusBuilder
 */
@Service
public class ProblemSolutionServiceImpl extends ServiceImpl<ProblemSolutionMapper, ProblemSolution> implements ProblemSolutionService {

    @Resource
    private UserMapper userMapper;

    @Override
    public SolutionVO getProblemSolution(Long problemId) {
        // 1. 查找此题目的第一篇高赞题解/官方题解
        LambdaQueryWrapper<ProblemSolution> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProblemSolution::getProblemId, problemId)
                .orderByDesc(ProblemSolution::getViewCount)
                .last("LIMIT 1");
        ProblemSolution solution = this.getOne(wrapper);

        AssertUtil.notNull(solution, ResultCode.NOT_FOUND, "该题目暂无官方题解");

        // 2. 构建 VO
        SolutionVO vo = new SolutionVO();
        BeanUtils.copyProperties(solution, vo);

        // 连表查询作者名称
        User author = userMapper.selectById(solution.getAuthorId());
        if (author != null) {
            vo.setAuthorName(author.getNickname());
        } else {
            vo.setAuthorName("佚名");
        }

        // 3. 异步更新阅读量 (利用 CompletableFuture 防止阻塞主线程)
        CompletableFuture.runAsync(() -> {
            try {
                ProblemSolution updateObj = new ProblemSolution();
                updateObj.setId(solution.getId());
                updateObj.setViewCount(solution.getViewCount() + 1);
                this.updateById(updateObj);
            } catch (Exception e) {
                log.error("异步更新题解阅读量失败, solutionId: {}");
            }
        });

        return vo;
    }
}