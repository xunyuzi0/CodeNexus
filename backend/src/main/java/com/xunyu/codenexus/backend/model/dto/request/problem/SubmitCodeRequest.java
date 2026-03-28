package com.xunyu.codenexus.backend.model.dto.request.problem;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 异步提交判题入参 DTO
 */
@Data
public class SubmitCodeRequest {
    @NotBlank(message = "代码不能为空")
    private String code;

    @NotBlank(message = "编程语言不能为空")
    private String language;
}