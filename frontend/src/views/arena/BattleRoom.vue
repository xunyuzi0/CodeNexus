<template>
  <div
    v-if="isVerifying"
    class="h-screen w-full bg-black flex items-center justify-center font-sans"
  >
    <div class="flex flex-col items-center gap-6">
      <div class="relative w-16 h-16">
        <div class="absolute inset-0 rounded-full border-4 border-zinc-800"></div>
        <div class="absolute inset-0 rounded-full border-4 border-t-[#FF4C00] animate-spin"></div>
        <Loader2 class="absolute inset-0 m-auto w-6 h-6 text-[#FF4C00] animate-pulse" />
      </div>
      <div class="text-center space-y-2">
        <h3 class="text-white font-bold tracking-wider text-lg">沙箱环境初始化</h3>
        <p class="text-zinc-500 font-mono text-xs tracking-[0.2em] uppercase animate-pulse">
          Establishing Secure Link...
        </p>
      </div>
    </div>
  </div>

  <div v-else class="h-screen w-full flex flex-col p-6 overflow-hidden relative bg-zinc-950">
    <div class="absolute inset-0 bg-grid-white/[0.02] pointer-events-none"></div>
    <div
      class="absolute top-0 right-0 w-[500px] h-[500px] bg-[#FF4C00]/5 blur-[100px] rounded-full pointer-events-none"
    ></div>

    <ArenaExitButton :needs-confirm="battleStatus === 'FIGHTING'" />

    <div
      class="absolute top-6 left-1/2 -translate-x-1/2 z-40 flex items-center gap-4 px-6 py-2 rounded-full bg-zinc-900/80 backdrop-blur-xl border border-white/10 shadow-2xl transition-all duration-500"
      :class="
        timer < 60 && battleStatus === 'FIGHTING'
          ? 'border-red-500/50 animate-pulse shadow-red-500/20'
          : ''
      "
    >
      <div class="flex items-center gap-2">
        <Timer class="w-5 h-5" :class="timer < 60 ? 'text-red-500' : 'text-[#FF4C00]'" />
        <span class="font-mono text-xl font-bold tracking-widest text-white tabular-nums">
          {{ formattedTime }}
        </span>
      </div>
      <div class="w-[1px] h-4 bg-white/20"></div>
      <div class="text-xs font-bold tracking-wider" :class="statusColor">
        {{ statusText }}
      </div>
    </div>

    <div class="flex-1 grid grid-cols-[20fr_55fr_25fr] gap-6 min-h-0 pt-12 relative z-10">
      <section
        class="flex flex-col h-full bg-zinc-900/40 backdrop-blur-md border border-white/5 rounded-2xl overflow-hidden group hover:border-white/10 transition-colors"
      >
        <div class="p-4 border-b border-white/5 bg-white/[0.02]">
          <div class="flex items-center gap-2 mb-2">
            <span
              class="px-2 py-0.5 rounded text-[10px] font-bold bg-emerald-500/10 text-emerald-500 border border-emerald-500/20"
            >
              简单
            </span>
            <span class="text-xs text-zinc-500 font-mono">#{{ problem.id }}</span>
          </div>
          <h2 class="text-lg font-bold text-white leading-tight">{{ problem.title }}</h2>
        </div>

        <div class="flex-1 overflow-y-auto p-4 space-y-6 custom-scrollbar">
          <div
            class="text-sm text-zinc-300 leading-relaxed space-y-4"
            v-html="problem.content"
          ></div>
          <div v-for="(example, idx) in problem.examples" :key="idx" class="space-y-2">
            <p class="text-xs font-bold text-zinc-500 uppercase tracking-wider">
              示例 {{ idx + 1 }}
            </p>
            <div class="bg-black/40 rounded-lg p-3 border border-white/5 font-mono text-xs">
              <div class="mb-1">
                <span class="text-zinc-500 select-none">输入: </span>
                <span class="text-zinc-300">{{ example.input }}</span>
              </div>
              <div>
                <span class="text-zinc-500 select-none">输出: </span>
                <span class="text-[#FF4C00]">{{ example.output }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="flex flex-col h-full relative min-h-0 gap-4">
        <div
          class="flex-1 relative rounded-xl transition-all duration-300 overflow-hidden flex flex-col"
          :class="
            isViewingOpponentCode
              ? 'ring-2 ring-amber-500/50 shadow-[0_0_30px_rgba(245,158,11,0.1)]'
              : ''
          "
        >
          <div
            v-if="isViewingOpponentCode"
            class="absolute top-0 left-0 right-0 z-50 bg-amber-500/90 text-black text-xs font-bold px-3 py-1 flex items-center justify-center gap-2 shadow-lg backdrop-blur-sm"
          >
            <Eye class="w-3 h-3" />
            <span>正在查看对手代码 (只读模式)</span>
          </div>

          <CodeEditor
            v-model="editorCode"
            language="java"
            :read-only="isViewingOpponentCode || battleStatus !== 'FIGHTING'"
            :loading="isSubmitting"
            class="h-full w-full"
          />
        </div>

        <div
          class="h-16 flex items-center justify-between shrink-0 bg-zinc-900/40 backdrop-blur-md border border-white/5 rounded-xl px-4"
        >
          <div class="flex items-center gap-4 text-xs font-mono text-zinc-500">
            <div class="flex items-center gap-1.5">
              <div
                class="w-2 h-2 rounded-full"
                :class="
                  isRunning || isSubmitting ? 'bg-yellow-500 animate-pulse' : 'bg-emerald-500'
                "
              ></div>
              <span>{{ systemStatusText }}</span>
            </div>
          </div>

          <div class="flex items-center gap-4">
            <Button
              variant="ghost"
              class="text-zinc-400 hover:text-white hover:bg-white/5 min-w-[100px]"
              :disabled="
                isRunning || isSubmitting || battleStatus !== 'FIGHTING' || isViewingOpponentCode
              "
              @click="handleRunTest"
            >
              <Loader2 v-if="isRunning" class="w-4 h-4 mr-2 animate-spin" />
              <Play v-else class="w-4 h-4 mr-2" />
              {{ isRunning ? '运行中' : '运行测试' }}
            </Button>

            <Button
              class="bg-[#FF4C00] hover:bg-[#ff5f1f] text-white font-bold px-8 shadow-[0_0_20px_rgba(255,76,0,0.3)] hover:shadow-[0_0_30px_rgba(255,76,0,0.5)] transition-all active:scale-95 min-w-[120px]"
              :disabled="
                isRunning || isSubmitting || battleStatus !== 'FIGHTING' || isViewingOpponentCode
              "
              @click="handleSubmit"
            >
              <Loader2 v-if="isSubmitting" class="w-4 h-4 mr-2 animate-spin" />
              <Send v-else class="w-4 h-4 mr-2" />
              {{ isSubmitting ? '提交中' : '提交代码' }}
            </Button>
          </div>
        </div>
      </section>

      <section class="flex flex-col h-full gap-4 min-h-0">
        <div
          class="bg-zinc-900/60 backdrop-blur-md border border-white/5 rounded-2xl p-5 relative overflow-hidden shrink-0"
        >
          <div class="flex items-center justify-between mb-6">
            <div class="flex items-center gap-3">
              <div class="relative">
                <div
                  class="w-10 h-10 rounded-full bg-zinc-800 border border-white/10 overflow-hidden"
                >
                  <img :src="opponent.avatar" class="w-full h-full object-cover" />
                </div>
                <div
                  class="absolute -bottom-0.5 -right-0.5 w-3 h-3 bg-emerald-500 border-2 border-zinc-900 rounded-full flex items-center justify-center"
                >
                  <div
                    v-if="battleStatus === 'FIGHTING'"
                    class="w-full h-full rounded-full bg-emerald-400 animate-ping opacity-75"
                  ></div>
                </div>
              </div>
              <div>
                <h3 class="text-sm font-bold text-white">{{ opponent.name }}</h3>
                <div
                  class="flex items-center gap-2 text-[10px] text-zinc-500 font-mono tracking-wider"
                >
                  <span>RANK 1,204</span>
                  <span class="w-1 h-1 rounded-full bg-zinc-600"></span>
                  <span>{{ opponent.linesOfCode }} 行</span>
                </div>
              </div>
            </div>
            <Swords class="w-6 h-6 text-zinc-600" />
          </div>

          <div class="space-y-4">
            <div class="space-y-1.5">
              <div class="flex justify-between text-[10px] uppercase font-bold tracking-wider">
                <span class="text-[#FF4C00]">我方</span>
                <span class="text-zinc-400">{{ myProgress }}% AC</span>
              </div>
              <div class="h-1.5 w-full bg-zinc-800 rounded-full overflow-hidden">
                <div
                  class="h-full bg-[#FF4C00] shadow-[0_0_10px_#FF4C00] transition-all duration-500 ease-out"
                  :style="{ width: myProgress + '%' }"
                ></div>
              </div>
            </div>

            <div class="space-y-1.5">
              <div class="flex justify-between text-[10px] uppercase font-bold tracking-wider">
                <span class="text-rose-500">对手</span>
                <span class="text-zinc-400">{{ opponent.progress }}% AC</span>
              </div>
              <div class="h-1.5 w-full bg-zinc-800 rounded-full overflow-hidden">
                <div
                  class="h-full bg-rose-500 shadow-[0_0_10px_rgba(244,63,94,0.5)] transition-all duration-500 ease-out"
                  :style="{ width: opponent.progress + '%' }"
                ></div>
              </div>
            </div>
          </div>
        </div>

        <div
          class="flex-1 bg-black/40 backdrop-blur-md border border-white/5 rounded-2xl p-0 flex flex-col min-h-0"
        >
          <div class="flex items-center gap-2 px-4 py-3 border-b border-white/5 shrink-0">
            <Activity class="w-3 h-3 text-zinc-500" />
            <span class="text-xs font-bold text-zinc-500 uppercase tracking-widest">实时情报</span>
          </div>

          <div class="flex-1 overflow-y-auto custom-scrollbar p-4 space-y-3 font-mono text-xs">
            <TransitionGroup name="list">
              <div
                v-for="log in battleLogs"
                :key="log.id"
                class="flex gap-3 items-start p-2 rounded-lg bg-white/[0.02] border border-white/[0.02]"
              >
                <div class="shrink-0 mt-0.5">
                  <FileText v-if="log.type === 'CODE_UPDATE'" class="w-3.5 h-3.5 text-blue-400" />
                  <AlertCircle
                    v-else-if="log.type === 'TEST_FAIL'"
                    class="w-3.5 h-3.5 text-rose-500"
                  />
                  <CheckCircle2
                    v-else-if="log.type === 'TEST_PASS'"
                    class="w-3.5 h-3.5 text-yellow-500"
                  />
                  <Trophy v-else-if="log.type === 'SUBMIT_AC'" class="w-3.5 h-3.5 text-[#FF4C00]" />
                  <Info v-else class="w-3.5 h-3.5 text-zinc-500" />
                </div>
                <div class="flex flex-col gap-0.5 min-w-0">
                  <span class="text-zinc-300 leading-tight">{{ log.message }}</span>
                  <span class="text-[10px] text-zinc-600">{{ log.time }}</span>
                </div>
              </div>
            </TransitionGroup>
          </div>
        </div>
      </section>
    </div>

    <Transition name="fade">
      <div
        v-if="battleStatus === 'VICTORY' || battleStatus === 'DEFEAT'"
        class="absolute inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-md p-6"
      >
        <div
          class="w-full max-w-md rounded-3xl border p-8 flex flex-col items-center text-center shadow-2xl relative overflow-hidden"
          :class="
            battleStatus === 'VICTORY'
              ? 'bg-[#09090b] border-[#FF4C00]/30 shadow-[#FF4C00]/20'
              : 'bg-[#09090b] border-zinc-800'
          "
        >
          <div
            v-if="battleStatus === 'VICTORY'"
            class="absolute top-0 left-1/2 -translate-x-1/2 w-64 h-64 bg-[#FF4C00]/20 blur-[80px] rounded-full pointer-events-none"
          ></div>

          <div class="relative mb-6">
            <Trophy
              v-if="battleStatus === 'VICTORY'"
              class="w-20 h-20 text-[#FF4C00] drop-shadow-[0_0_15px_#FF4C00]"
            />
            <XCircle v-else class="w-20 h-20 text-zinc-600" />
          </div>

          <h2 class="text-4xl font-black italic tracking-tighter text-white mb-2">
            {{ battleStatus === 'VICTORY' ? 'VICTORY' : 'DEFEAT' }}
          </h2>
          <p class="text-zinc-500 font-medium mb-8">
            {{ battleStatus === 'VICTORY' ? '恭喜！你击败了对手' : '很遗憾，对手抢先一步' }}
          </p>

          <div
            class="flex items-center gap-2 mb-8 px-4 py-2 rounded-full bg-white/5 border border-white/5"
          >
            <span class="text-xs text-zinc-400 font-bold uppercase tracking-wider">排位积分</span>
            <span
              class="font-mono font-bold text-lg"
              :class="battleStatus === 'VICTORY' ? 'text-emerald-400' : 'text-rose-400'"
            >
              {{ battleStatus === 'VICTORY' ? '+25' : '-15' }}
            </span>
          </div>

          <div class="flex flex-col gap-3 w-full">
            <div class="flex gap-3 w-full">
              <Button
                class="flex-1 bg-[#FF4C00] hover:bg-[#ff5f1f] text-white font-bold h-12 rounded-xl"
                @click="router.replace('/battle/matchmaking')"
              >
                <RotateCcw class="w-4 h-4 mr-2" /> 再次匹配
              </Button>
              <Button
                class="flex-1 bg-zinc-800 hover:bg-zinc-700 text-white font-bold h-12 rounded-xl border border-white/5"
                @click="continueResearch"
              >
                <ArrowRight class="w-4 h-4 ml-2" /> 继续研究
              </Button>
            </div>

            <Button
              v-if="battleStatus === 'DEFEAT'"
              variant="ghost"
              class="w-full text-zinc-500 hover:text-white hover:bg-white/5 h-10 rounded-xl"
              @click="viewOpponentCode"
            >
              <Eye class="w-4 h-4 mr-2" /> 查看对手代码
            </Button>
            <Button
              v-else
              variant="ghost"
              class="w-full text-zinc-500 hover:text-white hover:bg-white/5 h-10 rounded-xl"
              @click="addToFavorites"
            >
              <Star class="w-4 h-4 mr-2" /> 收藏本题
            </Button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  Timer,
  Play,
  Send,
  Swords,
  Activity,
  Loader2,
  Trophy,
  XCircle,
  AlertCircle,
  CheckCircle2,
  FileText,
  Info,
  RotateCcw,
  ArrowRight,
  Eye,
  Star,
} from 'lucide-vue-next'
import { Button } from '@/components/ui/button'
import ArenaExitButton from '@/components/arena/ArenaExitButton.vue'
import CodeEditor from '@/components/code/CodeEditor.vue'
import { storage } from '@/utils/storage'
import { checkRoomValidity } from '@/api/arena'

