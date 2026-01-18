package com.xunyu.codenexus.backend.aop.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author xunyu
 * @date 2026/1/10 19:45
 * @description: 操作日志注解
 */

@Target(ElementType.METHOD) // 只需要标记在方法上
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {

    /**
     * 业务操作描述 (例如: "用户提交代码", "删除题目")
     */
    String detail() default "";
}
