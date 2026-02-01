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
  username: string
  nickname?: string
  avatar?: string
  email?: string
  role?: string // 单角色字符串 (如 'admin')
  roles?: string[] // 多角色支持 (如 ['admin', 'editor'])
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
