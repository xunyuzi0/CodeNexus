package com.xunyu.codenexus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xunyu.codenexus.backend.mapper.UserPreferenceMapper;
import com.xunyu.codenexus.backend.model.dto.request.user.UpdatePreferenceRequest;
import com.xunyu.codenexus.backend.model.entity.UserPreference;
import com.xunyu.codenexus.backend.service.UserPreferenceService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class UserPreferenceServiceImpl extends ServiceImpl<UserPreferenceMapper, UserPreference> implements UserPreferenceService {

    @Override
    public boolean upsertUserPreference(Long userId, UpdatePreferenceRequest request) {
        // 1. 查找是否存在该用户的偏好记录
        LambdaQueryWrapper<UserPreference> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPreference::getUserId, userId);
        UserPreference existPreference = this.getOne(queryWrapper);

        UserPreference preferenceToSave = new UserPreference();
        BeanUtils.copyProperties(request, preferenceToSave);
        preferenceToSave.setUserId(userId);

        if (existPreference != null) {
            // 更新操作
            preferenceToSave.setId(existPreference.getId());
            return this.updateById(preferenceToSave);
        } else {
            // 插入操作
            return this.save(preferenceToSave);
        }
    }
}