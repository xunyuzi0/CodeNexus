package com.xunyu.codenexus.backend.model.dto.response.arena;

import lombok.Data;

/**
 * 匹配状态轮询响应 VO
 */
@Data
public class MatchStatusVO {
    /**
     * 匹配状态: MATCHING, SUCCESS, FAILED
     */
    private String status;

    /**
     * 匹配成功后的房间短码
     */
    private String roomCode;

    /**
     * 匹配成功后用于入场验票的临时凭证
     */
    private String ticket;
}