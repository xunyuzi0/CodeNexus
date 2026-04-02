package com.xunyu.codenexus.backend.engine.sandbox;

import cn.hutool.json.JSONUtil;
import com.xunyu.codenexus.backend.engine.arena.ArenaWebSocketHandler;
import com.xunyu.codenexus.backend.mapper.ProblemSubmissionMapper;
import com.xunyu.codenexus.backend.mapper.ProblemTestcaseMapper;
import com.xunyu.codenexus.backend.model.dto.response.problem.SubmitResponseVO;
import com.xunyu.codenexus.backend.model.entity.ProblemSubmission;
import com.xunyu.codenexus.backend.model.entity.ProblemTestcase;
import com.xunyu.codenexus.backend.service.ArenaSettlementService;
import com.xunyu.codenexus.backend.service.ProblemSubmissionService;
import com.xunyu.codenexus.backend.service.impl.ProblemRunServiceImpl;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Component
public class JudgeDispatcher {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ProblemSubmissionMapper problemSubmissionMapper;
    @Resource
    private ProblemTestcaseMapper problemTestcaseMapper;
    @Resource
    private DockerSandboxEngine dockerSandboxEngine;
    @Resource
    private ProblemSubmissionService problemSubmissionService;
    @Resource
    private ArenaSettlementService arenaSettlementService;
    @Resource
    private ArenaWebSocketHandler arenaWebSocketHandler;

    // 引入 Spring 底层 JDBC 模板，降维打击 ORM 框架
    @Resource
    private JdbcTemplate jdbcTemplate;

    private ExecutorService judgeThreadPool;
    private Thread pollerThread;
    private volatile boolean isRunning = true;

    @PostConstruct
    public void init() {
        this.judgeThreadPool = new ThreadPoolExecutor(
                4, 8, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        this.pollerThread = new Thread(this::pollQueue, "Judge-Poller-Thread");
        this.pollerThread.start();
    }

    @PreDestroy
    public void destroy() {
        isRunning = false;
        if (pollerThread != null) pollerThread.interrupt();
        if (judgeThreadPool != null) judgeThreadPool.shutdown();
    }

    private void pollQueue() {
        while (isRunning && !Thread.currentThread().isInterrupted()) {
            try {
                String submissionIdStr = stringRedisTemplate.opsForList().leftPop(ProblemRunServiceImpl.JUDGE_QUEUE_KEY, 2, TimeUnit.SECONDS);
                if (StringUtils.hasText(submissionIdStr)) {
                    Long submissionId = Long.parseLong(submissionIdStr);
                    judgeThreadPool.submit(() -> processSubmission(submissionId));
                }
            } catch (Exception e) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    // 🎯 终极修复：使用原生 SQL 绕过 MyBatis-Plus 的 is_deleted=0 拦截！
    private String findActiveRoomCode(Long userId) {
        try {
            // 物理查询：只要你进过这个房间，不管是不是 is_deleted，我都把你抓出来
            String sql = "SELECT r.room_code FROM arena_room r " +
                    "JOIN arena_room_user ru ON r.id = ru.room_id " +
                    "WHERE ru.user_id = ? ORDER BY ru.id DESC LIMIT 1";
            String roomCode = jdbcTemplate.queryForObject(sql, String.class, userId);

            if (roomCode != null) {
                String statusSql = "SELECT status FROM arena_room WHERE room_code = ?";
                String status = jdbcTemplate.queryForObject(statusSql, String.class, roomCode);
                // 只要还没发奖 (FINISHED)，不管是 BATTLING 还是被误判的 DISMISSED，统统允许结算！
                if (!"FINISHED".equals(status)) {
                    return roomCode;
                }
            }
        } catch (Exception e) {
            log.warn("[判题调度器] 物理搜寻房间失败, UserID: {}", userId);
        }
        return null;
    }

    private void processSubmission(Long submissionId) {
        try {
            ProblemSubmission submission = problemSubmissionMapper.selectById(submissionId);
            if (submission == null) return;

            Long problemId = submission.getProblemId();
            String code = submission.getCode();

            String roomCode = findActiveRoomCode(submission.getUserId());

            // ... 测试用例拉取与沙箱执行逻辑保持不变 ...
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProblemTestcase> testcaseWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            testcaseWrapper.eq(ProblemTestcase::getProblemId, problemId).orderByAsc(ProblemTestcase::getSortOrder);
            List<ProblemTestcase> testcases = problemTestcaseMapper.selectList(testcaseWrapper);

            SubmitResponseVO statusVO = new SubmitResponseVO();
            statusVO.setStatus("JUDGING");
            List<SubmitResponseVO.CheckpointVO> checkpoints = new ArrayList<>();

            for (ProblemTestcase tc : testcases) {
                SubmitResponseVO.CheckpointVO cp = new SubmitResponseVO.CheckpointVO();
                cp.setId(tc.getId());
                cp.setStatus("PENDING");
                checkpoints.add(cp);
            }
            statusVO.setCheckpoints(checkpoints);
            flushCache(submissionId, statusVO);

            if (roomCode != null) {
                arenaWebSocketHandler.pushSystemBattleLog(roomCode, "INFO", "exec: JVM sandbox initialized...", null);
            }

            int finalStatus = 1;
            int totalTime = 0;

            for (int i = 0; i < testcases.size(); i++) {
                ProblemTestcase tc = testcases.get(i);
                SubmitResponseVO.CheckpointVO currentCp = checkpoints.get(i);
                currentCp.setStatus("RUNNING");
                flushCache(submissionId, statusVO);

                DockerSandboxEngine.SandboxResult result;
                if ("java".equalsIgnoreCase(submission.getLanguage())) {
                    result = dockerSandboxEngine.executeJava(code, tc.getInputData());
                } else {
                    result = new DockerSandboxEngine.SandboxResult("ERROR", "不支持的语言", 0);
                }

                currentCp.setTime(result.timeCost() + "ms");
                totalTime += (int) result.timeCost();

                if ("TLE".equals(result.status())) {
                    currentCp.setStatus("TLE");
                    finalStatus = 3;
                    break;
                } else if ("ERROR".equals(result.status())) {
                    currentCp.setStatus("RE");
                    finalStatus = 5;
                    break;
                }

                String actualOutput = result.output() != null ? result.output().trim() : "";
                String expectedOutput = tc.getExpectedOutput() != null ? tc.getExpectedOutput().trim() : "";

                if (actualOutput.equals(expectedOutput)) {
                    currentCp.setStatus("AC");
                } else {
                    currentCp.setStatus("WA");
                    finalStatus = 2;
                    break;
                }
                flushCache(submissionId, statusVO);
            }

            problemSubmissionService.updateSubmissionStatus(submissionId, finalStatus, totalTime, 0.0);
            statusVO.setStatus("OK");

            if (finalStatus == 1) {
                statusVO.setMessage("所有测试用例均通过！(Accepted)");
            }
            flushCache(submissionId, statusVO);
            log.info("[判题调度器] 任务 {} 执行完毕! 最终状态: {}, 总耗时: {}ms", submissionId, finalStatus, totalTime);

        } catch (Exception e) {
            log.error("[判题调度器] 系统异常", e);
            problemSubmissionService.updateSubmissionStatus(submissionId, 6, 0, 0.0);
        }
    }

    private void flushCache(Long submissionId, SubmitResponseVO vo) {
        stringRedisTemplate.opsForValue().set(
                ProblemRunServiceImpl.JUDGE_STATUS_PREFIX + submissionId,
                JSONUtil.toJsonStr(vo),
                1, TimeUnit.HOURS
        );
    }
}