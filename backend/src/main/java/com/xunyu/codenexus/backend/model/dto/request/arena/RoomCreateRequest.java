package com.xunyu.codenexus.backend.model.dto.request.arena;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建竞技场房间请求参数
 */
@Data
public class RoomCreateRequest {

    /**
     * 房间类型: 1-私人, 2-公开
     */
    @NotNull(message = "房间类型不能为空")
    private Integer roomType;

    /**
     * 可选，私人房间密码
     */
    private String password;

    /**
     * 可选，指定题目ID。为空则后端按房主段位随机生成
     */
    private Long problemId;
}