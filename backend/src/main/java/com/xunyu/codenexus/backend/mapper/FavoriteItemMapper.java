// src/main/java/com/xunyu/codenexus/backend/mapper/FavoriteItemMapper.java
package com.xunyu.codenexus.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xunyu.codenexus.backend.model.entity.FavoriteItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FavoriteItemMapper extends BaseMapper<FavoriteItem> {
}