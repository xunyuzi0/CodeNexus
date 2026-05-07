package com.xunyu.codenexus.backend.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xunyu.codenexus.backend.common.context.UserContext;
import com.xunyu.codenexus.backend.common.result.ResultCode;
import com.xunyu.codenexus.backend.engine.sandbox.DockerSandboxEngine;
import com.xunyu.codenexus.backend.mapper.ProblemMapper;
import com.xunyu.codenexus.backend.mapper.ProblemSubmissionMapper;
import com.xunyu.codenexus.backend.mapper.UserProblemStateMapper;
import com.xunyu.codenexus.backend.model.dto.request.problem.RunCodeRequest;
import com.xunyu.codenexus.backend.model.dto.request.problem.SubmitCodeRequest;
import com.xunyu.codenexus.backend.model.dto.response.problem.RunResultVO;
import com.xunyu.codenexus.backend.model.dto.response.problem.SubmitResponseVO;
import com.xunyu.codenexus.backend.model.entity.ProblemSubmission;
import com.xunyu.codenexus.backend.model.entity.UserProblemState;
import com.xunyu.codenexus.backend.service.ProblemRunService;
import com.xunyu.codenexus.backend.utils.AssertUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 核心代码执行与判题业务逻辑实现类
 *
 * @author CodeNexusBuilder (The Core Specialist)
 */
@Slf4j
@Service
public class ProblemRunServiceImpl implements ProblemRunService {

    // ==========================================
    // 架构常量定义
    // ==========================================
    /**
     * Redis MQ 队列名：判题消费者会阻塞监听此队列
     */
    public static final String JUDGE_QUEUE_KEY = "codenexus:queue:judge";
    /**
     * Redis 状态机缓存前缀：用于前端高频轮询，防止击穿 MySQL
     */
    public static final String JUDGE_STATUS_PREFIX = "codenexus:judge:status:";

    @Resource
    private DockerSandboxEngine dockerSandboxEngine;

    @Resource
    private ProblemMapper problemMapper;

    @Resource
    private ProblemSubmissionMapper problemSubmissionMapper;

    @Resource
    private UserProblemStateMapper userProblemStateMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    // ==========================================
    // 第 1 阶段：同步自测代码 (已跑通)
    // ==========================================
    @Override
    public List<RunResultVO> runCode(Long problemId, RunCodeRequest request) {
        List<RunResultVO> results = new ArrayList<>();

        // 如果用户没有传任何 input，给一个默认的空输入跑一次测试编译是否通过
        List<String> inputs = request.getInputs() != null && !request.getInputs().isEmpty()
                ? request.getInputs()
                : List.of("");

        for (String input : inputs) {
            RunResultVO vo = new RunResultVO();
            vo.setInput(input);
            vo.setExpected(null); // 自测模式没有标准输出做对照，只能返回 null

            // 调度底层沙箱引擎
            if ("java".equalsIgnoreCase(request.getLanguage())) {
                DockerSandboxEngine.SandboxResult result = dockerSandboxEngine.executeJava(request.getCode(), input);

                // 状态映射
                if ("TLE".equals(result.status())) {
                    vo.setStatus("TLE"); // 运行超时
                } else if ("ERROR".equals(result.status())) {
                    vo.setStatus("ERROR"); // 编译或运行时报错
                } else {
                    vo.setStatus("AC"); // 正常跑通（由于没有 expected 对比，只要不报错算临时 AC）
                }

                vo.setOutput(result.output());
                vo.setRuntime(result.timeCost() + "ms");
            } else {
                vo.setStatus("ERROR");
                vo.setOutput("暂不支持的编程语言: " + request.getLanguage());
                vo.setRuntime("0ms");
            }
            results.add(vo);
        }

        return results;
    }


