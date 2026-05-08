<template>
  <div
    class="h-full flex flex-col bg-white dark:bg-zinc-950 border-l border-zinc-200 dark:border-white/5 overflow-hidden select-none relative"
  >
    <div
      class="shrink-0 p-4 space-y-3 border-b border-zinc-200 dark:border-white/5 relative z-20 bg-white dark:bg-zinc-950"
    >
      <div
        v-for="player in players"
        :key="player.id"
        class="flex items-center justify-between p-3 rounded-xl bg-zinc-100 dark:bg-zinc-900/30 border border-zinc-200 dark:border-white/5 backdrop-blur-md transition-all duration-300 hover:bg-zinc-200 dark:hover:bg-zinc-900/50 gap-2"
        :class="{ 'ring-1 ring-amber-400/50 bg-amber-400/10': player.status === 'PASSED' }"
      >
        <div class="flex items-center gap-4 min-w-0">
          <div class="relative shrink-0 w-11 h-11 flex items-center justify-center">
            <div
              v-if="player.status === 'IDLE'"
              class="absolute inset-0 rounded-full border-[2.5px] border-white/50 shadow-[0_0_12px_rgba(255,255,255,0.2)] animate-idle-breath"
            ></div>

            <div
              v-else-if="player.status === 'TYPING'"
              class="absolute inset-0 rounded-full border-[2.5px] border-white shadow-[0_0_20px_rgba(255,255,255,0.6)] animate-typing-pulse"
              :style="{ animationDuration: getTypingDuration(player.cpm) }"
            ></div>

            <div
              v-else-if="player.status === 'PING'"
              class="absolute inset-0 rounded-full border-2 border-blue-400 shadow-[0_0_20px_#60a5fa] animate-ping-flash"
            ></div>
            <svg
              v-else-if="player.status === 'RUNNING_TESTS'"
              class="absolute inset-0 w-full h-full animate-spin-slow text-yellow-500"
              viewBox="0 0 100 100"
            >
              <circle
                cx="50"
                cy="50"
                r="48"
                fill="none"
                stroke="currentColor"
                stroke-width="4"
                stroke-dasharray="60 140"
                stroke-linecap="round"
              />
              <circle
                cx="50"
                cy="50"
                r="48"
                fill="none"
                stroke="currentColor"
                stroke-width="4"
                stroke-dasharray="20 280"
                stroke-linecap="round"
                class="opacity-50"
              />
            </svg>
            <div
              v-else-if="player.status === 'TEST_FAILED'"
              class="absolute inset-0 rounded-full border-2 border-orange-500 animate-fail-flash"
            ></div>
            <div
              v-else-if="player.status === 'TEST_PASSED'"
              class="absolute inset-0 rounded-full border-2 border-emerald-500 shadow-[0_0_15px_#10b981] animate-pass-glow"
            ></div>
            <div
              v-else-if="player.status === 'SUBMITTING'"
              class="absolute inset-0 rounded-full border-2 border-purple-400 shadow-[0_0_20px_#c084fc] animate-submit-flash"
            ></div>
            <div
              v-else-if="player.status === 'SUBMIT_FAILED'"
              class="absolute inset-0 rounded-full border-2 border-red-500 shadow-[0_0_25px_#ef4444] animate-err-burst"
            ></div>
            <div
              v-else-if="player.status === 'PASSED'"
              class="absolute inset-0 rounded-full border-2 border-[#FFb700] shadow-[0_0_30px_#FFb700] animate-ac-burst"
            ></div>

            <img
              :src="player.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${player.id}`"
              alt="avatar"
              class="w-9 h-9 rounded-full bg-zinc-100 dark:bg-zinc-950 border border-zinc-300 dark:border-white/10 object-cover relative z-10 transition-transform duration-300"
              :class="{
                'animate-ac-shake': player.status === 'PASSED',
                'animate-err-shake': player.status === 'SUBMIT_FAILED',
              }"
            />
          </div>

          <div class="flex flex-col min-w-0">
            <span
              class="text-xs font-bold text-zinc-700 dark:text-zinc-300 truncate"
              :class="{ 'text-amber-500 dark:text-amber-400': player.status === 'PASSED' }"
            >
              {{ player.name }}
            </span>
            <span
              class="text-[10px] text-zinc-500 font-mono tracking-wider flex items-center gap-1"
            >
              {{ player.isMe ? '我方' : '敌方' }}
              <span v-if="player.status === 'PASSED'" class="text-amber-400 text-[9px] font-bold"
                >· AC</span
              >
            </span>
          </div>
        </div>

        <div
          class="flex flex-col items-end font-mono text-[10px] tracking-widest text-right shrink-0"
        >
          <span class="text-zinc-500"
            >代码: <span class="text-zinc-300">{{ formatNumber(player.loc) }}</span> 行</span
          >
          <span class="text-zinc-500 transition-colors">
            手速: <span class="text-zinc-300">{{ formatNumber(player.cpm) }}</span> 次/分
          </span>
        </div>
      </div>
    </div>

    <div
      class="flex-1 flex flex-col min-h-0 bg-zinc-50 dark:bg-[#050505] relative z-10 overflow-hidden"
    >
      <Transition name="holo">
        <div
          v-if="activeHolo"
          class="absolute inset-0 z-50 pointer-events-none flex items-center justify-center overflow-hidden bg-zinc-100/80 dark:bg-black/60 backdrop-blur-[2px]"
        >
          <div class="flex flex-col items-center justify-center animate-holo-float">
            <component
              :is="activeHolo.config.icon"
              class="w-20 h-20 mb-4 opacity-90 dark:mix-blend-screen filter drop-shadow-[0_0_25px_currentColor]"
              :class="activeHolo.config.color"
            />
            <span
              class="font-mono font-bold text-sm tracking-widest text-transparent bg-clip-text bg-gradient-to-t dark:to-white to-zinc-900"
              :class="activeHolo.config.gradientFrom"
            >
              {{ activeHolo.isMe ? activeHolo.config.myHolo : activeHolo.config.enemyHolo }}
            </span>
            <div
              class="mt-3 text-[10px] text-zinc-500 dark:text-zinc-400 font-mono tracking-widest animate-pulse"
            >
              {{ activeHolo.isMe ? '正在发送战术信号...' : '正在接收战术信号...' }}
            </div>
          </div>
          <div
            class="absolute inset-0 bg-scanline opacity-20 dark:opacity-30 pointer-events-none"
          ></div>
        </div>
      </Transition>

      <div
        class="px-4 py-2 shrink-0 border-b border-zinc-200 dark:border-white/5 flex items-center justify-between relative z-20"
      >
        <span class="text-[10px] text-zinc-500 font-mono tracking-widest">>>_ 战况日志终端</span>
        <div
          class="w-1.5 h-1.5 bg-[#FF4C00] rounded-full animate-pulse shadow-[0_0_5px_rgba(255,76,0,0.5)]"
        ></div>
      </div>

      <div
        ref="logContainer"
        class="flex-1 overflow-y-auto no-scrollbar p-4 flex flex-col gap-3 relative z-20"
      >
        <TransitionGroup name="list">
          <div v-for="log in logs" :key="log.id" class="flex items-start gap-2 group">
            <span class="text-[10px] text-zinc-700 font-mono shrink-0 mt-0.5">
              [{{ log.timestamp }}]
            </span>
            <div
              class="flex flex-col gap-0.5 text-xs font-mono tracking-tight leading-relaxed transition-colors duration-200"
              :class="getLogColor(log.prefix)"
            >
              <div class="flex items-start gap-1.5">
                <span class="font-bold opacity-80 shrink-0">[{{ getPrefixText(log.prefix) }}]</span>
                <span :class="{ 'animate-err-shake inline-block': log.prefix === 'ERR ' }">
                  {{ log.message }}
                </span>
              </div>
            </div>
          </div>
        </TransitionGroup>
      </div>
    </div>

    <div
      class="h-[60px] shrink-0 border-t border-zinc-200 dark:border-white/5 bg-white dark:bg-zinc-950 flex items-center justify-center gap-6 px-4 relative z-10"
    >
      <button
        v-for="(ping, index) in tacticalPings"
        :key="index"
        @click="emitPing(ping)"
        :disabled="cdTimer > 0 || isBattleEnded"
        :title="ping.label"
        class="relative p-3 rounded-xl bg-zinc-100 dark:bg-zinc-900/80 border border-zinc-200 dark:border-white/5 text-zinc-500 dark:text-zinc-400 transition-all duration-300 flex items-center justify-center group focus:outline-none"
        :class="
          cdTimer > 0 || isBattleEnded
            ? 'opacity-40 cursor-not-allowed'
            : 'hover:text-zinc-900 dark:hover:text-white hover:border-[#FF4C00]/40 hover:bg-[#FF4C00]/10 hover:shadow-[0_0_15px_rgba(255,76,0,0.15)] active:scale-95'
        "
      >
        <component
          :is="ping.icon"
          class="w-5 h-5 transition-transform duration-300"
          :class="{ 'group-hover:scale-110': cdTimer === 0 && !isBattleEnded }"
        />
        <div
          v-if="cdTimer > 0"
          class="absolute inset-0 flex items-center justify-center font-mono text-[10px] font-bold text-[#FF4C00] bg-black/60 rounded-xl"
        >
          {{ cdTimer }}s
        </div>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { useSessionStorage } from '@vueuse/core'
