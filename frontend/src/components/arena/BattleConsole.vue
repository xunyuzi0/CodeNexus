<template>
  <div
    class="h-full flex flex-col bg-zinc-950 border-l border-white/5 overflow-hidden select-none relative"
  >
    <div class="shrink-0 p-4 space-y-3 border-b border-white/5 relative z-20 bg-zinc-950">
      <div
        v-for="player in players"
        :key="player.id"
        class="flex items-center justify-between p-3 rounded-xl bg-zinc-900/30 border border-white/5 backdrop-blur-md transition-all duration-300 hover:bg-zinc-900/50 gap-2"
        :class="{ 'ring-1 ring-amber-400/50 bg-amber-400/10': player.status === 'PASSED' }"
      >
        <div class="flex items-center gap-4 min-w-0">
          <div class="relative shrink-0 w-11 h-11 flex items-center justify-center">
            <div
              v-if="player.status === 'IDLE'"
              class="absolute inset-0 rounded-full border border-zinc-700/50 animate-idle-breath"
            ></div>

            <div
              v-else-if="player.status === 'TYPING'"
              class="absolute inset-0 rounded-full border border-white/60 transition-all duration-300"
              :class="{
                'border-blue-300/80 shadow-[0_0_15px_rgba(147,197,253,0.5)]': player.cpm > 300,
              }"
              :style="{ animation: `pulse ${getTypingDuration(player.cpm)} infinite` }"
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
              :src="player.avatar"
              alt="avatar"
              class="w-9 h-9 rounded-full bg-zinc-950 border border-white/10 object-cover relative z-10 transition-transform duration-300"
              :class="{
                'animate-ac-shake': player.status === 'PASSED',
                'animate-err-shake': player.status === 'SUBMIT_FAILED',
              }"
            />
          </div>

          <div class="flex flex-col min-w-0">
            <span
              class="text-xs font-bold text-zinc-300 truncate"
              :class="{ 'text-amber-400': player.status === 'PASSED' }"
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
          <span
            class="text-zinc-500 transition-colors"
            :class="{ 'text-blue-300 font-bold': player.status === 'TYPING' && player.cpm > 300 }"
          >
            手速: <span class="text-zinc-300">{{ formatNumber(player.cpm) }}</span> 次/分
          </span>
        </div>
      </div>
    </div>

    <div class="flex-1 flex flex-col min-h-0 bg-[#050505] relative z-10 overflow-hidden">
      <Transition name="holo">
        <div
          v-if="activeHolo"
          class="absolute inset-0 z-50 pointer-events-none flex items-center justify-center overflow-hidden bg-black/60 backdrop-blur-[2px]"
        >
          <div class="flex flex-col items-center justify-center animate-holo-float">
            <component
              :is="activeHolo.config.icon"
              class="w-20 h-20 mb-4 opacity-90 mix-blend-screen filter drop-shadow-[0_0_25px_currentColor]"
              :class="activeHolo.config.color"
            />
            <span
              class="font-mono font-bold text-sm tracking-widest text-transparent bg-clip-text bg-gradient-to-t to-white"
              :class="activeHolo.config.gradientFrom"
            >
              {{ activeHolo.isMe ? activeHolo.config.myHolo : activeHolo.config.enemyHolo }}
            </span>
            <div class="mt-3 text-[10px] text-zinc-400 font-mono tracking-widest animate-pulse">
              {{ activeHolo.isMe ? '正在发送战术信号...' : '正在接收战术信号...' }}
            </div>
          </div>
          <div class="absolute inset-0 bg-scanline opacity-30 pointer-events-none"></div>
        </div>
      </Transition>

      <div
        class="px-4 py-2 shrink-0 border-b border-white/5 flex items-center justify-between relative z-20"
      >
        <span class="text-[10px] text-zinc-500 font-mono tracking-widest">>>_ 战况日志终端</span>
        <div
          class="w-1.5 h-1.5 bg-[#FF4C00] rounded-full animate-pulse shadow-[0_0_5px_rgba(255,76,0,0.5)]"
        ></div>
      </div>

      <div class="flex-1 overflow-y-auto no-scrollbar p-4 flex flex-col gap-3 relative z-20">
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
      class="h-[60px] shrink-0 border-t border-white/5 bg-zinc-950 flex items-center justify-center gap-6 px-4 relative z-10"
    >
      <button
        v-for="(ping, index) in tacticalPings"
        :key="index"
        @click="emitPing(ping)"
        :disabled="cdTimer > 0"
        :title="ping.label"
        class="relative p-3 rounded-xl bg-zinc-900/80 border border-white/5 text-zinc-400 transition-all duration-300 flex items-center justify-center group focus:outline-none"
        :class="
          cdTimer > 0
            ? 'opacity-40 cursor-not-allowed'
            : 'hover:text-white hover:border-[#FF4C00]/40 hover:bg-[#FF4C00]/10 hover:shadow-[0_0_15px_rgba(255,76,0,0.15)] active:scale-95'
        "
      >
        <component
          :is="ping.icon"
          class="w-5 h-5 transition-transform duration-300"
          :class="{ 'group-hover:scale-110': cdTimer === 0 }"
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
import { ref, onMounted } from 'vue'
import { Bug, Coffee, Rocket } from 'lucide-vue-next'

