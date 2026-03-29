<template>
  <div
    class="relative w-full h-full flex flex-col items-center justify-center py-12 overflow-hidden"
  >
    <div
      class="absolute inset-0 bg-grid-black/[0.02] dark:bg-grid-white/[0.02] pointer-events-none"
    ></div>
    <div
      class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[500px] h-[500px] bg-[#FF4C00]/5 blur-[100px] rounded-full pointer-events-none"
    ></div>

    <div class="relative z-10 text-center mb-12" v-motion-slide-visible-top>
      <h1
        class="text-4xl md:text-5xl font-black italic tracking-tighter text-zinc-900 dark:text-white mb-3"
      >
        零号竞技场 <span class="text-[#FF4C00]">ZERO</span>
      </h1>
      <p class="text-zinc-500 font-medium tracking-wide">打破孤岛 · 实时代码竞技场</p>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 relative z-10 px-6 max-w-6xl w-full">
      <TiltCard
        v-for="card in cards"
        :key="card.type"
        v-bind="card"
        @click="triggerAction(card.type as ActionType)"
      />
    </div>

    <ArenaDialog
      v-model="showDialog"
      :title="dialogConfig.title"
      :confirm-text="dialogConfig.btnText"
      :confirm-disabled="currentAction === 'JOIN' && otpString.length !== 6 && !isErrorAlert"
      @confirm="handleConfirm"
    >
      <div v-if="currentAction !== 'JOIN' || isErrorAlert" class="text-center">
        <div
          class="w-12 h-12 mx-auto rounded-full flex items-center justify-center mb-6 border"
          :class="
            isErrorAlert
              ? 'bg-red-500/10 border-red-500/20'
              : currentAction === 'MATCH'
                ? 'bg-purple-500/10 border-purple-500/20'
                : 'bg-[#FF4C00]/10 border-[#FF4C00]/20'
          "
        >
          <component
            :is="dialogConfig.icon"
            class="w-6 h-6"
            :class="
              isErrorAlert
                ? 'text-red-500'
                : currentAction === 'MATCH'
                  ? 'text-purple-500'
                  : 'text-[#FF4C00]'
            "
          />
        </div>
        <p class="text-zinc-400 text-sm leading-relaxed" v-html="dialogConfig.desc"></p>
      </div>

      <div v-else class="flex flex-col items-center">
        <p class="text-zinc-500 text-xs mb-6 uppercase tracking-widest">请输入通行密钥</p>
        <div class="flex justify-center gap-3">
          <input
            v-for="(digit, index) in 6"
            :key="index"
            ref="otpInputs"
            v-model="otpCode[index]"
            type="text"
            maxlength="1"
            class="w-10 h-12 md:w-12 md:h-14 bg-zinc-900 border border-zinc-700 rounded-lg text-center text-xl md:text-2xl font-mono font-bold text-white focus:border-[#FF4C00] focus:ring-1 focus:ring-[#FF4C00] outline-none transition-all caret-[#FF4C00] shadow-inner"
            @input="handleInput($event, index)"
            @keydown.delete="handleDelete($event, index)"
            @paste="handlePaste"
          />
        </div>
      </div>
    </ArenaDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Swords, Ticket, Zap, Maximize2, Radio, AlertCircle, XCircle, Info } from 'lucide-vue-next'
import ArenaDialog from '@/components/arena/ArenaDialog.vue'
import TiltCard from '@/components/arena/TiltCard.vue'
import { storage } from '@/utils/storage'

// 引入真实的后端 API
import { createRoom, joinRoom } from '@/api/arena'

const router = useRouter()
const route = useRoute()

