<template>
  <div
    class="h-screen w-full bg-[#050505] text-white flex flex-col relative overflow-hidden select-none"
  >
    <ArenaDialog
      v-model="escapeAlert.show"
      :title="isMatchMode ? '对局已中止' : '房间已解散'"
      :cancel-text="isMatchMode ? '回到主页' : '重新创建'"
      :confirm-text="isMatchMode ? '继续匹配' : '回到主页'"
      @cancel="isMatchMode ? forceGoHome() : handleRecreate()"
      @confirm="isMatchMode ? handleContinueMatch() : forceGoHome()"
    >
      <div class="text-center flex flex-col items-center">
        <AlertTriangle class="w-12 h-12 text-red-500/80 mb-4" />
        <p class="text-zinc-300 font-medium mb-6 text-base">{{ escapeAlert.message }}</p>

        <div
          v-if="isMatchMode && escapeAlert.scoreDetail"
          class="inline-flex items-center gap-3 px-4 py-2.5 rounded-lg bg-zinc-900/80 border border-white/5 mb-2"
        >
          <span class="text-xs text-zinc-500 tracking-widest">排位结算</span>
          <div class="w-[1px] h-3 bg-zinc-700"></div>
          <span
            class="text-base font-mono font-bold"
            :class="escapeAlert.scoreDetail.includes('-') ? 'text-red-400' : 'text-zinc-400'"
          >
            {{ escapeAlert.scoreDetail }}
          </span>
        </div>
      </div>
    </ArenaDialog>

    <div
      v-if="isVerifying"
      class="absolute inset-0 z-50 bg-[#050505] flex flex-col items-center justify-center"
    >
      <Loader2 class="w-10 h-10 text-[#FF4C00] animate-spin mb-4" />
      <p class="text-zinc-500 font-mono tracking-[0.2em] animate-pulse">正在核验安全凭证...</p>
    </div>

    <header
      class="h-16 flex items-center px-4 border-b border-white/5 bg-zinc-950/80 backdrop-blur-md relative z-20"
    >
      <div class="flex-1 flex items-center">
        <ArenaExitButton @click="handleLeaveRoom" />
      </div>

      <div
        v-if="isMatchMode"
        class="absolute left-1/2 -translate-x-1/2 flex items-center gap-3 px-5 py-1.5 rounded-full backdrop-blur-xl border shadow-lg transition-all duration-500 bg-red-500/10 border-red-500/50 shadow-red-500/20 animate-pulse cursor-default"
      >
        <Timer class="w-4 h-4 text-red-500" />
        <span class="font-mono text-base font-bold tracking-widest tabular-nums text-red-500">
          00:{{ timeLeft.toString().padStart(2, '0') }}
        </span>
        <div class="w-[1px] h-3 bg-white/20"></div>
        <span class="text-xs font-bold tracking-wider text-red-500">准备倒计时</span>
      </div>

      <div
        v-else
        class="absolute left-1/2 -translate-x-1/2 flex items-center gap-3 bg-black/40 px-4 py-1.5 rounded-full border border-white/5 hover:border-[#FF4C00]/30 transition-colors group cursor-default shadow-sm"
      >
        <span class="text-zinc-500 text-[10px] font-bold tracking-widest uppercase">Room</span>
        <div class="w-[1px] h-3 bg-zinc-800"></div>
        <span class="text-white font-mono font-bold tracking-widest text-sm">{{ roomCode }}</span>

        <button
          @click="copy(roomCode)"
          class="p-1.5 rounded-md hover:bg-white/10 transition-colors focus:outline-none relative"
          title="复制房间号"
        >
          <Check
            v-if="copied"
            class="w-3.5 h-3.5 text-emerald-500 animate-in zoom-in duration-200"
          />
          <Copy
            v-else
            class="w-3.5 h-3.5 text-zinc-400 group-hover:text-white transition-colors animate-in zoom-in duration-200"
          />
        </button>
      </div>

      <div class="flex-1"></div>
    </header>

    <div class="flex-1 flex items-center justify-center relative z-10 px-8">
      <div
        class="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 flex flex-col items-center z-30"
      >
        <div
          class="w-[1px] h-32 bg-gradient-to-b from-transparent via-[#FF4C00]/50 to-transparent mb-4"
        ></div>
        <div
          class="w-16 h-16 rounded-full bg-zinc-950 border border-white/10 flex items-center justify-center shadow-[0_0_30px_rgba(255,76,0,0.15)]"
        >
          <Swords class="w-6 h-6 text-[#FF4C00]" />
        </div>
        <div
          class="w-[1px] h-32 bg-gradient-to-t from-transparent via-[#FF4C00]/50 to-transparent mt-4"
        ></div>
      </div>

      <div class="w-full max-w-6xl flex items-center justify-between gap-12">
        <div
          class="flex-1 flex flex-col items-center transform transition-transform duration-500 hover:scale-105"
        >
          <div class="relative w-48 h-48 mb-6">
            <div
              class="absolute inset-0 rounded-full border-4 transition-colors duration-300"
              :class="
                myStatus.isReady
                  ? 'border-emerald-500 shadow-[0_0_40px_rgba(16,185,129,0.3)]'
                  : 'border-zinc-800'
              "
            ></div>
            <img
              :src="userStore.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=Me'"
              class="w-full h-full rounded-full object-cover p-2 bg-zinc-950"
            />
            <div
              v-if="myStatus.isReady"
              class="absolute bottom-2 right-2 w-8 h-8 rounded-full bg-emerald-500 border-4 border-zinc-950 flex items-center justify-center"
            >
              <Check class="w-4 h-4 text-white" />
            </div>
          </div>
          <h3 class="text-2xl font-bold tracking-tight mb-2">
            {{ userStore.nickname || '我方特工' }}
          </h3>
          <p class="text-zinc-500 font-mono text-xs tracking-widest uppercase">
            {{ myStatus.isReady ? '已准备' : '待命中' }}
          </p>
        </div>

        <div
          class="flex-1 flex flex-col items-center transform transition-transform duration-500 hover:scale-105"
        >
          <div class="relative w-48 h-48 mb-6">
            <div
              class="absolute inset-0 rounded-full border-4 transition-colors duration-300"
              :class="
                !opponent
                  ? 'border-dashed border-zinc-800 animate-spin-slow'
                  : opponent.isReady
                    ? 'border-red-500 shadow-[0_0_40px_rgba(239,68,68,0.3)]'
                    : 'border-zinc-800'
              "
            ></div>

            <template v-if="opponent">
              <img
                :src="opponent.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=Enemy'"
                class="w-full h-full rounded-full object-cover p-2 bg-zinc-950"
              />
              <div
                v-if="opponent.isReady"
                class="absolute bottom-2 left-2 w-8 h-8 rounded-full bg-red-500 border-4 border-zinc-950 flex items-center justify-center"
              >
                <Check class="w-4 h-4 text-white" />
              </div>
            </template>
            <div
              v-else
              class="w-full h-full flex items-center justify-center rounded-full p-2 bg-zinc-950"
            >
              <UserMinus class="w-12 h-12 text-zinc-800" />
            </div>
          </div>

          <h3
            class="text-2xl font-bold tracking-tight mb-2"
            :class="opponent ? 'text-white' : 'text-zinc-600'"
          >
            {{ opponent ? opponent.name : '等待对手接入...' }}
          </h3>
          <p class="text-zinc-500 font-mono text-xs tracking-widest uppercase">
            <span v-if="!opponent" class="animate-pulse">正在扫描网络...</span>
            <span v-else>{{ opponent.isReady ? '已准备' : '待命中' }}</span>
          </p>
        </div>
      </div>
    </div>

    <div
      class="h-32 shrink-0 flex items-center justify-center relative z-20 border-t border-white/5 bg-zinc-950/80 backdrop-blur-md"
    >
      <button
        @click="toggleReady"
        :disabled="!opponent"
        class="relative px-16 py-4 rounded-xl font-bold text-lg tracking-widest uppercase transition-all duration-300 overflow-hidden group disabled:opacity-40 disabled:cursor-not-allowed"
        :class="
          myStatus.isReady
            ? 'bg-zinc-800 text-zinc-300 border border-white/10'
            : 'bg-[#FF4C00] text-white shadow-[0_0_20px_rgba(255,76,0,0.4)] hover:shadow-[0_0_40px_rgba(255,76,0,0.6)]'
        "
      >
        <span class="relative z-10 flex items-center gap-2">
          <Power class="w-5 h-5" />
          {{ myStatus.isReady ? '取消准备' : '准备战斗' }}
        </span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  Swords,
  UserMinus,
  Check,
  Power,
  Loader2,
  AlertTriangle,
  Timer,
  Copy,
} from 'lucide-vue-next'
import { useClipboard } from '@vueuse/core'
import ArenaExitButton from '@/components/arena/ArenaExitButton.vue'
import ArenaDialog from '@/components/arena/ArenaDialog.vue'
import { useUserStore } from '@/stores/user'
import { checkRoomValidity } from '@/api/arena'
import { BattleWebSocket } from '@/utils/battle-ws'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const roomCode = ref(route.params.roomId as string)
const ticket = ref(route.query.ticket as string)

