<template>
  <div
    class="fixed inset-0 z-50 bg-black flex flex-col items-center justify-center overflow-hidden font-sans select-none"
  >
    <div
      class="absolute inset-0 bg-[radial-gradient(circle_at_center,#1a1a1a_0%,#000000_100%)]"
    ></div>
    <div class="absolute inset-0 bg-grid-white/[0.02] opacity-50"></div>

    <div class="relative flex items-center justify-center">
      <div
        class="absolute w-[600px] h-[600px] border border-white/5 rounded-full animate-ping [animation-duration:3s]"
      ></div>
      <div
        class="absolute w-[400px] h-[400px] border border-white/10 rounded-full animate-ping [animation-duration:3s] [animation-delay:0.5s]"
      ></div>

      <div class="absolute inset-[-200px] animate-[spin_4s_linear_infinite] opacity-30">
        <div
          class="w-full h-full bg-gradient-to-t from-transparent via-[#FF4C00]/20 to-transparent w-[2px] mx-auto"
        ></div>
      </div>

      <div
        class="relative z-10 w-32 h-32 rounded-full p-1 transition-all duration-500"
        :class="
          found
            ? 'bg-emerald-500 shadow-[0_0_50px_#10b981]' // 成功保持绿色
            : 'bg-zinc-800 shadow-[0_0_30px_rgba(255,76,0,0.4)]' // 搜索中为橙色光晕
        "
      >
        <div class="w-full h-full rounded-full overflow-hidden relative bg-zinc-900">
          <img
            :src="userStore.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix'"
            class="w-full h-full object-cover"
          />
          <div class="absolute inset-0 bg-[#FF4C00]/10 mix-blend-overlay" v-if="!found"></div>
        </div>

        <div
          v-if="!found"
          class="absolute -inset-4 rounded-full border border-[#FF4C00]/30 animate-pulse"
        ></div>
      </div>
    </div>

    <div class="relative z-10 mt-16 text-center space-y-2">
      <h2
        class="text-3xl font-black italic tracking-wider uppercase transition-colors duration-300"
        :class="found ? 'text-emerald-500 drop-shadow-[0_0_10px_#10b981]' : 'text-white'"
      >
        {{ found ? 'MATCH FOUND' : 'SEARCHING...' }}
      </h2>
      <p class="text-zinc-500 font-mono text-xs tracking-[0.3em] h-4 uppercase">
        {{ found ? 'Redirecting to Lobby...' : 'Scanning Neural Network...' }}
      </p>
    </div>

    <div class="absolute inset-0 pointer-events-none overflow-hidden opacity-30">
      <div class="absolute top-1/4 left-1/4 text-[10px] text-[#FF4C00] font-mono animate-bounce">
        Ping: 14ms
      </div>
      <div class="absolute bottom-1/3 right-1/4 text-[10px] text-[#FF4C00] font-mono animate-pulse">
        Zone: CN-Deep-1
      </div>
    </div>

    <div class="absolute bottom-12 left-0 right-0 z-20 flex justify-center">
      <button
        v-if="!found"
        @click="cancelMatch"
        class="h-12 px-8 rounded-full bg-white/5 border border-white/10 text-zinc-300 font-medium tracking-wide hover:bg-red-500/10 hover:border-red-500/50 hover:text-red-500 transition-all active:scale-95 flex items-center justify-center backdrop-blur-md shadow-xl group"
      >
        <X class="w-4 h-4 mr-2 opacity-50 group-hover:opacity-100 transition-opacity" />
        取消匹配
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, onBeforeRouteLeave } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { X } from 'lucide-vue-next'
import { storage } from '@/utils/storage'

// [新增]: 引入真实的后端 API
import { joinMatchmaking, cancelMatchmaking, getMatchStatus } from '@/api/arena'

const router = useRouter()
const userStore = useUserStore()

const found = ref(false)
const isCanceled = ref(false)

// [改造]: 轮询定时器与跳转定时器
let pollTimer: number | null = null
let redirectTimer: number | null = null

// --- 工具函数：清理定时器 ---
const clearTimers = () => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
  if (redirectTimer) {
    clearTimeout(redirectTimer)
    redirectTimer = null
  }
}

// --- 核心流转：清理与退出匹配池 ---
const cleanup = async () => {
  isCanceled.value = true
  clearTimers()

  // 确保离开页面时，一次性凭证被消费销毁
  storage.remove('MATCH_PENDING', 'session')

  // [后端契约]: 如果还没匹配成功就离开了页面，必须通知后端退出排队池
  if (!found.value) {
    try {
      await cancelMatchmaking()
    } catch (error) {
      console.warn('[Arena] Backend cancel match failed:', error)
    }
  }
}

// 路由守卫：拦截所有离开行为 (无论是匹配成功、点击取消、还是浏览器后退)
onBeforeRouteLeave((to, from, next) => {
  cleanup()
  next()
})

const checkMatchSession = (): boolean => {
  const hasTicket = storage.get<string>('MATCH_PENDING', 'session') === 'true'
  if (!hasTicket) {
    router.replace({
      path: '/arena',
      query: { error: 'MATCH_INVALID' },
    })
    return false
  }
  return true
}

// --- [后端契约]: 核心轮询逻辑 ---
const startPolling = () => {
  // 每 2 秒轮询一次状态
  pollTimer = window.setInterval(async () => {
    if (isCanceled.value) return

    try {
      const res = await getMatchStatus()

      if (res.status === 'SUCCESS' && res.roomCode && res.ticket) {
        // 1. 匹配成功，锁定状态
        found.value = true
        clearTimers() // 停止轮询

        // 2. 匹配成功后，留出 1.5 秒给用户看“MATCH FOUND”的动效，然后火速跳转
        redirectTimer = window.setTimeout(() => {
          if (isCanceled.value) return

          router.replace({
            name: 'ArenaLobby',
            params: { roomId: res.roomCode },
            // [极其重要]: 将后端下发的 ticket 带给大厅作为验票凭证
            query: { mode: 'RANKED', ticket: res.ticket },
          })
        }, 1500)
      } else if (res.status === 'FAILED') {
        // 后端提示匹配异常，终止轮询并抛出错误
        clearTimers()
        router.replace({ path: '/arena', query: { error: 'MATCH_INVALID' } })
      }
      // 遇到 MATCHING 状态，什么都不做，等待下一个 2 秒
    } catch (error) {
      console.error('[Arena] Polling error:', error)
      // 若是由于网络波动导致的错误，request.ts 已有容错，暂不中断轮询
    }
  }, 2000)
}

onMounted(async () => {
  // 1. 优先执行前端安全防跳检 (防止直接在地址栏输入URL)
  if (!checkMatchSession()) {
    return
  }

  // 2. 真实调用后端，加入天梯池
  try {
    const isJoined = await joinMatchmaking()
    if (isJoined) {
      // 3. 加入成功，开始轮询
      startPolling()
    }
  } catch (error) {
    console.error('[Arena] Failed to join matchmaking pool:', error)
    router.replace({ path: '/arena', query: { error: 'MATCH_INVALID' } })
  }
})

const cancelMatch = () => {
  // 直接触发路由跳转，onBeforeRouteLeave 守卫会自动执行 cleanup (包含调用 DELETE 取消接口)
  router.replace('/arena')
}

onUnmounted(() => {
  cleanup()
})
</script>
