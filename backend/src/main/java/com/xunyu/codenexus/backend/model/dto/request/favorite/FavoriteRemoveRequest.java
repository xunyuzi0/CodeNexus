// src/main/java/com/xunyu/codenexus/backend/model/dto/request/favorite/FavoriteRemoveRequest.java
package com.xunyu.codenexus.backend.model.dto.request.favorite;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 从收藏夹移除题目入参 DTO
 *
 * @author CodeNexusBuilder
 */
@Data
public class FavoriteRemoveRequest {

    @NotNull(message = "题目ID不能为空")
    private Long problemId;

    @NotNull(message = "收藏夹ID不能为空")
    private Long folderId;
}