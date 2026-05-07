package com.xunyu.codenexus.backend.model.dto.request.admin;

import com.xunyu.codenexus.backend.model.dto.request.BaseQueryRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminArenaQueryRequest extends BaseQueryRequest {
    /** 房间状态筛选：WAITING/BATTLING/FINISHED/DISMISSED */
    private String status;
    /** 房间类型筛选：1-私人, 2-公开, 3-天梯 */
    private Integer roomType;
}
