<template>
  <div
    v-if="isVerifying"
    class="min-h-screen w-full bg-black flex items-center justify-center font-sans"
  >
    <div class="flex flex-col items-center gap-6">
      <div class="relative w-16 h-16">
        <div class="absolute inset-0 rounded-full border-4 border-zinc-800"></div>
        <div class="absolute inset-0 rounded-full border-4 border-t-[#FF4C00] animate-spin"></div>
        <Loader2 class="absolute inset-0 m-auto w-6 h-6 text-[#FF4C00] animate-pulse" />
      </div>
      <div class="text-center space-y-2">
        <h3 class="text-white font-bold tracking-wider text-lg">正在建立神经连接</h3>
        <p class="text-zinc-500 font-mono text-xs tracking-[0.2em] uppercase animate-pulse">
          Verifying Neural Link...
        </p>
      </div>
    </div>
  </div>

  <div
    v-else
    class="relative min-h-screen flex flex-col items-center bg-black overflow-hidden selection:bg-[#FF4C00] selection:text-white"
  >
    <div
      class="absolute inset-0 bg-[linear-gradient(to_right,#80808012_1px,transparent_1px),linear-gradient(to_bottom,#80808012_1px,transparent_1px)] bg-[size:40px_40px] [mask-image:radial-gradient(ellipse_80%_50%_at_50%_0%,#000_70%,transparent_110%)] pointer-events-none"
    ></div>
    <div
      class="absolute inset-0 bg-gradient-to-b from-black/0 via-black/0 to-zinc-950 pointer-events-none"
    ></div>

    <div class="w-full relative z-20 h-20 flex items-center px-6 justify-between">
      <ArenaExitButton @click="showExitDialog = true" />

      <div v-if="mode === 'CUSTOM'" class="absolute left-1/2 -translate-x-1/2">
        <div
          class="flex items-center gap-3 bg-zinc-900/80 backdrop-blur-md border border-white/5 px-4 py-2 rounded-full cursor-pointer hover:border-[#FF4C00]/50 transition-colors group"
          @click="copyRoomId"
        >
          <span class="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
          <span class="text-zinc-400 text-xs font-bold tracking-wider">ROOM:</span>
          <span
            class="text-white font-mono font-bold text-lg tracking-widest group-hover:text-[#FF4C00] transition-colors"
            >{{ roomId }}</span
          >
          <Copy class="w-3 h-3 text-zinc-600 ml-1 group-hover:text-white transition-colors" />
        </div>
      </div>
    </div>

    <div class="flex-1 flex w-full max-w-6xl items-center justify-center relative z-10 mt-[-60px]">
      <div
        class="flex-1 flex flex-col items-center justify-center relative transition-all duration-500"
      >
        <div class="relative group">
          <div
            class="absolute inset-0 blur-[60px] rounded-full transition-all duration-700"
            :class="isSelfReady ? 'bg-emerald-500/20' : 'bg-[#FF4C00]/0'"
          ></div>

          <div
            class="relative w-32 h-32 md:w-48 md:h-48 rounded-full border-4 p-1 transition-all duration-500"
            :class="
              isSelfReady
                ? 'border-emerald-500 shadow-[0_0_30px_rgba(16,185,129,0.3)]'
                : 'border-[#FF4C00] shadow-[0_0_20px_rgba(255,76,0,0.2)]'
            "
          >
            <img
              :src="userStore.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix'"
              class="w-full h-full rounded-full object-cover bg-zinc-900"
            />

            <div
              v-if="isSelfReady"
              v-motion
              :initial="{ scale: 0, opacity: 0 }"
              :enter="{ scale: 1, opacity: 1, transition: { type: 'spring' } }"
              class="absolute -top-2 -right-2 bg-zinc-900 rounded-full border border-emerald-500/30 p-1"
            >
              <CheckCircle2
                class="w-8 h-8 text-emerald-500 drop-shadow-[0_0_10px_rgba(16,185,129,0.6)]"
              />
            </div>

            <div
              v-if="mode === 'CUSTOM' && isHost"
              class="absolute -bottom-3 left-1/2 -translate-x-1/2 bg-[#FF4C00] text-black text-[10px] font-black px-3 py-0.5 rounded-sm uppercase tracking-widest shadow-lg"
            >
              HOST
            </div>
          </div>
        </div>

        <h2 class="mt-8 text-2xl font-bold text-white">{{ userStore.nickname || 'Commander' }}</h2>
        <p
          class="font-mono text-sm tracking-[0.2em] mt-2 transition-colors duration-300 font-bold uppercase"
          :class="isSelfReady ? 'text-emerald-500' : 'text-zinc-500'"
        >
          {{ isSelfReady ? '已准备' : '待准备' }}
        </p>
      </div>

      <div class="shrink-0 relative mx-12 md:mx-20 flex flex-col items-center min-w-[200px]">
        <div class="mb-12 flex flex-col items-center" v-if="!isLaunching">
          <div class="text-xs text-zinc-500 font-bold tracking-widest mb-2 flex items-center gap-2">
            <Timer class="w-3 h-3" />
            <span>房间解散</span>
          </div>
          <div
            class="font-mono text-5xl md:text-6xl font-black italic tracking-tighter tabular-nums transition-colors duration-300"
            :class="
              countdown <= 10
                ? 'text-[#FF4C00] animate-pulse drop-shadow-[0_0_15px_rgba(255,76,0,0.5)]'
                : 'text-white'
            "
          >
            {{ formattedTime }}
          </div>
        </div>

        <div
          class="text-[100px] md:text-[140px] font-black italic leading-none text-transparent bg-clip-text bg-gradient-to-b from-white/10 to-transparent tracking-tighter select-none absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 pointer-events-none"
        >
          VS
        </div>
      </div>

      <div class="flex-1 flex flex-col items-center justify-center relative min-h-[320px]">
        <template v-if="opponent">
          <div
            v-motion
            :initial="{ opacity: 0, x: 20 }"
            :enter="{ opacity: 1, x: 0 }"
            class="flex flex-col items-center"
          >
            <div class="relative group">
              <div
                class="absolute inset-0 blur-[60px] rounded-full transition-all duration-700"
                :class="opponent.isReady ? 'bg-emerald-500/20' : 'bg-white/5'"
              ></div>

              <div
                class="relative w-32 h-32 md:w-48 md:h-48 rounded-full border-4 p-1 transition-all duration-500"
                :class="
                  opponent.isReady
                    ? 'border-emerald-500 shadow-[0_0_30px_rgba(16,185,129,0.3)]'
                    : 'border-zinc-700'
                "
              >
                <img
                  :src="opponent.avatar"
                  class="w-full h-full rounded-full object-cover bg-zinc-900"
                />

                <div
                  v-if="opponent.isReady"
                  v-motion
                  :initial="{ scale: 0, opacity: 0 }"
                  :enter="{ scale: 1, opacity: 1, transition: { type: 'spring' } }"
                  class="absolute -top-2 -right-2 bg-zinc-900 rounded-full border border-emerald-500/30 p-1"
                >
                  <CheckCircle2
                    class="w-8 h-8 text-emerald-500 drop-shadow-[0_0_10px_rgba(16,185,129,0.6)]"
                  />
                </div>

                <div
                  v-if="mode === 'CUSTOM' && opponent.isHost"
                  class="absolute -bottom-3 left-1/2 -translate-x-1/2 bg-zinc-600 text-white text-[10px] font-black px-3 py-0.5 rounded-sm uppercase tracking-widest shadow-lg"
                >
                  HOST
                </div>
              </div>
            </div>

            <h2 class="mt-8 text-2xl font-bold text-zinc-200">{{ opponent.name }}</h2>
            <p
              class="font-mono text-sm tracking-[0.2em] mt-2 transition-colors duration-300 font-bold uppercase"
              :class="opponent.isReady ? 'text-emerald-500' : 'text-zinc-600'"
            >
              {{ opponent.isReady ? '已准备' : '待准备' }}
            </p>
          </div>
        </template>

        <template v-else>
          <div class="flex flex-col items-center gap-6 opacity-60">
            <div
              class="relative w-32 h-32 md:w-48 md:h-48 rounded-full border-2 border-dashed border-zinc-700 flex items-center justify-center"
            >
              <div class="absolute inset-0 bg-[#FF4C00]/5 rounded-full animate-pulse"></div>
              <div
                class="absolute inset-0 rounded-full border-t-2 border-[#FF4C00]/50 animate-[spin_3s_linear_infinite]"
              ></div>
              <Loader2 class="w-8 h-8 text-[#FF4C00] animate-spin" />
            </div>

            <div class="text-center space-y-1">
              <h2 class="text-xl font-bold text-zinc-600 tracking-wide">信号检索中</h2>
              <p class="text-xs font-mono text-zinc-600 tracking-widest uppercase">
                {{ mode === 'CUSTOM' ? '等待玩家...' : '匹配成功...' }}
              </p>
            </div>
          </div>
        </template>
      </div>
    </div>

    <div class="absolute bottom-12 w-full flex justify-center z-30">
      <button
        @click="handleSelfReady"
        :disabled="isSelfReady || !opponent"
        class="group relative w-64 h-16 rounded-xl overflow-hidden transition-all duration-300 transform active:scale-95 disabled:active:scale-100 disabled:cursor-not-allowed"
        :class="[
          !opponent
            ? 'bg-zinc-900 border border-zinc-800 opacity-50'
            : isSelfReady
              ? 'bg-zinc-900 border border-zinc-800'
              : 'bg-[#FF4C00] shadow-[0_0_30px_rgba(255,76,0,0.4)] hover:shadow-[0_0_50px_rgba(255,76,0,0.6)] hover:bg-[#ff5f1f]',
        ]"
      >
        <div class="relative z-10 flex items-center justify-center gap-3 h-full">
          <span v-if="!opponent" class="text-zinc-500 font-bold tracking-widest text-sm">
            等待对手加入
          </span>
          <span
            v-else-if="!isSelfReady"
            class="text-white font-black text-lg tracking-[0.2em] uppercase italic"
          >
            确认出战
          </span>
          <div v-else class="flex items-center gap-2 text-zinc-500 font-bold tracking-widest">
            <span class="w-2 h-2 bg-emerald-500 rounded-full animate-pulse"></span>
            <span>等待开始...</span>
          </div>
        </div>

        <div
          v-if="!isSelfReady && opponent"
          class="absolute inset-0 bg-gradient-to-r from-transparent via-white/20 to-transparent skew-x-12 translate-x-[-150%] group-hover:animate-shine"
        ></div>
      </button>
    </div>

    <div
      v-if="isLaunching"
      class="fixed inset-0 bg-black z-[100] flex items-center justify-center flex-col gap-8"
      v-motion
      :initial="{ opacity: 0 }"
      :enter="{ opacity: 1 }"
    >
      <div
        class="text-[#FF4C00] font-black text-6xl italic tracking-tighter animate-pulse drop-shadow-[0_0_30px_rgba(255,76,0,0.6)]"
      >
        战斗开始!
      </div>
      <div class="w-64 h-1 bg-zinc-900 rounded-full overflow-hidden">
        <div class="h-full bg-[#FF4C00] w-full animate-[progress_1s_ease-in-out]"></div>
      </div>
    </div>

    <ArenaDialog
      :model-value="showTimeoutDialog"
      title="房间已解散"
      confirm-text="返回大厅"
      @confirm="exitLobby"
      @update:model-value="exitLobby"
    >
      <div class="text-center space-y-4">
        <p class="text-zinc-400">房间存活时间已到期，已自动安全解散。</p>
      </div>
    </ArenaDialog>

    <ArenaDialog
      v-model="showExitDialog"
      title="确认离开房间？"
      confirm-text="确认离开"
      @confirm="confirmManualExit"
    >
      <div class="text-center space-y-4">
        <p class="text-zinc-400">
          离开后，您的位置将<span class="text-red-500 font-bold">不再保留</span>。
        </p>
      </div>
    </ArenaDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router'