const router = useRouter()
const route = useRoute()

// --- Types ---
type BattleStatus = 'PREPARING' | 'FIGHTING' | 'VICTORY' | 'DEFEAT'
type LogType = 'INFO' | 'CODE_UPDATE' | 'TEST_FAIL' | 'TEST_PASS' | 'SUBMIT_AC'

interface BattleLog {
  id: number
  time: string
  message: string
  type: LogType
}

// --- State ---
const isVerifying = ref(true) //  安全检查 Loading
const battleStatus = ref<BattleStatus>('PREPARING')
const isRunning = ref(false)
const isSubmitting = ref(false)
const isViewingOpponentCode = ref(false)
const timer = ref(1800)
const myProgress = ref(0)
const battleLogs = ref<BattleLog[]>([])

// Editor Content
const defaultCode = `public class Solution {
    public int solve(int[] nums) {
        // 在此处编写你的代码...
        return 0;
    }
}`
const opponentMockCode = `// [OPPONENT CODE - READ ONLY]
public class Solution {
    // 对手使用了优化的二分查找算法
    public int solve(int[] nums) {
        int left = 0, right = nums.length - 1;
        while(left <= right) {
            // Complex logic implementation...
        }
        return -1;
    }
}`
const editorCode = ref(defaultCode)

// Opponent State
const opponent = ref({
  name: 'AlgorithmMaster_99',
  avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=John',
  progress: 0,
  linesOfCode: 0,
})

