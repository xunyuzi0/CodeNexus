<template>
  <div
    class="relative p-6 md:p-8 font-sans transition-all duration-300"
    :class="[
      isFixedMode
        ? 'h-[calc(100vh-80px)] overflow-hidden'
        : 'min-h-[calc(100vh-80px)] overflow-y-auto',
    ]"
  >
    <div
      class="fixed top-[-20%] left-[10%] w-[500px] h-[500px] bg-[#FF4C00]/10 blur-[120px] rounded-full pointer-events-none z-0 mix-blend-screen"
    ></div>
    <div
      class="fixed bottom-[-10%] right-[5%] w-[600px] h-[600px] bg-purple-900/10 blur-[100px] rounded-full pointer-events-none z-0 mix-blend-screen"
    ></div>

    <div
      class="relative z-10 shrink-0 max-w-7xl mx-auto w-full mb-8 flex flex-col justify-center gap-1 opacity-0"
      v-motion
      :initial="{ opacity: 0, x: -20 }"
      :enter="{ opacity: 1, x: 0, transition: { duration: 400 } }"
    >
      <div class="flex items-center gap-3 mb-1">
        <div
          class="p-2 bg-zinc-900/50 rounded-xl border border-white/10 shadow-sm backdrop-blur-sm"
        >
          <LayoutDashboard class="w-6 h-6 text-[#FF4C00]" />
        </div>
        <h1 class="text-3xl font-black tracking-tight text-white">仪表盘</h1>
      </div>
      <p class="text-zinc-500 text-sm font-medium tracking-wide pl-1">
        实时监控您的编程活动、算力指标与成长轨迹
      </p>
    </div>

    <div
      class="relative z-10 w-full max-w-7xl mx-auto grid grid-cols-1 md:grid-cols-4 auto-rows-[minmax(180px,auto)] gap-6"
    >
      <div
        class="group md:col-span-2 relative overflow-hidden rounded-3xl bg-zinc-900/50 backdrop-blur-xl border border-white/5 p-8 flex flex-col justify-between hover:border-[#FF4C00]/30 transition-all duration-500 hover:shadow-[0_0_30px_rgba(255,76,0,0.1)] opacity-0"
        :class="isFixedMode ? 'h-full' : 'min-h-[240px]'"
        v-motion
        :initial="{ opacity: 0, y: 20 }"
        :enter="{ opacity: 1, y: 0, transition: { duration: 400, delay: 50 } }"
      >
        <div class="relative z-10 flex justify-between items-start">
          <div>
            <div class="flex items-center gap-2 mb-2">
              <div class="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></div>
              <span class="text-xs font-bold text-zinc-500 tracking-wider">当前会话</span>
            </div>
            <h3 class="text-2xl font-bold text-white">
              {{ greeting }},
              <span class="text-[#FF4C00]">{{ userStore.nickname || '指挥官' }}</span>
            </h3>
            <p class="text-zinc-400 text-sm mt-1 max-w-xs leading-relaxed line-clamp-2">
              检测到新的算法挑战已上线。今日算力充沛，建议立即开始训练。
            </p>
          </div>
          <Zap
            class="w-12 h-12 text-zinc-800 group-hover:text-[#FF4C00]/20 transition-colors duration-500"
          />
        </div>

        <div class="relative z-10 mt-4 flex items-baseline gap-4">
          <span
            class="text-5xl md:text-6xl font-black font-mono text-white tracking-tighter tabular-nums"
          >
            {{ formattedTime }}
          </span>
          <span
            class="text-sm font-bold text-zinc-500 tracking-widest border-l border-zinc-700 pl-4 whitespace-nowrap"
          >
            {{ currentDate }}
          </span>
        </div>

        <div
          class="absolute bottom-0 left-0 w-full h-[1px] bg-gradient-to-r from-transparent via-[#FF4C00]/50 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500"
        ></div>
      </div>

      <div
        class="md:col-span-1 relative overflow-hidden rounded-3xl bg-zinc-900/50 backdrop-blur-xl border border-white/5 p-1 hover:border-[#FF4C00]/30 transition-all duration-500 group opacity-0"
        :class="isFixedMode ? 'h-full' : 'min-h-[240px]'"
        v-motion
        :initial="{ opacity: 0, y: 20 }"
        :enter="{ opacity: 1, y: 0, transition: { duration: 400, delay: 100 } }"
      >
        <div
          class="h-full w-full rounded-[20px] bg-black/40 flex flex-col items-center justify-center relative overflow-hidden p-6"
        >
          <div v-if="!isCheckedIn" class="flex flex-col items-center z-10 w-full">
            <button
              @click="handleCheckIn"
              class="relative w-24 h-24 rounded-full flex items-center justify-center mb-4 group/btn outline-none cursor-pointer"
            >
              <div class="absolute inset-0 bg-[#FF4C00]/20 rounded-full animate-ping-slow"></div>
              <div
                class="absolute inset-0 bg-[#FF4C00]/10 rounded-full blur-md group-hover/btn:bg-[#FF4C00]/30 transition-colors"
              ></div>
              <div
                class="relative w-20 h-20 bg-gradient-to-b from-[#FF4C00] to-[#9a2e00] rounded-full flex items-center justify-center shadow-[0_0_20px_#FF4C00] border-2 border-[#ff8c5a] hover:scale-105 active:scale-95 transition-all duration-200"
              >
                <Zap class="w-10 h-10 text-white fill-white" />
              </div>
            </button>
            <h3 class="text-white font-bold text-lg tracking-wide">每日充能</h3>
            <p class="text-xs text-zinc-500 mt-1 text-center">点击注入算力</p>
          </div>

          <div
            v-else
            class="flex flex-col items-center z-10 w-full animate-in zoom-in duration-300"
          >
            <div class="relative w-20 h-20 mb-4 flex items-center justify-center">
              <div class="absolute inset-0 bg-emerald-500/20 rounded-full blur-xl"></div>
              <Flame
                class="w-12 h-12 text-emerald-500 drop-shadow-[0_0_10px_rgba(16,185,129,0.8)] animate-pulse"
              />
            </div>
            <div class="text-center">
              <h3
                class="text-emerald-400 font-bold text-lg tracking-wide shadow-emerald-500/50 drop-shadow-sm"
              >
                能量已满
              </h3>
              <p
                class="text-xs text-zinc-400 mt-2 bg-white/5 px-3 py-1 rounded-md border border-white/5 flex items-center justify-center gap-1"
              >
                连续打卡
                <span class="text-[#FF4C00] font-bold text-sm mx-1">{{ checkInDays }}</span> 天
              </p>
            </div>
          </div>
          <div
            class="absolute inset-0 bg-[linear-gradient(rgba(255,255,255,0.03)_1px,transparent_1px),linear-gradient(90deg,rgba(255,255,255,0.03)_1px,transparent_1px)] bg-[size:20px_20px] [mask-image:radial-gradient(ellipse_80%_80%_at_50%_50%,#000_40%,transparent_100%)] pointer-events-none"
          ></div>
        </div>
      </div>

      <div
        class="md:col-span-1 md:row-span-2 relative overflow-hidden rounded-3xl bg-zinc-900/50 backdrop-blur-xl border border-white/5 flex flex-col hover:border-[#FF4C00]/30 transition-all duration-500 hover:translate-y-[-2px] hover:shadow-xl opacity-0"
        :class="isFixedMode ? 'h-full' : 'min-h-[500px]'"
        v-motion
        :initial="{ opacity: 0, x: 20 }"
        :enter="{ opacity: 1, x: 0, transition: { duration: 400, delay: 150 } }"
      >
        <div class="p-6 border-b border-white/5 bg-white/[0.02]">
          <h3 class="text-xs font-bold text-zinc-500 tracking-wider flex items-center gap-2">
            <Activity class="w-4 h-4" /> 核心指标
          </h3>
        </div>

        <div class="flex-1 flex flex-col divide-y divide-white/5">
          <div
            class="flex-1 p-6 flex flex-col justify-center group/item hover:bg-white/[0.02] transition-colors"
          >
            <div class="flex items-center justify-between mb-2">
              <span class="text-xs text-zinc-400 font-medium">战力评分</span>
              <Trophy
                class="w-4 h-4 text-[#FF4C00] opacity-50 group-hover/item:opacity-100 transition-opacity"
              />
            </div>
            <div
              class="text-4xl font-black font-mono text-white tracking-tighter drop-shadow-[0_0_15px_rgba(255,76,0,0.3)]"
            >
              {{ userStore.userInfo?.rankScore || 1450 }}
            </div>
            <div class="text-[10px] text-emerald-500 font-medium mt-1 flex items-center gap-1">
              <span
                class="w-0 h-0 border-l-[3px] border-l-transparent border-r-[3px] border-r-transparent border-b-[4px] border-b-emerald-500"
              ></span>
              本周提升 24
            </div>
          </div>

          <div
            class="flex-1 p-6 flex flex-col justify-center group/item hover:bg-white/[0.02] transition-colors"
          >
            <div class="flex items-center justify-between mb-2">
              <span class="text-xs text-zinc-400 font-medium">全站排名</span>
              <Crown
                class="w-4 h-4 text-yellow-500 opacity-50 group-hover/item:opacity-100 transition-opacity"
              />
            </div>
            <div class="text-3xl font-bold font-mono text-zinc-200 tracking-tighter">
              #{{ userStore.userInfo?.globalRank || 8848 }}
            </div>
            <div class="w-full bg-zinc-800 h-1 rounded-full mt-3 overflow-hidden">
              <div class="h-full bg-yellow-600 w-[65%] shadow-[0_0_10px_#ca8a04]"></div>
            </div>
          </div>

          <div
            class="flex-1 p-6 flex flex-col justify-center group/item hover:bg-white/[0.02] transition-colors"
          >
            <div class="flex items-center justify-between mb-2">
              <span class="text-xs text-zinc-400 font-medium">解题总量</span>
              <Target
                class="w-4 h-4 text-blue-400 opacity-50 group-hover/item:opacity-100 transition-opacity"
              />
            </div>
            <div class="flex items-baseline gap-2">
              <span class="text-3xl font-bold font-mono text-white tracking-tighter">128</span>
              <span class="text-xs text-zinc-600 font-mono">/ 500</span>
            </div>
          </div>
        </div>
      </div>

      <div
        class="md:col-span-2 relative rounded-3xl bg-zinc-900/50 backdrop-blur-xl border border-white/5 p-6 hover:border-[#FF4C00]/30 transition-all duration-500 group/heatmap flex flex-col opacity-0"
        :class="isFixedMode ? 'h-full min-h-[260px]' : 'min-h-[260px]'"
        v-motion
        :initial="{ opacity: 0, y: 20 }"
        :enter="{ opacity: 1, y: 0, transition: { duration: 400, delay: 200 } }"
      >
        <div class="flex items-center justify-between mb-4 shrink-0">
          <div>
            <h3 class="text-sm font-bold text-white tracking-wide">思维活跃度</h3>
            <p class="text-xs text-zinc-500 mt-0.5 font-mono">
              {{ displayYear }}年 {{ displayPeriod }}
            </p>
          </div>

          <div class="flex items-center gap-4">
            <div class="flex items-center gap-1">
              <button
                @click="switchPeriod(-1)"
                class="p-1 rounded-md hover:bg-white/10 text-zinc-500 hover:text-white transition-colors"
              >
                <ChevronLeft class="w-4 h-4" />
              </button>
              <button
                @click="switchPeriod(1)"
                class="p-1 rounded-md hover:bg-white/10 text-zinc-500 hover:text-white transition-colors"
              >
                <ChevronRight class="w-4 h-4" />
              </button>
            </div>

            <div class="flex items-center gap-1.5 text-[10px] text-zinc-500 font-mono">
              <span>少</span>
              <div class="flex gap-1">
                <div class="w-3 h-3 rounded-[3px] bg-zinc-800 border border-white/5"></div>
                <div class="w-3 h-3 rounded-[3px] bg-[#FF4C00]/20 border border-white/5"></div>
                <div class="w-3 h-3 rounded-[3px] bg-[#FF4C00]/50 border border-white/5"></div>
                <div class="w-3 h-3 rounded-[3px] bg-[#FF4C00] border border-white/5"></div>
              </div>
              <span>多</span>
            </div>
          </div>
        </div>

        <div class="flex-1 w-full min-h-0 flex items-center justify-center">
          <div class="flex items-start gap-2">
            <div
              class="grid grid-rows-7 gap-1 text-[10px] text-zinc-600 font-mono font-bold leading-none select-none shrink-0"
            >
              <div class="h-4 flex items-center">Mon</div>
              <div class="h-4"></div>
              <div class="h-4 flex items-center">Wed</div>
              <div class="h-4"></div>
              <div class="h-4 flex items-center">Fri</div>
              <div class="h-4"></div>
              <div class="h-4"></div>
            </div>

            <div class="relative">
              <div class="grid grid-rows-7 grid-flow-col gap-1 w-max">
                <div
                  v-for="(item, index) in heatmapData"
                  :key="index"
                  class="w-4 h-4 rounded-[3px] transition-colors duration-200 relative group/cell"
                  :class="[
                    item.date ? heatmapColors[item.level] : 'bg-transparent pointer-events-none',
                    item.date && item.level > 0
                      ? 'border border-white/5'
                      : 'border border-transparent',
                  ]"
                >
                  <div
                    v-if="item.date"
                    class="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 px-3 py-1.5 bg-zinc-900 border border-white/20 rounded-md text-xs text-zinc-200 font-mono whitespace-nowrap opacity-0 group-hover/cell:opacity-100 transition-opacity duration-200 pointer-events-none z-50 shadow-[0_4px_20px_rgba(0,0,0,0.5)] select-none min-w-max"
                  >
                    <span class="text-zinc-400">{{ item.date }}:</span>
                    <span class="font-bold ml-1" :class="levelTextColors[item.level]">{{
                      getLevelText(item.level)
                    }}</span>
                    <div
                      class="absolute -bottom-1 left-1/2 -translate-x-1/2 w-2 h-2 bg-zinc-900 border-b border-r border-white/20 rotate-45"
                    ></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div
        class="md:col-span-1 relative rounded-3xl p-0 flex flex-col gap-2 bg-transparent border-none shadow-none opacity-0"
        :class="isFixedMode ? 'h-full min-h-[260px]' : 'min-h-[260px]'"
        v-motion
        :initial="{ opacity: 0, y: 20 }"
        :enter="{ opacity: 1, y: 0, transition: { duration: 400, delay: 250 } }"
      >
        <button
          @click="triggerAction('RANKED')"
          class="flex-1 relative group overflow-hidden rounded-2xl bg-gradient-to-br from-zinc-800/50 via-zinc-900/50 to-black/50 border border-white/5 hover:border-[#FF4C00]/30 flex items-center px-6 transition-all duration-300 ease-[cubic-bezier(0.25,0.8,0.25,1)] hover:-translate-y-1 hover:shadow-[0_0_30px_rgba(255,76,0,0.15)] cursor-pointer"
        >
          <div
            class="absolute inset-0 bg-white/[0.02] opacity-0 group-hover:opacity-100 transition-opacity duration-300 pointer-events-none"
          ></div>

          <div
            class="w-10 h-10 rounded-full bg-white/5 flex items-center justify-center mr-4 group-hover:bg-[#FF4C00] group-hover:text-white transition-colors duration-300 z-10"
          >
            <Swords class="w-5 h-5 text-zinc-400 group-hover:text-white transition-colors" />
          </div>
          <div class="text-left z-10">
            <div
              class="text-xs font-bold text-zinc-500 group-hover:text-[#FF4C00] tracking-wider transition-colors duration-300"
            >
              排位赛
            </div>
            <div class="text-sm font-black text-white group-hover:text-white transition-colors">
              进入战场
            </div>
          </div>
          <ChevronRight
            class="w-5 h-5 ml-auto text-zinc-600 group-hover:text-[#FF4C00] group-hover:translate-x-1 transition-all duration-300 z-10"
          />
        </button>

        <button
          @click="triggerAction('PRACTICE')"
          class="flex-1 relative group overflow-hidden rounded-2xl bg-gradient-to-br from-zinc-800/50 via-zinc-900/50 to-black/50 border border-white/5 hover:border-zinc-500/30 flex items-center px-6 transition-all duration-300 ease-[cubic-bezier(0.25,0.8,0.25,1)] hover:-translate-y-1 hover:shadow-[0_0_30px_rgba(255,255,255,0.05)] cursor-pointer"
        >
          <div
            class="absolute inset-0 bg-white/[0.02] opacity-0 group-hover:opacity-100 transition-opacity duration-300 pointer-events-none"
          ></div>

          <div
            class="w-10 h-10 rounded-full bg-white/5 flex items-center justify-center mr-4 group-hover:bg-zinc-200 group-hover:text-black transition-colors duration-300 z-10"
          >
            <Dices class="w-5 h-5 text-zinc-400 group-hover:text-black transition-colors" />
          </div>
          <div class="text-left z-10">
            <div
              class="text-xs font-bold text-zinc-500 group-hover:text-zinc-300 tracking-wider transition-colors duration-300"
            >
              每日一练
            </div>
            <div class="text-sm font-black text-white group-hover:text-white transition-colors">
              随机挑战
            </div>
          </div>
        </button>
      </div>
    </div>

    <ArenaDialog
      v-model="showDialog"
      :title="dialogConfig.title"
      :confirm-text="dialogConfig.confirmText"
      @confirm="handleConfirm"
    >
      <div class="text-center">
        <div
          class="w-16 h-16 mx-auto rounded-full flex items-center justify-center mb-6 border"
          :class="
            actionType === 'RANKED'
              ? 'bg-purple-500/10 border-purple-500/20'
              : 'bg-[#FF4C00]/10 border-[#FF4C00]/20'
          "
        >
          <component
            :is="dialogConfig.icon"
            class="w-8 h-8"
            :class="actionType === 'RANKED' ? 'text-purple-500' : 'text-[#FF4C00]'"
          />
        </div>
        <p class="text-zinc-400 text-sm leading-relaxed" v-html="dialogConfig.desc"></p>

        <div
          v-if="actionType === 'RANKED'"
          class="mt-4 p-3 bg-black/40 rounded-lg border border-white/5 text-xs text-zinc-500 font-mono"
        >
          当前排位积分: <span class="text-white">{{ userStore.userInfo?.rankScore || 1450 }}</span>
        </div>
      </div>
    </ArenaDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useNow, useDateFormat, useWindowSize } from '@vueuse/core'
