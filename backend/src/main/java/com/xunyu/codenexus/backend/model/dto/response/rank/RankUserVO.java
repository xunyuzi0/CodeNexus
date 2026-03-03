// src/main/java/com/xunyu/codenexus/backend/model/dto/response/rank/RankUserVO.java
package com.xunyu.codenexus.backend.model.dto.response.rank;

import lombok.Data;

/**
 * 排行榜列表单条记录出参 VO
 *
 * @author CodeNexusBuilder
 */
@Data
public class RankUserVO {
    private Long id;
    // 对应 nickname
    private String name;
    // 对应 avatar_url
    private String avatar;
    // 战力积分
    private Integer score;
    // 胜率
    private Integer winRate;
    // 动态计算的段位
    private String tier;
    // 动态计算的当前列表排名
    private Integer rank;
}