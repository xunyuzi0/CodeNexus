package com.xunyu.codenexus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xunyu.codenexus.backend.engine.arena.ArenaWebSocketHandler;
import com.xunyu.codenexus.backend.mapper.ArenaRoomMapper;
import com.xunyu.codenexus.backend.mapper.ArenaRoomUserMapper;
import com.xunyu.codenexus.backend.mapper.UserStatisticsMapper;
import com.xunyu.codenexus.backend.model.entity.ArenaRoom;
import com.xunyu.codenexus.backend.model.entity.ArenaRoomUser;
import com.xunyu.codenexus.backend.model.entity.UserStatistics;
import com.xunyu.codenexus.backend.service.ArenaSettlementService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 天梯对战结算引擎实现类
 *
 * @author CodeNexusBuilder
 */
@Slf4j
@Service
public class ArenaSettlementServiceImpl implements ArenaSettlementService {

    @Resource
    private ArenaRoomMapper arenaRoomMapper;

    @Resource
    private ArenaRoomUserMapper arenaRoomUserMapper;

    @Resource
    private UserStatisticsMapper userStatisticsMapper;

    @Resource
    private ArenaWebSocketHandler arenaWebSocketHandler;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void settleMatch(String roomCode, Long winnerId) {
        // 1. 查询房间，防并发重复结算 (如果有两个人同时提交 AC)
        LambdaQueryWrapper<ArenaRoom> roomQw = new LambdaQueryWrapper<>();
        roomQw.eq(ArenaRoom::getRoomCode, roomCode).eq(ArenaRoom::getIsDeleted, 0);
        ArenaRoom room = arenaRoomMapper.selectOne(roomQw);

        if (room == null || "FINISHED".equals(room.getStatus())) {
            log.warn("[天梯结算] 房间 {} 不存在或已结算，跳过", roomCode);
            return;
        }

        // 2. 将房间状态推进为 FINISHED，锁死对局
        room.setStatus("FINISHED");
        arenaRoomMapper.updateById(room);

        // 3. 获取房间内所有玩家
        LambdaQueryWrapper<ArenaRoomUser> ruQw = new LambdaQueryWrapper<>();
        ruQw.eq(ArenaRoomUser::getRoomId, room.getId()).eq(ArenaRoomUser::getIsDeleted, 0);
        List<ArenaRoomUser> roomUsers = arenaRoomUserMapper.selectList(ruQw);

        // 4. 计算积分并落地
        for (ArenaRoomUser ru : roomUsers) {
            Long userId = ru.getUserId();
            boolean isWinner = userId.equals(winnerId);

            // 确保统计记录存在
            LambdaQueryWrapper<UserStatistics> statsQw = new LambdaQueryWrapper<>();
            statsQw.eq(UserStatistics::getUserId, userId).eq(UserStatistics::getIsDeleted, 0);
            UserStatistics stats = userStatisticsMapper.selectOne(statsQw);

            if (stats == null) {
                stats = new UserStatistics();
                stats.setUserId(userId);
                stats.setSolvedCount(0);
                stats.setContinuousCheckinDays(0);
                stats.setArenaScore(1000);
                stats.setArenaMatches(0);
                stats.setArenaWins(0);
                userStatisticsMapper.insert(stats);
            }

            // 【计分规则】胜者 +50分，败者 -30分
            int scoreChange = isWinner ? 50 : -30;

            LambdaUpdateWrapper<UserStatistics> updateWrapper = new LambdaUpdateWrapper<>();
            // 总对局数 +1，更新积分（用 GREATEST 防跌破 0 分）
            updateWrapper.eq(UserStatistics::getId, stats.getId())
                    .setSql("arena_matches = arena_matches + 1")
                    .setSql("arena_score = GREATEST(arena_score + " + scoreChange + ", 0)");

            if (isWinner) {
                updateWrapper.setSql("arena_wins = arena_wins + 1");
            }

            userStatisticsMapper.update(null, updateWrapper);
        }

        log.info("[天梯结算] 房间 {} 结算完成！胜者: {}, 分数已更新。", roomCode, winnerId);

        // 5. 推送结算系统广播到前端对战面板
        arenaWebSocketHandler.pushSystemBattleLog(
                roomCode,
                "SUCCESS",
                "🏆 MATCH FINISHED! 玩家 " + winnerId + " 率先 AC 夺得胜利！天梯积分已结算。",
                null
        );
    }
}