// Cards Data
const cards = [
  {
    type: 'CREATE',
    title: '创建房间',
    desc: '生成专属邀请码<br />等待好友挑战',
    icon: Swords,
    footerText: '立即创建',
    containerClass:
      'bg-gradient-to-b from-white/[0.03] to-transparent border-white/10 hover:border-[#FF4C00]/50 shadow-[0_0_15px_rgba(255,255,255,0.03)] hover:shadow-[0_0_50px_rgba(255,76,0,0.3)]',
    iconBgClass: '',
    iconClass: 'text-zinc-400 group-hover:text-[#FF4C00]',
  },
  {
    type: 'MATCH',
    title: '排位匹配',
    desc: '寻找旗鼓相当的对手',
    icon: Zap,
    footerText: '开始匹配',
    containerClass:
      'bg-gradient-to-b from-purple-500/[0.05] to-transparent border-purple-500/30 hover:border-purple-500 shadow-[0_0_20px_rgba(168,85,247,0.1)] hover:shadow-[0_0_60px_rgba(168,85,247,0.4)]',
    iconBgClass: 'bg-purple-500/10 border-purple-500/20',
    iconClass: 'text-purple-500',
  },
  {
    type: 'JOIN',
    title: '加入房间',
    desc: '输入 6 位邀请码<br />连接至现有对局',
    icon: Ticket,
    footerText: '立即加入',
    containerClass:
      'bg-gradient-to-b from-white/[0.03] to-transparent border-white/10 hover:border-[#FF4C00]/50 shadow-[0_0_15px_rgba(255,255,255,0.03)] hover:shadow-[0_0_50px_rgba(255,76,0,0.3)]',
    iconBgClass: '',
    iconClass: 'text-zinc-400 group-hover:text-[#FF4C00]',
  },
]

// Dialog Logic
type ActionType = 'CREATE' | 'MATCH' | 'JOIN'
const currentAction = ref<ActionType>('CREATE')
const showDialog = ref(false)
const otpCode = ref<string[]>(new Array(6).fill(''))
const otpInputs = ref<HTMLInputElement[]>([])
const otpString = computed(() => otpCode.value.join(''))

// 请求防抖/加载状态
const isSubmitting = ref(false)

// 错误状态存储
const isErrorAlert = ref(false)
const currentErrorData = ref<{ title: string; desc: string; icon: any } | null>(null)

const triggerAction = (type: ActionType) => {
  // 每次手动触发操作时，重置错误状态
  isErrorAlert.value = false
  currentErrorData.value = null

  currentAction.value = type
  if (type === 'JOIN') {
    otpCode.value = new Array(6).fill('')
    nextTick(() => otpInputs.value[0]?.focus())
  }
  showDialog.value = true
}

const dialogConfig = computed(() => {
  if (isErrorAlert.value && currentErrorData.value) {
    return {
      title: currentErrorData.value.title,
      desc: currentErrorData.value.desc,
      btnText: '我知道了',
      icon: currentErrorData.value.icon,
    }
  }

  if (currentAction.value === 'MATCH')
    return {
      title: '排位匹配确认',
      desc: '即将进入天梯匹配队列，预计等待时间 <span class="text-white font-bold">15s</span>。<br>系统将根据您的 Elo 分数寻找旗鼓相当的对手。',
      btnText: '确认开始',
      icon: Radio,
    }
  else if (currentAction.value === 'JOIN')
    return {
      title: '加入房间',
      desc: '',
      btnText: isSubmitting.value ? '加入中...' : '验证并加入',
      icon: Ticket,
    }
  else
    return {
      title: '即将进入沉浸战场',
      desc: '您即将离开控制台，进入隔离沙箱环境。<br>侧边栏将被隐藏，以提供极致的专注编程体验。',
      btnText: isSubmitting.value ? '创建中...' : '确认创建并进入',
      icon: Maximize2,
    }
})

