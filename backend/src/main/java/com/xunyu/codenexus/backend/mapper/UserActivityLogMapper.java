// src/main/java/com/xunyu/codenexus/backend/mapper/UserActivityLogMapper.java
package com.xunyu.codenexus.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xunyu.codenexus.backend.model.entity.UserActivityLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户活跃度日志表 Mapper
 *
 * @author CodeNexusBuilder
 */
@Mapper
public interface UserActivityLogMapper extends BaseMapper<UserActivityLog> {

    /**
     * 累加/更新用户当天的活跃度数据 (支持并发安全)
     * 使用 GREATEST(is_checkin, #{log.isCheckin}) 确保打卡状态一旦为 1 就不会被覆盖回 0。
     */
    @Insert("INSERT INTO user_activity_log (user_id, activity_date, ac_count, is_checkin, score_change, create_time, update_time, is_deleted) " +
            "VALUES (#{log.userId}, #{log.activityDate}, #{log.acCount}, #{log.isCheckin}, #{log.scoreChange}, #{log.createTime}, #{log.updateTime}, 0) " +
            "ON DUPLICATE KEY UPDATE " +
            "ac_count = ac_count + #{log.acCount}, " +
            "is_checkin = GREATEST(is_checkin, #{log.isCheckin}), " +
            "score_change = score_change + #{log.scoreChange}, " +
            "update_time = #{log.updateTime}, " +
            "is_deleted = 0")
    int upsertActivityLog(@Param("log") UserActivityLog log);
}