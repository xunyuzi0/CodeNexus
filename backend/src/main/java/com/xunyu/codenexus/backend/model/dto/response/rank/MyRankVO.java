package com.xunyu.codenexus.backend.model.dto.response.rank;

import lombok.Data;

/**
 * 当前用户个人排名信息出参 VO
 *
 * @author CodeNexusBuilder
 */
@Data
public class MyRankVO {
    // 个人全服排名 global_rank
    private Integer rank;
    // 战力积分
    private Integer score;
    // 动态段位称号
    private String tier;
    // 胜率
    private Integer winRate;
}