<template>
  <div
    class="h-screen w-full bg-zinc-100 dark:bg-[#050505] flex flex-col items-center justify-center relative overflow-hidden select-none"
  >
    <div
      class="absolute inset-0 pointer-events-none opacity-10 dark:opacity-0"
      style="
        background-image: radial-gradient(#a1a1aa 1px, transparent 1px);
        background-size: 32px 32px;
      "
    ></div>
    <div
      class="absolute inset-0 pointer-events-none opacity-0 dark:opacity-20"
      style="
        background-image: radial-gradient(#3f3f46 1px, transparent 1px);
        background-size: 32px 32px;
      "
    ></div>

    <button
      @click="handleToggleTheme"
      class="absolute top-4 right-4 z-50 p-2.5 rounded-full border backdrop-blur-md transition-all duration-300 hover:scale-110 active:scale-95 cursor-pointer"
      :class="
        isDark
          ? 'bg-zinc-900/80 border-white/10 text-zinc-400 hover:text-white hover:border-white/20'
          : 'bg-white border-zinc-300 text-zinc-500 hover:text-zinc-900 hover:border-zinc-400 shadow-md'
      "
      title="切换日间/夜间模式"
    >
      <Sun v-if="isDark" class="w-4 h-4" />
      <Moon v-else class="w-4 h-4" />
    </button>

    <div class="relative z-10 flex flex-col items-center">
      <div class="relative w-64 h-64 flex items-center justify-center mb-12">
        <div
          class="absolute inset-0 rounded-full border border-zinc-300 dark:border-white/10"
        ></div>
        <div class="absolute inset-4 rounded-full border border-zinc-200 dark:border-white/5"></div>
        <div class="absolute inset-8 rounded-full border border-zinc-200 dark:border-white/5"></div>

        <div
          class="absolute inset-0 rounded-full border-t-2 opacity-80"
          :class="
            isMatched
              ? 'border-emerald-500 animate-spin-fast shadow-[0_0_30px_#10b981]'
              : 'border-[#FF4C00] animate-spin-slow shadow-[0_0_20px_#FF4C00]'
          "
        ></div>

        <div
          v-if="!isMatched"
          class="absolute inset-0 rounded-full bg-gradient-to-tr from-transparent via-transparent to-[#FF4C00]/20 animate-spin-slow"
        ></div>

        <div
          class="relative z-20 w-20 h-20 rounded-full border-2 bg-zinc-100 dark:bg-zinc-900 flex items-center justify-center overflow-hidden transition-colors duration-500"
          :class="
            isMatched
              ? 'border-emerald-500 shadow-[0_0_20px_#10b981]'
              : 'border-zinc-300 dark:border-zinc-700'
          "
        >
          <img
            v-if="(userStore as any).avatar || userStore.userInfo?.userAvatar"
            :src="(userStore as any).avatar || userStore.userInfo?.userAvatar"
            alt="Me"
            class="w-full h-full object-cover"
          />
          <User v-else class="w-8 h-8 text-zinc-400 dark:text-zinc-500" />
        </div>

        <div
          v-if="isMatched"
          class="absolute -right-4 -top-4 w-12 h-12 rounded-full border-2 border-emerald-500 flex items-center justify-center animate-ping"
        >
          <Crosshair class="w-6 h-6 text-emerald-500" />
        </div>
      </div>

      <h2
        class="text-2xl font-black italic tracking-widest text-zinc-900 dark:text-white mb-3 uppercase"
      >
        {{ isMatched ? '目标锁定' : '正在扫描' }}
      </h2>
      <p
        class="text-zinc-500 font-mono text-sm tracking-widest mb-12 h-5 flex items-center justify-center"
        :class="isMatched ? 'text-emerald-500 dark:text-emerald-400 font-bold' : 'animate-pulse'"
      >
        {{ statusMessage }}
      </p>

      <button
        @click="handleCancel"
        :disabled="isCanceling || isMatched"
        class="relative px-16 py-4 rounded-xl font-bold text-lg tracking-widest uppercase transition-all duration-300 overflow-hidden disabled:opacity-50 disabled:cursor-not-allowed bg-[#FF4C00] text-white shadow-[0_0_20px_rgba(255,76,0,0.4)] hover:shadow-[0_0_40px_rgba(255,76,0,0.6)]"
      >
        <span class="relative z-10 flex items-center gap-2">
          <Loader2 v-if="isCanceling" class="w-5 h-5 animate-spin" />
          {{ isCanceling ? '中止中...' : '中止匹配' }}
        </span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Crosshair, Loader2, Sun, Moon } from 'lucide-vue-next'
import { useUserStore } from '@/stores/user'
import { joinMatchmaking, getMatchStatus, leaveMatchmaking } from '@/api/arena'
import { useDark, useToggle } from '@vueuse/core'

const isDark = useDark({
  storageKey: 'NEXUS_THEME',
  selector: 'html',
  attribute: 'class',
  valueDark: 'dark',
  valueLight: '',
})
const toggleDark = useToggle(isDark)

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const handleToggleTheme = () => {
  toggleDark()
}

const isMatched = ref(false)
const isCanceling = ref(false)
const statusMessage = ref('正在扫描全服匹配池...')

const goBack = () => {
  if (route.query.from === 'dashboard') {
    router.replace('/dashboard')
  } else {
    router.replace('/arena')
  }
}
let pollTimer: number | null = null

const startMatch = async () => {
  try {
    await joinMatchmaking()

    pollTimer = window.setInterval(async () => {
      try {
        const res = await getMatchStatus()
        // 【核心修复】：抓取后端返回的 ticket
        if (res.status === 'SUCCESS' && res.roomCode) {
          handleMatchSuccess(res.roomCode, res.ticket)
        }
      } catch (err) {
        console.error('获取匹配状态异常:', err)
      }
    }, 2000)
  } catch (error) {
    console.error('加入匹配失败:', error)
    statusMessage.value = '匹配服务暂时不可用'
    setTimeout(() => goBack(), 2000)
  }
}

// 【核心修复】：接收 ticket 并塞入路由 Query
const handleMatchSuccess = (roomCode: string, ticket?: string) => {
  if (pollTimer) {
    window.clearInterval(pollTimer)
    pollTimer = null
  }
  isMatched.value = true
  statusMessage.value = '匹配成功！正在建立安全连接...'

  setTimeout(() => {
    const query: Record<string, string> = { ticket: ticket || '' }
    if (route.query.from === 'dashboard') query.from = 'dashboard'
    router.replace({
      path: `/battle/lobby/${roomCode}`,
      query,
    })
  }, 1500)
}

const handleCancel = async () => {
  if (isCanceling.value || isMatched.value) return
  isCanceling.value = true
  statusMessage.value = '正在中断连接...'

  if (pollTimer) {
    window.clearInterval(pollTimer)
    pollTimer = null
  }

  try {
    await leaveMatchmaking()
  } catch (err) {
    console.error('退出匹配失败:', err)
  } finally {
    goBack()
  }
}

onMounted(() => {
  startMatch()
})

onBeforeUnmount(() => {
  if (!isMatched.value && !isCanceling.value) {
    if (pollTimer) window.clearInterval(pollTimer)
    leaveMatchmaking().catch(() => {})
  }
})
</script>

<style scoped>
.animate-spin-slow {
  animation: spin 4s linear infinite;
}
.animate-spin-fast {
  animation: spin 1s linear infinite;
}
</style>
