// src/main/java/com/xunyu/codenexus/backend/model/dto/response/favorite/FavoriteProblemVO.java
package com.xunyu.codenexus.backend.model.dto.response.favorite;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 收藏夹内的题目列表单条记录出参 VO
 *
 * @author CodeNexusBuilder
 */
@Data
public class FavoriteProblemVO {

    /**
     * 题目的物理主键 ID (与 problemId 保持一致，适配前端通用组件)
     */
    private Long id;

    /**
     * 题目 ID (保留原有字段以防其他地方使用)
     */
    private Long problemId;

    /**
     * 题目的展示 ID
     */
    private String displayId;

    /**
     * 题目名称
     */
    private String title;

    /**
     * 难度枚举 (1:简单, 2:中等, 3:困难)
     */
    private Integer difficulty;

    /**
     * 算法标签列表
     */
    private List<String> tags;

    /**
     * 全局首次通过率 (%)
     */
    private Double passRate;

    /**
     * 当前登录用户对该题的状态 ("PASSED"已通过, "ATTEMPTED"尝试过, null/空为未做)
     */
    private String status;

    /**
     * 收藏该题目的时间 (对应 favorite_item 的 create_time)
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime addTime;
}