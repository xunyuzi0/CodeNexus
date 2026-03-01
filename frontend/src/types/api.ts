/**
 * src/types/api.ts
 * 全局 API 类型定义
 * -----------------
 * 包含通用响应结构、用户信息模型、登录结果等核心类型。
 */

// 1. 通用后端响应结构 (泛型 T 默认为 any)
export interface ApiResponse<T = any> {
  code: number // 业务状态码 (例如 200 成功)
  message: string // 提示信息
  data: T // 泛型数据载荷
}

// 2. 原有用户信息模型 (保留以兼容遗留组件)
export interface UserInfo {
  id?: number | string
  userAccount: string // 账号
  userName?: string // 用户昵称
  userAvatar?: string // 用户头像
  userProfile?: string // 用户简介
  userRole?: string // 用户角色
  rankScore?: number // 排位分
  totalSolved?: number // 解题数
  globalRank?: number // 全球排名
}

// 3. 登录接口返回的数据结构
export interface LoginResult {
  token: string // 访问令牌 (JWT)
  userInfo?: UserInfo | UserProfileVO // 兼容新老返回类型
}

// 4. 分页响应结构 (通用列表)
export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}

// ==========================================
// 以下为 V2 新版接口对接新增的类型定义
// ==========================================

// 5. 偏好设置相关类型
export interface UserPreference {
  editorTheme: string
  fontSize: number
  systemNotifications: number // 1-开启, 0-关闭
  extraSettings?: string // 存储偏好语言等额外设置的 JSON 字符串
}

// 6. 用户完整档案出参 (VO)
export interface UserProfileVO {
  id: string | number
  username: string // 登录账号
  nickname: string // 对应旧版的 userName
  avatarUrl: string // 对应旧版的 userAvatar
  bio: string // 对应旧版的 userProfile
  email: string
  phone: string
  role: string // 对应旧版的 userRole
  preferences: UserPreference // 嵌套的偏好设置
}

// 7. 前端表单提交请求入参 (DTO)
export interface UpdateProfileRequest {
  nickname?: string
  avatarUrl?: string
  bio?: string
  email?: string
  phone?: string
}

export interface UpdatePreferenceRequest {
  editorTheme?: string
  fontSize?: number
  systemNotifications?: number
  extraSettings?: string
}

export interface UpdatePasswordRequest {
  oldPassword: string
  newPassword: string
}
