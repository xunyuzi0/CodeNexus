package com.xunyu.codenexus.backend.common.result;

import lombok.Getter;

/**
 * 统一定义 API 接口返回的状态码，避免在代码中硬编码数字。
 *
 * @author xunyu
 * @date 2026/1/10 23:22
 * @description: 响应状态码枚举类
 */

@Getter
public enum ResultCode {

    /**
     * 成功状态
     */
    SUCCESS(200, "操作成功"),

    /**
     * 错误状态
     */
    FAILED(500, "系统内部异常"),
    VALIDATE_FAILED(400, "参数检验失败"),

    /**
     * 权限
     */
    UNAUTHORIZED(401, "暂未登录或 Token 已经过期"),
    FORBIDDEN(403, "没有相关权限"),

    /* 具体的业务场景错误 (推荐添加) */
    USER_LOGIN_ERROR(400, "账号或密码错误"),
    USER_ACCOUNT_FORBIDDEN(403, "账号已被封禁"),
    NOT_FOUND(404, "资源不存在");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 状态描述
     */
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
