// src/main/java/com/xunyu/codenexus/backend/service/impl/DashboardServiceImpl.java
package com.xunyu.codenexus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xunyu.codenexus.backend.common.context.UserContext;
import com.xunyu.codenexus.backend.common.result.ResultCode;
import com.xunyu.codenexus.backend.exception.BusinessException;
import com.xunyu.codenexus.backend.mapper.ProblemMapper;
import com.xunyu.codenexus.backend.mapper.UserActivityLogMapper;
import com.xunyu.codenexus.backend.mapper.UserMapper;
import com.xunyu.codenexus.backend.mapper.UserStatisticsMapper;
import com.xunyu.codenexus.backend.model.dto.response.CheckInVO;
import com.xunyu.codenexus.backend.model.dto.response.DashboardStatsVO;
import com.xunyu.codenexus.backend.model.dto.response.HeatmapItemVO;
import com.xunyu.codenexus.backend.model.entity.Problem;
import com.xunyu.codenexus.backend.model.entity.User;
import com.xunyu.codenexus.backend.model.entity.UserActivityLog;
import com.xunyu.codenexus.backend.model.entity.UserStatistics;
import com.xunyu.codenexus.backend.service.DashboardService;
import com.xunyu.codenexus.backend.utils.AssertUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 仪表盘核心业务实现类
 *
 * @author CodeNexusBuilder
 */
