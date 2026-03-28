package com.xunyu.codenexus.backend.model.dto.request.problem;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 运行自测代码请求 DTO
 */
@Data
public class RunCodeRequest {
    @NotBlank(message = "代码不能为空")
    private String code;

    @NotBlank(message = "编程语言不能为空")
    private String language;

    // 用户自定义的输入数据数组，可能为空
    private List<String> inputs;
}