import { useClipboard } from '@vueuse/core'
import { useUserStore } from '@/stores/user'
import { Copy, CheckCircle2, Timer, Loader2 } from 'lucide-vue-next'
import ArenaExitButton from '@/components/arena/ArenaExitButton.vue'
import ArenaDialog from '@/components/arena/ArenaDialog.vue'
import { checkRoomValidity } from '@/api/arena'

// --- 类型定义 ---
interface Opponent {
  id: number
  name: string
  avatar: string
  isReady: boolean
  isHost: boolean
}

// --- 基础设置 ---
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { copy } = useClipboard()

const roomId = route.params.roomId as string
const mode = computed(() => (route.query.mode === 'RANKED' ? 'RANKED' : 'CUSTOM'))
const ticket = computed(() => route.query.ticket as string | undefined)

// --- 状态数据 ---
const isVerifying = ref(true)
const isSelfReady = ref(false)
const isHost = ref(false)
const opponent = ref<Opponent | null>(null)
const isLaunching = ref(false)
const countdown = ref(0) // 初始为 0，绝对等待后端时钟
const showTimeoutDialog = ref(false)

const showExitDialog = ref(false)
const isManualExit = ref(false)
const isSystemExit = ref(false)

let mainTimer: number | null = null
let ws: WebSocket | null = null

