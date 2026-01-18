package com.xunyu.codenexus.backend.exception;

import com.xunyu.codenexus.backend.common.result.Result;
import com.xunyu.codenexus.backend.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @author xunyu
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Object> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理 Spring Boot 参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Object> handleValidationException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = "参数校验失败";
        if (bindingResult.hasErrors()) {
            FieldError fieldError = bindingResult.getFieldError();
            if (fieldError != null) {
                message = fieldError.getDefaultMessage();
            }
        }
        log.warn("参数校验异常: {}", message);
        return Result.error(ResultCode.VALIDATE_FAILED.getCode(), message);
    }

    /**
     * 处理所有未知的系统异常 (兜底方案)
     */
    @ExceptionHandler(Exception.class)
    public Result<Object> handleException(Exception e) {
        // 使用 error 级别记录完整的堆栈信息，方便运维排查
        log.error("系统内部异常:", e);
        return Result.error(ResultCode.FAILED);
    }
}