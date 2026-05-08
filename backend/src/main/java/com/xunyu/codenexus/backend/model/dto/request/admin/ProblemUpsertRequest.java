package com.xunyu.codenexus.backend.model.dto.request.admin;

import com.xunyu.codenexus.backend.model.dto.response.problem.ExampleVO;
import com.xunyu.codenexus.backend.model.entity.ProblemTestcase;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ProblemUpsertRequest {
    @NotBlank(message = "题目标题不能为空")
    private String title;

    @NotBlank(message = "题目描述不能为空")
    private String description;

    @NotNull(message = "难度不能为空")
    private Integer difficulty;

    private List<String> tags;

    private Integer timeLimit = 1000;

    private Integer memoryLimit = 256;

    private List<ExampleVO> examples;

    /** 题目状态: 0-草稿, 1-已上架, 2-已下架 */
    private Integer status = 0;

    /** 判题检测点（创建题目时可选） */
    private List<ProblemTestcase> testcases;
}
