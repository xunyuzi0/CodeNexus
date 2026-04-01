package com.xunyu.codenexus.backend.engine.arena;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xunyu.codenexus.backend.mapper.ArenaRoomUserMapper;
import com.xunyu.codenexus.backend.model.entity.ArenaRoom;
import com.xunyu.codenexus.backend.model.entity.ArenaRoomUser;
import com.xunyu.codenexus.backend.model.entity.User;
import com.xunyu.codenexus.backend.service.ArenaRoomService;
import com.xunyu.codenexus.backend.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 竞技场 WebSocket 核心消息处理器与状态机
 * 处理大厅的玩家同步、准备状态流转、高频竞速遥测与开局广播
 *
 * @author CodeNexusBuilder (The Core Architect)
 */
@Slf4j
@Component
public class ArenaWebSocketHandler extends TextWebSocketHandler {

    /**
     * 【核心容器】本地线程安全的 WebSocket 会话池
     */
    private static final Map<String, Map<Long, WebSocketSession>> ROOM_SESSIONS = new ConcurrentHashMap<>();

    /**
     * 【状态机】记录每个房间内玩家的“准备”状态
     */
    private static final Map<String, Map<Long, Boolean>> ROOM_READY_STATE = new ConcurrentHashMap<>();

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Resource
    private UserService userService;
    @Resource
    private ArenaRoomService arenaRoomService;
    @Resource
    private ArenaRoomUserMapper arenaRoomUserMapper;

    // ==========================================
    // 1. 生命周期：连接建立 (Player Joined)
    // ==========================================
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String roomCode = extractRoomCode(session);
        Long userId = (Long) session.getAttributes().get("userId");

        log.info("[WS大厅] 玩家 {} 接入房间 {}", userId, roomCode);

        ROOM_SESSIONS.computeIfAbsent(roomCode, k -> new ConcurrentHashMap<>()).put(userId, session);
        ROOM_READY_STATE.computeIfAbsent(roomCode, k -> new ConcurrentHashMap<>()).putIfAbsent(userId, false);

