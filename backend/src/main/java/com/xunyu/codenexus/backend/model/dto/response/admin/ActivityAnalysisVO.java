package com.xunyu.codenexus.backend.model.dto.response.admin;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 活跃度分析 VO（难度分布、热门题目、语言分布）
 */
@Data
public class ActivityAnalysisVO {
    private Map<String, Long> difficultyDistribution;
    private List<TopProblemVO> topProblems;
    private Map<String, Long> languageDistribution;

    @Data
    public static class TopProblemVO {
        private Long id;
        private String displayId;
        private String title;
        private Long submitCount;
    }
}
