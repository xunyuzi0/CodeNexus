/**
 * src/utils/request.ts
 * Zeekr Infrastructure - Network Layer
 * -------------------------------------
 * [修复版]
 * 1. 统一引用全局类型定义，消除重复。
 * 2. 增强了 401 登出的安全性。
 */

import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { storage } from './storage'
import type { ApiResponse } from '@/types/api' // [修复] 引入源头类型

// [已删除] 本地重复定义的 ApiResponse

const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json;charset=utf-8' },
})

// --- Request 拦截器 ---
service.interceptors.request.use(
  (config) => {
    const token = storage.getToken()
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

// --- Response 拦截器 ---
service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data

    // 约定：code 200 为成功
    if (res.code === 200) {
      return res.data as any
    }

    // 业务错误处理
    console.error(`[Nexus API]: ${res.message}`)
    return Promise.reject(new Error(res.message || 'Error'))
  },
  (error) => {
    const status = error.response?.status

    if (status === 401) {
      console.warn('[Nexus Auth] Session Expired')
      storage.removeToken()

      // [修复] 强制跳转回登录页，带上当前页面作为重定向参数
      // 使用 window.location 确保彻底清空 Vue 内存状态
      const currentPath = window.location.pathname
      window.location.href = `/login?redirect=${currentPath}`

      return Promise.reject(error)
    }

    // 其他错误处理
    let message = '网络连接异常'
    if (status === 403) message = '没有权限访问该资源'
    if (status === 404) message = '请求的资源不存在'
    if (status === 500) message = '服务器内部故障'

    console.error(`[API Error ${status}]: ${message}`)
    return Promise.reject(error)
  },
)

export const request = <T = any>(config: AxiosRequestConfig): Promise<T> => {
  return service.request(config)
}

export default service