        sendSyncRoomMessage(session, roomCode);
        broadcastToRoom(roomCode, userId, buildMessage("PLAYER_JOINED", getPlayerInfo(roomCode, userId)));
    }

    // ==========================================
    // 2. 生命周期：接收消息 (Handle Action)
    // ==========================================
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
            // 兼容前端 AI 要求的 "type" 字段与旧版的 "action" 字段
            String action = json.getStr("type");
            if (!StringUtils.hasText(action)) {
                action = json.getStr("action");
            }

            JSONObject data = json.getJSONObject("data");

            switch (action) {
                case "READY":
                    boolean isReady = data.getBool("isReady");
                    handlePlayerReady(roomCode, userId, isReady);
                    break;

                case "TELEMETRY_SYNC":
                case "BATTLE_LOG":
                    // [跨端 Bug 修复核心] 拦截盲路由，强行注入发送者的 userId。
                    // 确保前端卡片状态机能根据此 ID 准确更新敌方的遥测数据与战术日志。
                    data.set("userId", userId);
                    String enrichedPayload = buildMessage(action, data);
                    broadcastToRoom(roomCode, userId, enrichedPayload);
                    break;
            }
        } catch (Exception e) {
            log.error("[WS大厅] 消息解析异常, payload: {}", payload, e);
        }
    }

    // ==========================================
    // 3. 生命周期：连接断开 (Player Left)
    // ==========================================
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String roomCode = extractRoomCode(session);
        Long userId = (Long) session.getAttributes().get("userId");

        log.info("[WS大厅] 玩家 {} 离开房间 {}", userId, roomCode);

        try {
            arenaRoomService.leaveWaitingRoom(roomCode, userId);
        } catch (Exception e) {
            log.error("[WS大厅] 处理玩家退出异常", e);
        }

        Map<Long, WebSocketSession> roomMap = ROOM_SESSIONS.get(roomCode);
        if (roomMap != null) {
            roomMap.remove(userId);
            Map<Long, Boolean> stateMap = ROOM_READY_STATE.get(roomCode);
            if (stateMap != null) stateMap.remove(userId);

            if (roomMap.isEmpty()) {
                ROOM_SESSIONS.remove(roomCode);
                ROOM_READY_STATE.remove(roomCode);
            } else {
                roomMap.values().forEach(s -> {
                    if (s.isOpen()) {
                        try {
                            sendSyncRoomMessage(s, roomCode);
                        } catch (IOException e) {
                            log.error("[WS大厅] 房主转移同步失败", e);
                        }
                    }
                });
            }
        }
    }

    // ==========================================
    // 4. 沙箱引擎专用后门：推送系统战况日志
    // ==========================================

    /**
     * 供异步沙箱线程 (JudgeDispatcher) 调用的跨线程推送接口
     */
    public void pushSystemBattleLog(String roomCode, String level, String logMessage, String rawPayload) {
        Map<Long, WebSocketSession> roomMap = ROOM_SESSIONS.get(roomCode);
        if (roomMap == null || roomMap.isEmpty()) return;

        try {
            JSONObject data = JSONUtil.createObj()
                    .set("timestamp", LocalTime.now().format(TIME_FORMATTER))
                    .set("level", level)
                    .set("message", logMessage);

            if (rawPayload != null) {
                data.set("rawPayload", rawPayload);
            }

            // 严格对齐前端 AI 的 JSON 格式要求
            JSONObject root = JSONUtil.createObj()
                    .set("type", "BATTLE_LOG")
                    .set("data", data);

            broadcastToRoom(roomCode, null, root.toString());
        } catch (Exception e) {
            log.error("[WS大厅] 推送沙箱日志到房间 {} 失败", roomCode, e);
        }
    }

    // ==========================================
    // 业务私有方法：准备状态流转与开局判定
    // ==========================================
    private void handlePlayerReady(String roomCode, Long userId, boolean isReady) {
        Map<Long, Boolean> stateMap = ROOM_READY_STATE.get(roomCode);
        if (stateMap == null) return;
        stateMap.put(userId, isReady);

        JSONObject readyData = JSONUtil.createObj().set("userId", userId).set("isReady", isReady);
        // 这里沿用旧版 action 字段以防前端旧逻辑报错，如果前端也改了可以换成 type
        broadcastToRoom(roomCode, null, buildMessage("PLAYER_READY", readyData));

        if (stateMap.size() == 2 && stateMap.values().stream().allMatch(ready -> ready)) {
            log.info("[WS引擎] 房间 {} 全员准备完毕，神经网络启动！", roomCode);

            LambdaQueryWrapper<ArenaRoom> qw = new LambdaQueryWrapper<>();
            qw.eq(ArenaRoom::getRoomCode, roomCode);
            ArenaRoom room = arenaRoomService.getOne(qw);

            JSONObject startData = JSONUtil.createObj()
                    .set("problemId", room.getProblemId())
                    .set("startTime", System.currentTimeMillis() + 2000);

            broadcastToRoom(roomCode, null, buildMessage("GAME_START", startData));

            // 开局后，顺便给前端面板推一条系统日志
            pushSystemBattleLog(roomCode, "INFO", "System: Game Started. May the code be with you.", null);
        }
    }

    // ==========================================
    // 底层支撑组件
    // ==========================================
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
                players.add(JSONUtil.createObj()
                        .set("userId", user.getId())
                        .set("nickname", user.getNickname() != null ? user.getNickname() : user.getUsername())
                        .set("avatarUrl", user.getAvatarUrl())
                        .set("isCreator", ru.getIsCreator())
                        .set("isReady", isReady));
            }
        }

        long expireTime = room.getCreateTime()
                .atZone(java.time.ZoneId.systemDefault())
                .toInstant().toEpochMilli() + (2 * 60 * 1000L);

        JSONObject syncData = JSONUtil.createObj()
                .set("expireTime", expireTime)
                .set("players", players);

        session.sendMessage(new TextMessage(buildMessage("SYNC_ROOM", syncData)));
    }

    private JSONObject getPlayerInfo(String roomCode, Long userId) {
        User user = userService.getById(userId);
        LambdaQueryWrapper<ArenaRoom> roomQw = new LambdaQueryWrapper<>();
        roomQw.eq(ArenaRoom::getRoomCode, roomCode);
        ArenaRoom room = arenaRoomService.getOne(roomQw);

        Integer isCreator = 0;
        if (room != null) {
            LambdaQueryWrapper<ArenaRoomUser> ruQw = new LambdaQueryWrapper<>();
            ruQw.eq(ArenaRoomUser::getRoomId, room.getId()).eq(ArenaRoomUser::getUserId, userId);
            ArenaRoomUser ru = arenaRoomUserMapper.selectOne(ruQw);
            if (ru != null) isCreator = ru.getIsCreator();
        }

        return JSONUtil.createObj()
                .set("userId", userId)
                .set("nickname", user.getNickname() != null ? user.getNickname() : user.getUsername())
                .set("avatarUrl", user.getAvatarUrl())
                .set("isCreator", isCreator)
                .set("isReady", false);
    }

    private void broadcastToRoom(String roomCode, Long excludeUserId, String message) {
        Map<Long, WebSocketSession> roomMap = ROOM_SESSIONS.get(roomCode);
        if (roomMap != null) {
            TextMessage textMessage = new TextMessage(message);
            roomMap.forEach((uid, session) -> {
                if (excludeUserId == null || !excludeUserId.equals(uid)) {
                    if (session.isOpen()) {
                        try {
                            // [并发安全] 保证同一 Session 发送消息不串流
                            synchronized (session) {
                                session.sendMessage(textMessage);
                            }
                        } catch (IOException e) {
                            log.error("[WS广播] 房间 {} 用户 {} 消息发送失败", roomCode, uid, e);
                        }
                    }
                }
            });
        }
    }

    private String buildMessage(String action, JSONObject data) {
        // 为了兼容旧逻辑，此处依然生成 action 字段，前端同时解析即可
        return JSONUtil.createObj().set("type", action).set("action", action).set("data", data).toString();
    }

    private String extractRoomCode(WebSocketSession session) {
        String path = session.getUri().getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }
}