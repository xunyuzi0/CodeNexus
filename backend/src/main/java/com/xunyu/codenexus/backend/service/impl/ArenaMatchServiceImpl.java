package com.xunyu.codenexus.backend.service.impl;

import cn.hutool.json.JSONUtil;
import com.xunyu.codenexus.backend.common.context.UserContext;
import com.xunyu.codenexus.backend.common.result.ResultCode;
import com.xunyu.codenexus.backend.model.dto.response.arena.MatchStatusVO;
import com.xunyu.codenexus.backend.model.entity.User;
import com.xunyu.codenexus.backend.service.ArenaMatchService;
import com.xunyu.codenexus.backend.service.UserService;
import com.xunyu.codenexus.backend.utils.AssertUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * 竞技场天梯匹配业务实现类
 *
 * @author CodeNexusBuilder
 */
@Slf4j
@Service
public class ArenaMatchServiceImpl implements ArenaMatchService {

    private static final String MATCH_POOL_KEY = "codenexus:arena:match:pool";
    private static final String MATCH_TIME_KEY = "codenexus:arena:match:time";
    private static final String MATCH_RESULT_KEY_PREFIX = "codenexus:arena:match:result:";

    // 【新增】：心跳键前缀
    private static final String MATCH_ACTIVE_KEY_PREFIX = "codenexus:arena:match:active:";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserService userService;

    @Override
    public boolean joinMatchPool() {
        Long userId = UserContext.getUserId();
        User user = userService.getById(userId);
        AssertUtil.notNull(user, ResultCode.UNAUTHORIZED, "用户异常");

        String userIdStr = String.valueOf(userId);

        // 1. 将用户天梯分打入 ZSET 队列
        stringRedisTemplate.opsForZSet().add(MATCH_POOL_KEY, userIdStr, user.getRatingScore());

        // 2. 记录加入队列的时间戳，用于计算等待时间扩大匹配阈值
        stringRedisTemplate.opsForHash().put(MATCH_TIME_KEY, userIdStr, String.valueOf(System.currentTimeMillis()));

        // 3. 【核心修复】：注入 8 秒心跳保活锁
        stringRedisTemplate.opsForValue().set(MATCH_ACTIVE_KEY_PREFIX + userIdStr, "1", 8, TimeUnit.SECONDS);

        log.info("[匹配系统] 用户 {} (排位分:{}) 已加入天梯匹配池", userId, user.getRatingScore());
        return true;
    }

    @Override
    public boolean leaveMatchPool() {
        Long userId = UserContext.getUserId();
        String userIdStr = String.valueOf(userId);

        stringRedisTemplate.opsForZSet().remove(MATCH_POOL_KEY, userIdStr);
        stringRedisTemplate.opsForHash().delete(MATCH_TIME_KEY, userIdStr);

        // 退出时顺便清理心跳（不清理也会在8秒后自动过期，手动清理更严谨）
        stringRedisTemplate.delete(MATCH_ACTIVE_KEY_PREFIX + userIdStr);

        log.info("[匹配系统] 用户 {} 取消了匹配", userId);
        return true;
    }

    @Override
    public MatchStatusVO pollMatchStatus() {
        Long userId = UserContext.getUserId();
        String userIdStr = String.valueOf(userId);

        // 【核心修复】：前端每次高频轮询，强制刷新 8 秒心跳锁！
        // 只要前端轮询停了（断网或关页面），心跳将在 8 秒后自动消散。
        stringRedisTemplate.opsForValue().set(MATCH_ACTIVE_KEY_PREFIX + userIdStr, "1", 8, TimeUnit.SECONDS);

        // 去信箱里看看有没有自己的匹配结果
        String resultJson = stringRedisTemplate.opsForValue().get(MATCH_RESULT_KEY_PREFIX + userId);

        MatchStatusVO vo = new MatchStatusVO();
        if (StringUtils.hasText(resultJson)) {
            // 匹配成功！
            vo = JSONUtil.toBean(resultJson, MatchStatusVO.class);
            vo.setStatus("SUCCESS");
        } else {
            // 检查自己是否还在池子里，防止被意外剔除
            Double score = stringRedisTemplate.opsForZSet().score(MATCH_POOL_KEY, userIdStr);
            if (score != null) {
                vo.setStatus("MATCHING");
            } else {
                // 不在池子里，信箱也没结果，说明匹配异常或者被后台踢出
                vo.setStatus("FAILED");
            }
        }
        return vo;
    }
}