import { Bug, Coffee, Rocket } from 'lucide-vue-next'

type PlayerStatus =
  | 'IDLE'
  | 'TYPING'
  | 'PING'
  | 'RUNNING_TESTS'
  | 'TEST_FAILED'
  | 'TEST_PASSED'
  | 'SUBMITTING'
  | 'SUBMIT_FAILED'
  | 'PASSED'
type LogPrefix = 'INFO' | 'MSG ' | 'EXEC' | 'FAIL' | 'PASS' | 'SUB ' | 'ERR ' | ' AC '

export interface Player {
  id: string
  name: string
  avatar: string
  status: PlayerStatus
  loc: number
  cpm: number
  isMe: boolean
}

export interface BattleLog {
  id: number
  timestamp: string
  prefix: LogPrefix
  message: string
}

interface PingConfig {
  type: string
  icon: any
  label: string
  myLog: string
  enemyLog: string
  myHolo: string
  enemyHolo: string
  color: string
  gradientFrom: string
}

// 🐛 核心修复：接收外部传来的 isBattleEnded 属性
const props = defineProps<{
  players: Player[]
  logs: BattleLog[]
  roomCode: string
  isBattleEnded?: boolean
}>()

const emit = defineEmits<{
  (e: 'ping', type: string): void
}>()

