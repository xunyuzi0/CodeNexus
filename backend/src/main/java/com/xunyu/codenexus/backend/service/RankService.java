// src/main/java/com/xunyu/codenexus/backend/service/RankService.java
package com.xunyu.codenexus.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunyu.codenexus.backend.model.dto.request.rank.LeaderboardQueryRequest;
import com.xunyu.codenexus.backend.model.dto.response.rank.MyRankVO;
import com.xunyu.codenexus.backend.model.dto.response.rank.RankUserVO;

/**
 * 排行榜业务接口
 *
 * @author CodeNexusBuilder
 */
public interface RankService {

    /**
     * 分页获取排行榜列表
     */
    Page<RankUserVO> getLeaderboard(LeaderboardQueryRequest request);

    /**
     * 获取当前登录用户的排名信息
     */
    MyRankVO getMyRank();
}