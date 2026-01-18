package com.xunyu.codenexus.backend.common.result;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 所有 Controller 接口的返回类型都应为此类的实例。
 *
 * @param <T> 数据载荷的类型
 * @author xunyu
 * @date 2026/1/8 18:42
 * @description: 通用 API 响应封装类
 */

@Data
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private int code;

    /**
     * 提示消息
     */
    private String message;

    /**
     * 实际数据载荷
     * 可以是对象、List、Map，甚至是 null
     */
    private T data;

    /**
     * 响应时间戳 (新增)
     * 方便前端排查请求耗时和顺序
     */
    private long timestamp;

    /**
     * 私有构造器，禁止外部直接 new，强制使用静态工厂方法
     */
    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功返回 - 无数据
     * 注意此处的<T>,这是一个泛型的声明,不要与类中的T混淆,因为他完全独立
     * 因为static方法在调用的时候,不需要new一个对象,因此类中的T根本还不存在
     *
     * @param <T> 泛型
     * @return Result
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(),
                null);
    }

    /**
     * 成功返回 - 带数据
     *
     * @param data 响应数据
     * @param <T>  泛型
     * @return Result
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(),
                data);
    }

    /**
     * 成功返回 - 自定义消息和数据
     *
     * @param data 响应数据
     * @param msg  自定义消息
     * @param <T>  泛型
     * @return Result
     */
    public static <T> Result<T> success(T data, String msg) {
        return new Result<>(ResultCode.SUCCESS.getCode(), msg, data);
    }

    /**
     * 失败返回 - 使用默认 500 错误和默认消息
     *
     * @param <T> 泛型
     * @return Result
     */
    public static <T> Result<T> error() {
        return new Result<>(ResultCode.FAILED.getCode(), ResultCode.FAILED.getMessage(),
                null);
    }

    /**
     * 失败返回 - 使用默认 500 错误，自定义消息
     *
     * @param msg 错误信息
     * @param <T> 泛型
     * @return Result
     */
    public static <T> Result<T> error(String msg) {
        return new Result<>(ResultCode.FAILED.getCode(), msg, null);
    }

    /**
     * 失败返回 - 自定义状态码和消息
     *
     * @param code 错误码
     * @param msg  错误信息
     * @param <T>  泛型
     * @return Result
     */
    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    /**
     * 失败返回 - 使用 ResultCode 枚举
     *
     * @param resultCode 错误枚举
     * @param <T>        泛型
     * @return Result
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }
}