const formattedTime = computed(() => {
  // [防御性编程]: 哪怕格式化时也做一次 Math.max 兜底，绝不让负号上 UI
  const safeTime = Math.max(0, countdown.value)
  const m = Math.floor(safeTime / 60)
    .toString()
    .padStart(2, '0')
  const s = (safeTime % 60).toString().padStart(2, '0')
  return `${m}:${s}`
})

// --- 路由守卫 ---
onBeforeRouteLeave((to, from, next) => {
  if (isLaunching.value || isSystemExit.value || isManualExit.value) {
    next()
  } else {
    next(false)
    showExitDialog.value = true
  }
})

// --- HTTP 验票守卫 ---
const initializeRoom = async () => {
  if (!roomId) {
    isSystemExit.value = true
    router.replace('/arena')
    return
  }

  try {
    const res = await checkRoomValidity(roomId, ticket.value)

    if (!res.isValid) {
      isSystemExit.value = true
      router.replace({ path: '/arena', query: { error: 'ROOM_INVALID' } })
      return
    }

    if (res.status === 'FINISHED' || res.status === 'DISMISSED') {
      isSystemExit.value = true
      router.replace({ path: '/arena', query: { error: 'BATTLE_ENDED' } })
      return
    }

    isVerifying.value = false

    // [架构师修复 1]: 移除此处的 startTimer()。不能再“抢跑”了！
    // 必须等待 WebSocket 连接成功并下发 expireTime 时才启动时钟。
    connectWebSocket()
  } catch (error) {
    console.error('[Arena] Room validity check failed:', error)
    isSystemExit.value = true
    router.replace('/arena')
  }
}

