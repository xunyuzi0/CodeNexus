package com.xunyu.codenexus.backend.service;

public interface ArenaSettlementService {
    // 恢复为 2 个参数，由底层物理查询对手
    void settleMatch(String roomCode, Long winnerId);
}