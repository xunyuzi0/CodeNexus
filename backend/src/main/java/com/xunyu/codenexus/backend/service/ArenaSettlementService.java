package com.xunyu.codenexus.backend.service;

/**
 * 天梯对战结算引擎接口
 *
 * @author CodeNexusBuilder
 */
public interface ArenaSettlementService {

    /**
     * 对战结算：处理排位分、胜场以及房间状态流转
     *
     * @param roomCode 房间短码
     * @param winnerId 率先 AC 的胜者 ID
     */
    void settleMatch(String roomCode, Long winnerId);
}