<template>
  <header
    class="h-14 shrink-0 flex items-center justify-between px-4 bg-zinc-950/80 backdrop-blur-md border-b border-white/5 z-50 relative select-none"
  >
    <div class="flex items-center gap-4">
      <button
        @click="$router.push('/problems')"
        class="group flex items-center gap-2 text-zinc-500 hover:text-white transition-colors duration-300"
      >
        <div
          class="p-1.5 rounded-lg border border-transparent group-hover:bg-white/5 group-hover:border-white/5 transition-all"
        >
          <ChevronLeft class="w-4 h-4 group-hover:-translate-x-0.5 transition-transform" />
        </div>
        <span class="text-xs font-medium hidden sm:block">退出</span>
      </button>

      <div class="h-4 w-[1px] bg-white/10 hidden sm:block"></div>

      <div class="flex items-center gap-3">
        <span class="text-sm font-mono text-zinc-500">1.</span>
        <h1
          class="text-sm font-bold text-zinc-100 tracking-wide truncate max-w-[200px] sm:max-w-md"
        >
          两数之和
        </h1>
        <span
          class="hidden sm:inline-flex items-center px-2 py-0.5 rounded text-[10px] font-bold border border-emerald-500/20 bg-emerald-500/10 text-emerald-500 tracking-wider uppercase"
        >
          Easy
        </span>
      </div>
    </div>

    <div
      class="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 hidden md:flex items-center gap-2.5 px-4 py-1.5 rounded-full bg-zinc-900/50 border border-white/5 shadow-[inset_0_1px_0_0_rgba(255,255,255,0.05)]"
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
      <div class="relative group cursor-pointer mr-2">
        <div
          class="w-8 h-8 rounded-full bg-zinc-800 border border-white/10 overflow-hidden ring-2 ring-transparent group-hover:ring-[#FF4C00]/20 transition-all"
        >
          <img
            src="https://api.dicebear.com/7.x/avataaars/svg?seed=Felix"
            alt="User"
            class="w-full h-full object-cover opacity-90 group-hover:opacity-100"
          />
        </div>
        <div
          class="absolute bottom-0 right-0 w-2.5 h-2.5 bg-emerald-500 border-2 border-zinc-950 rounded-full"
        ></div>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { ChevronLeft, Timer } from 'lucide-vue-next'

const props = defineProps<{
  isTimerPaused?: boolean
}>()

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