const { copy, copied } = useClipboard({ legacy: true })

const isVerifying = ref(true)

// 标识当前是否是天梯匹配模式
const isMatchMode = ref(false)

// --- 倒计时核心逻辑 ---
const timeLeft = ref(15)
let countdownTimer: number | null = null

const startTimer = () => {
  countdownTimer = window.setInterval(() => {
    if (timeLeft.value > 0) {
      timeLeft.value--
    } else {
      handleTimeUp()
    }
  }, 1000)
}

const handleTimeUp = () => {
  if (countdownTimer) clearInterval(countdownTimer)

  if (myStatus.isReady && opponent.value?.isReady) {
    return // 双方都已准备，等后端发 GAME_START 即可
  }

  // 这里的倒计时逻辑仅存在于排位赛模式下，因此直接使用排位的硬编码扣分提醒即可
  if (!myStatus.isReady && (!opponent.value || !opponent.value.isReady)) {
    escapeAlert.message = '倒计时结束，双方均未完成准备，房间已销毁'
    escapeAlert.scoreDetail = '无扣分'
    escapeAlert.show = true
    if (battleWs) battleWs.disconnect()
  } else if (!myStatus.isReady && opponent.value?.isReady) {
    if (battleWs) battleWs.send('ESCAPE_LOBBY') // 发送逃跑指令给后端扣分
    escapeAlert.message = '未在规定时间内准备，被系统判定为逃跑！'
    escapeAlert.scoreDetail = '-20'
    escapeAlert.show = true
  } else if (myStatus.isReady && (!opponent.value || !opponent.value.isReady)) {
    escapeAlert.message = '对手未在规定时间内准备，房间已销毁'
    escapeAlert.scoreDetail = '无扣分 (对方扣除 20 分)'
    escapeAlert.show = true
    if (battleWs) battleWs.disconnect()
  }
}
// ----------------------

