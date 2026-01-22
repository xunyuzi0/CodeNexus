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
 * 用于在业务层验证参数或逻辑是否符合预期。如果不符合，则抛出 BusinessException。
 * 核心逻辑：所有的断言失败，默认抛出 "400 参数校验失败" 类型的异常，而非 500 系统异常。
 *
 * @author xunyu
 * @date 2026/1/11 1:24
 * @description: 断言工具类
 */
public class AssertUtil {

    /**
     * 私有构造函数,防止工具类被实例化
     */
    private AssertUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 断言对象不为空
     * 如果对象为 null,则抛出异常
     *
     * @param object  待验证的对象
     * @param message 异常提示信息
     */
    public static void notNull(Object object, String message) {
        if (object == null) {
            throwException(message);
        }
    }

    /**
     * 断言对象必须为空
     * 如果对象不为 null，则抛出异常
     *
     * @param object  待验证的对象
     * @param message 异常提示信息
     */
    public static void isNull(Object object, String message) {
        if (object != null) {
            throwException(message);
        }
    }

    /**
     * 断言字符串不为空
     * 包含对 null、空字符串 "" 以及纯空格 "   " 的校验
     *
     * @param text    待验证的字符串
     * @param message 异常提示信息
     */
    public static void notEmpty(String text, String message) {
        if (!StringUtils.hasText(text)) {
            throwException(message);
        }
    }

    /**
     * 断言集合不为空
     *
     * @param collection 待验证的集合 (List, Set 等)
     * @param message    异常提示信息
     */
    public static void notEmpty(Collection<?> collection, String message) {
        if (CollectionUtils.isEmpty(collection)) {
            throwException(message);
        }
    }

    /**
     * 断言 Map 不为空
     * 优化：Map 应该使用 CollectionUtils.isEmpty(map) 重载方法，或者 ObjectUtils
     *
     * @param map     待验证的 Map
     * @param message 异常提示信息
     */
    public static void notEmpty(Map<?, ?> map, String message) {
        if (CollectionUtils.isEmpty(map)) {
            throwException(message);
        }
    }

    /**
     * 断言数组不为空
     */
    public static void notEmpty(Object[] array, String message) {
        if (ObjectUtils.isEmpty(array)) {
            throwException(message);
        }
    }

    /**
     * 断言字符串长度不超过指定值
     *
     * @param text    待验证字符串
     * @param max     最大长度
     * @param message 报错信息
     */
    public static void maxLen(String text, int max, String message) {
        if (text != null && text.length() > max) {
            throwException(message);
        }
    }

    /**
     * 断言字符串长度不小于指定值 (比如密码至少6位)
     */
    public static void minLen(String text, int min, String message) {
        if (text == null || text.length() < min) {
            throwException(message);
        }
    }

    /**
     * 断言布尔表达式为真 (True)
     * 如果表达式为 false，则抛出异常
     *
     * @param expression 布尔表达式
     * @param message    异常提示信息
     */
    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throwException(message);
        }
    }

    /**
     * 断言布尔表达式为假 (False)
     * 如果表达式为 true，则抛出异常
     *
     * @param expression 布尔表达式
     * @param message    异常提示信息
     */
    public static void isFalse(boolean expression, String message) {
        if (expression) {
            throwException(message);
        }
    }

    /**
     * 断言两个对象不相等
     * 如果两个对象相等 (equals 为 true)，则抛出异常
     *
     * @param obj1    对象1
     * @param obj2    对象2
     * @param message 异常提示信息
     */
    public static void notEquals(Object obj1, Object obj2, String message) {
        if (Objects.equals(obj1, obj2)) {
            throwException(message);
        }
    }

    /**
     * 断言两个对象必须相等
     * 如果两个对象不相等，则抛出异常
     *
     * @param obj1    对象1
     * @param obj2    对象2
     * @param message 异常提示信息
     */
    public static void equals(Object obj1, Object obj2, String message) {
        if (!Objects.equals(obj1, obj2)) {
            throwException(message);
        }
    }

    /**
     * 断言数字必须大于 0 (常用于校验 ID)
     */
    public static void isPositive(Long number, String message) {
        if (number == null || number <= 0) {
            throwException(message);
        }
    }

    /**
     * 断言数字必须在范围内 (常用于校验状态码、类型等)
     */
    public static void range(Integer number, int min, int max, String message) {
        if (number == null || number < min || number > max) {
            throwException(message);
        }
    }

    /**
     * 抛出业务异常
     * 关键点：这里显式传入 ResultCode.VALIDATE_FAILED.getCode() (即 400)。
     * 这样前端收到的就是 400 错误，而不是 500 系统错误。
     *
     * @param message 错误信息
     */
    private static void throwException(String message) {
        throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), message);
    }
}