// Mock Problem
const problem = ref({
  id: '1024',
  title: '寻找两个正序数组的中位数',
  content: `
    <p>给定两个大小分别为 <code>m</code> 和 <code>n</code> 的正序（从小到大）数组 <code>nums1</code> 和 <code>nums2</code>。请你找出并返回这两个正序数组的 <strong>中位数</strong> 。</p>
    <p>算法的时间复杂度应该为 <code>O(log (m+n))</code> 。</p>
  `,
  examples: [{ input: 'nums1 = [1,3], nums2 = [2]', output: '2.00000' }],
})

// --- Computed ---
const formattedTime = computed(() => {
  const m = Math.floor(timer.value / 60)
    .toString()
    .padStart(2, '0')
  const s = (timer.value % 60).toString().padStart(2, '0')
  return `${m}:${s}`
})

const statusText = computed(() => {
  switch (battleStatus.value) {
    case 'PREPARING':
      return '准备就绪'
    case 'FIGHTING':
      return '对决中'
    case 'VICTORY':
      return '胜利'
    case 'DEFEAT':
      return '失败'
  }
})

const statusColor = computed(() => {
  switch (battleStatus.value) {
    case 'PREPARING':
      return 'text-zinc-500'
    case 'FIGHTING':
      return 'text-[#FF4C00] animate-pulse'
    case 'VICTORY':
      return 'text-emerald-500'
    case 'DEFEAT':
      return 'text-zinc-500'
  }
})

