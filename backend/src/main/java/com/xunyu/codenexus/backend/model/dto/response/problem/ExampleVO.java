// src/main/java/com/xunyu/codenexus/backend/model/dto/response/problem/ExampleVO.java
package com.xunyu.codenexus.backend.model.dto.response.problem;

import lombok.Data;

/**
 * 题目的测试用例示例 VO
 * 作为 ProblemDetailVO 的子结构返回给前端，同时供 MyBatis-Plus 映射 JSON 字段
 *
 * @author CodeNexusBuilder
 */
@Data
public class ExampleVO {

    /**
     * 输入示例
     */
    private String input;

    /**
     * 输出示例
     */
    private String output;

    /**
     * 示例解释说明
     */
    private String explanation;
}