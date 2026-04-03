package com.xunyu.codenexus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 仪表盘核心业务实现类
 * 修复：精准处理混合日期类型 (UserStatistics用Date，UserActivityLog用LocalDate)
 * 修复：摒弃覆盖式 upsert，采用增量级原子 Update
 * 修复：Redis opsForSet.add 返回类型 Long 的坑
 */
@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

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

    // --- 日期类型转换工具方法 (仅供 UserStatistics 使用) ---
    private Date toDate(LocalDate localDate) {
        if (localDate == null) return null;
        return java.sql.Date.valueOf(localDate);
    }

    private LocalDate toLocalDate(Date date) {
        if (date == null) return null;
        return new java.sql.Date(date.getTime()).toLocalDate();
    }
    // --------------------------------------------------------

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
            stats.setArenaScore(1000);
            stats.setArenaMatches(0);
            stats.setArenaWins(0);
        }

        DashboardStatsVO vo = new DashboardStatsVO();
        vo.setRankScore(user.getRatingScore());
        vo.setSolvedCount(stats.getSolvedCount());
        vo.setContinuousCheckInDays(stats.getContinuousCheckinDays());

        LocalDate today = LocalDate.now();
        LocalDate lastCheckin = toLocalDate(stats.getLastCheckinDate());
        vo.setIsCheckedInToday(today.equals(lastCheckin));

        // 计算天梯积分与胜率
        int matches = stats.getArenaMatches() == null ? 0 : stats.getArenaMatches();
        int wins = stats.getArenaWins() == null ? 0 : stats.getArenaWins();
        vo.setArenaScore(stats.getArenaScore() == null ? 1000 : stats.getArenaScore());
        vo.setArenaMatches(matches);
        vo.setArenaWins(wins);

        if (matches == 0) {
            vo.setWinRate(0.0);
        } else {
            double rate = ((double) wins / matches) * 100;
            vo.setWinRate(new BigDecimal(rate).setScale(1, RoundingMode.HALF_UP).doubleValue());
        }

        vo.setTotalProblems(Math.toIntExact(problemMapper.selectCount(new LambdaQueryWrapper<>())));

        LambdaQueryWrapper<User> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.and(w -> w
                .gt(User::getRatingScore, user.getRatingScore())
                .or(w2 -> w2.eq(User::getRatingScore, user.getRatingScore()).lt(User::getId, user.getId()))
        );
        long realTimeRank = userMapper.selectCount(countWrapper) + 1;
        vo.setGlobalRank((int) realTimeRank);

        QueryWrapper<UserActivityLog> logWrapper = new QueryWrapper<>();
        logWrapper.select("IFNULL(SUM(score_change), 0) as total_change")
                .eq("user_id", userId)
                .ge("activity_date", today.minusDays(7));
        List<Map<String, Object>> sumResult = userActivityLogMapper.selectMaps(logWrapper);

        int weeklyChange = 0;
        if (sumResult != null && !sumResult.isEmpty()) {
            Map<String, Object> firstRow = sumResult.get(0);
            if (firstRow != null && firstRow.get("total_change") != null) {
                weeklyChange = ((Number) firstRow.get("total_change")).intValue();
            }
        }
        vo.setWeeklyScoreChange(weeklyChange);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CheckInVO checkIn() {
        Long userId = UserContext.getUserId();
        LocalDate today = LocalDate.now();
        String lockKey = CHECKIN_LOCK_PREFIX + userId + ":" + today.toString();

        // 1. 获取 Redis 防并发锁
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", 12, TimeUnit.HOURS);
        if (Boolean.FALSE.equals(locked)) {
            throw new BusinessException("今日已打卡，请勿重复操作");
        }

        try {
            UserStatistics stats = userStatisticsMapper.selectOne(new LambdaQueryWrapper<UserStatistics>().eq(UserStatistics::getUserId, userId));
            boolean isNew = (stats == null);
            if (isNew) {
                stats = new UserStatistics();
                stats.setUserId(userId);
                stats.setSolvedCount(0);
                stats.setContinuousCheckinDays(1);
            } else {
                LocalDate lastCheckin = toLocalDate(stats.getLastCheckinDate());
                AssertUtil.isFalse(today.equals(lastCheckin), "今日已完成充能");
                LocalDate yesterday = today.minusDays(1);
                stats.setContinuousCheckinDays(yesterday.equals(lastCheckin) ? stats.getContinuousCheckinDays() + 1 : 1);
            }

            stats.setLastCheckinDate(toDate(today));
            if (isNew) userStatisticsMapper.insert(stats);
            else userStatisticsMapper.updateById(stats);

            int reward = 5;
            User user = userMapper.selectById(userId);
            user.setRatingScore(user.getRatingScore() + reward);
            userMapper.updateById(user);
            stringRedisTemplate.opsForZSet().add(REDIS_GLOBAL_RANK_KEY, String.valueOf(userId), user.getRatingScore());

            LambdaQueryWrapper<UserActivityLog> logQw = new LambdaQueryWrapper<>();
            logQw.eq(UserActivityLog::getUserId, userId).eq(UserActivityLog::getActivityDate, today);
            UserActivityLog dailyLog = userActivityLogMapper.selectOne(logQw);

            if (dailyLog == null) {
                dailyLog = new UserActivityLog();
                dailyLog.setUserId(userId);
                dailyLog.setActivityDate(today);
                dailyLog.setIsCheckin(1);
                dailyLog.setScoreChange(reward);
                dailyLog.setAcCount(0);
                userActivityLogMapper.insert(dailyLog);
            } else {
                LambdaUpdateWrapper<UserActivityLog> logUpd = new LambdaUpdateWrapper<>();
                logUpd.eq(UserActivityLog::getId, dailyLog.getId())
                        .setSql("is_checkin = 1, score_change = IFNULL(score_change, 0) + " + reward);
                userActivityLogMapper.update(null, logUpd);
            }

            CheckInVO vo = new CheckInVO();
            vo.setSuccess(true);
            vo.setContinuousCheckInDays(stats.getContinuousCheckinDays());
            vo.setReward("充能成功！获得 " + reward + " 点天梯分");
            return vo;

        } catch (Exception e) {
            stringRedisTemplate.delete(lockKey);
            log.error("用户打卡发生异常，已释放防并发锁: {}", userId, e);
            throw e;
        }
    }

    @Override
    public List<HeatmapItemVO> getActivityHeatmap(Integer year) {
        Long userId = UserContext.getUserId();
        List<UserActivityLog> logs = userActivityLogMapper.selectList(new LambdaQueryWrapper<UserActivityLog>()
                .eq(UserActivityLog::getUserId, userId)
                .between(UserActivityLog::getActivityDate, LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31)));

        List<HeatmapItemVO> results = new ArrayList<>();
        for (UserActivityLog log : logs) {
            HeatmapItemVO vo = new HeatmapItemVO();
            vo.setDate(log.getActivityDate().toString());

            // 计算总活跃度 (签到算 1 次 + 当日 AC 题数)
            int count = (log.getIsCheckin() != null ? log.getIsCheckin() : 0) + (log.getAcCount() != null ? log.getAcCount() : 0);
            vo.setSubmissionCount(count);

            // 0:无, 1档(低):>=1, 2档(中):>=2, 3档(高):>=5, 4档(极高):>=8
            vo.setLevel(count >= 8 ? 4 : count >= 5 ? 3 : count >= 2 ? 2 : count >= 1 ? 1 : 0);
            results.add(vo);
        }
        return results;
    }

    @Override
    public void recordProblemAc(Long userId, Long problemId) {
        String redisKey = PROBLEM_AC_SET_PREFIX + userId + ":" + LocalDate.now();

        // 🎯 核心大坑修复：Redis 的 add 操作返回的是 Long 类型，代表被成功加入 Set 的元素个数！
        Long addedCount = stringRedisTemplate.opsForSet().add(redisKey, String.valueOf(problemId));

        // 只有 > 0 时才表示这个题目今天是第一次被 AC
        if (addedCount != null && addedCount > 0) {

            stringRedisTemplate.expire(redisKey, 24, TimeUnit.HOURS);

            LocalDate today = LocalDate.now();

            LambdaQueryWrapper<UserActivityLog> logQw = new LambdaQueryWrapper<>();
            logQw.eq(UserActivityLog::getUserId, userId).eq(UserActivityLog::getActivityDate, today);
            UserActivityLog dailyLog = userActivityLogMapper.selectOne(logQw);

            if (dailyLog == null) {
                dailyLog = new UserActivityLog();
                dailyLog.setUserId(userId);
                dailyLog.setActivityDate(today);
                dailyLog.setAcCount(1);
                dailyLog.setIsCheckin(0);
                dailyLog.setScoreChange(0);
                userActivityLogMapper.insert(dailyLog);
            } else {
                LambdaUpdateWrapper<UserActivityLog> logUpd = new LambdaUpdateWrapper<>();
                logUpd.eq(UserActivityLog::getId, dailyLog.getId())
                        // 精准累加 ac_count，绝不触碰和覆盖 is_checkin 的值
                        .setSql("ac_count = IFNULL(ac_count, 0) + 1");
                userActivityLogMapper.update(null, logUpd);
            }
        }
    }
}