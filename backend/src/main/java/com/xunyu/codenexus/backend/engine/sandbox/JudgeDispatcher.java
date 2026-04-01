package com.xunyu.codenexus.backend.engine.sandbox;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xunyu.codenexus.backend.engine.arena.ArenaWebSocketHandler;
import com.xunyu.codenexus.backend.mapper.ArenaRoomMapper;
import com.xunyu.codenexus.backend.mapper.ArenaRoomUserMapper;
import com.xunyu.codenexus.backend.mapper.ProblemSubmissionMapper;
import com.xunyu.codenexus.backend.mapper.ProblemTestcaseMapper;
import com.xunyu.codenexus.backend.model.dto.response.problem.SubmitResponseVO;
import com.xunyu.codenexus.backend.model.entity.ArenaRoom;
import com.xunyu.codenexus.backend.model.entity.ArenaRoomUser;
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
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 核心异步判题消费者调度器 (The Dispatcher Worker)
 * 支持判题进度实时推送至竞技大厅战况面板，并触发天梯结算
 *
 * @author CodeNexusBuilder
 */
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

    // 注入我们全新编写的天梯结算引擎
    @Resource
    private ArenaSettlementService arenaSettlementService;

    @Resource
    private ArenaWebSocketHandler arenaWebSocketHandler;
    @Resource
    private ArenaRoomUserMapper arenaRoomUserMapper;
    @Resource
    private ArenaRoomMapper arenaRoomMapper;

    private ExecutorService judgeThreadPool;
    private Thread pollerThread;
    private volatile boolean isRunning = true;

    @PostConstruct
    public void init() {
        this.judgeThreadPool = new ThreadPoolExecutor(
                4, 8,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        this.pollerThread = new Thread(this::pollQueue, "Judge-Poller-Thread");
        this.pollerThread.start();
        log.info("[判题调度器] 核心消费者引擎启动成功...");
    }

    @PreDestroy
    public void destroy() {
        isRunning = false;
        if (pollerThread != null) pollerThread.interrupt();
        if (judgeThreadPool != null) judgeThreadPool.shutdown();
        log.info("[判题调度器] 核心消费者引擎已平滑关闭。");
    }

    private void pollQueue() {
        while (isRunning && !Thread.currentThread().isInterrupted()) {
            try {
                String submissionIdStr = stringRedisTemplate.opsForList().leftPop(ProblemRunServiceImpl.JUDGE_QUEUE_KEY, 2, TimeUnit.SECONDS);

                if (StringUtils.hasText(submissionIdStr)) {
                    Long submissionId = Long.parseLong(submissionIdStr);
                    log.info("[判题调度器] 获取到新任务 SubmissionId: {}, 准备派发...", submissionId);
                    judgeThreadPool.submit(() -> processSubmission(submissionId));
                }
            } catch (Exception e) {
                log.error("[判题调度器] 拉取队列任务发生异常", e);
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    private String findActiveRoomCode(Long userId) {
        LambdaQueryWrapper<ArenaRoomUser> ruQw = new LambdaQueryWrapper<>();
        ruQw.eq(ArenaRoomUser::getUserId, userId).orderByDesc(ArenaRoomUser::getId).last("LIMIT 1");
        ArenaRoomUser ru = arenaRoomUserMapper.selectOne(ruQw);

        if (ru != null) {
            ArenaRoom room = arenaRoomMapper.selectById(ru.getRoomId());
            if (room != null && ("WAITING".equals(room.getStatus()) || "BATTLING".equals(room.getStatus()))) {
                return room.getRoomCode();
            }
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

            LambdaQueryWrapper<ProblemTestcase> testcaseWrapper = new LambdaQueryWrapper<>();
            testcaseWrapper.eq(ProblemTestcase::getProblemId, problemId).orderByAsc(ProblemTestcase::getSortOrder);
            List<ProblemTestcase> testcases = problemTestcaseMapper.selectList(testcaseWrapper);

            SubmitResponseVO statusVO = new SubmitResponseVO();
            statusVO.setStatus("JUDGING");
            statusVO.setMessage("正在执行测试用例...");
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
                arenaWebSocketHandler.pushSystemBattleLog(roomCode, "INFO", "exec: JVM sandbox initialized, running test cases...", null);
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
                    if (roomCode != null) {
                        arenaWebSocketHandler.pushSystemBattleLog(roomCode, "ERROR", "exec: Time Limit Exceeded on Test Case " + (i + 1), null);
                    }
                    break;
                } else if ("ERROR".equals(result.status())) {
                    currentCp.setStatus("RE");
                    statusVO.setMessage("编译或运行报错:\n" + result.output());
                    log.error("[沙箱引擎拦截] 任务 {} 执行报错: \n{}", submissionId, result.output());

                    finalStatus = 5;
                    if (roomCode != null) {
                        arenaWebSocketHandler.pushSystemBattleLog(roomCode, "ERROR", "exec: Compilation/Runtime Error! Check syntax.", result.output());
                    }
                    break;
                }

                String actualOutput = result.output() != null ? result.output().trim() : "";
                String expectedOutput = tc.getExpectedOutput() != null ? tc.getExpectedOutput().trim() : "";

                if (actualOutput.equals(expectedOutput)) {
                    currentCp.setStatus("AC");
                    if (roomCode != null) {
                        arenaWebSocketHandler.pushSystemBattleLog(roomCode, "SUCCESS", "exec: Test Case " + (i + 1) + "/" + testcases.size() + " Passed... [" + result.timeCost() + "ms]", null);
                    }
                } else {
                    currentCp.setStatus("WA");
                    finalStatus = 2;
                    statusVO.setMessage("解答错误! 期望输出: " + expectedOutput + ", 实际输出: " + actualOutput);

                    if (roomCode != null) {
                        arenaWebSocketHandler.pushSystemBattleLog(roomCode, "WARN", "exec: Wrong Answer on Test Case " + (i + 1) + ". Differs from expected output.", null);
                    }
                    break;
                }
                flushCache(submissionId, statusVO);
            }

            // 更新状态机打卡
            problemSubmissionService.updateSubmissionStatus(submissionId, finalStatus, totalTime, 0.0);

            statusVO.setStatus("OK");
            if (finalStatus == 1) {
                statusVO.setMessage("所有测试用例均通过！(Accepted)");
                if (roomCode != null) {
                    arenaWebSocketHandler.pushSystemBattleLog(roomCode, "SUCCESS", "exec: All test cases passed! (Accepted)", null);

                    // 🎯 核心联动：有人 AC，立即通过结算引擎打响比赛结束的鸣枪，并结算排位分！
                    arenaSettlementService.settleMatch(roomCode, submission.getUserId());
                }
            }
            flushCache(submissionId, statusVO);

            log.info("[判题调度器] 任务 {} 执行完毕! 最终状态: {}, 总耗时: {}ms", submissionId, finalStatus, totalTime);

        } catch (Exception e) {
            log.error("[判题调度器] 任务 {} 执行发生不可逆系统级异常!", submissionId, e);
            problemSubmissionService.updateSubmissionStatus(submissionId, 6, 0, 0.0);

            SubmitResponseVO errorVO = new SubmitResponseVO();
            errorVO.setStatus("SYSTEM_ERROR");
            errorVO.setMessage("平台底层沙箱调度失败: " + e.getMessage());
            flushCache(submissionId, errorVO);
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