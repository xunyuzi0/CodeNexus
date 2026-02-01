<template>
  <aside
    class="fixed left-4 top-1/2 -translate-y-1/2 z-50 h-[95vh] w-[240px] bg-zinc-900/80 backdrop-blur-xl border border-white/5 rounded-2xl flex flex-col items-center py-8 shadow-2xl shadow-black/50 transition-all duration-500 ease-out"
  >
    <div class="mb-12 select-none">
      <h1 class="text-3xl font-black tracking-[0.2em] text-white/90 font-mono">NEXUS</h1>
      <div class="h-1 w-8 bg-[#FF4C00] mx-auto mt-2 rounded-full shadow-[0_0_10px_#FF4C00]"></div>
    </div>

    <nav class="w-full px-4 space-y-2 flex-1">
      <template v-for="item in menuItems" :key="item.path">
        <RouterLink
          :to="item.path"
          class="group relative flex items-center h-12 px-4 rounded-lg transition-all duration-300"
          :class="[route.path === item.path ? 'text-white' : 'text-zinc-500 hover:text-zinc-300']"
        >
          <div
            v-if="route.path === item.path"
            class="absolute left-0 h-6 w-1 bg-[#FF4C00] rounded-r-full shadow-[0_0_15px_#FF4C00]"
          ></div>

          <component
            :is="item.icon"
            class="w-5 h-5 mr-4 transition-transform duration-300 group-hover:scale-110"
            :class="{
              'text-[#FF4C00] drop-shadow-[0_0_5px_rgba(255,76,0,0.5)]': route.path === item.path,
            }"
          />

          <span
            class="text-sm tracking-wide transition-all duration-300"
            :class="{
              'font-bold drop-shadow-[0_0_8px_rgba(255,255,255,0.5)]': route.path === item.path,
            }"
          >
            {{ item.name }}
          </span>

          <div
            class="absolute inset-0 bg-white/5 rounded-lg opacity-0 group-hover:opacity-100 transition-opacity duration-300 -z-10"
          ></div>
        </RouterLink>
      </template>
    </nav>
  </aside>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import { LayoutDashboard, Code2, Swords, Trophy, User } from 'lucide-vue-next'

const route = useRoute()

const menuItems = [
  { name: '控制台', path: '/dashboard', icon: LayoutDashboard },
  { name: '题库中心', path: '/problems', icon: Code2 },
  { name: '竞技场', path: '/arena', icon: Swords },
  { name: '排行榜', path: '/rank', icon: Trophy },
  { name: '个人档案', path: '/profile', icon: User },
]
</script>
