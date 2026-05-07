package com.xunyu.codenexus.backend.model.dto.response.admin;

import com.xunyu.codenexus.backend.model.dto.response.LoginLogVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminUserDetailVO extends AdminUserVO {
    private String phone;
    private String bio;
    private Integer solvedCount;
    private Integer arenaScore;
    private Integer arenaMatches;
    private Integer arenaWins;
    private Double winRate;
    private Integer continuousCheckinDays;
    private List<LoginLogVO> recentLoginLogs;
}
