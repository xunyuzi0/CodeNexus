// src/router/index.ts

import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import BasicLayout from '@/layouts/BasicLayout.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: BasicLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'LayoutDashboard' },
      },
      {
        path: 'problems',
        name: 'Problems',
        component: () => import('@/views/problem/index.vue'),
        meta: { title: '题库中心', icon: 'Code2' },
      },
      {
        path: 'favorites',
        name: 'Favorites',
        component: () => import('@/views/favorites/index.vue'),
        meta: { title: '我的收藏', icon: 'Star' },
      },
      {
        path: 'favorites/:id',
        name: 'FavoritesDetail',
        component: () => import('@/views/favorites/Detail.vue'),
        meta: { title: '收藏详情', hidden: true },
      },
      // [REMOVED]: ProblemDetail moved to root level
      {
        path: 'arena',
        name: 'ArenaIndex',
        component: () => import('@/views/arena/index.vue'),
        meta: { title: '竞技场', icon: 'Swords' },
      },
      {
        path: 'rank',
        name: 'Rank',
        component: () => import('@/views/rank/index.vue'),
        meta: { title: '算力天梯', icon: 'Trophy' },
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人档案', icon: 'User' },
      },
      // 管理端路由
      {
        path: 'admin',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/index.vue'),
        meta: { title: '管理后台', icon: 'Shield', requiresAdmin: true },
      },
      {
        path: 'admin/users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/users.vue'),
        meta: { title: '用户管理', icon: 'Users', requiresAdmin: true },
      },
      {
        path: 'admin/problems',
        name: 'AdminProblems',
        component: () => import('@/views/admin/problems.vue'),
        meta: { title: '题库管理', icon: 'FileCode', requiresAdmin: true },
      },
      {
        path: 'admin/arena',
        name: 'AdminArena',
        component: () => import('@/views/admin/arena.vue'),
        meta: { title: '对战记录', icon: 'Swords', requiresAdmin: true },
      },
    ],
  },
  // [NEW]: 独立的全屏刷题路由
  {
    path: '/problems/:id',
    name: 'ProblemDetail',
    component: () => import('@/views/problem/Detail.vue'),
    meta: { title: '沉浸刷题', hidden: true },
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404',
    meta: { hidden: true },
  },
  {
    path: '/battle',
    component: () => import('@/layouts/ArenaLayout.vue'),
    children: [
      {
        path: 'matchmaking',
        name: 'ArenaMatchmaking',
        component: () => import('@/views/arena/Matchmaking.vue'),
        meta: { title: '正在匹配...' },
      },
      {
        path: 'lobby/:roomId',
        name: 'ArenaLobby',
        component: () => import('@/views/arena/Lobby.vue'),
        meta: { title: '备战大厅' },
      },
      {
        path: 'room/:roomId',
        name: 'ArenaBattle',
        component: () => import('@/views/arena/BattleRoom.vue'),
        meta: { title: 'Code Battle' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0, behavior: 'smooth' }
    }
  },
})

export default router
