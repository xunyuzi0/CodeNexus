package com.xunyu.codenexus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunyu.codenexus.backend.common.result.ResultCode;
import com.xunyu.codenexus.backend.mapper.*;
import com.xunyu.codenexus.backend.model.dto.request.admin.*;
import com.xunyu.codenexus.backend.model.dto.request.problem.SolutionAddRequest;
import com.xunyu.codenexus.backend.service.ProblemSolutionService;
import com.xunyu.codenexus.backend.model.dto.response.LoginLogVO;
import com.xunyu.codenexus.backend.model.dto.response.admin.*;
import com.xunyu.codenexus.backend.model.dto.response.problem.ExampleVO;
import com.xunyu.codenexus.backend.model.dto.response.problem.SolutionVO;
import com.xunyu.codenexus.backend.model.entity.*;
import com.xunyu.codenexus.backend.model.enums.ArenaRoomStatus;
import com.xunyu.codenexus.backend.service.AdminService;
import com.xunyu.codenexus.backend.utils.AssertUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理后台核心业务实现类
 */
@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private UserStatisticsMapper userStatisticsMapper;
    @Resource
    private UserLoginLogMapper userLoginLogMapper;
    @Resource
    private UserActivityLogMapper userActivityLogMapper;
    @Resource
    private ProblemMapper problemMapper;
    @Resource
    private ProblemSubmissionMapper problemSubmissionMapper;
    @Resource
    private ProblemTestcaseMapper problemTestcaseMapper;
    @Resource
    private ArenaRoomMapper arenaRoomMapper;
    @Resource
    private ArenaRoomUserMapper arenaRoomUserMapper;
    @Resource
    private ProblemSolutionService problemSolutionService;

    // ==================== 用户管理 ====================

    @Override
    public Page<AdminUserVO> getUserPage(AdminUserQueryRequest request) {
        Page<User> pageParam = new Page<>(request.getCurrent(), request.getPageSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // keyword 模糊匹配 username / nickname / email
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w
                    .like(User::getUsername, request.getKeyword())
                    .or()
                    .like(User::getNickname, request.getKeyword())
                    .or()
                    .like(User::getEmail, request.getKeyword())
            );
        }
        // role 精确匹配
        if (StringUtils.hasText(request.getRole())) {
            wrapper.eq(User::getRole, request.getRole());
        }
        wrapper.orderByAsc(User::getId);

        Page<User> userPage = userMapper.selectPage(pageParam, wrapper);
        Page<AdminUserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());

        List<AdminUserVO> voList = userPage.getRecords().stream().map(user -> {
            AdminUserVO vo = new AdminUserVO();
            BeanUtils.copyProperties(user, vo);
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public AdminUserDetailVO getUserDetail(Long userId) {
        User user = userMapper.selectById(userId);
        AssertUtil.notNull(user, ResultCode.NOT_FOUND, "用户不存在");

        // 组装基础字段
        AdminUserDetailVO vo = new AdminUserDetailVO();
        BeanUtils.copyProperties(user, vo);

        // 关联 UserStatistics
        LambdaQueryWrapper<UserStatistics> statsWrapper = new LambdaQueryWrapper<>();
        statsWrapper.eq(UserStatistics::getUserId, userId);
        UserStatistics stats = userStatisticsMapper.selectOne(statsWrapper);
        if (stats != null) {
            vo.setSolvedCount(stats.getSolvedCount());
            vo.setArenaScore(stats.getArenaScore());
            vo.setArenaMatches(stats.getArenaMatches());
            vo.setArenaWins(stats.getArenaWins());
            vo.setContinuousCheckinDays(stats.getContinuousCheckinDays());
        }

        // 胜率（数据库存的是百分比整数，如 60 代表 60%，直接透传）
        if (user.getWinRate() != null) {
            vo.setWinRate(user.getWinRate().doubleValue());
        }

        // 最近 5 条登录日志
        LambdaQueryWrapper<UserLoginLog> logWrapper = new LambdaQueryWrapper<>();
        logWrapper.eq(UserLoginLog::getUserId, userId)
                .orderByDesc(UserLoginLog::getCreateTime)
                .last("LIMIT 5");
        List<UserLoginLog> logs = userLoginLogMapper.selectList(logWrapper);
        List<LoginLogVO> logVOs = logs.stream().map(log -> {
            LoginLogVO logVO = new LoginLogVO();
            BeanUtils.copyProperties(log, logVO);
            return logVO;
        }).collect(Collectors.toList());
        vo.setRecentLoginLogs(logVOs);

        return vo;
    }

    @Override
    public void updateUserRole(Long userId, String role) {
        User user = userMapper.selectById(userId);
        AssertUtil.notNull(user, ResultCode.NOT_FOUND, "用户不存在");
        user.setRole(role);
        userMapper.updateById(user);
    }

    @Override
    public void banUser(Long userId) {
        User user = userMapper.selectById(userId);
        AssertUtil.notNull(user, ResultCode.NOT_FOUND, "用户不存在");
        user.setRole("ban");
        userMapper.updateById(user);
    }

    @Override
    public void unbanUser(Long userId) {
        User user = userMapper.selectById(userId);
        AssertUtil.notNull(user, ResultCode.NOT_FOUND, "用户不存在");
        user.setRole("user");
        userMapper.updateById(user);
    }

    // ==================== 题库管理 ====================

    @Override
    public Page<AdminProblemVO> getProblemPage(AdminProblemQueryRequest request) {
        Page<Problem> pageParam = new Page<>(request.getCurrent(), request.getPageSize());
        LambdaQueryWrapper<Problem> wrapper = new LambdaQueryWrapper<>();

        // keyword 模糊匹配 title / displayId
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w
                    .like(Problem::getTitle, request.getKeyword())
                    .or()
                    .like(Problem::getDisplayId, request.getKeyword())
            );
        }
        if (request.getDifficulty() != null) {
            wrapper.eq(Problem::getDifficulty, request.getDifficulty());
        }
        if (request.getStatus() != null) {
            wrapper.eq(Problem::getStatus, request.getStatus());
        }
        wrapper.orderByAsc(Problem::getId);

        Page<Problem> problemPage = problemMapper.selectPage(pageParam, wrapper);
        Page<AdminProblemVO> voPage = new Page<>(problemPage.getCurrent(), problemPage.getSize(), problemPage.getTotal());

        List<AdminProblemVO> voList = problemPage.getRecords().stream().map(problem -> {
            AdminProblemVO vo = new AdminProblemVO();
            BeanUtils.copyProperties(problem, vo);

            // displayId 兜底
            if (!StringUtils.hasText(vo.getDisplayId())) {
                vo.setDisplayId("P-" + (problem.getId() + 1000));
            }
            // 通过率
            if (problem.getSubmitNum() == null || problem.getSubmitNum() == 0) {
                vo.setPassRate(0.0);
            } else {
                double rate = (double) problem.getAcceptedNum() / problem.getSubmitNum();
                vo.setPassRate(new BigDecimal(rate).setScale(4, RoundingMode.HALF_UP).doubleValue());
            }
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public AdminProblemDetailVO getProblemDetail(Long problemId) {
        Problem problem = problemMapper.selectById(problemId);
        AssertUtil.notNull(problem, ResultCode.NOT_FOUND, "题目不存在");

        AdminProblemDetailVO vo = new AdminProblemDetailVO();
        BeanUtils.copyProperties(problem, vo);

        // displayId 兜底
        if (!StringUtils.hasText(vo.getDisplayId())) {
            vo.setDisplayId("P-" + (problem.getId() + 1000));
        }
        // 通过率
        if (problem.getSubmitNum() == null || problem.getSubmitNum() == 0) {
            vo.setPassRate(0.0);
        } else {
            double rate = (double) problem.getAcceptedNum() / problem.getSubmitNum();
            vo.setPassRate(new BigDecimal(rate).setScale(4, RoundingMode.HALF_UP).doubleValue());
        }
        return vo;
    }

    @Override
    public Long createProblem(ProblemUpsertRequest request) {
        Problem problem = new Problem();
        BeanUtils.copyProperties(request, problem);
        problem.setSubmitNum(0);
        problem.setAcceptedNum(0);
        problem.setStatus(request.getStatus() != null ? request.getStatus() : 0);
        problemMapper.insert(problem);

        // 如果有检测点，批量保存
        if (request.getTestcases() != null && !request.getTestcases().isEmpty()) {
            Long problemId = problem.getId();
            for (int i = 0; i < request.getTestcases().size(); i++) {
                ProblemTestcase tc = request.getTestcases().get(i);
                tc.setId(null); // 确保自增
                tc.setProblemId(problemId);
                if (tc.getSortOrder() == null) {
                    tc.setSortOrder(i);
                }
                problemTestcaseMapper.insert(tc);
            }
        }

        return problem.getId();
    }

    @Override
    public void updateProblem(Long problemId, ProblemUpsertRequest request) {
        Problem problem = problemMapper.selectById(problemId);
        AssertUtil.notNull(problem, ResultCode.NOT_FOUND, "题目不存在");
        BeanUtils.copyProperties(request, problem);
        problem.setId(problemId);
        problemMapper.updateById(problem);
    }

    @Override
    public void deleteProblem(Long problemId) {
        Problem problem = problemMapper.selectById(problemId);
        AssertUtil.notNull(problem, ResultCode.NOT_FOUND, "题目不存在");
        problemMapper.deleteById(problemId);
    }

    @Override
    public void publishProblem(Long problemId) {
        Problem problem = problemMapper.selectById(problemId);
        AssertUtil.notNull(problem, ResultCode.NOT_FOUND, "题目不存在");
        problem.setStatus(1);
        problemMapper.updateById(problem);
    }

    @Override
    public void offlineProblem(Long problemId) {
        Problem problem = problemMapper.selectById(problemId);
        AssertUtil.notNull(problem, ResultCode.NOT_FOUND, "题目不存在");
        problem.setStatus(2);
        problemMapper.updateById(problem);
    }

    @Override
    public List<ProblemTestcase> getTestcases(Long problemId) {
        LambdaQueryWrapper<ProblemTestcase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProblemTestcase::getProblemId, problemId)
                .orderByAsc(ProblemTestcase::getSortOrder);
        return problemTestcaseMapper.selectList(wrapper);
    }

    @Override
    public Long addTestcase(Long problemId, ProblemTestcase testcase) {
        testcase.setProblemId(problemId);
        testcase.setId(null);
        problemTestcaseMapper.insert(testcase);
        return testcase.getId();
    }

    @Override
    public void updateTestcase(Long testcaseId, ProblemTestcase testcase) {
        ProblemTestcase existing = problemTestcaseMapper.selectById(testcaseId);
        AssertUtil.notNull(existing, ResultCode.NOT_FOUND, "测试用例不存在");
        testcase.setId(testcaseId);
        problemTestcaseMapper.updateById(testcase);
    }

    @Override
    public void deleteTestcase(Long testcaseId) {
        ProblemTestcase existing = problemTestcaseMapper.selectById(testcaseId);
        AssertUtil.notNull(existing, ResultCode.NOT_FOUND, "测试用例不存在");
        problemTestcaseMapper.deleteById(testcaseId);
    }

    // ==================== 题解管理 ====================

    @Override
    public List<SolutionVO> getProblemSolutions(Long problemId) {
        // 复用 ProblemSolutionService 的方法获取题解列表（含作者信息）
        return problemSolutionService.getProblemSolutionList(problemId);
    }

    @Override
    public void deleteSolution(Long solutionId) {
        // 管理员可删除任意题解，不需要校验 authorId
        ProblemSolution solution = problemSolutionService.getById(solutionId);
        AssertUtil.notNull(solution, ResultCode.NOT_FOUND, "题解不存在或已被删除");
        problemSolutionService.removeById(solutionId);
    }

    @Override
    public void publishOfficialSolution(Long problemId, SolutionAddRequest request) {
        ProblemSolution solution = new ProblemSolution();
        solution.setProblemId(problemId);
        solution.setAuthorId(0L); // 官方题解不关联具体用户
        solution.setIsOfficial(1); // 标记为官方题解
        solution.setTitle(request.getTitle());
        solution.setContent(request.getContent());
        solution.setCode(request.getCode());
        solution.setViewCount(0);
        problemSolutionService.save(solution);
    }

    // ==================== 对战记录 ====================

    @Override
    public Page<AdminArenaRoomVO> getArenaRoomPage(AdminArenaQueryRequest request) {
        Page<ArenaRoom> pageParam = new Page<>(request.getCurrent(), request.getPageSize());
        LambdaQueryWrapper<ArenaRoom> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(ArenaRoom::getStatus, request.getStatus());
        }
        if (request.getRoomType() != null) {
            wrapper.eq(ArenaRoom::getRoomType, request.getRoomType());
        }
        wrapper.orderByDesc(ArenaRoom::getCreateTime);

        Page<ArenaRoom> roomPage = arenaRoomMapper.selectPage(pageParam, wrapper);
        return buildArenaRoomVOPage(roomPage);
    }

    @Override
    public AdminArenaRoomVO getArenaRoomDetail(Long roomId) {
        ArenaRoom room = arenaRoomMapper.selectById(roomId);
        AssertUtil.notNull(room, ResultCode.NOT_FOUND, "房间不存在");
        return buildArenaRoomVO(room);
    }

    @Override
    public Page<AdminArenaRoomVO> getAbnormalArenaRooms(AdminArenaQueryRequest request) {
        // 异常房间：status=BATTLING 且 createTime 在 30 分钟前，或有 ESCAPED 状态用户的房间
        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);

        Page<ArenaRoom> pageParam = new Page<>(request.getCurrent(), request.getPageSize());

        // 查询 BATTLING 且创建时间超过 30 分钟的房间
        LambdaQueryWrapper<ArenaRoom> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArenaRoom::getStatus, ArenaRoomStatus.FIGHTING.getValue())
                .le(ArenaRoom::getCreateTime, thirtyMinutesAgo)
                .orderByDesc(ArenaRoom::getCreateTime);

        Page<ArenaRoom> roomPage = arenaRoomMapper.selectPage(pageParam, wrapper);
        Page<AdminArenaRoomVO> voPage = buildArenaRoomVOPage(roomPage);

        // 补充有 ESCAPED 状态用户的房间（翻页查询额外的异常房间）
        LambdaQueryWrapper<ArenaRoomUser> escapedWrapper = new LambdaQueryWrapper<>();
        escapedWrapper.eq(ArenaRoomUser::getStatus, "ESCAPED")
                .select(ArenaRoomUser::getRoomId)
                .groupBy(ArenaRoomUser::getRoomId);
        List<Object> escapedRoomIds = arenaRoomUserMapper.selectObjs(escapedWrapper);

        if (!escapedRoomIds.isEmpty()) {
            List<Long> roomIdList = escapedRoomIds.stream()
                    .map(obj -> ((Number) obj).longValue())
                    .collect(Collectors.toList());

            // 排除已在结果中的 roomId
            Set<Long> existingIds = voPage.getRecords().stream()
                    .map(AdminArenaRoomVO::getId)
                    .collect(Collectors.toSet());
            roomIdList.removeAll(existingIds);

            if (!roomIdList.isEmpty()) {
                List<ArenaRoom> escapedRooms = arenaRoomMapper.selectBatchIds(roomIdList);
                List<AdminArenaRoomVO> escapedVOs = escapedRooms.stream()
                        .map(this::buildArenaRoomVO)
                        .collect(Collectors.toList());
                voPage.getRecords().addAll(escapedVOs);
                voPage.setTotal(voPage.getTotal() + escapedVOs.size());
            }
        }

        return voPage;
    }

    // ==================== 数据统计 ====================

    @Override
    public AdminStatsOverviewVO getStatsOverview() {
        AdminStatsOverviewVO vo = new AdminStatsOverviewVO();

        // 总用户数
        vo.setTotalUsers(userMapper.selectCount(new LambdaQueryWrapper<>()));

        // 总题目数
        vo.setTotalProblems(problemMapper.selectCount(new LambdaQueryWrapper<>()));

        // 总提交数
        vo.setTotalSubmissions(problemSubmissionMapper.selectCount(new LambdaQueryWrapper<>()));

        // 今日新增用户
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LambdaQueryWrapper<User> todayUserWrapper = new LambdaQueryWrapper<>();
        todayUserWrapper.ge(User::getCreateTime, todayStart);
        vo.setTodayNewUsers(userMapper.selectCount(todayUserWrapper));

        // 今日提交数
        LambdaQueryWrapper<ProblemSubmission> todaySubmitWrapper = new LambdaQueryWrapper<>();
        todaySubmitWrapper.ge(ProblemSubmission::getCreateTime, todayStart);
        vo.setTodaySubmissions(problemSubmissionMapper.selectCount(todaySubmitWrapper));

        // 总体通过率
        Long totalSubmissions = vo.getTotalSubmissions();
        if (totalSubmissions != null && totalSubmissions > 0) {
            LambdaQueryWrapper<ProblemSubmission> acWrapper = new LambdaQueryWrapper<>();
            acWrapper.eq(ProblemSubmission::getStatus, 1);
            Long acCount = problemSubmissionMapper.selectCount(acWrapper);
            double rate = acCount.doubleValue() / totalSubmissions;
            vo.setOverallPassRate(new BigDecimal(rate).setScale(4, RoundingMode.HALF_UP).doubleValue());
        } else {
            vo.setOverallPassRate(0.0);
        }

        // 活跃用户数（最近 7 天有活动记录的去重用户数）
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        QueryWrapper<UserActivityLog> activeWrapper = new QueryWrapper<>();
        activeWrapper.select("COUNT(DISTINCT user_id) as cnt")
                .ge("activity_date", sevenDaysAgo);
        List<Map<String, Object>> activeResult = userActivityLogMapper.selectMaps(activeWrapper);
        vo.setActiveUserCount(extractCountFromMap(activeResult));

        // 在线房间数（WAITING 或 BATTLING）
        LambdaQueryWrapper<ArenaRoom> onlineWrapper = new LambdaQueryWrapper<>();
        onlineWrapper.in(ArenaRoom::getStatus, ArenaRoomStatus.WAITING.getValue(), ArenaRoomStatus.FIGHTING.getValue());
        vo.setOnlineRoomCount(arenaRoomMapper.selectCount(onlineWrapper));

        return vo;
    }

    @Override
    public List<DailyTrendVO> getStatsTrends(int days) {
        LocalDate startDate = LocalDate.now().minusDays(days - 1);

        // 提交趋势（按天 GROUP BY）
        QueryWrapper<ProblemSubmission> submitQuery = new QueryWrapper<>();
        submitQuery.select("DATE(create_time) as date",
                        "COUNT(*) as submission_count",
                        "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as accepted_count")
                .ge("create_time", startDate.atStartOfDay())
                .groupBy("DATE(create_time)")
                .orderByAsc("date");
        List<Map<String, Object>> submitRows = problemSubmissionMapper.selectMaps(submitQuery);

        // 新增用户趋势（按天 GROUP BY）
        QueryWrapper<User> userQuery = new QueryWrapper<>();
        userQuery.select("DATE(create_time) as date", "COUNT(*) as new_user_count")
                .ge("create_time", startDate.atStartOfDay())
                .groupBy("DATE(create_time)")
                .orderByAsc("date");
        List<Map<String, Object>> userRows = userMapper.selectMaps(userQuery);

        // 转为 Map 便于合并
        Map<String, Map<String, Object>> submitMap = new LinkedHashMap<>();
        for (Map<String, Object> row : submitRows) {
            String dateStr = String.valueOf(row.get("date"));
            submitMap.put(dateStr, row);
        }
        Map<String, Map<String, Object>> userMap = new LinkedHashMap<>();
        for (Map<String, Object> row : userRows) {
            String dateStr = String.valueOf(row.get("date"));
            userMap.put(dateStr, row);
        }

        // 生成连续 N 天的数据
        List<DailyTrendVO> trends = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            String dateStr = date.toString();

            DailyTrendVO vo = new DailyTrendVO();
            vo.setDate(dateStr);

            Map<String, Object> submitRow = submitMap.get(dateStr);
            if (submitRow != null) {
                vo.setSubmissionCount(toLong(submitRow.get("submission_count")));
                vo.setAcceptedCount(toLong(submitRow.get("accepted_count")));
            } else {
                vo.setSubmissionCount(0L);
                vo.setAcceptedCount(0L);
            }

            Map<String, Object> userRow = userMap.get(dateStr);
            if (userRow != null) {
                vo.setNewUserCount(toLong(userRow.get("new_user_count")));
            } else {
                vo.setNewUserCount(0L);
            }

            trends.add(vo);
        }
        return trends;
    }

    @Override
    public ActivityAnalysisVO getStatsActivity() {
        ActivityAnalysisVO vo = new ActivityAnalysisVO();

        // 难度分布：按 difficulty GROUP BY COUNT
        QueryWrapper<Problem> diffQuery = new QueryWrapper<>();
        diffQuery.select("difficulty", "COUNT(*) as cnt")
                .groupBy("difficulty");
        List<Map<String, Object>> diffRows = problemMapper.selectMaps(diffQuery);
        Map<String, Long> diffMap = new LinkedHashMap<>();
        for (Map<String, Object> row : diffRows) {
            Integer diff = toInt(row.get("difficulty"));
            String label = switch (diff) {
                case 1 -> "Easy";
                case 2 -> "Medium";
                case 3 -> "Hard";
                default -> "Unknown";
            };
            diffMap.put(label, toLong(row.get("cnt")));
        }
        vo.setDifficultyDistribution(diffMap);

        // 热门题目 TOP 10（按 submitNum 降序）
        LambdaQueryWrapper<Problem> topWrapper = new LambdaQueryWrapper<>();
        topWrapper.isNotNull(Problem::getSubmitNum)
                .gt(Problem::getSubmitNum, 0)
                .orderByDesc(Problem::getSubmitNum)
                .last("LIMIT 10");
        List<Problem> topProblems = problemMapper.selectList(topWrapper);
        List<ActivityAnalysisVO.TopProblemVO> topVOs = topProblems.stream().map(p -> {
            ActivityAnalysisVO.TopProblemVO topVO = new ActivityAnalysisVO.TopProblemVO();
            topVO.setId(p.getId());
            topVO.setDisplayId(StringUtils.hasText(p.getDisplayId()) ? p.getDisplayId() : "P-" + (p.getId() + 1000));
            topVO.setTitle(p.getTitle());
            topVO.setSubmitCount(p.getSubmitNum() != null ? p.getSubmitNum().longValue() : 0L);
            return topVO;
        }).collect(Collectors.toList());
        vo.setTopProblems(topVOs);

        // 语言分布：按 language GROUP BY COUNT
        QueryWrapper<ProblemSubmission> langQuery = new QueryWrapper<>();
        langQuery.select("language", "COUNT(*) as cnt")
                .groupBy("language");
        List<Map<String, Object>> langRows = problemSubmissionMapper.selectMaps(langQuery);
        Map<String, Long> langMap = new LinkedHashMap<>();
        for (Map<String, Object> row : langRows) {
            String lang = String.valueOf(row.get("language"));
            langMap.put(lang, toLong(row.get("cnt")));
        }
        vo.setLanguageDistribution(langMap);

        return vo;
    }

    // ==================== 私有工具方法 ====================

    /**
     * 批量构建 ArenaRoomVO 分页（带关联查询）
     */
    private Page<AdminArenaRoomVO> buildArenaRoomVOPage(Page<ArenaRoom> roomPage) {
        Page<AdminArenaRoomVO> voPage = new Page<>(roomPage.getCurrent(), roomPage.getSize(), roomPage.getTotal());
        List<AdminArenaRoomVO> voList = roomPage.getRecords().stream()
                .map(this::buildArenaRoomVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 构建单个 ArenaRoomVO（关联查询 creatorName / problemTitle / playerCount）
     */
    private AdminArenaRoomVO buildArenaRoomVO(ArenaRoom room) {
        AdminArenaRoomVO vo = new AdminArenaRoomVO();
        BeanUtils.copyProperties(room, vo);

        // 查询房主名称
        if (room.getCreatorId() != null) {
            User creator = userMapper.selectById(room.getCreatorId());
            if (creator != null) {
                vo.setCreatorName(StringUtils.hasText(creator.getNickname()) ? creator.getNickname() : creator.getUsername());
            }
        }

        // 查询题目标题
        if (room.getProblemId() != null) {
            Problem problem = problemMapper.selectById(room.getProblemId());
            if (problem != null) {
                vo.setProblemTitle(problem.getTitle());
            }
        }

        // 查询玩家数量
        LambdaQueryWrapper<ArenaRoomUser> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(ArenaRoomUser::getRoomId, room.getId());
        Long playerCount = arenaRoomUserMapper.selectCount(countWrapper);
        vo.setPlayerCount(playerCount != null ? playerCount.intValue() : 0);

        return vo;
    }

    /**
     * 从聚合查询结果中提取 count 值
     */
    private Long extractCountFromMap(List<Map<String, Object>> result) {
        if (result == null || result.isEmpty()) return 0L;
        Map<String, Object> row = result.get(0);
        if (row == null || row.isEmpty()) return 0L;
        Object cnt = row.values().iterator().next();
        return toLong(cnt);
    }

    private Long toLong(Object obj) {
        if (obj == null) return 0L;
        return ((Number) obj).longValue();
    }

    private Integer toInt(Object obj) {
        if (obj == null) return 0;
        return ((Number) obj).intValue();
    }
}