const logContainer = ref<HTMLElement | null>(null)

const tacticalPings: PingConfig[] = [
  {
    type: 'bug',
    icon: Bug,
    label: '嘲讽: 祝你遇到 Bug',
    myLog: '我方向对方发送了一只 Bug 🐛',
    enemyLog: '对方向你发送了一只 Bug 🐛',
    myHolo: '🐛 我方向对方发送了一只 Bug',
    enemyHolo: '🐛 对方向你发送了一只 Bug',
    color: 'text-emerald-500',
    gradientFrom: 'from-emerald-500',
  },
  {
    type: 'coffee',
    icon: Coffee,
    label: '悠闲: 我去喝咖啡了',
    myLog: '我方向对方展示了悠闲的咖啡 ☕',
    enemyLog: '对方觉得你需要喝杯咖啡冷静一下 ☕',
    myHolo: '☕ 我方向对方展示了悠闲的咖啡',
    enemyHolo: '☕ 对方觉得你需要喝杯咖啡冷静一下',
    color: 'text-blue-400',
    gradientFrom: 'from-blue-400',
  },
  {
    type: 'rocket',
    icon: Rocket,
    label: '竞速: 准备提交起飞',
    myLog: '我方向对方暗示你快提交了 🚀',
    enemyLog: '对方向你暗示他快提交了 🚀',
    myHolo: '🚀 我方向对方暗示你快提交了',
    enemyHolo: '🚀 对方向你暗示他快提交了',
    color: 'text-[#FF4C00]',
    gradientFrom: 'from-[#FF4C00]',
  },
]

const cdUnlockTime = useSessionStorage<number>(`nexus_battle_ping_cd_${props.roomCode}`, 0)
const cdTimer = ref(0)
let cdInterval: number | null = null
const activeHolo = ref<{ config: PingConfig; isMe: boolean } | null>(null)
let holoTimeout: number | null = null

