<template>
  <div class="p-6 max-w-7xl mx-auto space-y-6">
    <div
      v-motion
      :initial="{ opacity: 0, y: -20 }"
      :enter="{ opacity: 1, y: 0, transition: { duration: 500 } }"
    >
      <h2 class="text-3xl font-bold tracking-tight text-zinc-900 dark:text-zinc-100">仪表盘</h2>
      <p class="text-zinc-500">欢迎回来，指挥官。</p>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <div
        class="col-span-1 md:col-span-2 group relative overflow-hidden rounded-3xl bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-white/5 p-8 transition-all duration-500 hover:border-zinc-300 dark:hover:border-white/10 hover:shadow-2xl dark:hover:shadow-black/50"
        v-motion
        :initial="{ opacity: 0, scale: 0.95 }"
        :enter="{ opacity: 1, scale: 1, transition: { duration: 500, delay: 100 } }"
      >
        <div class="relative z-10 h-full flex flex-col justify-between">
          <div>
            <h3 class="text-lg font-medium text-zinc-500 mb-1">CURRENT SESSION</h3>
            <p class="text-4xl md:text-5xl font-black text-zinc-900 dark:text-white tracking-tight">
              {{ formattedTime }}
            </p>
            <p class="text-zinc-400 mt-2">{{ currentDate }}</p>
          </div>

          <div class="mt-8">
            <p class="text-xl font-medium text-zinc-700 dark:text-zinc-300">
              Hello, <span class="text-[#FF4C00]">{{ userStore.nickname || 'Developer' }}</span>
            </p>
            <p class="text-sm text-zinc-500 mt-1">今天又是写出完美代码的一天。</p>
          </div>
        </div>

        <Cpu
          class="absolute -right-8 -bottom-8 w-64 h-64 text-zinc-100 dark:text-white/[0.02] group-hover:scale-110 transition-transform duration-700 ease-out"
        />
      </div>

      <div
        class="col-span-1 group relative overflow-hidden rounded-3xl bg-[#FF4C00] p-8 text-white transition-all duration-500 hover:shadow-[0_0_30px_rgba(255,76,0,0.4)]"
        v-motion
        :initial="{ opacity: 0, x: 20 }"
        :enter="{ opacity: 1, x: 0, transition: { duration: 500, delay: 200 } }"
      >
        <div class="relative z-10 h-full flex flex-col justify-between">
          <div class="flex items-center justify-between">
            <h3 class="text-white/80 font-medium">已解决题目</h3>
            <Trophy class="w-6 h-6 text-white/80" />
          </div>

          <div>
            <span class="text-6xl font-black tracking-tighter">128</span>
            <span class="text-xl ml-2 text-white/60">/ 500</span>
          </div>

          <div class="w-full bg-black/20 h-2 rounded-full overflow-hidden mt-4">
            <div class="h-full bg-white w-[25%]"></div>
          </div>
        </div>

        <div
          class="absolute top-0 right-0 w-32 h-32 bg-white/10 rounded-full blur-3xl -translate-y-1/2 translate-x-1/2"
        ></div>
      </div>

      <div
        class="col-span-1 md:col-span-3 min-h-[400px] group relative overflow-hidden rounded-3xl bg-white dark:bg-zinc-900 border border-zinc-200 dark:border-white/5 p-6 transition-all duration-500 hover:border-zinc-300 dark:hover:border-white/10"
        v-motion
        :initial="{ opacity: 0, y: 20 }"
        :enter="{ opacity: 1, y: 0, transition: { duration: 500, delay: 300 } }"
      >
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-lg font-semibold text-zinc-900 dark:text-zinc-100">能力多维分析</h3>
          <button
            class="text-xs px-3 py-1 rounded-full bg-zinc-100 dark:bg-zinc-800 text-zinc-500 hover:text-zinc-900 dark:hover:text-white transition-colors"
          >
            查看详情
          </button>
        </div>

        <div
          class="w-full h-[300px] flex items-center justify-center border-2 border-dashed border-zinc-200 dark:border-zinc-800 rounded-2xl bg-zinc-50/50 dark:bg-black/20"
        >
          <div class="text-center">
            <Radar class="w-12 h-12 text-zinc-300 dark:text-zinc-700 mx-auto mb-3" />
            <p class="text-zinc-400 dark:text-zinc-600">ECharts 能力雷达图 预留位</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useNow, useDateFormat } from '@vueuse/core'
import { useUserStore } from '@/stores/user'
import { Cpu, Trophy, Radar } from 'lucide-vue-next'

const userStore = useUserStore()
const now = useNow()

// 格式化时间
const formattedTime = useDateFormat(now, 'HH:mm:ss')
const currentDate = useDateFormat(now, 'YYYY年MM月DD日 dddd')
</script>
