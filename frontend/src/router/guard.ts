/**
 * src/router/guard.ts
 * [修复版]
 * 优化了 NProgress 配置，解决刷新时的闪烁问题。
 */

import router from '@/router'
import { useUserStore } from '@/stores/user'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

// [配置优化]
// speed: 动画速度 (ms)
// showSpinner: 不显示转圈
// trickle: 设为 false 可以避免自动"慢慢走"，防止快速加载时的视觉跳动
// minimum: 最小百分比
NProgress.configure({
  showSpinner: false,
  speed: 400,
  minimum: 0.2,
})

const whiteList = ['/login', '/register', '/404']

router.beforeEach(async (to, from, next) => {
  // 开启进度条
  NProgress.start()

  const userStore = useUserStore()
  const hasToken = userStore.token

  // 动态标题
  const title = to.meta.title ? `${to.meta.title} - CodeNexus` : 'CodeNexus'
  document.title = title

  if (hasToken) {
    if (to.path === '/login') {
      next({ path: '/' })
    } else {
      const hasRoles = userStore.roles && userStore.roles.length > 0
      if (hasRoles) {
        next()
      } else {
        try {
          await userStore.fetchUserInfo()
          next({ ...to, replace: true })
        } catch (error) {
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
  // 结束进度条
  NProgress.done()
})
