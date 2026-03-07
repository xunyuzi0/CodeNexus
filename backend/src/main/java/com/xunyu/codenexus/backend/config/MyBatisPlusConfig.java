// src/main/java/com/xunyu/codenexus/backend/config/MyBatisPlusConfig.java
package com.xunyu.codenexus.backend.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis Plus 配置类
 * 用于自动填充 createTime 和 updateTime，以及注册分页插件
 *
 * @author xunyu
 */
@Slf4j
@Configuration
// implements MetaObjectHandler 这是 MyBatis Plus 提供的一个钩子接口。
// 只要你实现了它，每次执行 SQL 前，MP 都会先把对象拿给你过目一遍。
public class MyBatisPlusConfig implements MetaObjectHandler {

    /**
     * 注册 MyBatis-Plus 分页插件 (解决 total 和 pages 始终为 0 的问题)
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 因为你的项目使用的是 MySQL，所以这里显式指定 DbType.MYSQL
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * 插入时的填充策略
     * 调用 mapper.insert() 时自动触发
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 参数1: metaObject (当前操作的对象)
        // 参数2: "createTime" (Java实体类里的字段名，注意不是数据库列名！)
        // 参数3: LocalDateTime.class (字段类型)
        // 参数4: LocalDateTime.now() (要填的具体值)
        // strictInsertFill (严格模式)：
        // 它的逻辑是:如果手动设置,不会自动填充;如果为 null,我再自动填充
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class,
                LocalDateTime.now());
        // 插入时，updateTime 也要有一份初始值
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class,
                LocalDateTime.now());
    }

    /**
     * 更新时的填充策略
     * 调用 mapper.updateById() 时自动触发
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class,
                LocalDateTime.now());
    }
}