package com.xunyu.codenexus.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 竞技场房间表实体类
 *
 * @author CodeNexusBuilder
 */
@Data
@TableName("arena_room")
public class ArenaRoom {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 房间短码 (如: A8X9P2)
     */
    private String roomCode;

    /**
     * 房间类型: 1-私人邀请, 2-公开大厅, 3-天梯匹配
     */
    private Integer roomType;

    /**
     * 房间密码
     */
    private String password;

    /**
     * 题目ID
     */
    private Long problemId;

    /**
     * 状态: WAITING, FIGHTING, FINISHED, DISMISSED
     */
    private String status;

    /**
     * 房主ID
     */
    private Long creatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}