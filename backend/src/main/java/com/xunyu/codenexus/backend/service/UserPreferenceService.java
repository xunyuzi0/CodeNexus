package com.xunyu.codenexus.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xunyu.codenexus.backend.model.dto.request.user.UpdatePreferenceRequest;
import com.xunyu.codenexus.backend.model.entity.UserPreference;

public interface UserPreferenceService extends IService<UserPreference> {
    /**
     * 更新或初始化用户偏好设置 (UPSERT)
     */
    boolean upsertUserPreference(Long userId, UpdatePreferenceRequest request);
}