package com.xunyu.codenexus.backend.common.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 核心原理：
 * 基于 Java 的 ThreadLocal 实现。ThreadLocal 为每个线程提供独立的变量副本。
 * 并通过内部类将 UserId 和 UserRole 合并为一个 UserInfo 对象存储。
 * 这样可以保证用户上下文的原子性（要么全有，要么全无），且利于后续扩展（如增加 tenantId, userName 等）。
 *
 * @author xunyu
 * @date 2026/1/10 20:54
 * @description: 用户上下文信息管理类(一个基于线程隔离的全局变量容器)
 */
public class UserContext {

    /**
     * 核心容器：存储一个关键的键,并且是固定的唯一一个
     * 每一个用户调用时,都会在自己的线程中,打开threadLocals Map,并将下面这个USER_THREAD_LOCAL中存着的key和数据都放进去
     * 等到调用查询的时候,每一个用户由于有不同的线程,因此数据被存在了每个用户自己的线程中,即使共用一个key也是互不干扰
     */
    private static final ThreadLocal<UserInfo> USER_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 添加私有构造函数
     * 原因:本类为工具类,只需要使用静态方法,因此不能被实例化
     * 如果没有添加这个私有的构造函数(此时没有任何构造函数),那么编译器会自动添加public无参构造
     */
    private UserContext() {
        // 防止类内部误调用或反射攻击(反射可以强行调用私有构造函数)
        throw new IllegalStateException("Utility class");
    }

    /**
     * 设置用户上下文信息(由拦截器调用并设置)
     *
     * @param userId   用户ID
     * @param userRole 用户角色
     */
    public static void set(Long userId, String userRole) {
        USER_THREAD_LOCAL.set(new UserInfo(userId, userRole));
    }

    /**
     * 获取当前登录用户的 ID(任何地方都能用)
     *
     * @return Long 用户ID (未登录则返回 null)
     */
    public static Long getUserId() {
        UserInfo userInfo = USER_THREAD_LOCAL.get();
        return userInfo != null ? userInfo.getUserId() : null;
    }

    /**
     * 获取当前登录用户的角色(任何地方都能用)
     *
     * @return String 用户角色 (未登录则返回 null)
     */
    public static String getUserRole() {
        UserInfo userInfo = USER_THREAD_LOCAL.get();
        return userInfo != null ? userInfo.getUserRole() : null;
    }

    /**
     * 获取完整的用户信息对象 (用于需要一次性获取所有信息的场景)
     *
     * @return UserInfo
     */
    public static UserInfo getUser() {
        return USER_THREAD_LOCAL.get();
    }

    /**
     * 清理当前线程的上下文
     * 防止内存泄漏
     */
    public static void remove() {
        USER_THREAD_LOCAL.remove();
    }

    /**
     * 内部类：承载用户上下文数据
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {

        /**
         * 用户ID
         */
        private Long userId;

        /**
         * 用户角色
         */
        private String userRole;
    }
}