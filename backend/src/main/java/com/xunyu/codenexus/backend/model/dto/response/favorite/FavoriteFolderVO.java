// src/main/java/com/xunyu/codenexus/backend/model/dto/response/favorite/FavoriteFolderVO.java
package com.xunyu.codenexus.backend.model.dto.response.favorite;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏夹列表单条记录/详情出参 VO
 *
 * @author CodeNexusBuilder
 */
@Data
public class FavoriteFolderVO {

    /**
     * 收藏夹 ID
     */
    private Long id;

    /**
     * 收藏夹名称
     */
    private String name;

    /**
     * 收藏夹描述
     */
    private String description;

    /**
     * 是否公开: 0-私有, 1-公开
     */
    private Integer isPublic;

    /**
     * 是否为默认收藏夹 (前端需求: 1为默认不可删除，0为自定义)
     */
    private Integer isDefault;

    /**
     * 封面图URL
     */
    private String coverUrl;

    /**
     * 包含的题目数量 (动态统计)
     */
    private Integer problemCount;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}