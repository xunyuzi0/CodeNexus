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

    private Long id;

    private String name;

    private String description;

    // 对应前端预期的 isDefault/isPublic
    private Integer isPublic;

    private String coverUrl;

    /**
     * 包含的题目数量 (动态统计)
     */
    private Integer problemCount;

    /**
     * 创建时间 (前端详情页顶部需要展示)
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}