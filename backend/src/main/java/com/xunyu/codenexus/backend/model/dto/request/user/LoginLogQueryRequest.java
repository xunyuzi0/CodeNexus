package com.xunyu.codenexus.backend.model.dto.request.user;

import com.xunyu.codenexus.backend.model.dto.request.BaseQueryRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 登录日志查询入参 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LoginLogQueryRequest extends BaseQueryRequest {
    // 继承了分页及排序字段，暂不需要额外查询条件
}