package com.xunyu.codenexus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xunyu.codenexus.backend.engine.arena.ArenaWebSocketHandler;
import com.xunyu.codenexus.backend.mapper.UserMapper;
import com.xunyu.codenexus.backend.mapper.UserStatisticsMapper;
import com.xunyu.codenexus.backend.model.entity.User;
import com.xunyu.codenexus.backend.model.entity.UserStatistics;
import com.xunyu.codenexus.backend.service.ArenaSettlementService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
public class ArenaSettlementServiceImpl implements ArenaSettlementService {

    @Resource
    private UserStatisticsMapper userStatisticsMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ArenaWebSocketHandler arenaWebSocketHandler;
    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void settleMatch(String roomCode, Long winnerId) {
        String roomSql = "SELECT id, status FROM arena_room WHERE room_code = ?";
        List<Map<String, Object>> rooms = jdbcTemplate.queryForList(roomSql, roomCode);
        if (rooms.isEmpty()) return;

        Long roomId = ((Number) rooms.get(0).get("id")).longValue();
        String status = (String) rooms.get(0).get("status");

        if ("FINISHED".equals(status)) return;
        jdbcTemplate.update("UPDATE arena_room SET status = 'FINISHED' WHERE id = ?", roomId);

        // 🎯 核心碾压逻辑：抛弃数据库，直接读取 WebSocket 内存黑匣子！
        Set<Long> historyUsers = arenaWebSocketHandler.getRoomHistory(roomCode);
        List<Long> userIds = new ArrayList<>(historyUsers);

        if (userIds.size() < 2) {
            log.warn("[天梯结算] 房间 {} 玩家数据严重缺失 (内存记录仅 {} 人)，无法计算Elo", roomCode, userIds.size());
            return;
        }

        // 精准推导败者 ID
        Long loserId = null;
        for (Long uid : userIds) {
            if (!uid.equals(winnerId)) {
                loserId = uid;
                break;
            }
        }

        Map<Long, Integer> currentRatings = new HashMap<>();
        for (Long uid : userIds) {
            User u = userMapper.selectById(uid);
            currentRatings.put(uid, (u != null && u.getRatingScore() != null) ? u.getRatingScore() : 1000);
        }

        int winnerDisplayChange = 0;
        int loserDisplayChange = 0;

        for (Long userId : userIds) {
            boolean isWinner = userId.equals(winnerId);
            int myRating = currentRatings.getOrDefault(userId, 1000);
            int opRating = currentRatings.getOrDefault(isWinner ? loserId : winnerId, 1000);

            double expectedScore = 1.0 / (1.0 + Math.pow(10, (opRating - myRating) / 400.0));
            int scoreChange = (int) Math.round(32 * ((isWinner ? 1.0 : 0.0) - expectedScore));

            if (isWinner) winnerDisplayChange = scoreChange;
            else loserDisplayChange = scoreChange;

            int finalAbsoluteScore = Math.max(myRating + scoreChange, 0);

            LambdaQueryWrapper<UserStatistics> statsQw = new LambdaQueryWrapper<>();
            statsQw.eq(UserStatistics::getUserId, userId).eq(UserStatistics::getIsDeleted, 0);
            if (!userStatisticsMapper.exists(statsQw)) {
                UserStatistics initStats = new UserStatistics();
                initStats.setUserId(userId);
                initStats.setArenaScore(1000);
                initStats.setArenaMatches(0);
                initStats.setArenaWins(0);
                userStatisticsMapper.insert(initStats);
            }

            LambdaUpdateWrapper<UserStatistics> statsUpdate = new LambdaUpdateWrapper<>();
            statsUpdate.eq(UserStatistics::getUserId, userId);
            String sqlSet = "arena_matches = IFNULL(arena_matches, 0) + 1, arena_score = " + finalAbsoluteScore;
            if (isWinner) sqlSet += ", arena_wins = IFNULL(arena_wins, 0) + 1";
            statsUpdate.setSql(sqlSet);
            userStatisticsMapper.update(null, statsUpdate);

            UserStatistics latestStats = userStatisticsMapper.selectOne(statsQw);
            if (latestStats != null) {
                int matches = latestStats.getArenaMatches();
                int wins = latestStats.getArenaWins();
                int newWinRate = matches > 0 ? (int) Math.round((double) wins / matches * 100) : 0;

                LambdaUpdateWrapper<User> userUpdate = new LambdaUpdateWrapper<>();
                userUpdate.eq(User::getId, userId)
                        .set(User::getRatingScore, finalAbsoluteScore)
                        .set(User::getWinRate, newWinRate);
                userMapper.update(null, userUpdate);
            }
        }

        log.info("[天梯结算] 房间 {} 结算完毕！Elo变动: 胜者({}), 败者({})", roomCode, winnerDisplayChange, loserDisplayChange);
        String settlementMsg = String.format("🏆 MATCH FINISHED! 玩家 %d 率先 AC 夺得胜利！", winnerId);
        arenaWebSocketHandler.pushSystemBattleLog(roomCode, "SUCCESS", settlementMsg, null);
        arenaWebSocketHandler.pushSettlementEvent(roomCode, winnerId, winnerDisplayChange, loserDisplayChange);
    }
}