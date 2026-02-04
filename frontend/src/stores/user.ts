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

  const avatar = computed(() => {
    return userInfo.value?.userAvatar || ''
  })

  const nickname = computed(() => {
    const info = userInfo.value
    if (!info) return '访客指挥官'
    return info.userName || info.userAccount || 'Nexus Commander'
  })

  // --- Actions ---

  function setToken(newToken: string) {
    token.value = newToken
    storage.setToken(newToken)
  }

  async function fetchUserInfo() {
    try {
      const data = await getUserInfoApi()
      userInfo.value = data

      if (data.userRole) {
        roles.value = [data.userRole]
      } else {
        roles.value = ['user']
      }

      return data
    } catch (error) {
      console.error('[User Store] GetInfo Error:', error)
      throw error
    }
  }

  async function login(form: LoginForm) {
    try {
      const res = await loginApi(form)
      // 假设后端返回结构是 { token: '...' } 或直接是 token 字符串，视具体后端实现调整
      // 这里根据之前的 auth.ts 定义，res 应该是 LoginResult
      const accessToken = res.token

      if (!accessToken) {
        throw new Error('登录失败：未获取到 Token')
      }

      setToken(accessToken)

      // 并行获取用户信息
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
