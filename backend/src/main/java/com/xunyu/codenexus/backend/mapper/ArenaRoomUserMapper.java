package com.xunyu.codenexus.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xunyu.codenexus.backend.model.entity.ArenaRoomUser;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 竞技场房间用户关联表 Mapper
 */
@Mapper
public interface ArenaRoomUserMapper extends BaseMapper<ArenaRoomUser> {

    /**
     * 物理删除房间用户关联（绕过 MyBatis-Plus 逻辑删除，防止唯一索引冲突）
     */
    @Delete("DELETE FROM arena_room_user WHERE id = #{id}")
    int physicalDeleteById(@Param("id") Long id);
}