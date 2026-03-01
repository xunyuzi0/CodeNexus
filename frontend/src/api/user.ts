import request from '@/utils/request'
import type {
  UserProfileVO,
  UpdateProfileRequest,
  UpdatePreferenceRequest,
  UpdatePasswordRequest,
} from '@/types/api'

// 获取当前用户的完整档案 (包含偏好设置)
export function getMyProfile() {
  return request.get<UserProfileVO>('/user/profile/me')
}

// 更新基础档案
export function updateProfile(data: UpdateProfileRequest) {
  return request.patch<boolean>('/user/profile', data)
}

// 更新偏好设置
export function updatePreferences(data: UpdatePreferenceRequest) {
  return request.put<boolean>('/user/preferences', data)
}

// 修改密码
export function updatePassword(data: UpdatePasswordRequest) {
  return request.put<boolean>('/user/security/password', data)
}

// 注销账号 (危险操作)
export function deleteAccount() {
  return request.delete<boolean>('/user/security/account')
}
