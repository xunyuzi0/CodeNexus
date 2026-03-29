package com.xunyu.codenexus.backend.service;

import com.xunyu.codenexus.backend.model.dto.response.arena.MatchStatusVO;

/**
 * 竞技场天梯匹配业务逻辑接口
 *
 * @author CodeNexusBuilder
 */
public interface ArenaMatchService {

    /**
     * 加入天梯匹配池
     */
    boolean joinMatchPool();

    /**
     * 取消天梯匹配
     */
    boolean leaveMatchPool();

    /**
     * 轮询获取匹配状态
     */
    MatchStatusVO pollMatchStatus();
}