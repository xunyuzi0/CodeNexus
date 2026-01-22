import { createRouter, createWebHistory } from 'vue-router'
import BasicLayout from '@/layouts/BasicLayout.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      // 路由懒加载
      // 只有访问这个页面时才加载代码,提升首屏速度
      component: () => import('@/views/Login.vue'),
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/Register.vue'),
    },
    {
      path: '/forgot-password',
      name: 'ForgotPassword',
      component: () => import('@/views/ForgotPassword.vue'),
    },
    {
      path: '/',
      name: 'Dashboard',
      component: () => import('@/views/Dashboard.vue'),
      // 路由元信息Meta
      // requiresAuth = true,表示需要登录
      meta: { requiresAuth: true },
    },

    // 业务页面全部包裹在 BasicLayout 下
    {
      path: '/',
      component: BasicLayout, // 父路由使用布局组件
      meta: { requiresAuth: true },
      children: [
        {
          path: '', // 默认子路由，对应 /
          name: 'Dashboard',
          component: () => import('@/views/Dashboard.vue'),
        },
        // 未来添加:
        // { path: 'problems', component: ... }
        // { path: 'room/:id', component: ... }
      ],
    },
  ],
})

// 全局前置守卫
// 每次跳转页面前都会拦截下来检查
router.beforeEach((to, from, next) => {
  // 从本地拿 Token
  const token = localStorage.getItem('token')

  // 逻辑：如果去的页面需要登录,而且你没有 Token
  if (to.meta.requiresAuth && !token) {
    // 跳转登录页
    next('/login')
  } else {
    // 放行
    next()
  }
})

export default router
