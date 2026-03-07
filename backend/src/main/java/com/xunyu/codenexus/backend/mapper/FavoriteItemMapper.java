// src/main/java/com/xunyu/codenexus/backend/mapper/FavoriteItemMapper.java
package com.xunyu.codenexus.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xunyu.codenexus.backend.model.entity.FavoriteItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FavoriteItemMapper extends BaseMapper<FavoriteItem> {

    /**
     * 利用 MySQL 的 ON DUPLICATE KEY UPDATE 特性解决逻辑删除冲突。
     * 如果记录不存在则插入；如果因唯一索引(folder_id, problem_id)冲突，则将 is_deleted 更新为 0 以恢复记录。
     */
    @Insert("INSERT INTO favorite_item (user_id, folder_id, problem_id, create_time, update_time, is_deleted) " +
            "VALUES (#{userId}, #{folderId}, #{problemId}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE is_deleted = 0, update_time = NOW()")
    int upsertFavoriteItem(FavoriteItem favoriteItem);
}