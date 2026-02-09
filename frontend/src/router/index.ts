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
        meta: { title: '控制台', icon: 'LayoutDashboard' },
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
        meta: { title: '收藏详情', hidden: true }, // hidden: true 表示不显示在侧边栏自动生成的菜单中(如果有的话)
      },
      {
        path: 'problems/:id',
        name: 'ProblemDetail',
        component: () => import('@/views/problem/Detail.vue'),
        meta: { title: '题目详情', hidden: true },
      },
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
    ],
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
  // [REFACTORED] 沉浸式战斗布局，路径变更为 /battle
  {
    path: '/battle',
    component: () => import('@/layouts/ArenaLayout.vue'),
    children: [
      {
        path: 'matchmaking', // [NEW] 匹配界面
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
        path: 'room/:roomId', // 修改 battle 路径名称以避免混淆
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
