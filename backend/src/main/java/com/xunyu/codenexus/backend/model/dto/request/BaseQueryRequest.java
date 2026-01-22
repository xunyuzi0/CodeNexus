package com.xunyu.codenexus.backend.model.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 封装所有列表查询通用的参数（分页、排序、时间范围）。
 * 所有需要分页查询的 DTO 都应该继承此类。
 *
 * @author xunyu
 * @date 2026/1/11 17:19
 * @description: 查询请求参数基类
 */
@Data
public class BaseQueryRequest {

    /**
     * 当前页码
     */
    private Integer current = 1;

    /**
     * 每页显示条数
     */
    private Integer pageSize = 10;

    /**
     * 排序字段 (例如: "create_time")
     */
    private String sortField;

    /**
     * 排序规则 (ascend / descend)
     */
    private String sortOrder;

    /**
     * 开始时间
     * 前端传入格式示例: "2026-01-01 12:00:00"
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 数据库查询偏移量 (LIMIT offset, size)
     * 计算公式: (current - 1) * pageSize
     * 优化：直接通过 getter 计算，不再依赖 AOP 切面注入
     * 前端无需传递，也不会返回给前端
     */
    @JsonIgnore
    public Long getOffset() {
        return (long) (getCurrent() - 1) * getPageSize();
    }

    /**
     * 获取每页条数 (做安全限制)
     * 防止前端恶意传递 size=10000 拖垮数据库
     */
    public Integer getPageSize() {
        if (this.pageSize == null || this.pageSize < 1) {
            return 10;
        }
        // 限制最大每页 100 条
        if (this.pageSize > 100) {
            return 100;
        }
        return this.pageSize;
    }

    /**
     * 获取当前页码 (做安全限制)
     */
    public Integer getCurrent() {
        if (this.current == null || this.current < 1) {
            return 1;
        }
        return this.current;
    }
}