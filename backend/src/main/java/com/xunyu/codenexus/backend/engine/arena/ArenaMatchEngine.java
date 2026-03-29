package com.xunyu.codenexus.backend.engine.arena;

import cn.hutool.json.JSONUtil;
import com.xunyu.codenexus.backend.model.dto.response.arena.MatchStatusVO;
import com.xunyu.codenexus.backend.service.ArenaRoomService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 核心匹配引擎后台调度器
 * 绝对摒弃 MySQL 轮询，纯内存与 Redis 级别操作 (已剥离 Redisson)
 *
 * @author CodeNexusBuilder
 */
@Slf4j
@Component
public class ArenaMatchEngine {

    // 提取静态常量解决魔法值重复问题
    private static final String MATCH_POOL_KEY = "codenexus:arena:match:pool";
    private static final String MATCH_TIME_KEY = "codenexus:arena:match:time";
    private static final String LOCK_KEY = "codenexus:lock:matchmaking";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ArenaRoomService arenaRoomService;

    @Scheduled(fixedDelay = 2000)
    public void executeMatchmaking() {
        // 使用原生的 Redis SETNX 实现轻量级分布式锁，避免引入冗余的 Redisson 依赖
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(LOCK_KEY, "1", 10, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            return; // 未抢到锁，直接返回
        }

        try {
            Set<String> waitingUsers = stringRedisTemplate.opsForZSet().range(MATCH_POOL_KEY, 0, -1);
            if (CollectionUtils.isEmpty(waitingUsers) || waitingUsers.size() < 2) {
                return;
            }

            processMatchingLogic(waitingUsers);

        } finally {
            // 释放锁
            stringRedisTemplate.delete(LOCK_KEY);
        }
    }

    /**
     * 抽取匹配核心逻辑，降低圈复杂度 (Cognitive Complexity)
     */
    private void processMatchingLogic(Set<String> waitingUsers) {
        long currentTime = System.currentTimeMillis();
        Set<String> matchedSet = new HashSet<>();

        for (String userIdStr : waitingUsers) {
            if (matchedSet.contains(userIdStr)) continue;

            Double myScore = stringRedisTemplate.opsForZSet().score(MATCH_POOL_KEY, userIdStr);
            if (myScore == null) continue;

            String joinTimeStr = (String) stringRedisTemplate.opsForHash().get(MATCH_TIME_KEY, userIdStr);
            long joinTime = joinTimeStr != null ? Long.parseLong(joinTimeStr) : currentTime;
            int waitSeconds = (int) ((currentTime - joinTime) / 1000);

            double delta = Math.min(50 + (waitSeconds / 5.0) * 25, 300);

            Set<String> candidates = stringRedisTemplate.opsForZSet().rangeByScore(MATCH_POOL_KEY, myScore - delta, myScore + delta);
            if (candidates == null) continue;

            String opponentId = findOpponent(userIdStr, candidates, matchedSet);

            if (opponentId != null) {
                matchedSet.add(userIdStr);
                matchedSet.add(opponentId);

                stringRedisTemplate.opsForZSet().remove(MATCH_POOL_KEY, userIdStr, opponentId);
                stringRedisTemplate.opsForHash().delete(MATCH_TIME_KEY, userIdStr, opponentId);

                final String opponentFinal = opponentId;
                Long u1 = Long.parseLong(userIdStr);
                Long u2 = Long.parseLong(opponentFinal);
                CompletableFuture.runAsync(() -> buildRoomAndNotify(u1, u2));

                log.info("[匹配引擎] 天梯配对成功！ 红方: {} vs 蓝方: {}", userIdStr, opponentId);
            }
        }
    }

    /**
     * 在候选人中寻找第一个可用对手
     */
    private String findOpponent(String userIdStr, Set<String> candidates, Set<String> matchedSet) {
        for (String candidate : candidates) {
            if (!candidate.equals(userIdStr) && !matchedSet.contains(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private void buildRoomAndNotify(Long u1, Long u2) {
        try {
            String roomCode = arenaRoomService.createMatchRoom(u1, u2);

            String t1 = UUID.randomUUID().toString();
            String t2 = UUID.randomUUID().toString();

            stringRedisTemplate.opsForValue().set("codenexus:arena:ticket:" + t1, roomCode + ":" + u1, 60, TimeUnit.SECONDS);
            stringRedisTemplate.opsForValue().set("codenexus:arena:ticket:" + t2, roomCode + ":" + u2, 60, TimeUnit.SECONDS);

            MatchStatusVO resultU1 = new MatchStatusVO();
            resultU1.setRoomCode(roomCode);
            resultU1.setTicket(t1);

            MatchStatusVO resultU2 = new MatchStatusVO();
            resultU2.setRoomCode(roomCode);
            resultU2.setTicket(t2);

            stringRedisTemplate.opsForValue().set("codenexus:arena:match:result:" + u1, JSONUtil.toJsonStr(resultU1), 30, TimeUnit.SECONDS);
            stringRedisTemplate.opsForValue().set("codenexus:arena:match:result:" + u2, JSONUtil.toJsonStr(resultU2), 30, TimeUnit.SECONDS);

        } catch (Exception e) {
            log.error("[匹配引擎] 创建比赛房间失败，严重故障 u1:{}, u2:{}", u1, u2, e);
        }
    }
}