const myStatus = reactive({
  isReady: false,
})

const opponent = ref<any>(null)
let battleWs: BattleWebSocket | null = null

// 逃跑警告状态
const escapeAlert = reactive({
  show: false,
  message: '',
  scoreDetail: '',
})

const initLobby = async () => {
  if (!roomCode.value) {
    router.replace('/arena')
    return
  }

  try {
    const validity = await checkRoomValidity(roomCode.value, ticket.value)

    if (!validity.isValid) {
      alert(validity.message || '房间已失效或门票无效')
      router.replace('/arena')
      return
    }

    // 判断是天梯还是私人房间
    isMatchMode.value = validity.roomType === 3 || !!ticket.value

    isVerifying.value = false
    setupWebSocket()

    if (isMatchMode.value) {
      startTimer()
    }
  } catch (err) {
    console.error('房间校验异常:', err)
    router.replace('/arena')
  }
}

const setupWebSocket = () => {
  const token = (userStore as any).token || userStore.token
  if (!token) {
    router.replace('/login')
    return
  }

  battleWs = new BattleWebSocket(roomCode.value, token)

  battleWs.on('SYNC_ROOM', (data: any) => {
    const enemy = data.players?.find(
      (p: any) => String(p.userId) !== String(userStore.userInfo?.id),
    )
    if (enemy) {
      opponent.value = {
        id: enemy.userId,
        name: enemy.nickname || enemy.userName || 'Unknown Player',
        avatar: enemy.avatarUrl || enemy.userAvatar,
        isReady: enemy.isReady,
      }
    }
  })

  battleWs.on('PLAYER_JOINED', (data: any) => {
    if (String(data.userId) !== String(userStore.userInfo?.id)) {
      opponent.value = {
        id: data.userId,
        name: data.nickname || data.userName,
        avatar: data.avatarUrl || data.userAvatar,
        isReady: false,
      }
    }
  })

  battleWs.on('PLAYER_READY', (data: any) => {
    if (String(data.userId) === String(userStore.userInfo?.id)) {
      myStatus.isReady = data.isReady
    } else if (opponent.value && String(opponent.value.id) === String(data.userId)) {
      opponent.value.isReady = data.isReady
    }
  })

  battleWs.on('GAME_START', (data: any) => {
    if (countdownTimer) clearInterval(countdownTimer)
    setTimeout(() => {
      router.replace({
        path: `/battle/room/${roomCode.value}`,
        query: {
          problemId: data.problemId,
          startTime: data.startTime,
        },
      })
    }, 1000)
  })

  // 🎯 核心修复：根据接收到的后端 isRanked 标识，切分逃跑/解散弹窗文案
  battleWs.on('PLAYER_ESCAPED', (data: any) => {
    if (countdownTimer) clearInterval(countdownTimer) // 只要有人退出，立即停止倒计时

    const myId = userStore.userInfo?.id || (userStore as any).id || (userStore as any).userId
    const isMe = String(data.escapeeId) === String(myId)
    const isRanked = data.isRanked !== undefined ? data.isRanked : isMatchMode.value

    if (isRanked) {
      // 天梯排位模式下的逃跑文案与扣分显示
      const deducted = data.deductedScore || 20
      if (isMe) {
        escapeAlert.message = '您已被系统判定为主动断线退出！'
        escapeAlert.scoreDetail = `-${deducted}`
      } else {
        escapeAlert.message = '对手落荒而逃，系统已将其裁定为战败！'
        escapeAlert.scoreDetail = `对方 -${deducted}`
      }
    } else {
      // 私有建房模式下的离开/解散文案 (无扣分)
      if (isMe) {
        escapeAlert.message = '您主动退出了房间！'
      } else {
        escapeAlert.message = '好友已离开，房间已解散！'
      }
      escapeAlert.scoreDetail = ''
    }

    escapeAlert.show = true
  })

  battleWs.connect()
}

