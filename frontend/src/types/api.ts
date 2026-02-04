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

// 2. 用户信息模型 (根据你的 user.ts 和后端实际返回调整)
export interface UserInfo {
  id?: number | string

  // --- 核心匹配字段 (对应后端 Entity) ---
  userAccount: string // 账号
  userName?: string // 用户昵称 (后端字段名为 userName)
  userAvatar?: string // 用户头像
  userProfile?: string // 用户简介 (后端字段名为 userProfile)
  userRole?: string // 用户角色 (enum 序列化后通常为字符串 "user" 或 "admin")

  // --- 扩展字段 (前端 Mock 或计算用) ---
  rankScore?: number // 排位分 (后续补)
  totalSolved?: number // 解题数 (后续补)
  globalRank?: number // 全球排名
}

// 3. 登录接口返回的数据结构
export interface LoginResult {
  token: string // 访问令牌 (JWT)
  userInfo: UserInfo // 登录时一同返回的用户信息 (可选，视后端实现而定)
}

// 4. 分页响应结构 (为后续列表页做准备)
export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}
