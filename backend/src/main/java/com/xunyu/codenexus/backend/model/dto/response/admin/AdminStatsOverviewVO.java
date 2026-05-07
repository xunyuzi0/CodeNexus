package com.xunyu.codenexus.backend.model.dto.response.admin;

import lombok.Data;

@Data
public class AdminStatsOverviewVO {
    private Long totalUsers;
    private Long totalProblems;
    private Long totalSubmissions;
    private Long todayNewUsers;
    private Long todaySubmissions;
    private Double overallPassRate;
    private Long activeUserCount;
    private Long onlineRoomCount;
}
