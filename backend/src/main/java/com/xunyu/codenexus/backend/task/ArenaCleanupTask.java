package com.xunyu.codenexus.backend.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xunyu.codenexus.backend.mapper.ArenaRoomMapper;
import com.xunyu.codenexus.backend.model.entity.ArenaRoom;
import com.xunyu.codenexus.backend.model.enums.ArenaRoomStatus;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 竞技场房间清理定时任务
 */
@Slf4j
@Component
public class ArenaCleanupTask {

    @Resource
    private ArenaRoomMapper arenaRoomMapper;

    /**
     * 每分钟清理超时的 WAITING 房间
     * 超过 2 分钟仍处于 WAITING 状态的房间，自动标记为 DISMISSED
     */
    @Scheduled(fixedDelay = 60000)
    public void cleanupExpiredWaitingRooms() {
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(2);

        LambdaUpdateWrapper<ArenaRoom> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper
                .eq(ArenaRoom::getStatus, ArenaRoomStatus.WAITING.getValue())
                .le(ArenaRoom::getCreateTime, expireTime)
                .set(ArenaRoom::getStatus, ArenaRoomStatus.DISMISSED.getValue());

        int updated = arenaRoomMapper.update(null, updateWrapper);
        if (updated > 0) {
            log.info("[竞技场清理] 已将 {} 个超时的 WAITING 房间标记为 DISMISSED", updated);
        }
    }
}
