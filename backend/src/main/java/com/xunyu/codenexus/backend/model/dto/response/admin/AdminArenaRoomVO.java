package com.xunyu.codenexus.backend.model.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminArenaRoomVO {
    private Long id;
    private String roomCode;
    private Integer roomType;
    private Long problemId;
    private String problemTitle;
    private String status;
    private Long creatorId;
    private String creatorName;
    private Integer playerCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
