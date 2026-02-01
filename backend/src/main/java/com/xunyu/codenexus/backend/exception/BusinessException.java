package com.xunyu.codenexus.backend.exception;

import com.xunyu.codenexus.backend.common.result.ResultCode;
import lombok.Getter;

/**
 * 自定义业务逻辑异常类
 *
 * @author xunyu
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 异常状态码
     */
    private final int code;

    /**
     * 构造方法：使用默认状态码 (500) 和自定义消息
     *
     * @param message 错误描述
     */
    public BusinessException(String message) {
        // 把消息交给父类 RuntimeException 管理
        super(message);
        this.code = ResultCode.FAILED.getCode();
    }

    /**
     * 构造方法：使用 ResultCode 枚举
     *
     * @param resultCode 错误枚举
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    /**
     * 构造方法：自定义状态码和消息
     *
     * @param code    自定义状态码
     * @param message 错误描述
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}