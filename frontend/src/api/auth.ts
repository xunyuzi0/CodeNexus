/**
 * src/api/auth.ts
 * [更新] 适配后端新接口 /user/register, /user/login, /user/get/login
 */

import { request } from '@/utils/request'
import type { LoginResult, UserInfo } from '@/types/api'

// 扩展表单结构，兼容注册时的确认密码
export interface LoginForm {
  userAccount: string // 对应后端的账号字段 (通常后端叫 userAccount 或 username，此处统一映射)
  userPassword: string // 对应后端的密码字段
  checkPassword?: string // 注册专用
}

enum Api {
  Login = '/user/login',
  Register = '/user/register',
  GetUserInfo = '/user/get/login', // 获取当前登录用户信息
  Logout = '/user/logout', // 假设后端也有对应的登出，如果没有可暂时保留原样
}

/**
 * 用户注册
 * @param data 注册表单数据
 */
export function register(data: LoginForm) {
  return request<number>({
    url: Api.Register,
    method: 'post',
    data,
  })
}

/**
 * 用户登录
 * @param data 登录表单数据
 */
export function login(data: LoginForm) {
  return request<LoginResult>({
    url: Api.Login,
    method: 'post',
    data,
  })
}

/**
 * 获取当前登录用户详细信息
 */
export function getUserInfo() {
  return request<UserInfo>({
    url: Api.GetUserInfo,
    method: 'get',
  })
}

/**
 * 退出登录
 */
export function logout() {
  return request<void>({
    url: Api.Logout,
    method: 'post',
  })
}
