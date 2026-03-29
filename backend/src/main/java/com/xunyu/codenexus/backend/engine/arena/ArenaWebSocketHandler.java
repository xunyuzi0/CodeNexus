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
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 竞技场 WebSocket 核心消息处理器与状态机
 * 处理大厅的玩家同步、准备状态流转与开局广播
 *
 * @author CodeNexusBuilder
 */
@Slf4j
@Component
public class ArenaWebSocketHandler extends TextWebSocketHandler {

    /**
     * 【核心容器】本地线程安全的 WebSocket 会话池
     * 数据结构: RoomCode -> (UserId -> WebSocketSession)
     * 为什么这么写：外层 Map 隔离房间，内层 Map 隔离玩家，支持高并发下的精确多播(Multicast)
     */
    private static final Map<String, Map<Long, WebSocketSession>> ROOM_SESSIONS = new ConcurrentHashMap<>();
    /**
     * 【状态机】记录每个房间内玩家的“准备”状态
     * 数据结构: RoomCode -> (UserId -> isReady)
     */
    private static final Map<String, Map<Long, Boolean>> ROOM_READY_STATE = new ConcurrentHashMap<>();
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

        // 初始化房间级别的并发容器
        ROOM_SESSIONS.computeIfAbsent(roomCode, k -> new ConcurrentHashMap<>()).put(userId, session);
        ROOM_READY_STATE.computeIfAbsent(roomCode, k -> new ConcurrentHashMap<>()).putIfAbsent(userId, false);

        // 给当前连接的玩家推送：全量房间同步数据 (SYNC_ROOM)
        sendSyncRoomMessage(session, roomCode);

        // 给房间里【其他】玩家广播：有新玩家加入了！ (PLAYER_JOINED)
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

        // 预防心跳包 (Ping/Pong)
        if ("ping".equalsIgnoreCase(payload)) {
            session.sendMessage(new TextMessage("pong"));
            return;
        }

        try {
            JSONObject json = JSONUtil.parseObj(payload);
            String action = json.getStr("action");
            JSONObject data = json.getJSONObject("data");

            if ("READY".equals(action)) {
                boolean isReady = data.getBool("isReady");
                handlePlayerReady(roomCode, userId, isReady);
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

        // 1. 触发后端的退出与房主转移逻辑 (落库)
        try {
            arenaRoomService.leaveWaitingRoom(roomCode, userId);
        } catch (Exception e) {
            log.error("[WS大厅] 处理玩家退出异常", e);
        }

        // 2. 清理内存状态机
        Map<Long, WebSocketSession> roomMap = ROOM_SESSIONS.get(roomCode);
        if (roomMap != null) {
            roomMap.remove(userId);

            Map<Long, Boolean> stateMap = ROOM_READY_STATE.get(roomCode);
            if (stateMap != null) stateMap.remove(userId);

            if (roomMap.isEmpty()) {
                ROOM_SESSIONS.remove(roomCode);
                ROOM_READY_STATE.remove(roomCode);
            } else {
                // 3. 重新广播全量同步 (因为房主可能变了，直接发 SYNC_ROOM 最安全！)
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
    // 业务私有方法：准备状态流转与开局判定
    // ==========================================
    private void handlePlayerReady(String roomCode, Long userId, boolean isReady) {
        // 1. 更新内存状态机
        Map<Long, Boolean> stateMap = ROOM_READY_STATE.get(roomCode);
        if (stateMap == null) return;
        stateMap.put(userId, isReady);

        // 2. 广播给房间内的所有人（包括自己，用于确认），谁的状态变了
        JSONObject readyData = JSONUtil.createObj().set("userId", userId).set("isReady", isReady);
        broadcastToRoom(roomCode, null, buildMessage("PLAYER_READY", readyData));

        // 3. 核心判定：是否全员准备完毕？
        // 竞技场规则：人数必须达到 2 人，且所有人的 isReady 都为 true
        if (stateMap.size() == 2 && stateMap.values().stream().allMatch(ready -> ready)) {
            log.info("[WS引擎] 房间 {} 全员准备完毕，神经网络启动！", roomCode);

            // 查出该房间关联的题目 ID
            LambdaQueryWrapper<ArenaRoom> qw = new LambdaQueryWrapper<>();
            qw.eq(ArenaRoom::getRoomCode, roomCode);
            ArenaRoom room = arenaRoomService.getOne(qw);

            // 构建统一倒计时开局时间 (例如: 当前时间戳 + 2秒延迟，让两端同时开始播放动画)
            JSONObject startData = JSONUtil.createObj()
                    .set("problemId", room.getProblemId())
                    .set("startTime", System.currentTimeMillis() + 2000);

            // 广播游戏开始！
            broadcastToRoom(roomCode, null, buildMessage("GAME_START", startData));
        }
    }

    // ==========================================
    // 底层支撑组件
    // ==========================================

    /**
     * 发送全量同步消息给新加入的玩家
     */
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

        // 【新增】计算绝对的房间销毁时间戳 (例如：创建时间 + 30分钟)
        long expireTime = room.getCreateTime()
                .atZone(java.time.ZoneId.systemDefault())
                .toInstant().toEpochMilli() + (2 * 60 * 1000L);

        // 【修改】将 expireTime 加入到 SYNC_ROOM 报文中
        JSONObject syncData = JSONUtil.createObj()
                .set("expireTime", expireTime)
                .set("players", players);

        session.sendMessage(new TextMessage(buildMessage("SYNC_ROOM", syncData)));
    }

    /**
     * 获取单个玩家的展示信息
     */
    private JSONObject getPlayerInfo(String roomCode, Long userId) {
        User user = userService.getById(userId);

        // 查是否为房主
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
                .set("isReady", false); // 刚进房间肯定没准备
    }

    /**
     * 向房间内的指定用户广播消息
     *
     * @param excludeUserId 需要排除的用户ID (传 null 表示广播给所有人)
     */
    private void broadcastToRoom(String roomCode, Long excludeUserId, String message) {
        Map<Long, WebSocketSession> roomMap = ROOM_SESSIONS.get(roomCode);
        if (roomMap != null) {
            TextMessage textMessage = new TextMessage(message);
            roomMap.forEach((uid, session) -> {
                if (excludeUserId == null || !excludeUserId.equals(uid)) {
                    if (session.isOpen()) {
                        try {
                            session.sendMessage(textMessage);
                        } catch (IOException e) {
                            log.error("[WS广播] 房间 {} 用户 {} 消息发送失败", roomCode, uid, e);
                        }
                    }
                }
            });
        }
    }

    /**
     * 辅助工具：构建标准格式的 JSON 字符串
     */
    private String buildMessage(String action, JSONObject data) {
        return JSONUtil.createObj().set("action", action).set("data", data).toString();
    }

    /**
     * 辅助工具：从 URI 中提取房间短码 (例如 /api/ws/arena/A8X9P2)
     */
    private String extractRoomCode(WebSocketSession session) {
        String path = session.getUri().getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }
}