package com.xunyu.codenexus.backend.model.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AdminProblemVO {
    private Long id;
    private String displayId;
    private String title;
    private Integer difficulty;
    private List<String> tags;
    private Integer status;
    private Integer submitNum;
    private Integer acceptedNum;
    private Double passRate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
