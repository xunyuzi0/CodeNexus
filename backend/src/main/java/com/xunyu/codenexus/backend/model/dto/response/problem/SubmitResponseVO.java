package com.xunyu.codenexus.backend.model.dto.response.problem;

import lombok.Data;

import java.util.List;

/**
 * 判题进度轮询出参 VO
 */
@Data
public class SubmitResponseVO {
    // "JUDGING", "OK", "COMPILE_ERROR", "SYSTEM_ERROR"
    private String status;
    private String message;
    private List<CheckpointVO> checkpoints;

    @Data
    public static class CheckpointVO {
        private Long id;
        // "PENDING", "RUNNING", "AC", "WA", "TLE", "RE", "MLE"
        private String status;
        private String time;
        private String memory;
    }
}