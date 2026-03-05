// src/main/java/com/xunyu/codenexus/backend/model/entity/FavoriteFolder.java
package com.xunyu.codenexus.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏夹表实体类
 *
 * @author CodeNexusBuilder
 */
@Data
@TableName("favorite_folder")
public class FavoriteFolder {

    /**
     * 主键, 文件夹ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 文件夹名称
     */
    private String name;

    /**
     * 文件夹描述
     */
    private String description;

    /**
     * 是否公开: 0-私有, 1-公开
     */
    private Integer isPublic;

    /**
     * 封面图URL
     */
    private String coverUrl;

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