const updateCdTimer = () => {
  const now = Date.now()
  if (cdUnlockTime.value > now) {
    cdTimer.value = Math.ceil((cdUnlockTime.value - now) / 1000)
  } else {
    cdTimer.value = 0
    if (cdInterval) {
      clearInterval(cdInterval)
      cdInterval = null
    }
  }
}

onMounted(() => {
  updateCdTimer()
  if (cdTimer.value > 0) {
    cdInterval = window.setInterval(updateCdTimer, 1000)
  }
})

onUnmounted(() => {
  if (cdInterval) clearInterval(cdInterval)
  if (holoTimeout) clearTimeout(holoTimeout)
})

const getPrefixText = (prefix: LogPrefix) => {
  switch (prefix) {
    case 'INFO':
      return '系统'
    case 'MSG ':
      return '消息'
    case 'EXEC':
    case 'FAIL':
    case 'PASS':
      return '测试'
    case 'SUB ':
    case 'ERR ':
    case ' AC ':
      return '提交'
  }
}

const getLogColor = (prefix: LogPrefix) => {
  switch (prefix) {
    case 'INFO':
      return 'text-zinc-500'
    case 'MSG ':
      return 'text-blue-400'
    case 'EXEC':
      return 'text-yellow-500/80'
    case 'FAIL':
      return 'text-orange-500'
    case 'PASS':
      return 'text-emerald-500'
    case 'SUB ':
      return 'text-purple-400'
    case 'ERR ':
      return 'text-red-500'
    case ' AC ':
      return 'text-amber-400 font-bold drop-shadow-[0_0_8px_rgba(251,191,36,0.5)]'
  }
}

const getTypingDuration = (cpm: number) => {
  const minDuration = 0.4
  const maxDuration = 3.0
  const duration = minDuration + (maxDuration - minDuration) * Math.exp(-cpm / 350)
  return `${duration.toFixed(2)}s`
}

const formatNumber = (num: number) => num.toString().padStart(3, '0')
const formatTimeNum = (num: number) => num.toString().padStart(2, '0')

const showHologram = (config: PingConfig, isMe: boolean) => {
  if (holoTimeout) clearTimeout(holoTimeout)
  activeHolo.value = { config, isMe }

  holoTimeout = window.setTimeout(() => {
    activeHolo.value = null
  }, 3000)
}

const emitPing = (ping: PingConfig) => {
  // 🐛 双保险：在函数内部再次验证是否被冻结
  if (cdTimer.value > 0 || props.isBattleEnded) return

  cdUnlockTime.value = Date.now() + 20000
  updateCdTimer()

  if (!cdInterval) {
    cdInterval = window.setInterval(updateCdTimer, 1000)
  }

  emit('ping', ping.type)

  const now = new Date()
  const timeStr = `${formatTimeNum(now.getHours())}:${formatTimeNum(now.getMinutes())}:${formatTimeNum(now.getSeconds())}`
  props.logs.push({ id: Date.now(), timestamp: timeStr, prefix: 'MSG ', message: ping.myLog })

  showHologram(ping, true)
}

watch(
  () => props.logs.length,
  () => {
    nextTick(() => {
      if (logContainer.value) {
        logContainer.value.scrollTo({
          top: logContainer.value.scrollHeight,
          behavior: 'smooth',
        })
      }
    })
  },
)

watch(
  () => props.players[1]?.status,
  (newStatus) => {
    if (newStatus === 'PING') {
      nextTick(() => {
        const lastLog = props.logs[props.logs.length - 1]
        let config: PingConfig = tacticalPings[0]!
        if (lastLog?.message.includes('咖啡')) config = tacticalPings[1]!
        if (lastLog?.message.includes('起飞') || lastLog?.message.includes('提交'))
          config = tacticalPings[2]!

        showHologram(config, false)
      })
    }
  },
)
</script>

