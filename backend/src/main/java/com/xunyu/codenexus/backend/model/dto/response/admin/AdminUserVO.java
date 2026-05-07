package com.xunyu.codenexus.backend.model.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String email;
    private String role;
    private Integer ratingScore;
    private Integer globalRank;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
