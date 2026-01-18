package com.xunyu.codenexus.backend.aop.auth;

import com.xunyu.codenexus.backend.model.enums.UserRoleEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验注解
 * 用于限制接口只能被特定角色的用户访问
 *
 * @author xunyu
 */

// 指定标记在方法上
// SOURCE:代码编译完就扔了(比如 @Override)
// CLASS：编译在 .class 文件里，但程序跑起来后 JVM 就看不到了
// RUNTIME:表示程序运行起来之后，这个标签依然存在
@Target(ElementType.METHOD)
// 在运行时有效
@Retention(RetentionPolicy.RUNTIME)
public @interface Protector {

    /**
     * 允许访问的角色
     * 默认只要登录就能访问 (UserRoleEnum.USER)
     */
    UserRoleEnum role() default UserRoleEnum.USER;
}