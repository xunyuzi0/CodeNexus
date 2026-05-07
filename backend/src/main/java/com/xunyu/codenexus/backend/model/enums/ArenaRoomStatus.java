package com.xunyu.codenexus.backend.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 竞技场房间状态枚举
 */
@Getter
public enum ArenaRoomStatus {

    /**
     * 等待中
     */
    WAITING("WAITING", "等待中"),

    /**
     * 对战中
     */
    FIGHTING("FIGHTING", "对战中"),

    /**
     * 已结束
     */
    FINISHED("FINISHED", "已结束"),

    /**
     * 已解散
     */
    DISMISSED("DISMISSED", "已解散");

    @EnumValue
    @JsonValue
    private final String value;

    private final String label;

    ArenaRoomStatus(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public static ArenaRoomStatus getEnumByValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        for (ArenaRoomStatus status : ArenaRoomStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return null;
    }
}