const systemStatusText = computed(() => {
  if (isViewingOpponentCode.value) return '只读模式'
  if (isRunning.value) return '正在运行...'
  if (isSubmitting.value) return '正在提交...'
  return '沙箱环境就绪'
})

// --- Game Logic ---
let gameTimer: number | null = null
let opponentTimer: number | null = null

// 房间安全校验
const initializeBattle = async () => {
  const roomId = route.params.roomId as string
  if (!roomId) {
    router.replace('/arena')
    return
  }

  try {
    const isValid = await checkRoomValidity(roomId)
    if (!isValid) {
      console.warn('[Arena Security] Battle room invalid or expired:', roomId)
      // [修改后]: 带上错误码 (这里使用 BATTLE_ENDED 或 ROOM_INVALID 均可，视业务语境而定)
      router.replace({
        path: '/arena',
        query: { error: 'BATTLE_ENDED' },
      })
    } else {
      isVerifying.value = false
      // 校验通过后，正式开始战斗
      startBattle()
    }
  } catch (error) {
    console.error('Room check failed:', error)
    router.replace('/arena')
  }
}

onMounted(() => {
  initializeBattle()
})

onUnmounted(() => {
  stopTimers()
})

function stopTimers() {
  if (gameTimer) clearInterval(gameTimer)
  if (opponentTimer) clearInterval(opponentTimer)
}

