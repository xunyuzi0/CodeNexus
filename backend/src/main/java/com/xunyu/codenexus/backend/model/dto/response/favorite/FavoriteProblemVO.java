// src/main/java/com/xunyu/codenexus/backend/model/dto/response/favorite/FavoriteProblemVO.java
package com.xunyu.codenexus.backend.model.dto.response.favorite;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏夹内的题目列表单条记录出参 VO
 *
 * @author CodeNexusBuilder
 */
@Data
public class FavoriteProblemVO {

    private Long problemId;

    private String title;

    private Integer difficulty;

    /**
     * 通过率
     */
    private Double passRate;

    /**
     * 收藏该题目的时间 (对应 favorite_item 的 create_time)
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime addTime;
}