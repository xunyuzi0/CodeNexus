package com.xunyu.codenexus.backend.utils;

import com.xunyu.codenexus.backend.common.result.ResultCode;
import com.xunyu.codenexus.backend.exception.BusinessException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * 业务断言工具类
 * 规范：
 * 1. notNull: 校验对象引用不为 null (适用于 POJO, Integer 等)
 * 2. notEmpty: 校验容器/字符串不为 null 且内容也不为null (适用于 List, Map, String, Array)
 *
 * @author xunyu
 */
public class AssertUtil {

    private AssertUtil() {
        throw new IllegalStateException("Utility class");
    }

    // ========================================================================
    //  核心异常抛出逻辑
    // ========================================================================

    /**
     * 默认抛出参数校验失败异常
     *
     * @param message 异常信息
     */
    private static void throwException(String message) {
        throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), message);
    }

    /**
     * 自定义异常类型抛出
     *
     * @param resultCode 结果码枚举
     * @param message    异常信息
     */
    private static void throwException(ResultCode resultCode, String message) {
        throw new BusinessException(resultCode.getCode(), message);
    }

    // ========================================================================
    //  1. 逻辑断言 (True/False)
    // ========================================================================

    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throwException(message);
        }
    }

    public static void isTrue(boolean expression, ResultCode resultCode, String message) {
        if (!expression) {
            throwException(resultCode, message);
        }
    }

    public static void isFalse(boolean expression, String message) {
        if (expression) {
            throwException(message);
        }
    }

    public static void isFalse(boolean expression, ResultCode resultCode, String message) {
        if (expression) {
            throwException(resultCode, message);
        }
    }

    // ========================================================================
    //  2. 非空断言 (NotNull - 仅校验引用)
    //  适用于：User, Order, Integer 等普通对象
    // ========================================================================

    public static void notNull(Object object, String message) {
        if (object == null) {
            throwException(message);
        }
    }

    public static void notNull(Object object, ResultCode resultCode, String message) {
        if (object == null) {
            throwException(resultCode, message);
        }
    }

    public static void isNull(Object object, String message) {
        if (object != null) {
            throwException(message);
        }
    }

    public static void isNull(Object object, ResultCode resultCode, String message) {
        if (object != null) {
            throwException(resultCode, message);
        }
    }

    // ========================================================================
    //  3. 内容非空断言 (NotEmpty - 校验引用 + 内容)
    //  适用于：String, Collection, Map, Array
    // ========================================================================

    /**
     * 校验字符串 (不仅不能为null，也不能是空字符串或纯空格)
     */
    public static void notEmpty(String text, String message) {
        if (!StringUtils.hasText(text)) {
            throwException(message);
        }
    }

    /**
     * 校验集合 (List, Set)
     */
    public static void notEmpty(Collection<?> collection, String message) {
        if (CollectionUtils.isEmpty(collection)) {
            throwException(message);
        }
    }

    /**
     * 校验 Map
     */
    public static void notEmpty(Map<?, ?> map, String message) {
        if (CollectionUtils.isEmpty(map)) {
            throwException(message);
        }
    }

    /**
     * 校验数组
     */
    public static void notEmpty(Object[] array, String message) {
        if (ObjectUtils.isEmpty(array)) {
            throwException(message);
        }
    }

    // ========================================================================
    //  4. 其他常用校验
    // ========================================================================

    public static void notEquals(Object obj1, Object obj2, String message) {
        if (Objects.equals(obj1, obj2)) {
            throwException(message);
        }
    }

    public static void notEquals(Object obj1, Object obj2, ResultCode resultCode, String message) {
        if (Objects.equals(obj1, obj2)) {
            throwException(resultCode, message);
        }
    }

    public static void equals(Object obj1, Object obj2, String message) {
        if (!Objects.equals(obj1, obj2)) {
            throwException(message);
        }
    }

    public static void minLen(String text, int min, String message) {
        if (text == null || text.length() < min) {
            throwException(message);
        }
    }

    public static void maxLen(String text, int max, String message) {
        if (text != null && text.length() > max) {
            throwException(message);
        }
    }


    //补充
    public static void equals(Object obj1, Object obj2, ResultCode resultCode, String message) {
        if (!Objects.equals(obj1, obj2)) {
            throwException(resultCode, message);
        }
    }
}