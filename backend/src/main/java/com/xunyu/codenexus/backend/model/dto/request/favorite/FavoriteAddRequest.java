// src/main/java/com/xunyu/codenexus/backend/model/dto/request/favorite/FavoriteAddRequest.java
package com.xunyu.codenexus.backend.model.dto.request.favorite;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 添加题目到收藏夹入参 DTO
 *
 * @author CodeNexusBuilder
 */
@Data
public class FavoriteAddRequest {

    @NotNull(message = "题目ID不能为空")
    private Long problemId;

    @NotNull(message = "收藏夹ID不能为空")
    private Long folderId;
}