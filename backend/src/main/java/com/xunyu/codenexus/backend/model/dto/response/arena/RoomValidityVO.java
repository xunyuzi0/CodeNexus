package com.xunyu.codenexus.backend.model.dto.response.arena;

import lombok.Data;

/**
 * 房间状态校验与验票响应 VO
 */
@Data
public class RoomValidityVO {
    private Boolean isValid;
    private Integer roomType;
    private String status;
    private Long problemId;
    private String message;
}