// ==========================================
// WebSocket 通信与后端状态机联动
// ==========================================
const connectWebSocket = () => {
  const token = userStore.token
  if (!token) return

  const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const wsHost = import.meta.env.VITE_WS_URL || 'ws://localhost:8080'
  const wsUrl = `${wsHost}/api/ws/arena/${roomId}?token=${token}`

  ws = new WebSocket(wsUrl)

  ws.onopen = () => {
    console.log('⚡ [Arena WS] Neural link established.')
  }

  ws.onmessage = (event) => {
    try {
      const payload = JSON.parse(event.data)
      handleWsMessage(payload)
    } catch (e) {
      console.error('[Arena WS] Failed to parse message:', e)
    }
  }

  ws.onclose = (event) => {
    console.warn('🔌 [Arena WS] Disconnected.', event.reason)
  }
}

const handleWsMessage = (payload: any) => {
  const { action, data } = payload
  const myId = (userStore.userInfo as any)?.id

  switch (action) {
    case 'SYNC_ROOM': {
      // [架构师修复 2]: 绝对时钟同步，并精准启动定时器
      if (data.expireTime) {
        const remainingSeconds = Math.floor((data.expireTime - Date.now()) / 1000)
        countdown.value = Math.max(0, remainingSeconds)

        // 如果还没启动过定时器，并且时间大于0，正式发车
        if (!mainTimer && countdown.value > 0) {
          startTimer()
        } else if (countdown.value <= 0) {
          // 如果后端传来的时间本身就已经过期了，直接触发弹窗
          handleTimeout()
        }
      }

      // 身份与列表覆盖更新 (Host Migration)
      const me = data.players.find((p: any) => p.userId === myId)
      if (me) {
        isSelfReady.value = me.isReady
        isHost.value = Number(me.isCreator) === 1
      }

      const other = data.players.find((p: any) => p.userId !== myId)
      if (other) {
        opponent.value = {
          id: other.userId,
          name: other.nickname,
          avatar:
            other.avatarUrl || `https://api.dicebear.com/7.x/avataaars/svg?seed=${other.nickname}`,
          isReady: other.isReady,
          isHost: Number(other.isCreator) === 1,
        }
      } else {
        opponent.value = null
      }
      break
    }
    case 'PLAYER_JOINED': {
      if (data.userId !== myId) {
        opponent.value = {
          id: data.userId,
          name: data.nickname,
          avatar:
            data.avatarUrl || `https://api.dicebear.com/7.x/avataaars/svg?seed=${data.nickname}`,
          isReady: data.isReady,
          isHost: Number(data.isCreator) === 1,
        }
      }
      break
    }
    case 'PLAYER_READY': {
      if (data.userId === myId) {
        isSelfReady.value = data.isReady
      } else if (opponent.value && opponent.value.id === data.userId) {
        opponent.value.isReady = data.isReady
      }
      break
    }
    case 'PLAYER_LEFT': {
      if (opponent.value && opponent.value.id === data.userId) {
        opponent.value = null
        if (isSelfReady.value) {
          sendReadyAction(false)
        }
      }
      break
    }
    case 'GAME_START': {
      clearAllTimers()
      isLaunching.value = true

      setTimeout(() => {
        router.replace({
          name: 'ArenaBattle',
          params: { roomId },
          query: { mode: mode.value, problemId: data.problemId },
        })
      }, 1500)
      break
    }
  }
}

