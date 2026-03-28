package com.xunyu.codenexus.backend.model.dto.response.problem;

import lombok.Data;

/**
 * 自测运行结果 VO
 */
@Data
public class RunResultVO {
    // 状态枚举: "AC" | "WA" | "ERROR" | "TLE"
    private String status;
    private String input;
    private String output;
    private String expected; // 自测模式下通常为 null
    private String runtime; // 例如 "15ms"
}