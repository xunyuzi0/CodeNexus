<template>
  <aside
    class="fixed top-1/2 -translate-y-1/2 z-50 h-[95vh] bg-white/90 dark:bg-zinc-900/80 backdrop-blur-xl border border-zinc-200 dark:border-white/5 rounded-2xl flex flex-col items-center py-8 shadow-xl dark:shadow-2xl dark:shadow-black/50 overflow-hidden"
    :class="[
      isSidebarCollapsed
        ? 'w-0 left-0 opacity-0 -translate-x-full pointer-events-none'
        : 'w-[240px] left-4 opacity-100 translate-x-0',
      'transition-all duration-500 ease-[cubic-bezier(0.25,0.8,0.25,1)]',
    ]"
  >
    <div class="mb-12 select-none whitespace-nowrap">
      <h1
        class="text-3xl font-black tracking-[0.2em] text-zinc-900/90 dark:text-white/90 font-mono"
      >
        NEXUS
      </h1>
      <div class="h-1 w-8 bg-[#FF4C00] mx-auto mt-2 rounded-full shadow-[0_0_10px_#FF4C00]"></div>
    </div>

    <nav class="w-full px-4 space-y-2 flex-1 w-[240px] overflow-y-auto no-scrollbar">
      <template v-for="item in menuItems" :key="item.path">
        <RouterLink
          :to="item.path"
          class="group relative flex items-center h-12 px-4 rounded-lg transition-all duration-300 whitespace-nowrap"
          :class="[
            isActive(item)
              ? 'text-zinc-900 dark:text-white'
              : 'text-zinc-400 dark:text-zinc-500 hover:text-zinc-600 dark:hover:text-zinc-300',
          ]"
        >
          <div
            v-if="isActive(item)"
            class="absolute left-0 h-6 w-1 bg-[#FF4C00] rounded-r-full shadow-[0_0_15px_#FF4C00]"
          ></div>

          <component
            :is="item.icon"
            class="w-5 h-5 mr-4 transition-transform duration-300 group-hover:scale-110 shrink-0"
            :class="{
              'text-[#FF4C00] drop-shadow-[0_0_5px_rgba(255,76,0,0.5)]': isActive(item),
            }"
          />

          <span
            class="text-sm tracking-wide transition-all duration-300"
            :class="{
              'font-bold drop-shadow-[0_0_8px_rgba(255,255,255,0.5)]': isActive(item),
            }"
          >
            {{ item.name }}
          </span>

          <div
            class="absolute inset-0 bg-zinc-100 dark:bg-white/5 rounded-lg opacity-0 group-hover:opacity-100 transition-opacity duration-300 -z-10"
          ></div>
        </RouterLink>
      </template>
    </nav>
  </aside>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import {
  LayoutDashboard,
  Code2,
  Swords,
  Trophy,
  User,
  Star,
  Shield,
  Users,
  FileCode,
} from 'lucide-vue-next'
import { useUiStore } from '@/stores/ui'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const uiStore = useUiStore()
const userStore = useUserStore()
const { isSidebarCollapsed } = storeToRefs(uiStore)

const menuItems = computed(() => {
  if (userStore.roles.includes('admin')) {
    return [
      { name: '管理后台', path: '/admin', icon: Shield },
      { name: '用户管理', path: '/admin/users', icon: Users },
      { name: '题库管理', path: '/admin/problems', icon: FileCode },
      { name: '对战记录', path: '/admin/arena', icon: Swords },
    ]
  }

  return [
    { name: '仪表盘', path: '/dashboard', icon: LayoutDashboard },
    { name: '题库中心', path: '/problems', icon: Code2 },
    { name: '我的收藏', path: '/favorites', icon: Star },
    { name: '竞技场', path: '/arena', icon: Swords },
    { name: '排行榜', path: '/rank', icon: Trophy },
    { name: '个人档案', path: '/profile', icon: User },
  ]
})

function isActive(item: { path: string }) {
  // 管理后台首页需精确匹配，避免 /admin/users 等子路由也高亮
  if (item.path === '/admin') return route.path === '/admin'
  return route.path === item.path || route.path.startsWith(item.path + '/')
}
</script>