const sendReadyAction = (status: boolean) => {
  if (ws && ws.readyState === WebSocket.OPEN) {
    ws.send(
      JSON.stringify({
        action: 'READY',
        data: { isReady: status },
      }),
    )
  }
}

const handleSelfReady = () => {
  if (isSelfReady.value || !opponent.value) return
  sendReadyAction(true)
}
// ==========================================

// [架构师修复 3]: 定时器逻辑大升级
const startTimer = () => {
  if (mainTimer) return // 防抖，防止多次启动

  mainTimer = window.setInterval(() => {
    // 只有在时间大于0的时候才扣减
    if (countdown.value > 0) {
      countdown.value--
    }

    // 一旦发现小于等于0，立刻处理后事
    if (countdown.value <= 0) {
      countdown.value = 0 // 物理清零兜底
      handleTimeout()
    }
  }, 1000)
}

const handleTimeout = () => {
  clearAllTimers()
  countdown.value = 0 // 再次强制兜底

  // 极端情况判断：如果俩人最后1秒都准备了，让它开局，不弹窗
  if (isSelfReady.value && opponent.value?.isReady) return

  // 否则，强制拉起超时离场弹窗
  showTimeoutDialog.value = true
}

const copyRoomId = () => copy(roomId)

// [架构师修复 4]: 退房逻辑
const exitLobby = () => {
  // 这会绕过离开确认弹窗，直接触发路由跳转
  isSystemExit.value = true
  router.replace('/arena')
  // 路由跳转会自动触发 onUnmounted 里的 closeWebSocket()，后端借此感知“玩家退出并销毁房间”
}

const confirmManualExit = () => {
  isManualExit.value = true
  showExitDialog.value = false
  router.replace('/arena')
}

const clearAllTimers = () => {
  if (mainTimer) {
    clearInterval(mainTimer)
    mainTimer = null
  }
}

const closeWebSocket = () => {
  if (ws) {
    ws.close()
    ws = null
  }
}

onMounted(() => {
  initializeRoom()
})

onUnmounted(() => {
  clearAllTimers()
  closeWebSocket() // 这里就是呼叫后端的“退房物理开关”
})
</script>

<style scoped>
/* 按钮流光动画 */
@keyframes shine {
  from {
    transform: translateX(-150%) skewX(12deg);
  }
  to {
    transform: translateX(150%) skewX(12deg);
  }
}
.animate-shine {
  animation: shine 1.5s infinite;
}

/* 进度条加载动画 */
@keyframes progress {
  from {
    width: 0%;
  }
  to {
    width: 100%;
  }
}
</style>
