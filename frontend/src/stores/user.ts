import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { storage } from '@/utils/storage'
import {
  login as loginApi,
  logout as logoutApi,
  getUserInfo as getUserInfoApi,
  type LoginForm,
} from '@/api/auth'
import type { UserInfo } from '@/types/api'

export const useUserStore = defineStore('user', () => {
  // --- State ---
  const token = ref<string | null>(storage.getToken())
  const userInfo = ref<UserInfo | null>(null)
  const roles = ref<string[]>([])

  // --- Getters ---
  const isLogin = computed(() => !!token.value)
  const avatar = computed(() => userInfo.value?.avatar || '')
  const nickname = computed(() => userInfo.value?.nickname || userInfo.value?.username || '')

  // --- Actions ---
  function setToken(newToken: string) {
    token.value = newToken
    storage.setToken(newToken)
  }

  async function fetchUserInfo() {
    try {
      const data = await getUserInfoApi()
      userInfo.value = data

      // [修复] 角色兜底逻辑
      // 如果后端没返回 role，赋予默认权限，防止路由死循环
      if (data.role) {
        roles.value = [data.role]
      } else if (data.roles && data.roles.length > 0) {
        roles.value = data.roles
      } else {
        // 默认给予基础权限
        roles.value = ['DEVELOPER']
      }

      return data
    } catch (error) {
      console.error('[User Store] GetInfo Error:', error)
      throw error
    }
  }

  // 登录 Action
  async function login(form: LoginForm) {
    try {
      // 1. 调用登录接口
      const res = await loginApi(form)

      // 假设后端直接返回 token 字符串，或者对象中包含 token
      // 如果后端返回结构是 { id: 1, ... } 而 Token 在 header 里，需要改 request.ts
      // 这里默认维持原架构假设：res 包含 token 字段
      const accessToken = res.token

      if (!accessToken) {
        throw new Error('登录失败：未获取到 Token')
      }

      setToken(accessToken)

      // 2. 并行获取用户信息
      // 这里会自动调用更新后的 /user/get/login 接口
      await fetchUserInfo()

      return res
    } catch (error) {
      throw error
    }
  }

  async function logout() {
    try {
      await logoutApi().catch((err) => console.warn('Logout API failed:', err))
    } finally {
      token.value = null
      userInfo.value = null
      roles.value = []
      storage.removeToken()
    }
  }

  return {
    token,
    userInfo,
    roles,
    isLogin,
    avatar,
    nickname,
    login,
    logout,
    fetchUserInfo,
  }
})