import {
  Zap,
  Trophy,
  Target,
  Activity,
  Flame,
  Swords,
  Dices,
  ChevronRight,
  ChevronLeft,
  Crown,
  Radio,
  LayoutDashboard,
} from 'lucide-vue-next'
import ArenaDialog from '@/components/arena/ArenaDialog.vue'

// --- 状态管理 ---
const router = useRouter()
const userStore = useUserStore()
const now = useNow()

const formattedTime = useDateFormat(now, 'HH:mm:ss')
const currentDate = useDateFormat(now, 'YYYY年MM月DD日 dddd')

// --- [智能锁定逻辑] ---
const { width, height } = useWindowSize()
// 规则：宽度大于 1024px (大屏) 且 高度大于 800px (足够容纳内容) 时，锁定页面
const isFixedMode = computed(() => width.value >= 1024 && height.value >= 800)

const greeting = computed(() => {
  const hour = now.value.getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '早上好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

// 打卡逻辑
const isCheckedIn = ref(false)
const checkInDays = ref(41)
const handleCheckIn = () => {
  if (isCheckedIn.value) return
  isCheckedIn.value = true
  checkInDays.value++
}

// 战备弹窗逻辑
const showDialog = ref(false)
const actionType = ref<'RANKED' | 'PRACTICE'>('RANKED')

const dialogConfig = computed(() => {
  if (actionType.value === 'RANKED') {
    return {
      title: '排位赛匹配确认',
      desc: '即将进入天梯匹配队列，系统将根据您的 Elo 分数寻找旗鼓相当的对手。<br><span class="text-purple-400 font-bold">胜利将增加排位分，失败将扣除。</span>',
      confirmText: '开始匹配',
      icon: Radio,
    }
  } else {
    return {
      title: '每日一练挑战',
      desc: '系统将随机抽取一道适合您当前段位的题目。<br>练习模式不计入排位分，但会增加活跃度。',
      confirmText: '立即开始',
      icon: Dices,
    }
  }
})

const triggerAction = (type: 'RANKED' | 'PRACTICE') => {
  actionType.value = type
  showDialog.value = true
}

const handleConfirm = () => {
  showDialog.value = false
  if (actionType.value === 'RANKED') {
    router.push('/battle/matchmaking')
  } else {
    router.push('/problems/1001')
  }
}

// --- 热力图核心逻辑 (Half-Year View) ---

const currentYear = ref(new Date().getFullYear())
const currentPeriod = ref(new Date().getMonth() < 6 ? 0 : 1) // 0: 上半年, 1: 下半年

interface HeatmapItem {
  date: string | null
  level: number
}

const heatmapData = ref<HeatmapItem[]>([])

const displayYear = computed(() => currentYear.value)
const displayPeriod = computed(() => (currentPeriod.value === 0 ? '1月 - 6月' : '7月 - 12月'))

const switchPeriod = (direction: number) => {
  let newPeriod = currentPeriod.value + direction
  let newYear = currentYear.value

  if (newPeriod > 1) {
    newPeriod = 0
    newYear++
  } else if (newPeriod < 0) {
    newPeriod = 1
    newYear--
  }

  currentPeriod.value = newPeriod
  currentYear.value = newYear
  generateHeatmapData()
}

const heatmapColors = [
  'bg-zinc-800', // Level 0
  'bg-[#FF4C00]/20', // Level 1
  'bg-[#FF4C00]/40', // Level 2
  'bg-[#FF4C00]/70', // Level 3
  'bg-[#FF4C00]', // Level 4
]

const levelTextColors = [
  'text-zinc-500',
  'text-[#FF4C00]/60',
  'text-[#FF4C00]/80',
  'text-[#FF4C00]',
  'text-white',
]

const getLevelText = (level: number) => {
  const map = ['无数据', '低', '中', '高', '极高']
  return map[level] || '未知'
}

const generateHeatmapData = () => {
  const data: HeatmapItem[] = []

  const year = currentYear.value
  const isFirstHalf = currentPeriod.value === 0

  const startMonth = isFirstHalf ? 0 : 6
  const endMonth = isFirstHalf ? 5 : 11

  const startDate = new Date(year, startMonth, 1)
  const endDate = new Date(year, endMonth + 1, 0)

  // 补全前面的空位 (周一为起始)
  // getDay(): 0(周日) -> 需要补6个
  // getDay(): 1(周一) -> 需要补0个
  let startDay = startDate.getDay()
  if (startDay === 0) startDay = 7
  const paddingCount = startDay - 1

  for (let i = 0; i < paddingCount; i++) {
    data.push({ date: null, level: 0 })
  }

  const tempDate = new Date(startDate)
  while (tempDate <= endDate) {
    const dateStr = tempDate.toISOString().split('T')[0]
    let level = 0
    if (Math.random() > 0.4) {
      level = Math.ceil(Math.random() * 4)
    }
    data.push({ date: dateStr, level })
    tempDate.setDate(tempDate.getDate() + 1)
  }

  heatmapData.value = data
}

onMounted(() => {
  generateHeatmapData()
})
</script>

<style scoped>
@keyframes ping-slow {
  75%,
  100% {
    transform: scale(2);
    opacity: 0;
  }
}
.animate-ping-slow {
  animation: ping-slow 2s cubic-bezier(0, 0, 0.2, 1) infinite;
}
</style>
