import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { storage } from '@/utils/storage'
import { login as loginApi, logout as logoutApi, type LoginForm } from '@/api/auth'
import { getMyProfile } from '@/api/user'
import type { UserInfo, UserProfileVO } from '@/types/api'
import { useSettingsStore } from '@/stores/settings'

export const useUserStore = defineStore('user', () => {
  // --- State ---
  const token = ref<string | null>(storage.getToken())
  // 兼容新老后端返回的数据结构
  const userInfo = ref<UserInfo | UserProfileVO | null>(null)
  const roles = ref<string[]>([])

  // --- Getters ---
  const isLogin = computed(() => !!token.value)

  // 优雅降级：优先取新版字段 avatarUrl，没有则取旧版 userAvatar
  const avatar = computed(() => {
    const info = userInfo.value as any
    return info?.avatarUrl || info?.userAvatar || ''
  })

  // 优雅降级：优先取 nickname，其次 username，最后旧版 userName
  const nickname = computed(() => {
    const info = userInfo.value as any
    if (!info) return '访客指挥官'
    return (
      info?.nickname || info?.username || info?.userName || info?.userAccount || 'Nexus Commander'
    )
  })

  // --- Actions ---
  function setToken(newToken: string) {
    token.value = newToken
    storage.setToken(newToken)
  }

  // 获取完整个人档案及偏好 (替代原有的 fetchUserInfo)
  async function fetchUserProfile() {
    try {
      const data = await getMyProfile()
      userInfo.value = data

      // 角色处理兼容
      const role = (data as any).role || (data as any).userRole
      roles.value = role ? [role] : ['user']

      // 【核心联动】如果拿到了后端的偏好设置，同步更新到本地的 settingsStore
      if (data.preferences) {
        const settingsStore = useSettingsStore()
        if (data.preferences.editorTheme) {
          settingsStore.settings.editorTheme = data.preferences.editorTheme as any
        }
        if (data.preferences.fontSize) {
          settingsStore.settings.editorFontSize = data.preferences.fontSize
        }
      }

      return data
    } catch (error) {
      console.error('[User Store] fetchUserProfile Error:', error)
      throw error
    }
  }

  async function login(form: LoginForm) {
    try {
      const res = await loginApi(form)
      const accessToken = res.token

      if (!accessToken) {
        throw new Error('登录失败：未获取到 Token')
      }

      setToken(accessToken)

      // 登录后调用最新的 fetchUserProfile 并行获取信息
      await fetchUserProfile()

      return res
    } catch (error) {
      throw error
    }
  }

  async function logout() {
    try {
      await logoutApi().catch((err) => console.warn('Logout API failed:', err))
    } finally {
      // 彻底清除本地状态
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
    fetchUserProfile,
  }
})
