package com.xunyu.codenexus.backend.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * @author xunyu
 * @date 2026/1/11 18:08
 * @description: 用户角色枚举
 */
@Getter
public enum UserRoleEnum {

    /**
     * 普通用户
     */
    USER("user", "普通用户"),

    /**
     * 管理员
     */
    ADMIN("admin", "管理员"),

    /**
     * 被封号的用户
     */
    BAN("ban", "被封号");

    /**
     * 标记 @EnumValue 表示会将此字段的值存入数据库
     * 标记 @JsonValue 表示 API 返回 "admin"(也就是 value) 而不是 "ADMIN"(枚举对象的名称)
     */
    @EnumValue
    @JsonValue
    private final String value;

    /**
     * 描述文本
     */
    private final String text;

    UserRoleEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    /**
     * 根据 value 获取枚举对象 (用于反序列化或业务逻辑)
     *
     * @param value 例如 "admin"
     * @return UserRoleEnum
     */
    public static UserRoleEnum getEnumByValue(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        for (UserRoleEnum roleEnum : UserRoleEnum.values()) {
            if (roleEnum.value.equals(value)) {
                return roleEnum;
            }
        }
        return null;
    }
}