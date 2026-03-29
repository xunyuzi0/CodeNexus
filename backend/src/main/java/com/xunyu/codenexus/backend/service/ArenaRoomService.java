package com.xunyu.codenexus.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xunyu.codenexus.backend.model.dto.request.arena.RoomCreateRequest;
import com.xunyu.codenexus.backend.model.dto.response.arena.RoomCreateVO;
import com.xunyu.codenexus.backend.model.dto.response.arena.RoomValidityVO;
import com.xunyu.codenexus.backend.model.entity.ArenaRoom;

/**
 * 竞技场房间业务逻辑接口
 *
 * @author CodeNexusBuilder
 */
public interface ArenaRoomService extends IService<ArenaRoom> {

    /**
     * 创建私人/公开房间
     */
    RoomCreateVO createRoom(RoomCreateRequest request);

    /**
     * 进入房间前的前置校验与验票
     */
    RoomValidityVO checkRoomValidity(String roomCode, String ticket);

    /**
     * 系统级方法：供匹配引擎异步调用，创建天梯匹配房间
     *
     * @param user1Id 玩家1
     * @param user2Id 玩家2
     * @return 生成的房间短码
     */
    String createMatchRoom(Long user1Id, Long user2Id);

    /**
     * @param roomCode
     * @param password
     * @return
     */
    Boolean joinPrivateRoom(String roomCode, String password);

    /**
     * 玩家离开等待大厅 (处理退房与房主转移)
     */
    void leaveWaitingRoom(String roomCode, Long userId);
}