@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    // Redis 缓存 Key 前缀统一定义
    private static final String REDIS_GLOBAL_RANK_KEY = "codenexus:rank:global";
    private static final String CHECKIN_LOCK_PREFIX = "codenexus:checkin:lock:";
    private static final String PROBLEM_AC_SET_PREFIX = "codenexus:ac:set:";
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserStatisticsMapper userStatisticsMapper;
    @Resource
    private UserActivityLogMapper userActivityLogMapper;
    @Resource
    private ProblemMapper problemMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public DashboardStatsVO getDashboardStats() {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        AssertUtil.notNull(user, ResultCode.UNAUTHORIZED, "用户不存在");

        LambdaQueryWrapper<UserStatistics> statsWrapper = new LambdaQueryWrapper<>();
        statsWrapper.eq(UserStatistics::getUserId, userId);
        UserStatistics stats = userStatisticsMapper.selectOne(statsWrapper);
        if (stats == null) {
            stats = new UserStatistics();
            stats.setContinuousCheckinDays(0);
            stats.setSolvedCount(0);
        }

        LocalDate today = LocalDate.now();

        DashboardStatsVO vo = new DashboardStatsVO();
        vo.setRankScore(user.getRatingScore());
        vo.setSolvedCount(stats.getSolvedCount());
        vo.setContinuousCheckInDays(stats.getContinuousCheckinDays());
        vo.setIsCheckedInToday(today.equals(stats.getLastCheckinDate()));

        LambdaQueryWrapper<Problem> problemWrapper = new LambdaQueryWrapper<>();
        vo.setTotalProblems(Math.toIntExact(problemMapper.selectCount(problemWrapper)));

        Long redisRank = stringRedisTemplate.opsForZSet().reverseRank(REDIS_GLOBAL_RANK_KEY, String.valueOf(userId));
        if (redisRank != null) {
            vo.setGlobalRank(redisRank.intValue() + 1);
        } else {
            vo.setGlobalRank(user.getGlobalRank());
        }

        LocalDate weekAgo = today.minusDays(7);
        QueryWrapper<UserActivityLog> logWrapper = new QueryWrapper<>();
        logWrapper.select("IFNULL(SUM(score_change), 0) as total_change")
                .eq("user_id", userId)
                .ge("activity_date", weekAgo);
        List<Map<String, Object>> sumResult = userActivityLogMapper.selectMaps(logWrapper);
        if (sumResult != null && !sumResult.isEmpty()) {
            Number totalChange = (Number) sumResult.get(0).get("total_change");
            vo.setWeeklyScoreChange(totalChange != null ? totalChange.intValue() : 0);
        } else {
            vo.setWeeklyScoreChange(0);
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CheckInVO checkIn() {
        Long userId = UserContext.getUserId();
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DateTimeFormatter.BASIC_ISO_DATE);

        // 1. 分布式防重发锁
        String lockKey = CHECKIN_LOCK_PREFIX + userId + ":" + dateStr;
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", 12, TimeUnit.HOURS);
        if (Boolean.FALSE.equals(locked)) {
            throw new BusinessException("操作过于频繁或今日已打卡");
        }

        // 2. 更新统计扩展表
        LambdaQueryWrapper<UserStatistics> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserStatistics::getUserId, userId);
        UserStatistics stats = userStatisticsMapper.selectOne(wrapper);
        boolean isNew = (stats == null);
        if (isNew) {
            stats = new UserStatistics();
            stats.setUserId(userId);
            stats.setContinuousCheckinDays(0);
            stats.setSolvedCount(0);
        } else {
            AssertUtil.isFalse(today.equals(stats.getLastCheckinDate()), "今日已完成充能");
        }

        LocalDate yesterday = today.minusDays(1);
        if (yesterday.equals(stats.getLastCheckinDate())) {
            stats.setContinuousCheckinDays(stats.getContinuousCheckinDays() + 1);
        } else {
            stats.setContinuousCheckinDays(1);
        }
        stats.setLastCheckinDate(today);

        if (isNew) {
            userStatisticsMapper.insert(stats);
        } else {
            userStatisticsMapper.updateById(stats);
        }

        // 3. 发放打卡积分奖励
        int rewardScore = 5;
        User user = userMapper.selectById(userId);
        user.setRatingScore(user.getRatingScore() + rewardScore);
        userMapper.updateById(user);
        stringRedisTemplate.opsForZSet().add(REDIS_GLOBAL_RANK_KEY, String.valueOf(userId), user.getRatingScore());

        // 4. 更新日志表：注意这里 isCheckin 设为 1，acCount 增加 0 (解决命名冲突，改用 activityLog)
        UserActivityLog activityLog = new UserActivityLog();
        activityLog.setUserId(userId);
        activityLog.setActivityDate(today);
        activityLog.setAcCount(0);
        activityLog.setIsCheckin(1); // 关键修改：标记为已打卡
        activityLog.setScoreChange(rewardScore);
        userActivityLogMapper.upsertActivityLog(activityLog);

        CheckInVO vo = new CheckInVO();
        vo.setSuccess(true);
        vo.setContinuousCheckInDays(stats.getContinuousCheckinDays());
        vo.setReward("充能成功！获得 " + rewardScore + " 点天梯排位分！");

        return vo;
    }

    @Override
    public List<HeatmapItemVO> getActivityHeatmap(Integer year) {
        Long userId = UserContext.getUserId();
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        LambdaQueryWrapper<UserActivityLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserActivityLog::getUserId, userId)
                .ge(UserActivityLog::getActivityDate, startDate)
                .le(UserActivityLog::getActivityDate, endDate);
        List<UserActivityLog> logs = userActivityLogMapper.selectList(wrapper);

        List<HeatmapItemVO> resultList = new ArrayList<>();
        // 解决命名冲突，改用 activityLog
        for (UserActivityLog activityLog : logs) {
            HeatmapItemVO vo = new HeatmapItemVO();
            vo.setDate(activityLog.getActivityDate().toString());

            // 1. 核心规则：当日总活跃度 = 打卡分(0或1) + 当日AC去重题目数
            int checkinScore = (activityLog.getIsCheckin() != null && activityLog.getIsCheckin() == 1) ? 1 : 0;
            int acScore = (activityLog.getAcCount() != null) ? activityLog.getAcCount() : 0;
            int dailyScore = checkinScore + acScore;

            // 2. 将计算出的总分借用 submissionCount 传给前端展示
            vo.setSubmissionCount(dailyScore);

            // 3. 严格遵循架构师定义的 Level 映射公式
            int level;
            if (dailyScore >= 9) {
                level = 4; // 极限橘，今日肝帝
            } else if (dailyScore >= 6) {
                level = 3; // 高亮橘
            } else if (dailyScore >= 3) {
                level = 2; // 中亮橘
            } else if (dailyScore >= 1) {
                level = 1; // 微亮橘 (打卡了或者做出1-2题)
            } else {
                level = 0; // 深灰色，全盘摸鱼
            }

            vo.setLevel(level);
            resultList.add(vo);
        }
        return resultList;
    }

    @Override
    public void recordProblemAc(Long userId, Long problemId) {
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DateTimeFormatter.BASIC_ISO_DATE);

        // 1. 构建 Redis Set Key：区分用户和日期
        String redisSetKey = PROBLEM_AC_SET_PREFIX + userId + ":" + dateStr;

        // 2. 尝试向 Set 中添加该题目的 ID。如果是当日首次 AC 这道题，SADD 会返回 1；如果已经存在，返回 0
        Long addedCount = stringRedisTemplate.opsForSet().add(redisSetKey, String.valueOf(problemId));

        // 为该 Set 设置 48 小时过期时间，防止内存泄漏 (只需要存活过今天即可)
        stringRedisTemplate.expire(redisSetKey, 48, TimeUnit.HOURS);

        // 3. 如果是当日新做的(去重后有效)，则给 activity_log 的 ac_count + 1
        if (addedCount != null && addedCount > 0) {
            // 解决命名冲突，改用 activityLog
            UserActivityLog activityLog = new UserActivityLog();
            activityLog.setUserId(userId);
            activityLog.setActivityDate(today);
            activityLog.setAcCount(1);      // SQL 会执行 ac_count = ac_count + 1
            activityLog.setIsCheckin(0);    // SQL 的 GREATEST() 保证不会覆盖掉已打卡的 1 状态
            activityLog.setScoreChange(0);  // 刷题积分这里暂时不加，由其他模块或根据题目难度发分

            userActivityLogMapper.upsertActivityLog(activityLog);

            // 此时 log 准确指向 @Slf4j 注入的日志对象
            log.info("用户 {} 今日首次 AC 题目 {}, 活跃度 +1", userId, problemId);
        } else {
            // 此时 log 准确指向 @Slf4j 注入的日志对象
            log.info("用户 {} 今日重复 AC 题目 {}, 不增加活跃度", userId, problemId);
        }
    }
}