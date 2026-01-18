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
     * 操作成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 业务异常 (通用错误)
     */
    FAILED(500, "系统内部异常"),

    /**
     * 参数校验错误
     */
    VALIDATE_FAILED(400, "参数检验失败"),

    /**
     * 未登录或 Token 过期
     */
    UNAUTHORIZED(401, "暂未登录或 Token 已经过期"),

    /**
     * 没有权限访问该资源
     */
    FORBIDDEN(403, "没有相关权限");

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