    // ==========================================
    // 第 2 阶段：异步提交判题 (打入 MQ)
    // ==========================================
    @Override
    public Long submitCode(Long problemId, SubmitCodeRequest request) {
        Long userId = UserContext.getUserId();

        // 1. 数据库落盘：创建 PENDING 状态的提交记录
        ProblemSubmission submission = new ProblemSubmission();
        submission.setUserId(userId);
        submission.setProblemId(problemId);
        submission.setLanguage(request.getLanguage());
        submission.setCode(request.getCode());
        submission.setStatus(0); // 0 代表 PENDING (正在排队/执行中)
        problemSubmissionMapper.insert(submission);

        Long submissionId = submission.getId();

        // 2. 只有用户首次提交该题时才增加提交次数（避免重复提交导致通过率下降）
        LambdaQueryWrapper<UserProblemState> stateQuery = new LambdaQueryWrapper<>();
        stateQuery.eq(UserProblemState::getUserId, userId)
                .eq(UserProblemState::getProblemId, problemId)
                .eq(UserProblemState::getIsDeleted, 0);
        UserProblemState existingState = userProblemStateMapper.selectOne(stateQuery);
        if (existingState == null) {
            problemMapper.updateSubmitNum(problemId);
        }

        // 3. 初始化 Redis 判题状态缓存 (预先填充，防止前端第一秒轮询拿不到数据报错)
        SubmitResponseVO initialStatus = new SubmitResponseVO();
        initialStatus.setStatus("JUDGING");
        initialStatus.setMessage("任务已进入评测队列，等待沙箱资源分配...");
        initialStatus.setCheckpoints(new ArrayList<>()); // 刚排队时还没有任何检查点状态

        stringRedisTemplate.opsForValue().set(
                JUDGE_STATUS_PREFIX + submissionId,
                JSONUtil.toJsonStr(initialStatus),
                1, TimeUnit.HOURS // 缓存保留1小时即可，节省 Redis 内存
        );

        // 4. 将任务投递到 Redis List 作为轻量级 MQ
        // 生产者从右侧 (RightPush) 进，后续我们写的消费者从左侧 (LeftPop) 阻塞拉取
        stringRedisTemplate.opsForList().rightPush(JUDGE_QUEUE_KEY, String.valueOf(submissionId));

        log.info("[核心调度引擎] 判题任务投递成功! SubmissionId: {}, 当前队列积压量: {}",
                submissionId, stringRedisTemplate.opsForList().size(JUDGE_QUEUE_KEY));

        // 4. 立刻返回 ID 给前端，Web 线程释放，极大地提高了接口并发吞吐量
        return submissionId;
    }


    // ==========================================
    // 第 3 阶段：状态高频轮询 (Redis Cache 读取)
    // ==========================================
    @Override
    public SubmitResponseVO pollStatus(Long submissionId) {
        // 1. 越权校验：绝对不能让别人通过穷举 ID 偷窥你的判题进度和代码
        ProblemSubmission submission = problemSubmissionMapper.selectById(submissionId);
        AssertUtil.notNull(submission, ResultCode.NOT_FOUND, "提交记录不存在");
        AssertUtil.equals(submission.getUserId(), UserContext.getUserId(), ResultCode.FORBIDDEN, "无权查看该判题记录");

        // 2. O(1) 复杂度从 Redis 抓取实时状态 (我们的后台判题消费者会疯狂更新这个 Key)
        String statusJson = stringRedisTemplate.opsForValue().get(JUDGE_STATUS_PREFIX + submissionId);
        if (StringUtils.hasText(statusJson)) {
            return JSONUtil.toBean(statusJson, SubmitResponseVO.class);
        }

        // 3. 兜底处理 (Fallback)：如果 1 小时后 Redis 缓存过期被驱逐了，或者消费者挂了
        // 我们根据数据库的历史记录反向推导状态，保证前端总能拿到结果
        SubmitResponseVO fallbackVO = new SubmitResponseVO();
        if (submission.getStatus() == 0) {
            fallbackVO.setStatus("JUDGING");
            fallbackVO.setMessage("判题时间过长或沙箱排队拥挤，请稍后在历史记录中查看...");
        } else {
            fallbackVO.setStatus("OK"); // 状态大于 0，说明沙箱已经跑完了
            fallbackVO.setMessage("判题已完成，因缓存过期，详细执行用例请查看历史提交记录。");
        }

        return fallbackVO;
    }
}