package com.xunyu.codenexus.backend.model.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志出参 VO
 */
@Data
public class LoginLogVO {
    private Long id;
    private String loginIp;
    private String loginDevice;
    private Integer loginStatus;
    private LocalDateTime createTime;
}