package com.xunyu.codenexus.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户统计大盘表 (新增了天梯积分与胜率统计)
 *
 * @author CodeNexusBuilder
 */
@TableName(value = "user_statistics")
@Data
public class UserStatistics implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 关联用户ID
     */
    private Long userId;
    /**
     * 已解决(AC)的题目总数
     */
    private Integer solvedCount;
    /**
     * 当前连续打卡天数
     */
    private Integer continuousCheckinDays;
    /**
     * 最后一次打卡日期
     */
    private Date lastCheckinDate;
    /**
     * 天梯排位分数 (初始1000)
     */
    private Integer arenaScore;
    /**
     * 天梯总对局数
     */
    private Integer arenaMatches;
    /**
     * 天梯获胜场数
     */
    private Integer arenaWins;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 逻辑删除(0-未删除, 1-已删除)
     */
    private Integer isDeleted;
}