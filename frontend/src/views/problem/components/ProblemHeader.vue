<template>
  <header
    class="h-14 shrink-0 flex items-center justify-between px-4 bg-white dark:bg-zinc-950/80 backdrop-blur-md border-b border-zinc-200 dark:border-white/5 z-50 relative select-none"
  >
    <div class="flex items-center gap-4">
      <button
        @click="goBack"
        class="group flex items-center gap-2 text-zinc-500 hover:text-zinc-900 dark:hover:text-white transition-colors duration-300"
      >
        <div
          class="p-1.5 rounded-lg border border-transparent group-hover:bg-zinc-100 dark:group-hover:bg-white/5 group-hover:border-zinc-200 dark:group-hover:border-white/5 transition-all"
        >
          <ChevronLeft class="w-4 h-4 group-hover:-translate-x-0.5 transition-transform" />
        </div>
        <span class="text-xs font-medium hidden sm:block">退出</span>
      </button>

      <div class="h-4 w-[1px] bg-zinc-200 dark:bg-white/10 hidden sm:block"></div>

      <div class="flex items-center gap-3">
        <span class="text-sm font-mono text-zinc-500">{{ problem?.displayId || 'P-?' }}</span>
        <h1
          class="text-sm font-bold text-zinc-900 dark:text-zinc-100 tracking-wide truncate max-w-[200px] sm:max-w-md"
        >
          {{ problem?.title || '加载中...' }}
        </h1>
        <span
          v-if="problem"
          class="hidden sm:inline-flex items-center px-2 py-0.5 rounded text-[10px] font-bold border tracking-wider uppercase"
          :class="[
            problem.difficulty === 'EASY'
              ? 'border-emerald-500/20 bg-emerald-500/10 text-emerald-500'
              : problem.difficulty === 'MEDIUM'
                ? 'border-amber-500/20 bg-amber-500/10 text-amber-500'
                : 'border-rose-500/20 bg-rose-500/10 text-rose-500',
          ]"
        >
          {{ problem.difficulty }}
        </span>
      </div>
    </div>

    <div
      class="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 hidden md:flex items-center gap-2.5 px-4 py-1.5 rounded-full bg-zinc-100 dark:bg-zinc-900/50 border border-zinc-200 dark:border-white/5 shadow-sm dark:shadow-[inset_0_1px_0_0_rgba(255,255,255,0.05)]"
    >
      <Timer class="w-3.5 h-3.5 text-[#FF4C00]" :class="{ 'opacity-50': isTimerPaused }" />
      <span
        class="font-mono text-sm font-medium tabular-nums tracking-wide transition-colors"
        :class="isTimerPaused ? 'text-emerald-500' : 'text-zinc-400'"
      >
        {{ formattedTime }}
      </span>
    </div>

    <div class="flex items-center gap-2">
      <button
        @click="toggleTheme"
        class="p-2 rounded-lg border transition-all duration-300 hover:scale-105 active:scale-95"
        :class="
          isDark
            ? 'bg-zinc-900/50 border-white/10 text-zinc-400 hover:text-white hover:border-white/20'
            : 'bg-zinc-100 border-zinc-200 text-zinc-500 hover:text-zinc-900 hover:border-zinc-300'
        "
        title="切换日间/夜间模式"
      >
        <Sun v-if="isDark" class="w-4 h-4" />
        <Moon v-else class="w-4 h-4" />
      </button>

      <div class="relative group cursor-pointer mr-2">
        <div
          class="w-8 h-8 rounded-full bg-zinc-200 dark:bg-zinc-800 border border-zinc-300 dark:border-white/10 overflow-hidden ring-2 ring-transparent group-hover:ring-[#FF4C00]/20 transition-all"
        >
          <img
            src="https://api.dicebear.com/7.x/avataaars/svg?seed=Felix"
            alt="User"
            class="w-full h-full object-cover opacity-90 group-hover:opacity-100"
          />
        </div>
        <div
          class="absolute bottom-0 right-0 w-2.5 h-2.5 bg-emerald-500 border-2 border-white dark:border-zinc-950 rounded-full"
        ></div>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ChevronLeft, Timer, Sun, Moon } from 'lucide-vue-next'
import type { Problem } from '@/api/problem' // 引入全局 Problem 类型
import { isDark, toggleTheme } from '@/composables/useTheme'

const props = defineProps<{
  isTimerPaused?: boolean
  problem?: Problem | null // 新增接收 Problem 属性
}>()

const router = useRouter()
const route = useRoute()

const goBack = () => {
  const from = route.query.from
  if (from === 'dashboard') {
    router.push('/dashboard')
  } else if (from === 'favorites' && route.query.folderId) {
    router.push(`/favorites/${route.query.folderId}`)
  } else {
    router.push('/problems')
  }
}

const seconds = ref(0)
let timerInterval: number | null = null

const formattedTime = computed(() => {
  const h = Math.floor(seconds.value / 3600)
    .toString()
    .padStart(2, '0')
  const m = Math.floor((seconds.value % 3600) / 60)
    .toString()
    .padStart(2, '0')
  const s = (seconds.value % 60).toString().padStart(2, '0')
  return `${h}:${m}:${s}`
})

// 监听外界抛出的暂停指令
watch(
  () => props.isTimerPaused,
  (paused) => {
    if (paused && timerInterval) {
      clearInterval(timerInterval)
      timerInterval = null
    } else if (!paused && !timerInterval) {
      timerInterval = window.setInterval(() => {
        seconds.value++
      }, 1000)
    }
  },
)

onMounted(() => {
  if (!props.isTimerPaused) {
    timerInterval = window.setInterval(() => {
      seconds.value++
    }, 1000)
  }
})

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval)
})
</script>
