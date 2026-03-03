// src/main/java/com/xunyu/codenexus/backend/controller/RankController.java
package com.xunyu.codenexus.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunyu.codenexus.backend.aop.auth.Protector;
import com.xunyu.codenexus.backend.common.result.Result;
import com.xunyu.codenexus.backend.model.dto.request.rank.LeaderboardQueryRequest;
import com.xunyu.codenexus.backend.model.dto.response.rank.MyRankVO;
import com.xunyu.codenexus.backend.model.dto.response.rank.RankUserVO;
import com.xunyu.codenexus.backend.model.enums.UserRoleEnum;
import com.xunyu.codenexus.backend.service.RankService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 排行榜模块控制器
 *
 * @author CodeNexusBuilder
 */
@RestController
@RequestMapping("/api/rank")
public class RankController {

    @Resource
    private RankService rankService;

    /**
     * 获取排行榜列表 (Top N)
     * 公开接口，无需 @Protector 鉴权
     */
    @GetMapping("/leaderboard")
    public Result<Page<RankUserVO>> getLeaderboard(LeaderboardQueryRequest request) {
        Page<RankUserVO> leaderboard = rankService.getLeaderboard(request);
        return Result.success(leaderboard);
    }

    /**
     * 获取当前登录用户的排名信息
     * 必须登录方可访问
     */
    @GetMapping("/me")
    @Protector(role = UserRoleEnum.USER)
    public Result<MyRankVO> getMyRank() {
        MyRankVO myRank = rankService.getMyRank();
        return Result.success(myRank);
    }
}