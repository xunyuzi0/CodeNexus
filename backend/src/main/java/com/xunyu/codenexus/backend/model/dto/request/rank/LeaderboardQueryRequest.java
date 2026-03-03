package com.xunyu.codenexus.backend.model.dto.request.rank;

import com.xunyu.codenexus.backend.model.dto.request.BaseQueryRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 获取排行榜列表入参 DTO
 * 继承 BaseQueryRequest，前端只需传入 current 和 pageSize
 *
 * @author CodeNexusBuilder
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LeaderboardQueryRequest extends BaseQueryRequest {
    // 暂不需要额外的查询条件，依靠基类的分页参数即可
}