// === 类型定义 ===
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

interface Player {
  id: string
  name: string
  avatar: string
  status: PlayerStatus
  loc: number
  cpm: number
  isMe: boolean
}

interface BattleLog {
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

// === Emit 事件声明 ===
const emit = defineEmits<{
  (e: 'ping', type: string): void
}>()

// === 数据状态 ===
const players = ref<Player[]>([
  {
    id: 'p_1',
    name: 'Code_Commander',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix&backgroundColor=transparent',
    status: 'TYPING',
    loc: 42,
    cpm: 215,
    isMe: true,
  },
  {
    id: 'p_2',
    name: 'Shadow_Strike',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Annie&backgroundColor=transparent',
    status: 'IDLE',
    loc: 108,
    cpm: 0,
    isMe: false,
  },
])

const logs = ref<BattleLog[]>([
  { id: 1, timestamp: '14:02:41', prefix: 'INFO', message: '沙箱容器初始化完毕，对局开始。' },
])

const tacticalPings: PingConfig[] = [
  {
    type: 'bug',
    icon: Bug,
    label: '嘲讽: 祝你遇到 Bug',
    myLog: '你向对方发送了一只 Bug 🐛',
    enemyLog: '对方向你发送了一只 Bug 🐛',
    myHolo: '🐛 你向对方发送了一只 Bug',
    enemyHolo: '🐛 对方向你发送了一只 Bug',
    color: 'text-emerald-500',
    gradientFrom: 'from-emerald-500',
  },
  {
    type: 'coffee',
    icon: Coffee,
    label: '悠闲: 我去喝咖啡了',
    myLog: '你向对方展示了悠闲的咖啡 ☕',
    enemyLog: '对方觉得你需要喝杯咖啡冷静一下 ☕',
    myHolo: '☕ 你向对方展示了悠闲的咖啡',
    enemyHolo: '☕ 对方觉得你需要喝杯咖啡冷静一下',
    color: 'text-blue-400',
    gradientFrom: 'from-blue-400',
  },
  {
    type: 'rocket',
    icon: Rocket,
    label: '竞速: 准备提交起飞',
    myLog: '你向对方暗示你快提交了🚀',
    enemyLog: '对方向你暗示他快提交了🚀',
    myHolo: '🚀 你向对方暗示你快提交了',
    enemyHolo: '🚀 对方向你暗示他快提交了',
    color: 'text-[#FF4C00]',
    gradientFrom: 'from-[#FF4C00]',
  },
]

const cdTimer = ref(0)
const activeHolo = ref<{ config: PingConfig; isMe: boolean } | null>(null)
let holoTimeout: number | null = null

// === 核心方法 ===

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
  if (cpm > 350) return '0.2s'
  if (cpm > 100) return '1s'
  return '2.5s'
}

const formatNumber = (num: number) => num.toString().padStart(3, '0')

