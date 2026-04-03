// src/main/java/com/xunyu/codenexus/backend/service/impl/RankServiceImpl.java
package com.xunyu.codenexus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunyu.codenexus.backend.common.context.UserContext;
import com.xunyu.codenexus.backend.common.result.ResultCode;
import com.xunyu.codenexus.backend.mapper.UserMapper;
import com.xunyu.codenexus.backend.model.dto.request.rank.LeaderboardQueryRequest;
import com.xunyu.codenexus.backend.model.dto.response.rank.MyRankVO;
import com.xunyu.codenexus.backend.model.dto.response.rank.RankUserVO;
import com.xunyu.codenexus.backend.model.entity.User;
import com.xunyu.codenexus.backend.service.RankService;
import com.xunyu.codenexus.backend.utils.AssertUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 排行榜业务实现类
 *
 * @author CodeNexusBuilder
 */
@Service
public class RankServiceImpl implements RankService {

    @Resource
    private UserMapper userMapper;

    @Override
    public Page<RankUserVO> getLeaderboard(LeaderboardQueryRequest request) {
        // 1. 构建分页与排序条件
        Page<User> pageParam = new Page<>(request.getCurrent(), request.getPageSize());
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        // 强制走 rating_score 降序索引，如果积分相同按 id 升序保证分页稳定
        queryWrapper.orderByDesc(User::getRatingScore).orderByAsc(User::getId);

        // 2. 执行分页查询
        Page<User> userPage = userMapper.selectPage(pageParam, queryWrapper);

        // 3. 结果集转换与动态字段组装
        Page<RankUserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<RankUserVO> voList = new ArrayList<>();

        // 计算当前页的起始排名偏移量
        long offset = request.getOffset();
        int index = 1;

        for (User user : userPage.getRecords()) {
            RankUserVO vo = new RankUserVO();
            vo.setId(user.getId());
            vo.setName(user.getNickname());
            vo.setAvatar(user.getAvatarUrl());
            vo.setScore(user.getRatingScore());
            vo.setWinRate(user.getWinRate());
            // 动态计算段位
            vo.setTier(calculateTier(user.getRatingScore()));
            // 动态计算排名: 偏移量 + 当前页索引
            vo.setRank((int) (offset + index));

            voList.add(vo);
            index++;
        }

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public MyRankVO getMyRank() {
        // 1. 严格从上下文获取用户ID
        Long userId = UserContext.getUserId();
        AssertUtil.notNull(userId, ResultCode.UNAUTHORIZED, "用户未登录");

        // 2. 查询用户数据
        User user = userMapper.selectById(userId);
        AssertUtil.notNull(user, ResultCode.NOT_FOUND, "用户不存在");

        // 3. 组装 VO 返回
        MyRankVO vo = new MyRankVO();

        // 👇 【核心修复】：放弃静态 globalRank，采用实时 O(logN) 算法计算我的名次
        // 实时名次 = (积分比我高的人) + (同分但 ID 比我小的人) + 1
        LambdaQueryWrapper<User> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.and(w -> w
                .gt(User::getRatingScore, user.getRatingScore())
                .or(w2 -> w2.eq(User::getRatingScore, user.getRatingScore()).lt(User::getId, user.getId()))
        );
        long realTimeRank = userMapper.selectCount(countWrapper) + 1;

        vo.setRank((int) realTimeRank);
        vo.setScore(user.getRatingScore());
        vo.setWinRate(user.getWinRate());
        vo.setTier(calculateTier(user.getRatingScore()));

        return vo;
    }

    /**
     * 内部方法：根据战力积分动态计算段位称号
     */
    private String calculateTier(Integer score) {
        if (score == null) {
            return "暂无段位";
        }
        if (score < 2000) return "坚韧黑铁";
        if (score < 2500) return "英勇黄铜";
        if (score < 3000) return "不屈白银";
        if (score < 3500) return "荣耀黄金";
        return "天梯传说";
    }
}