// 核心改造: 接入真实 API 处理确认动作
const handleConfirm = async () => {
  // 错误弹窗逻辑：点击仅关闭
  if (isErrorAlert.value) {
    showDialog.value = false
    isErrorAlert.value = false
    return
  }

  // 防止重复点击
  if (isSubmitting.value) return

  if (currentAction.value === 'MATCH') {
    storage.set('MATCH_PENDING', 'true', 'session')
    router.push('/battle/matchmaking')
  }
  // [架构师改造]: 加入房间前先调接口落盘
  else if (currentAction.value === 'JOIN') {
    if (otpString.value.length === 6) {
      try {
        isSubmitting.value = true
        // 1. 调用新增的加入房间 API
        await joinRoom(otpString.value)

        // 2. 数据库落盘成功后，再跳转大厅
        router.push(`/battle/lobby/${otpString.value}`)
        showDialog.value = false
      } catch (error: any) {
        // 如果后端拦截（如人满、密码错、房间不存在），直接在当前面板爆红
        isErrorAlert.value = true
        currentErrorData.value = {
          title: '加入房间失败',
          desc: error.message || '房间不存在或已满，请检查邀请码',
          icon: AlertCircle,
        }
        console.error('[Arena] Join room failed:', error)
      } finally {
        isSubmitting.value = false
      }
    }
  } else if (currentAction.value === 'CREATE') {
    try {
      isSubmitting.value = true
      // 传递 roomType 和 type 防止后端字段差异导致400
      const res = await createRoom({ roomType: 1, type: 1 })

      router.push({
        name: 'ArenaLobby',
        params: { roomId: res.roomCode },
        query: { mode: 'CUSTOM' },
      })
      showDialog.value = false
    } catch (error: any) {
      isErrorAlert.value = true
      currentErrorData.value = {
        title: '创建房间失败',
        desc: error.message || '神经连接遭到后端拒绝，请检查日志',
        icon: AlertCircle,
      }
      console.error('[Arena] Create room failed:', error)
    } finally {
      isSubmitting.value = false
    }
  }
}

const handleInput = (e: Event, index: number) => {
  const val = (e.target as HTMLInputElement).value
  if (val && index < 5) otpInputs.value[index + 1]?.focus()
}
const handleDelete = (e: KeyboardEvent, index: number) => {
  if (!otpCode.value[index] && index > 0) otpInputs.value[index - 1]?.focus()
}
const handlePaste = (e: ClipboardEvent) => {
  e.preventDefault()
  const text = e.clipboardData?.getData('text') || ''
  if (!text) return
  const chars = text.slice(0, 6).split('')
  chars.forEach((char, i) => {
    otpCode.value[i] = char
  })
  if (chars.length === 6) otpInputs.value[5]?.focus()
}

// --- 路由错误反馈逻辑 ---
const errorMap: Record<string, { title: string; desc: string; icon: any }> = {
  MATCH_INVALID: {
    title: '匹配会话失效',
    desc: '系统未找到有效的匹配请求，或当前匹配因超时已自动取消。',
    icon: XCircle,
  },
  ROOM_INVALID: {
    title: '无法加入房间',
    desc: '目标房间已解散、已开始游戏，或者您输入的房间号有误。',
    icon: AlertCircle,
  },
  BATTLE_ENDED: {
    title: '对战已结束',
    desc: '本次对战记录已归档，为了保证竞赛公平性，无法通过历史链接重新连接。',
    icon: Info,
  },
}

onMounted(() => {
  const error = route.query.error as string

  if (error && errorMap[error]) {
    currentErrorData.value = errorMap[error]
    isErrorAlert.value = true
    showDialog.value = true

    // 清除 URL 参数
    router.replace({ path: '/arena', query: {} })
  }
})
</script>

<style scoped>
.bg-grid-black {
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 32 32' width='32' height='32' fill='none' stroke='rgba(0, 0, 0, 0.05)'%3e%3cpath d='M0 .5H31.5V32' stroke-width='1' /%3e%3c/svg%3e");
}
.dark .bg-grid-white {
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 32 32' width='32' height='32' fill='none' stroke='rgba(255, 255, 255, 0.05)'%3e%3cpath d='M0 .5H31.5V32' stroke-width='1' /%3e%3c/svg%3e");
}
</style>