const getNowTime = () => {
  const now = new Date()
  return `${formatNumber(now.getHours())}:${formatNumber(now.getMinutes())}:${formatNumber(now.getSeconds())}`
}

const addLog = (prefix: LogPrefix, message: string) => {
  logs.value.push({ id: Date.now(), timestamp: getNowTime(), prefix, message })
}

const showHologram = (config: PingConfig, isMe: boolean) => {
  if (holoTimeout) clearTimeout(holoTimeout)
  activeHolo.value = { config, isMe }

  holoTimeout = window.setTimeout(() => {
    activeHolo.value = null
  }, 3000)
}

const emitPing = (ping: PingConfig) => {
  if (cdTimer.value > 0) return

  cdTimer.value = 20
  const interval = setInterval(() => {
    cdTimer.value--
    if (cdTimer.value <= 0) clearInterval(interval)
  }, 1000)

  emit('ping', ping.type)
  addLog('MSG ', ping.myLog)
  showHologram(ping, true)
}

// === 史诗级剧本：对手 Mock 演示引擎 ===
onMounted(() => {
  setInterval(() => {
    const me = players.value[0]
    if (me) {
      me.cpm = Math.floor(Math.random() * 80) + 120
      me.loc += Math.random() > 0.7 ? 1 : 0
    }
  }, 2000)

  const enemy = players.value[1]
  if (!enemy) return // 解决 TS18048 可能为未定义

  let step = 0

  const scriptInterval = setInterval(() => {
    step++
    switch (step) {
      case 2:
        enemy.status = 'TYPING'
        enemy.cpm = 380
        enemy.loc = 45
        break
      case 5:
        enemy.status = 'IDLE'
        enemy.cpm = 0
        break
      case 8:
        enemy.status = 'PING'
        const pingConfig = tacticalPings[1]
        if (pingConfig) {
          addLog('MSG ', pingConfig.enemyLog)
          showHologram(pingConfig, false)
        }
        setTimeout(() => {
          enemy.status = 'TYPING'
          enemy.cpm = 150
        }, 1500)
        break
      case 12:
        enemy.status = 'RUNNING_TESTS'
        enemy.cpm = 0
        addLog('EXEC', '对手启动了本地测试用例...')
        break
      case 14:
        enemy.status = 'TEST_FAILED'
        addLog('FAIL', '测试未通过 (用例 2/5 失败)！')
        setTimeout(() => {
          enemy.status = 'TYPING'
          enemy.cpm = 200
        }, 1000)
        break
      case 18:
        enemy.status = 'RUNNING_TESTS'
        enemy.cpm = 0
        addLog('EXEC', '对手再次启动了本地测试用例...')
        break
      case 20:
        enemy.status = 'TEST_PASSED'
        addLog('PASS', '警告：对手本地测试用例全部通过！')
        setTimeout(() => {
          enemy.status = 'IDLE'
        }, 3000)
        break
      case 22:
        enemy.status = 'SUBMITTING'
        addLog('SUB ', '对手提交了解答，云端正在进行最终判题...')
        break
      case 25:
        enemy.status = 'SUBMIT_FAILED'
        addLog('ERR ', '提交被驳回：解答错误 (WA: 3/10)！')
        setTimeout(() => {
          enemy.status = 'TYPING'
          enemy.cpm = 450
          enemy.loc += 2
        }, 1500)
        break
      case 28:
        enemy.status = 'SUBMITTING'
        addLog('SUB ', '对手再次提交了解答...')
        break
      case 31:
        enemy.status = 'PASSED'
        addLog(' AC ', '战斗结束：对手已成功 AC！')
        clearInterval(scriptInterval)
        break
    }
  }, 1000)
})
</script>

<style scoped>
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
    opacity: 0.1;
  }
  50% {
    opacity: 0.8;
  }
}
.animate-idle-breath {
  animation: idle-breath 4s ease-in-out infinite;
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

/* cspell:disable-next-line */
.bg-scanline {
  background: linear-gradient(to bottom, transparent 50%, rgba(255, 255, 255, 0.05) 51%);
  background-size: 100% 4px;
}
</style>
