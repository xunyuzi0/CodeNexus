package com.xunyu.codenexus.backend.model.dto.response.problem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 每日一练推荐结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyPracticeVO {

    /** 推荐题目 ID */
    private Long problemId;

    /** 当天内该题是否已被当前用户 AC */
    private Boolean alreadySolved;
}
