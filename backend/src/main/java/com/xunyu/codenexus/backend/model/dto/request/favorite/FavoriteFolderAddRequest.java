// src/main/java/com/xunyu/codenexus/backend/model/dto/request/favorite/FavoriteFolderAddRequest.java
package com.xunyu.codenexus.backend.model.dto.request.favorite;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建收藏夹入参 DTO
 *
 * @author CodeNexusBuilder
 */
@Data
public class FavoriteFolderAddRequest {

    /**
     * 文件夹名称 (必填)
     */
    @NotBlank(message = "文件夹名称不能为空")
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否公开: 0-私有, 1-公开 (默认 0)
     */
    private Integer isPublic = 0;

    /**
     * 封面图URL
     */
    private String coverUrl;
}