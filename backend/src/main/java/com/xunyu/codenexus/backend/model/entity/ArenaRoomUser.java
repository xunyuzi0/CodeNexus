package com.xunyu.codenexus.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 竞技场房间用户关联表实体类
 *
 * @author CodeNexusBuilder
 */
@Data
@TableName("arena_room_user")
public class ArenaRoomUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联 arena_room 的物理主键
     */
    private Long roomId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 是否为房主 (0-否, 1-是)
     */
    private Integer isCreator;

    /**
     * 状态: JOINED, READY, BATTLING, ESCAPED
     */
    private String status;

    /**
     * 战斗结果: WIN, LOSE, DRAW
     */
    private String result;

    /**
     * 天梯分变动 (+/-)
     */
    private Integer scoreChange;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}