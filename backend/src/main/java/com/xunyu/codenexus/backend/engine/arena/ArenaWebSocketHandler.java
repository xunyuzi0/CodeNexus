package com.xunyu.codenexus.backend.engine.arena;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xunyu.codenexus.backend.mapper.ArenaRoomUserMapper;
import com.xunyu.codenexus.backend.mapper.UserActivityLogMapper;
import com.xunyu.codenexus.backend.model.enums.ArenaRoomStatus;
import com.xunyu.codenexus.backend.model.entity.ArenaRoom;
import com.xunyu.codenexus.backend.model.entity.ArenaRoomUser;
import com.xunyu.codenexus.backend.model.entity.User;
import com.xunyu.codenexus.backend.model.entity.UserActivityLog;
import com.xunyu.codenexus.backend.service.ArenaRoomService;
import com.xunyu.codenexus.backend.service.ArenaSettlementService;
import com.xunyu.codenexus.backend.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ArenaWebSocketHandler extends TextWebSocketHandler {

    private static final Map<String, Map<Long, WebSocketSession>> ROOM_SESSIONS = new ConcurrentHashMap<>();
    private static final Map<String, Map<Long, Boolean>> ROOM_READY_STATE = new ConcurrentHashMap<>();

    // 🎯 终极武器：内存黑匣子！永久记录进入过该房间的所有玩家 ID，免疫数据库删除
    private static final Map<String, Set<Long>> ROOM_USER_HISTORY = new ConcurrentHashMap<>();

    // 标记对局已经正式开始的房间（区分大厅阶段和对战阶段）
    private static final Set<String> STARTED_ROOMS = ConcurrentHashMap.newKeySet();

    // 记录对局非正常结束的特殊原因 (如逃跑)
    private static final Map<String, String> MATCH_REASONS = new ConcurrentHashMap<>();

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Resource
    private UserService userService;
    @Resource
    private ArenaRoomService arenaRoomService;
    @Resource
    private ArenaRoomUserMapper arenaRoomUserMapper;

    // 【核心修复引入】：注入日志映射器以记录大厅逃跑扣分
    @Resource
    private UserActivityLogMapper userActivityLogMapper;

    @Resource
    @Lazy // 使用 Lazy 避免与 SettlementService 的循环依赖
    private ArenaSettlementService arenaSettlementService;

    // 暴露给结算引擎的黑匣子提取接口
    public Set<Long> getRoomHistory(String roomCode) {
        return ROOM_USER_HISTORY.getOrDefault(roomCode, Collections.emptySet());
    }

    // 暴露给结算引擎获取特殊结算原因
    public String getMatchReason(String roomCode) {
        return MATCH_REASONS.get(roomCode);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String roomCode = extractRoomCode(session);
        Long userId = (Long) session.getAttributes().get("userId");

        log.info("[WS大厅] 玩家 {} 接入房间 {}", userId, roomCode);

        ROOM_SESSIONS.computeIfAbsent(roomCode, k -> new ConcurrentHashMap<>()).put(userId, session);
        ROOM_READY_STATE.computeIfAbsent(roomCode, k -> new ConcurrentHashMap<>()).putIfAbsent(userId, false);

        // 玩家一进来，立刻写入黑匣子
        ROOM_USER_HISTORY.computeIfAbsent(roomCode, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(userId);

        sendSyncRoomMessage(session, roomCode);
        broadcastToRoom(roomCode, userId, buildMessage("PLAYER_JOINED", getPlayerInfo(roomCode, userId)));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String roomCode = extractRoomCode(session);
        Long userId = (Long) session.getAttributes().get("userId");
        String payload = message.getPayload();

        if ("ping".equalsIgnoreCase(payload)) {
            session.sendMessage(new TextMessage("pong"));
            return;
        }

        try {
            JSONObject json = JSONUtil.parseObj(payload);
            String action = json.getStr("type");
            if (!StringUtils.hasText(action)) action = json.getStr("action");

            JSONObject data = json.getJSONObject("data");
            switch (action) {
                case "READY":
                    handlePlayerReady(roomCode, userId, data.getBool("isReady"));
                    break;
                case "TELEMETRY_SYNC":
                case "BATTLE_LOG":
                    data.set("userId", userId);
                    broadcastToRoom(roomCode, userId, buildMessage(action, data));
                    break;
                case "ESCAPE_LOBBY":
                    // 玩家主动退出大厅
                    handlePlayerEscape(roomCode, userId);
                    break;
                case "SURRENDER":
                    // 玩家对战中主动投降
                    handlePlayerEscape(roomCode, userId);
                    break;
            }
        } catch (Exception e) {
            log.error("[WS大厅] 消息解析异常", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String roomCode = extractRoomCode(session);
        Long userId = (Long) session.getAttributes().get("userId");

        log.info("[WS大厅] 玩家 {} 离开房间 {}", userId, roomCode);

        try {
            arenaRoomService.leaveWaitingRoom(roomCode, userId);
        } catch (Exception e) {
        }

        Map<Long, WebSocketSession> roomMap = ROOM_SESSIONS.get(roomCode);
        if (roomMap != null) {
            roomMap.remove(userId);
            Map<Long, Boolean> stateMap = ROOM_READY_STATE.get(roomCode);
            if (stateMap != null) stateMap.remove(userId);

            if (roomMap.isEmpty()) {
                ROOM_SESSIONS.remove(roomCode);
                ROOM_READY_STATE.remove(roomCode);
                // 注意：这里绝对不清理 ROOM_USER_HISTORY，保留给延迟的结算引擎使用！
            } else {
                roomMap.values().forEach(s -> {
                    if (s.isOpen()) {
                        try {
                            sendSyncRoomMessage(s, roomCode);
                        } catch (IOException e) {
                        }
                    }
                });
            }
        }

        // 核心防逃跑机制：5秒延迟检验。防误杀（区分刷新网页/切换路由 和 真实断线逃跑）
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(5000);
                Map<Long, WebSocketSession> currentRoomMap = ROOM_SESSIONS.get(roomCode);
                boolean isReconnected = currentRoomMap != null && currentRoomMap.containsKey(userId);
                if (!isReconnected) {
                    handlePlayerEscape(roomCode, userId);
                }
            } catch (InterruptedException e) {
                log.error("断线延时检验被打断", e);
            }
        });
    }

    private void handlePlayerEscape(String roomCode, Long escapeeId) {
        ArenaRoom room = arenaRoomService.getOne(new LambdaQueryWrapper<ArenaRoom>().eq(ArenaRoom::getRoomCode, roomCode));
        if (room == null || ArenaRoomStatus.FINISHED.getValue().equals(room.getStatus())) return;

        // 🎯 核心判断：是否是排位模式
        boolean isRanked = room.getRoomType() != null && room.getRoomType() == 3;

        if (!STARTED_ROOMS.contains(roomCode)) {
            // LOBBY ESCAPE (大厅逃跑阶段)
            // 立即将房间状态标记为已解散，防止重复扣分
            room.setStatus(ArenaRoomStatus.DISMISSED.getValue());
            arenaRoomService.updateById(room);

            int actualDeducted = 0;

            if (isRanked) {
                // 🎯 核心修复：先获取玩家当前分数，精准计算实际扣除值（防止负数引发流水差异）
                User escapee = userService.getById(escapeeId);
                int currentScore = (escapee != null && escapee.getRatingScore() != null) ? escapee.getRatingScore() : 1000;
                actualDeducted = Math.min(currentScore, 20); // 最多扣除 20 分

                if (actualDeducted > 0) {
                    // 1. 更新用户主表积分
                    LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(User::getId, escapeeId).set(User::getRatingScore, currentScore - actualDeducted);
                    userService.update(updateWrapper);

                    // 2. 将负分写入当天的活动日志表 (user_activity_log)！
                    LocalDate today = LocalDate.now();
                    LambdaQueryWrapper<UserActivityLog> logQw = new LambdaQueryWrapper<>();
                    logQw.eq(UserActivityLog::getUserId, escapeeId).eq(UserActivityLog::getActivityDate, today);
                    UserActivityLog dailyLog = userActivityLogMapper.selectOne(logQw);

                    if (dailyLog == null) {
                        dailyLog = new UserActivityLog();
                        dailyLog.setUserId(escapeeId);
                        dailyLog.setActivityDate(today);
                        dailyLog.setScoreChange(-actualDeducted); // 记录负数流水
                        dailyLog.setIsCheckin(0);
                        dailyLog.setAcCount(0);
                        userActivityLogMapper.insert(dailyLog);
                    } else {
                        LambdaUpdateWrapper<UserActivityLog> logUpd = new LambdaUpdateWrapper<>();
                        logUpd.eq(UserActivityLog::getId, dailyLog.getId())
                                .setSql("score_change = IFNULL(score_change, 0) - " + actualDeducted);
                        userActivityLogMapper.update(null, logUpd);
                    }
                }
                log.info("[大厅逃跑] 天梯模式玩家 {} 逃跑，已扣除 {} 排位分", escapeeId, actualDeducted);
            } else {
                log.info("[大厅逃跑] 私有房间玩家 {} 退出，不扣除排位分", escapeeId);
            }

            // 通知剩余玩家触发前端警告弹窗，带上真实的扣除分值及 isRanked 标识
            broadcastToRoom(roomCode, null, buildMessage("PLAYER_ESCAPED",
                    JSONUtil.createObj()
                            .set("escapeeId", escapeeId)
                            .set("deductedScore", actualDeducted)
                            .set("isRanked", isRanked)));
        } else {
            // BATTLE ESCAPE (战斗逃跑阶段)
            Set<Long> history = getRoomHistory(roomCode);
            Long winnerId = history.stream().filter(id -> !id.equals(escapeeId)).findFirst().orElse(null);
            if (winnerId != null) {
                // 标记结算原因为逃跑，触发特殊结算视图
                MATCH_REASONS.put(roomCode, "ESCAPE");
                arenaSettlementService.settleMatch(roomCode, winnerId);
            }
        }
    }

    public void pushSystemBattleLog(String roomCode, String level, String logMessage, String rawPayload) {
        Map<Long, WebSocketSession> roomMap = ROOM_SESSIONS.get(roomCode);
        if (roomMap == null || roomMap.isEmpty()) return;

        try {
            JSONObject data = JSONUtil.createObj().set("timestamp", LocalTime.now().format(TIME_FORMATTER)).set("level", level).set("message", logMessage);
            if (rawPayload != null) data.set("rawPayload", rawPayload);
            broadcastToRoom(roomCode, null, JSONUtil.createObj().set("type", "BATTLE_LOG").set("data", data).toString());
        } catch (Exception e) {
        }
    }

    // 重载方法支持传递结算原因及排位标识
    public void pushSettlementEvent(String roomCode, Long winnerId, int winnerChange, int loserChange, String StringReason, boolean isRanked) {
        try {
            JSONObject data = JSONUtil.createObj()
                    .set("winnerId", winnerId)
                    .set("winnerChange", winnerChange)
                    .set("loserChange", loserChange)
                    .set("isRanked", isRanked);
            if (StringReason != null) {
                data.set("reason", StringReason);
            }
            broadcastToRoom(roomCode, null, JSONUtil.createObj().set("type", "MATCH_SETTLED").set("action", "MATCH_SETTLED").set("data", data).toString());
        } catch (Exception e) {
        }
    }

    private void handlePlayerReady(String roomCode, Long userId, boolean isReady) {
        Map<Long, Boolean> stateMap = ROOM_READY_STATE.get(roomCode);
        if (stateMap == null) return;
        stateMap.put(userId, isReady);

        broadcastToRoom(roomCode, null, buildMessage("PLAYER_READY", JSONUtil.createObj().set("userId", userId).set("isReady", isReady)));

        // 双方就绪，正式切入战斗模式
        if (stateMap.size() == 2 && stateMap.values().stream().allMatch(ready -> ready)) {
            STARTED_ROOMS.add(roomCode); // 标记战场已锁定
            LambdaQueryWrapper<ArenaRoom> qw = new LambdaQueryWrapper<>();
            qw.eq(ArenaRoom::getRoomCode, roomCode);
            ArenaRoom room = arenaRoomService.getOne(qw);

            room.setStatus(ArenaRoomStatus.FIGHTING.getValue());
            arenaRoomService.updateById(room);

            broadcastToRoom(roomCode, null, buildMessage("GAME_START", JSONUtil.createObj().set("problemId", room.getProblemId()).set("startTime", System.currentTimeMillis() + 2000)));
            pushSystemBattleLog(roomCode, "INFO", "System: Game Started. May the code be with you.", null);
        }
    }

    private void sendSyncRoomMessage(WebSocketSession session, String roomCode) throws IOException {
        LambdaQueryWrapper<ArenaRoom> roomQw = new LambdaQueryWrapper<>();
        roomQw.eq(ArenaRoom::getRoomCode, roomCode);
        ArenaRoom room = arenaRoomService.getOne(roomQw);
        if (room == null) return;

        LambdaQueryWrapper<ArenaRoomUser> ruQw = new LambdaQueryWrapper<>();
        ruQw.eq(ArenaRoomUser::getRoomId, room.getId());
        List<ArenaRoomUser> roomUsers = arenaRoomUserMapper.selectList(ruQw);

        List<JSONObject> players = new ArrayList<>();
        Map<Long, Boolean> stateMap = ROOM_READY_STATE.get(roomCode);

        for (ArenaRoomUser ru : roomUsers) {
            User user = userService.getById(ru.getUserId());
            if (user != null) {
                boolean isReady = stateMap != null && stateMap.getOrDefault(user.getId(), false);
                players.add(JSONUtil.createObj().set("userId", user.getId()).set("nickname", user.getNickname() != null ? user.getNickname() : user.getUsername()).set("avatarUrl", user.getAvatarUrl()).set("isCreator", ru.getIsCreator()).set("isReady", isReady));
            }
        }
        long expireTime = room.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() + (2 * 60 * 1000L);
        session.sendMessage(new TextMessage(buildMessage("SYNC_ROOM", JSONUtil.createObj().set("expireTime", expireTime).set("players", players))));
    }

    private JSONObject getPlayerInfo(String roomCode, Long userId) {
        User user = userService.getById(userId);
        return JSONUtil.createObj().set("userId", userId).set("nickname", user.getNickname() != null ? user.getNickname() : user.getUsername()).set("avatarUrl", user.getAvatarUrl()).set("isCreator", 0).set("isReady", false);
    }

    private void broadcastToRoom(String roomCode, Long excludeUserId, String message) {
        Map<Long, WebSocketSession> roomMap = ROOM_SESSIONS.get(roomCode);
        if (roomMap != null) {
            TextMessage textMessage = new TextMessage(message);
            roomMap.forEach((uid, session) -> {
                if (excludeUserId == null || !excludeUserId.equals(uid)) {
                    if (session.isOpen()) {
                        try {
                            synchronized (session) {
                                session.sendMessage(textMessage);
                            }
                        } catch (IOException e) {
                        }
                    }
                }
            });
        }
    }

    private String buildMessage(String action, JSONObject data) {
        return JSONUtil.createObj().set("type", action).set("action", action).set("data", data).toString();
    }

    private String extractRoomCode(WebSocketSession session) {
        String path = session.getUri().getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }
}