<style scoped>
/* 原有动画保持不变，这里省略以保持整洁 */
.no-scrollbar::-webkit-scrollbar {
  display: none;
}
.no-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
.list-enter-active,
.list-leave-active {
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.list-enter-from {
  opacity: 0;
  transform: translateY(15px);
}
.list-leave-to {
  opacity: 0;
  transform: translateY(-15px);
}
.animate-spin-slow {
  animation: spin 3s linear infinite;
}
@keyframes idle-breath {
  0%,
  100% {
    opacity: 0.4;
    transform: scale(1);
    box-shadow: 0 0 10px rgba(255, 255, 255, 0.1);
  }
  50% {
    opacity: 0.8;
    transform: scale(1.03);
    box-shadow: 0 0 20px rgba(255, 255, 255, 0.3);
  }
}
.animate-idle-breath {
  animation: idle-breath 3.5s ease-in-out infinite;
}
@keyframes typing-pulse {
  0%,
  100% {
    opacity: 0.5;
    transform: scale(1);
    box-shadow: 0 0 10px rgba(255, 255, 255, 0.2);
  }
  50% {
    opacity: 1;
    transform: scale(1.08);
    box-shadow: 0 0 25px rgba(255, 255, 255, 0.8);
  }
}
.animate-typing-pulse {
  animation-name: typing-pulse;
  animation-timing-function: ease-in-out;
  animation-iteration-count: infinite;
}
@keyframes ping-flash {
  0% {
    transform: scale(1);
    opacity: 1;
    box-shadow: 0 0 20px #60a5fa;
  }
  100% {
    transform: scale(1.5);
    opacity: 0;
    box-shadow: 0 0 0 #60a5fa;
  }
}
.animate-ping-flash {
  animation: ping-flash 1s cubic-bezier(0.1, 0.9, 0.2, 1) forwards;
}
@keyframes fail-flash {
  0%,
  100% {
    opacity: 0;
  }
  25%,
  75% {
    opacity: 1;
    box-shadow: 0 0 15px #f97316;
  }
  50% {
    opacity: 0;
  }
}
.animate-fail-flash {
  animation: fail-flash 0.8s ease-in-out;
}
@keyframes pass-glow {
  0%,
  100% {
    box-shadow: 0 0 5px #10b981;
  }
  50% {
    box-shadow: 0 0 20px #10b981;
  }
}
.animate-pass-glow {
  animation: pass-glow 3s ease-in-out forwards;
}
@keyframes submit-flash {
  0%,
  100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.3;
    transform: scale(1.02);
  }
}
.animate-submit-flash {
  animation: submit-flash 0.3s linear infinite;
}
@keyframes err-burst {
  0% {
    transform: scale(1);
    opacity: 1;
    box-shadow: 0 0 30px #ef4444;
  }
  100% {
    transform: scale(1.3);
    opacity: 0;
    box-shadow: 0 0 0 #ef4444;
  }
}
.animate-err-burst {
  animation: err-burst 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards;
}
@keyframes err-shake {
  0%,
  100% {
    transform: translateX(0);
  }
  20%,
  60% {
    transform: translateX(-4px) rotate(-2deg);
  }
  40%,
  80% {
    transform: translateX(4px) rotate(2deg);
  }
}
.animate-err-shake {
  animation: err-shake 0.4s ease-in-out;
}
@keyframes ac-burst {
  0% {
    transform: scale(0.8);
    opacity: 1;
    box-shadow: 0 0 10px #ffb700;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.8;
    box-shadow: 0 0 30px #ffb700;
  }
  100% {
    transform: scale(1);
    opacity: 1;
    box-shadow: 0 0 20px #ffb700;
  }
}
.animate-ac-burst {
  animation: ac-burst 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards;
}
@keyframes ac-shake {
  0% {
    transform: scale(1);
  }
  25% {
    transform: scale(1.15) rotate(-3deg);
  }
  50% {
    transform: scale(1.15) rotate(3deg);
  }
  75% {
    transform: scale(1.15) rotate(-3deg);
  }
  100% {
    transform: scale(1.1);
  }
}
.animate-ac-shake {
  animation: ac-shake 0.4s ease-in-out forwards;
}
.holo-enter-active,
.holo-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}
.holo-enter-from {
  opacity: 0;
  transform: scale(0.9) translateY(20px);
  filter: blur(10px);
}
.holo-leave-to {
  opacity: 0;
  transform: scale(1.1) translateY(-20px);
  filter: blur(10px);
}
@keyframes holo-float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}
.animate-holo-float {
  animation: holo-float 3s ease-in-out infinite;
}
.bg-scanline {
  background: linear-gradient(to bottom, transparent 50%, rgba(255, 255, 255, 0.05) 51%);
  background-size: 100% 4px;
}
</style>
