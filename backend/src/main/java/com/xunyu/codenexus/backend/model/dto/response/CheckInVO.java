// src/main/java/com/xunyu/codenexus/backend/model/dto/response/CheckInVO.java
package com.xunyu.codenexus.backend.model.dto.response;

import lombok.Data;

/**
 * 用户每日打卡出参 VO
 *
 * @author CodeNexusBuilder
 */
@Data
public class CheckInVO {
    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 打卡后的最新连续天数
     */
    private Integer continuousCheckInDays;

    /**
     * 获得的奖励描述
     */
    private String reward;
}