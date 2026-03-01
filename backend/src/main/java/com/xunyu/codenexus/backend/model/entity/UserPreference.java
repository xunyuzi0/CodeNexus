package com.xunyu.codenexus.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户偏好设置实体类
 *
 * @author CodeNexusBuilder
 */
@Data
// 开启 autoResultMap 以支持 JSON 字段的自动映射
@TableName(value = "user_preference", autoResultMap = true)
public class UserPreference {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的用户ID
     */
    private Long userId;

    /**
     * 代码编辑器主题
     */
    private String editorTheme;

    /**
     * 编辑器字体大小
     */
    private Integer fontSize;

    /**
     * 是否开启系统通知(1-开启, 0-关闭)
     */
    private Integer systemNotifications;

    /**
     * 扩展偏好设置(JSON格式, 预留给前端自由扩展)
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String extraSettings;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除(0-未删除, 1-已删除)
     */
    @TableLogic
    private Integer isDeleted;
}