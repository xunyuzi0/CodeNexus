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

const router = useRouter()
const userStore = useUserStore()

const found = ref(false)
const isCanceled = ref(false)

// 使用 number 类型以兼容浏览器环境的 setTimeout
let matchTimer: number | null = null
let redirectTimer: number | null = null

// [Refactor] 集中清理逻辑，防止幽灵跳转
const cleanup = () => {
  isCanceled.value = true

  if (matchTimer) {
    clearTimeout(matchTimer)
    matchTimer = null
  }

  if (redirectTimer) {
    clearTimeout(redirectTimer)
    redirectTimer = null
  }

  // 确保离开页面时（无论是跳转大厅还是取消），匹配凭证都被销毁
  storage.remove('MATCH_PENDING', 'session')
}

// [Refactor] 路由守卫：拦截所有离开行为
// 无论是匹配成功跳转大厅，还是点击取消按钮，亦或是浏览器后退，都会触发此守卫
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

  // 验票通过。虽然 cleanup 会兜底清除，但为了逻辑严谨性，此处可先消费凭证
  storage.remove('MATCH_PENDING', 'session')
  return true
}

onMounted(() => {
  // 优先执行安全检查
  if (!checkMatchSession()) {
    return
  }

  // 模拟匹配过程：3.5秒后匹配成功
  matchTimer = window.setTimeout(() => {
    if (isCanceled.value) return
    found.value = true

    // 匹配成功后，1.5秒跳转到大厅
    redirectTimer = window.setTimeout(() => {
      if (isCanceled.value) return

      const roomId = 'MATCH_' + Math.random().toString(36).substring(2, 6).toUpperCase()

      // [Security]: 跳转将触发 onBeforeRouteLeave，从而自动执行 cleanup
      router.replace({
        name: 'ArenaLobby',
        params: { roomId },
        query: { mode: 'RANKED' },
      })
    }, 1500)
  }, 3500)
})

const cancelMatch = () => {
  // [Refactor] 简化逻辑：直接触发路由跳转，依赖路由守卫执行清理
  router.replace('/arena')
}

onUnmounted(() => {
  // [Security] 兜底清理，防止路由守卫未覆盖的极端情况
  cleanup()
})
</script>
