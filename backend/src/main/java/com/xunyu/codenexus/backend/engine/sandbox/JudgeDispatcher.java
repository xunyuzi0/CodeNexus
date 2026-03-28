package com.xunyu.codenexus.backend.engine.sandbox;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xunyu.codenexus.backend.mapper.ProblemSubmissionMapper;
import com.xunyu.codenexus.backend.mapper.ProblemTestcaseMapper;
import com.xunyu.codenexus.backend.model.dto.response.problem.SubmitResponseVO;
import com.xunyu.codenexus.backend.model.entity.ProblemSubmission;
import com.xunyu.codenexus.backend.model.entity.ProblemTestcase;
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
        log.info("[判题调度器] 核心消费者引擎启动成功，开始监听队列: {}", ProblemRunServiceImpl.JUDGE_QUEUE_KEY);
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

    private void processSubmission(Long submissionId) {
        try {
            ProblemSubmission submission = problemSubmissionMapper.selectById(submissionId);
            if (submission == null) return;

            Long problemId = submission.getProblemId();
            String code = submission.getCode();

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

            int finalStatus = 1; // 1-AC
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
                    statusVO.setMessage("编译或运行报错:\n" + result.output());

                    // ================= 核心修复点 =================
                    // 将沙箱底层的报错堆栈直接打印到控制台，不再瞎子摸象！
                    log.error("[沙箱引擎拦截] 任务 {} 执行报错，沙箱控制台输出: \n{}", submissionId, result.output());
                    // ==============================================

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
                    statusVO.setMessage("解答错误! 期望输出: " + expectedOutput + ", 实际输出: " + actualOutput);
                    break;
                }
                flushCache(submissionId, statusVO);
            }

            submission.setStatus(finalStatus);
            submission.setTimeCost(totalTime);
            submission.setMemoryCost(0.0);
            problemSubmissionMapper.updateById(submission);

            statusVO.setStatus("OK");
            if (finalStatus == 1) {
                statusVO.setMessage("所有测试用例均通过！(Accepted)");
            }
            flushCache(submissionId, statusVO);

            log.info("[判题调度器] 任务 {} 执行完毕! 最终状态: {}, 总耗时: {}ms", submissionId, finalStatus, totalTime);

        } catch (Exception e) {
            log.error("[判题调度器] 任务 {} 执行发生不可逆系统级异常!", submissionId, e);

            ProblemSubmission submission = problemSubmissionMapper.selectById(submissionId);
            if (submission != null) {
                submission.setStatus(6); // 6-System Error
                problemSubmissionMapper.updateById(submission);
            }

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