function startBattle() {
  battleStatus.value = 'FIGHTING'
  addLog('INFO', '对局正式开始！请开始编写代码。')

  gameTimer = window.setInterval(() => {
    if (timer.value > 0) timer.value--
    else endGame('DEFEAT')
  }, 1000)

  opponentTimer = window.setInterval(() => {
    if (battleStatus.value !== 'FIGHTING') return
    const rand = Math.random()
    if (rand < 0.6) {
      opponent.value.linesOfCode += Math.floor(Math.random() * 3 + 1)
      if (Math.random() > 0.7)
        addLog('CODE_UPDATE', `对手写了 ${opponent.value.linesOfCode} 行代码`)
    } else if (rand < 0.8) {
      const success = Math.random() > 0.5
      if (success) {
        const gain = Math.min(10, 95 - opponent.value.progress)
        if (gain > 0) {
          opponent.value.progress += gain
          addLog('TEST_PASS', `对手通过了部分测试点 (${opponent.value.progress}%)`)
        }
      } else {
        addLog('TEST_FAIL', '对手运行测试失败')
      }
    } else if (rand > 0.98 && timer.value < 1700) {
      if (Math.random() > 0.5) {
        opponent.value.progress = 100
        addLog('SUBMIT_AC', '对手提交代码 AC！')
        endGame('DEFEAT')
      }
    }
  }, 3000)
}

function endGame(result: 'VICTORY' | 'DEFEAT') {
  stopTimers()
  battleStatus.value = result
}

// --- Player Actions ---
const handleRunTest = () => {
  if (isRunning.value) return
  isRunning.value = true
  setTimeout(() => {
    const success = Math.random() > 0.2
    if (success) {
      const gain = Math.min(15, 95 - myProgress.value)
      myProgress.value += gain
      addLog('TEST_PASS', `测试通过：通过率 ${myProgress.value}%`)
    } else {
      addLog('TEST_FAIL', '测试失败：输出结果不匹配')
    }
    isRunning.value = false
  }, 1500)
}

const handleSubmit = () => {
  if (isSubmitting.value) return
  isSubmitting.value = true
  setTimeout(() => {
    myProgress.value = 100
    addLog('SUBMIT_AC', '提交通过 (AC)！恭喜！')
    isSubmitting.value = false
    endGame('VICTORY')
  }, 2500)
}

function addLog(type: LogType, message: string) {
  const now = new Date()
  const timeStr = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`
  battleLogs.value.unshift({ id: Date.now(), time: timeStr, message, type })
}

// --- Settlement Actions ---
const addToFavorites = () => alert('已添加至收藏夹')
const continueResearch = () => {
  storage.set(`draft_code_${problem.value.id}`, editorCode.value)
  // [CRITICAL]: 离开战斗，销毁现场
  router.replace(`/problems/${problem.value.id}`)
}
const viewOpponentCode = () => {
  editorCode.value = opponentMockCode
  isViewingOpponentCode.value = true
  battleStatus.value = 'PREPARING' // Hide overlay to see code
}
</script>

<style scoped>
/* 自定义滚动条 */
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.2);
}

/* 动画定义 */
.list-enter-active,
.list-leave-active {
  transition: all 0.3s ease;
}
.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateX(-10px);
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
