// src/main/java/com/xunyu/codenexus/backend/model/dto/request/problem/SubmissionQueryRequest.java
package com.xunyu.codenexus.backend.model.dto.request.problem;

import com.xunyu.codenexus.backend.model.dto.request.BaseQueryRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 获取历史提交记录的分页查询入参 DTO
 * 继承 BaseQueryRequest 获取基础的分页与排序参数
 *
 * @author CodeNexusBuilder
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SubmissionQueryRequest extends BaseQueryRequest {
    // 根据契约，当前只需根据 URL 的 path 提取 problemId，这里暂时无需额外过滤条件
}