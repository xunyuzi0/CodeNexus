import { request } from '@/utils/request'
import type {
  UserProfileVO,
  UpdateProfileRequest,
  UpdatePreferenceRequest,
  UpdatePasswordRequest,
} from '@/types/api'

// 获取当前用户的完整档案 (包含偏好设置)
export function getMyProfile() {
  return request<UserProfileVO>({
    url: '/user/profile/me',
    method: 'GET',
  })
}

// 更新基础档案
export function updateProfile(data: UpdateProfileRequest) {
  return request<boolean>({
    url: '/user/profile',
    method: 'PATCH',
    data,
  })
}

// 更新偏好设置
export function updatePreferences(data: UpdatePreferenceRequest) {
  return request<boolean>({
    url: '/user/preferences',
    method: 'PUT',
    data,
  })
}

// 修改密码
export function updatePassword(data: UpdatePasswordRequest) {
  return request<boolean>({
    url: '/user/security/password',
    method: 'PUT',
    data,
  })
}

// 注销账号 (危险操作)
export function deleteAccount() {
  return request<boolean>({
    url: '/user/security/account',
    method: 'DELETE',
  })
}
