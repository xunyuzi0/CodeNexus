package com.xunyu.codenexus.backend.model.dto.request.admin;

import com.xunyu.codenexus.backend.model.dto.request.BaseQueryRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminProblemQueryRequest extends BaseQueryRequest {
    /** 搜索关键词（模糊匹配标题/displayId） */
    private String keyword;
    /** 难度筛选：1-Easy, 2-Medium, 3-Hard */
    private Integer difficulty;
    /** 状态筛选：0-草稿, 1-已上架, 2-已下架 */
    private Integer status;
}
