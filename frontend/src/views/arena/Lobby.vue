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
      <ArenaExitButton :needs-confirm="isSelfReady" />

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
              v-if="mode === 'CUSTOM'"
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
            <span>自动解散</span>
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
      v-model="showTimeoutDialog"
      title="匹配超时"
      confirm-text="返回大厅"
      @confirm="exitLobby"
    >
      <div class="text-center space-y-4">
        <p class="text-zinc-400">双方未能在规定时间内完成准备，房间已自动解散。</p>
      </div>
    </ArenaDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watchEffect } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useClipboard } from '@vueuse/core'
import { useUserStore } from '@/stores/user'
import { Copy, CheckCircle2, Timer, Loader2 } from 'lucide-vue-next'
import ArenaExitButton from '@/components/arena/ArenaExitButton.vue'
import ArenaDialog from '@/components/arena/ArenaDialog.vue'
import { checkRoomValidity } from '@/api/arena' //  引入 API

// --- 类型定义 ---
interface Opponent {
  name: string
  avatar: string
  isReady: boolean
}

// --- 基础设置 ---
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { copy } = useClipboard()

const roomId = route.params.roomId as string
// 模式判定：RANKED (排位) | CUSTOM (自建)
const mode = computed(() => (route.query.mode === 'RANKED' ? 'RANKED' : 'CUSTOM'))

// --- 状态数据 ---
const isVerifying = ref(true) //  安全检查 Loading 状态
const isSelfReady = ref(false)
const opponent = ref<Opponent | null>(null) // 初始为 null，表示空位
const isLaunching = ref(false)
const countdown = ref(mode.value === 'RANKED' ? 20 : 120)
const showTimeoutDialog = ref(false)

// 定时器引用
let mainTimer: number | null = null
let simEntryTimer: number | null = null
let simReadyTimer: number | null = null

// 格式化时间 mm:ss
const formattedTime = computed(() => {
  const m = Math.floor(countdown.value / 60)
    .toString()
    .padStart(2, '0')
  const s = (countdown.value % 60).toString().padStart(2, '0')
  return `${m}:${s}`
})

// --- 核心业务逻辑 ---

//  房间安全检查
const initializeRoom = async () => {
  if (!roomId) {
    router.replace('/arena')
    return
  }

  try {
    const isValid = await checkRoomValidity(roomId)
    if (!isValid) {
      console.warn('[Arena Security] Room is invalid or expired:', roomId)
      // [CRITICAL]: 校验失败，强制 replace 销毁当前历史，踢回大厅
      router.replace({
        path: '/arena',
        query: { error: 'ROOM_INVALID' },
      })
    } else {
      isVerifying.value = false
      // 只有通过校验才开始倒计时和模拟
      startTimer()
      initSimulation()
    }
  } catch (error) {
    console.error('Room check failed:', error)
    router.replace('/arena')
  }
}

// 1. 初始化模拟数据
const initSimulation = () => {
  if (mode.value === 'RANKED') {
    opponent.value = {
      name: 'DeepSeeker_AI',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=John',
      isReady: false,
    }
    simulateOpponentReady(2000, 8000)
  } else {
    opponent.value = null
    const entryDelay = Math.random() * 6000 + 2000
    simEntryTimer = setTimeout(() => {
      opponent.value = {
        name: 'AlgorithmMaster_99',
        avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Sarah',
        isReady: false,
      }
      simulateOpponentReady(2000, 5000)
    }, entryDelay)
  }
}

const simulateOpponentReady = (min: number, max: number) => {
  const delay = Math.random() * (max - min) + min
  simReadyTimer = setTimeout(() => {
    if (opponent.value) {
      opponent.value.isReady = true
    }
  }, delay)
}

// 2. 倒计时逻辑
const startTimer = () => {
  mainTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      handleTimeout()
    }
  }, 1000)
}

// 3. 用户点击准备
const handleSelfReady = () => {
  if (isSelfReady.value || !opponent.value) return
  isSelfReady.value = true
}

// 4. 超时处理
const handleTimeout = () => {
  clearAllTimers()
  if (isSelfReady.value && opponent.value?.isReady) return

  showTimeoutDialog.value = true
}

// 5. 监听游戏开始
watchEffect(() => {
  if (isSelfReady.value && opponent.value?.isReady && !isLaunching.value) {
    clearAllTimers()
    isLaunching.value = true

    // 播放转场动画 1.5秒后跳转
    setTimeout(() => {
      // [CRITICAL]: 战斗开始，销毁 Lobby 历史，防止回退
      router.replace({
        name: 'ArenaBattle',
        params: { roomId },
        query: { mode: mode.value },
      })
    }, 1500)
  }
})

// 工具函数
const copyRoomId = () => copy(roomId)
const exitLobby = () => router.replace('/arena') // [CRITICAL]: 超时退出使用 replace
const clearAllTimers = () => {
  if (mainTimer) clearInterval(mainTimer)
  if (simEntryTimer) clearTimeout(simEntryTimer)
  if (simReadyTimer) clearTimeout(simReadyTimer)
}

// 生命周期
onMounted(() => {
  // 替换原有的直接 startTimer，改为先校验
  initializeRoom()
})

onUnmounted(() => {
  clearAllTimers()
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
