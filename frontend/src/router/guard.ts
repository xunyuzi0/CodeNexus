/**
 * src/router/guard.ts
 * [修复版]
 * 修复了刷新页面时调用的旧版 fetchUserInfo 方法名导致强行踢出的 Bug
 */

import router from '@/router'
import { useUserStore } from '@/stores/user'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

NProgress.configure({
  showSpinner: false,
  speed: 400,
  minimum: 0.2,
})

const whiteList = ['/login', '/register', '/404']

router.beforeEach(async (to, from, next) => {
  NProgress.start()

  const userStore = useUserStore()
  const hasToken = userStore.token

  const title = to.meta.title ? `${to.meta.title} - CodeNexus` : 'CodeNexus'
  document.title = title

  if (hasToken) {
    if (to.path === '/login') {
      next({ path: '/' })
    } else {
      const hasRoles = userStore.roles && userStore.roles.length > 0
      if (hasRoles) {
        // 管理端路由守卫：非 ADMIN 角色禁止访问
        if (to.meta.requiresAdmin && !userStore.roles.includes('admin')) {
          next({ path: '/dashboard' })
          return
        }
        // 管理员访问根路径时重定向到管理后台
        if (userStore.roles.includes('admin') && (to.path === '/' || to.path === '/dashboard')) {
          next({ path: '/admin', replace: true })
          return
        }
        next()
      } else {
        try {
          await userStore.fetchUserProfile()
          // 管理员拉取档案后重定向到管理后台
          if (userStore.roles.includes('admin') && (to.path === '/' || to.path === '/dashboard')) {
            next({ path: '/admin', replace: true })
            return
          }
          next({ ...to, replace: true })
        } catch (error) {
          console.error('[Router Guard] 获取用户信息失败，强制登出:', error)
          await userStore.logout()
          next(`/login?redirect=${to.path}`)
        }
      }
    }
  } else {
    if (whiteList.includes(to.path)) {
      next()
    } else {
      next(`/login?redirect=${to.path}`)
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})
