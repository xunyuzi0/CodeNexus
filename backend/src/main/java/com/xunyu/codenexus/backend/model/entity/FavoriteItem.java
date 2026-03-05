// src/main/java/com/xunyu/codenexus/backend/model/entity/FavoriteItem.java
package com.xunyu.codenexus.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏夹题目明细表实体类
 *
 * @author CodeNexusBuilder
 */
@Data
@TableName("favorite_item")
public class FavoriteItem {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID (冗余字段，方便直接统计用户所有收藏)
     */
    private Long userId;

    /**
     * 收藏夹ID
     */
    private Long folderId;

    /**
     * 题目ID
     */
    private Long problemId;

    /**
     * 收藏时间
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