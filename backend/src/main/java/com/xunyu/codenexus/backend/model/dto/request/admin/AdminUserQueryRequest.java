package com.xunyu.codenexus.backend.model.dto.request.admin;

import com.xunyu.codenexus.backend.model.dto.request.BaseQueryRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminUserQueryRequest extends BaseQueryRequest {
    /** 搜索关键词（模糊匹配用户名/昵称/邮箱） */
    private String keyword;
    /** 角色筛选：user/admin/ban */
    private String role;
}
