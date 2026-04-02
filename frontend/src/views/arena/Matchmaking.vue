<template>
  <div
    class="h-screen w-full bg-[#050505] flex flex-col items-center justify-center relative overflow-hidden select-none"
  >
    <div
      class="absolute inset-0 pointer-events-none opacity-20"
      style="
        background-image: radial-gradient(#3f3f46 1px, transparent 1px);
        background-size: 32px 32px;
      "
    ></div>

    <div class="relative z-10 flex flex-col items-center">
      <div class="relative w-64 h-64 flex items-center justify-center mb-12">
        <div class="absolute inset-0 rounded-full border border-white/10"></div>
        <div class="absolute inset-4 rounded-full border border-white/5"></div>
        <div class="absolute inset-8 rounded-full border border-white/5"></div>

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
          class="relative z-20 w-20 h-20 rounded-full border-2 bg-zinc-900 flex items-center justify-center overflow-hidden transition-colors duration-500"
          :class="isMatched ? 'border-emerald-500 shadow-[0_0_20px_#10b981]' : 'border-zinc-700'"
        >
          <img
            v-if="(userStore as any).avatar || userStore.userInfo?.userAvatar"
            :src="(userStore as any).avatar || userStore.userInfo?.userAvatar"
            alt="Me"
            class="w-full h-full object-cover"
          />
          <User v-else class="w-8 h-8 text-zinc-500" />
        </div>

        <div
          v-if="isMatched"
          class="absolute -right-4 -top-4 w-12 h-12 rounded-full border-2 border-emerald-500 flex items-center justify-center animate-ping"
        >
          <Crosshair class="w-6 h-6 text-emerald-500" />
        </div>
      </div>

      <h2 class="text-2xl font-black italic tracking-widest text-white mb-3 uppercase">
        {{ isMatched ? '目标锁定' : '正在扫描' }}
      </h2>
      <p
        class="text-zinc-500 font-mono text-sm tracking-widest mb-12 h-5 flex items-center justify-center"
        :class="isMatched ? 'text-emerald-400 font-bold' : 'animate-pulse'"
      >
        {{ statusMessage }}
      </p>

      <button
        @click="handleCancel"
        :disabled="isCanceling || isMatched"
        class="group relative px-8 py-3 rounded-full bg-zinc-900 border border-white/10 hover:bg-zinc-800 transition-all duration-300 overflow-hidden disabled:opacity-50 disabled:cursor-not-allowed"
      >
        <div
          class="absolute inset-0 bg-gradient-to-r from-red-500/0 via-red-500/10 to-red-500/0 translate-x-[-100%] group-hover:translate-x-[100%] transition-transform duration-700"
        ></div>
        <span
          class="relative z-10 flex items-center gap-2 text-sm font-bold tracking-widest text-zinc-400 group-hover:text-red-400 uppercase transition-colors"
        >
          <Loader2 v-if="isCanceling" class="w-4 h-4 animate-spin" />
          <X v-else class="w-4 h-4" />
          {{ isCanceling ? '中止中...' : '中止匹配' }}
        </span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { User, Crosshair, X, Loader2 } from 'lucide-vue-next'
import { useUserStore } from '@/stores/user'
import { joinMatchmaking, getMatchStatus, leaveMatchmaking } from '@/api/arena'

const router = useRouter()
const userStore = useUserStore()

const isMatched = ref(false)
const isCanceling = ref(false)
const statusMessage = ref('正在扫描全服匹配池...')
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
    setTimeout(() => router.replace('/arena'), 2000)
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
    router.replace({
      path: `/battle/lobby/${roomCode}`,
      query: { ticket }, // 把钥匙带上！
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
    router.replace('/arena')
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