const toggleReady = () => {
  if (!battleWs) return
  battleWs.send('READY', { isReady: !myStatus.isReady })
}

const handleLeaveRoom = () => {
  // 🎯 核心修复：无论是排位还是私有房间，只要有对手存在，离开都应触发弹窗和 WS 通知
  // 没有对手的情况下可以直接退出不用管
  if (opponent.value) {
    if (battleWs) battleWs.send('ESCAPE_LOBBY')

    if (isMatchMode.value) {
      escapeAlert.message = '您主动退出了备战大厅！'
      escapeAlert.scoreDetail = `-20`
    } else {
      escapeAlert.message = '您主动退出了房间！'
      escapeAlert.scoreDetail = ''
    }
    escapeAlert.show = true
  } else {
    // 房间里只有自己，直接走人
    if (battleWs) battleWs.send('ESCAPE_LOBBY')
    forceGoHome()
  }
}

// 事件：点击“继续匹配” (仅排位可见)
const handleContinueMatch = () => {
  escapeAlert.show = false
  if (battleWs) battleWs.disconnect()
  router.replace('/battle/matchmaking')
}

// 🎯 事件：点击“重新创建” (仅私有房间可见)
const handleRecreate = () => {
  escapeAlert.show = false
  if (battleWs) battleWs.disconnect()
  // 携带有 action 标识，路由回 arena 大厅，可以在大厅侦听此参数自动呼出建房弹窗
  router.replace({ path: '/arena', query: { action: 'create' } })
}

// 事件：点击“回到主页” 或 点击对话框外部遮罩层
const forceGoHome = () => {
  if (battleWs) battleWs.disconnect()
  router.replace('/arena')
}

onMounted(() => {
  initLobby()
})

onBeforeUnmount(() => {
  if (countdownTimer) clearInterval(countdownTimer)
  if (battleWs) {
    battleWs.disconnect()
  }
})
</script>

<style scoped>
.animate-spin-slow {
  animation: spin 8s linear infinite;
}
</style>
