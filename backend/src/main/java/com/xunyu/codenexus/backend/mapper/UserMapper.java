package com.xunyu.codenexus.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xunyu.codenexus.backend.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 继承 BaseMapper<User> 后，自动获得标准的 CRUD 能力。
 * 如果需要复杂的自定义 SQL，可以在这里定义方法，并在 XML 中编写 SQL。
 *
 * @author xunyu
 * @date 2026/1/11 19:55
 * @description: 用户数据库操作接口
 */

@Mapper
public interface UserMapper extends BaseMapper<User> {

}