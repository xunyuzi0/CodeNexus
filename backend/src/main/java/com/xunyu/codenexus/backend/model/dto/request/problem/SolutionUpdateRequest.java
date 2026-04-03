// src/main/java/com/xunyu/codenexus/backend/model/dto/request/problem/SolutionUpdateRequest.java
package com.xunyu.codenexus.backend.model.dto.request.problem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SolutionUpdateRequest {

    @NotBlank(message = "题解标题不能为空")
    @Size(max = 128, message = "标题长度不能超过128个字符")
    private String title;

    @NotBlank(message = "题解正文不能为空")
    private String content;

    @NotBlank(message = "代码实现不能为空")
    private String code;
}