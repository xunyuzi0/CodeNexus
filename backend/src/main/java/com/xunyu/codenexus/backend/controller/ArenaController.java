package com.xunyu.codenexus.backend.controller;

import com.xunyu.codenexus.backend.aop.auth.Protector;
import com.xunyu.codenexus.backend.common.result.Result;
import com.xunyu.codenexus.backend.common.result.ResultCode;
import com.xunyu.codenexus.backend.model.dto.request.arena.RoomCreateRequest;
import com.xunyu.codenexus.backend.model.dto.response.arena.MatchStatusVO;
import com.xunyu.codenexus.backend.model.dto.response.arena.RoomCreateVO;
import com.xunyu.codenexus.backend.model.dto.response.arena.RoomValidityVO;
import com.xunyu.codenexus.backend.model.enums.UserRoleEnum;
import com.xunyu.codenexus.backend.service.ArenaMatchService;
import com.xunyu.codenexus.backend.service.ArenaRoomService;
import com.xunyu.codenexus.backend.service.ArenaSettlementService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 竞技场模块 API 控制器
 * 处理房间管理与天梯匹配的前端请求
 *
 * @author CodeNexusBuilder
 */
@Slf4j
@RestController
@RequestMapping("/api/arena")
public class ArenaController {

    @Resource
    private ArenaRoomService arenaRoomService;

    @Resource
    private ArenaMatchService arenaMatchService;

    @Resource
    private ArenaSettlementService arenaSettlementService;

    // ==========================================
    // 1. 房间管理模块 (Room Management)
    // ==========================================

    /**
     * 创建私人/公开房间
     */
    @PostMapping("/room")
    @Protector(role = UserRoleEnum.USER)
    public Result<RoomCreateVO> createRoom(@RequestBody @Validated RoomCreateRequest request) {
        RoomCreateVO vo = arenaRoomService.createRoom(request);
        return Result.success(vo, "房间创建成功");
    }


    // ==========================================
    // 2. 天梯匹配模块 (Matchmaking System)
    // ==========================================

    /**
     * 进入前校验房间状态与验票 (Lobby 前置守卫)
     * 天梯匹配玩家必须携带有效 ticket，普通邀请房间可以为空
     */
    @GetMapping("/room/{roomCode}/validity")
    @Protector(role = UserRoleEnum.USER)
    public Result<RoomValidityVO> checkRoomValidity(
            @PathVariable("roomCode") String roomCode,
            @RequestParam(value = "ticket", required = false) String ticket) {

        RoomValidityVO vo = arenaRoomService.checkRoomValidity(roomCode, ticket);
        return Result.success(vo);
    }

    /**
     * 加入天梯匹配池
     */
    @PostMapping("/match")
    @Protector(role = UserRoleEnum.USER)
    public Result<Boolean> joinMatchPool() {
        boolean result = arenaMatchService.joinMatchPool();
        return Result.success(result, "已加入匹配队列");
    }

    /**
     * 取消天梯匹配
     */
    @DeleteMapping("/match")
    @Protector(role = UserRoleEnum.USER)
    public Result<Boolean> leaveMatchPool() {
        boolean result = arenaMatchService.leaveMatchPool();
        return Result.success(result, "已取消匹配");
    }

    /**
     * 轮询匹配状态 (供前端 Matchmaking.vue 页面长轮询调用)
     */
    @GetMapping("/match/status")
    @Protector(role = UserRoleEnum.USER)
    public Result<MatchStatusVO> pollMatchStatus() {
        MatchStatusVO vo = arenaMatchService.pollMatchStatus();
        return Result.success(vo);
    }

    /**
     * 加入私人/公开房间 (物理入库)
     */
    @PostMapping("/room/{roomCode}/join")
    @Protector(role = UserRoleEnum.USER)
    public Result<Boolean> joinPrivateRoom(
            @PathVariable("roomCode") String roomCode,
            @RequestBody(required = false) Map<String, String> body) {

        String password = body != null ? body.get("password") : null;
        boolean result = arenaRoomService.joinPrivateRoom(roomCode, password);
        return Result.success(result, "加入房间成功");
    }

    @PostMapping("/settle")
    public Result<Void> forceSettleMatch(@RequestBody java.util.Map<String, Object> params) {
        String roomCode = (String) params.get("roomCode");
        Object winnerObj = params.get("winnerId");

        // 🚀 这里改成了你的系统中真实存在的 VALIDATE_FAILED
        if (roomCode == null || winnerObj == null) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "对战结算参数不完整");
        }

        Long winnerId = Long.valueOf(winnerObj.toString());
        // 只传房间号和赢家ID，让 Service 层自己去找败者
        arenaSettlementService.settleMatch(roomCode, winnerId);
        return Result.success();
    }
}