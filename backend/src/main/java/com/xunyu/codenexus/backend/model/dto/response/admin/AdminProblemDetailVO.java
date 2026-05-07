package com.xunyu.codenexus.backend.model.dto.response.admin;

import com.xunyu.codenexus.backend.model.dto.response.problem.ExampleVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 管理端题目详情 VO（包含描述、示例等完整字段）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdminProblemDetailVO extends AdminProblemVO {

    /**
     * 题目描述 (Markdown)
     */
    private String description;

    /**
     * 示例输入输出
     */
    private List<ExampleVO> examples;

    /**
     * 时间限制 (ms)
     */
    private Integer timeLimit;

    /**
     * 内存限制 (MB)
     */
    private Integer memoryLimit;
}
