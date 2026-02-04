<template>
  <div class="h-screen w-full flex flex-col p-6 overflow-hidden relative">
    <ArenaExitButton :needs-confirm="true" />

    <div
      class="absolute top-6 left-1/2 -translate-x-1/2 z-40 flex items-center gap-3 px-6 py-2 rounded-full bg-zinc-900/80 backdrop-blur-xl border border-white/10 shadow-2xl"
      :class="timer < 60 ? 'border-red-500/50 animate-pulse' : ''"
    >
      <Timer class="w-5 h-5" :class="timer < 60 ? 'text-red-500' : 'text-[#FF4C00]'" />
      <span class="font-mono text-xl font-bold tracking-widest text-white tabular-nums">
        {{ formattedTime }}
      </span>
    </div>

    <div class="flex-1 grid grid-cols-[20fr_55fr_25fr] gap-6 min-h-0 pt-12">
      <section
        class="flex flex-col h-full bg-zinc-900/40 backdrop-blur-md border border-white/5 rounded-2xl overflow-hidden group hover:border-white/10 transition-colors"
      >
        <div class="p-4 border-b border-white/5 bg-white/[0.02]">
          <div class="flex items-center gap-2 mb-2">
            <span
              class="px-2 py-0.5 rounded text-[10px] font-bold bg-emerald-500/10 text-emerald-500 border border-emerald-500/20"
              >EASY</span
            >
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
            <p class="text-xs font-bold text-zinc-500 uppercase">Example {{ idx + 1 }}</p>
            <div class="bg-black/40 rounded-lg p-3 border border-white/5 font-mono text-xs">
              <div class="mb-1">
                <span class="text-zinc-500 select-none">Input: </span
                ><span class="text-zinc-300">{{ example.input }}</span>
              </div>
              <div>
                <span class="text-zinc-500 select-none">Output: </span
                ><span class="text-[#FF4C00]">{{ example.output }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="flex flex-col h-full relative">
        <div
          class="flex-1 bg-black/60 backdrop-blur-sm border border-white/10 rounded-2xl overflow-hidden flex flex-col shadow-2xl"
        >
          <div
            class="h-10 border-b border-white/5 bg-white/[0.02] flex items-center justify-between px-4"
          >
            <div class="flex items-center gap-4">
              <div
                class="flex items-center gap-2 text-xs text-zinc-400 hover:text-white cursor-pointer transition-colors"
              >
                <Code2 class="w-4 h-4" />
                <span>Java (JDK 17)</span>
              </div>
            </div>
            <div class="flex items-center gap-2">
              <Settings
                class="w-4 h-4 text-zinc-500 hover:text-white cursor-pointer transition-colors"
              />
            </div>
          </div>

          <div class="flex-1 relative group">
            <div id="monaco-editor" class="absolute inset-0 p-4 font-mono text-sm text-zinc-400">
              // Write your solution here...
              <br />public class Solution { <br />&nbsp;&nbsp;public int solve(int[] nums) {
              <br />&nbsp;&nbsp;&nbsp;&nbsp;return 0; <br />&nbsp;&nbsp;} <br />}
            </div>
            <div
              class="absolute inset-0 pointer-events-none border-2 border-[#FF4C00]/0 group-hover:border-[#FF4C00]/10 transition-colors rounded-xl"
            ></div>
          </div>
        </div>

        <div class="h-16 mt-4 flex items-center justify-between">
          <div class="flex items-center gap-4 text-xs font-mono text-zinc-500">
            <div class="flex items-center gap-1.5">
              <div class="w-2 h-2 rounded-full bg-emerald-500"></div>
              <span>Sandbox Ready</span>
            </div>
          </div>

          <div class="flex items-center gap-4">
            <Button variant="ghost" class="text-zinc-400 hover:text-white hover:bg-white/5">
              <Play class="w-4 h-4 mr-2" /> 运行测试
            </Button>

            <Button
              class="bg-[#FF4C00] hover:bg-[#ff5f1f] text-white font-bold px-8 shadow-[0_0_20px_rgba(255,76,0,0.3)] hover:shadow-[0_0_30px_rgba(255,76,0,0.5)] transition-all active:scale-95"
            >
              <Send class="w-4 h-4 mr-2" /> 提交代码
            </Button>
          </div>
        </div>
      </section>

      <section class="flex flex-col h-full gap-6">
        <div
          class="bg-zinc-900/60 backdrop-blur-md border border-white/5 rounded-2xl p-5 relative overflow-hidden"
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
                    class="w-full h-full rounded-full bg-emerald-400 animate-ping opacity-75"
                  ></div>
                </div>
              </div>
              <div>
                <h3 class="text-sm font-bold text-white">{{ opponent.name }}</h3>
                <p class="text-[10px] text-zinc-500 font-mono tracking-wider">RANK 1,204</p>
              </div>
            </div>
            <Swords class="w-6 h-6 text-zinc-600" />
          </div>

          <div class="space-y-4">
            <div class="space-y-1.5">
              <div class="flex justify-between text-[10px] uppercase font-bold tracking-wider">
                <span class="text-[#FF4C00]">YOU</span>
                <span class="text-zinc-400">{{ myProgress }}%</span>
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
                <span class="text-rose-500">OPPONENT</span>
                <span class="text-zinc-400">{{ opponent.progress }}%</span>
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
          class="flex-1 bg-black/40 backdrop-blur-md border border-white/5 rounded-2xl p-4 flex flex-col min-h-0"
        >
          <div class="flex items-center gap-2 mb-3 pb-2 border-b border-white/5">
            <Activity class="w-3 h-3 text-zinc-500" />
            <span class="text-xs font-bold text-zinc-500 uppercase tracking-widest">Live Feed</span>
          </div>

          <div class="flex-1 overflow-y-auto custom-scrollbar space-y-3 font-mono text-xs">
            <TransitionGroup name="list">
              <div v-for="log in battleLogs" :key="log.id" class="flex gap-2 items-start">
                <span class="text-zinc-600 shrink-0">[{{ log.time }}]</span>
                <span :class="logColorMap[log.type]">{{ log.message }}</span>
              </div>
            </TransitionGroup>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Timer, Code2, Settings, Play, Send, Swords, Activity } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'
import ArenaExitButton from '@/components/arena/ArenaExitButton.vue' // [NEW]

// --- Mock Data: Problem ---
const problem = ref({
  id: '1024',
  title: '寻找两个正序数组的中位数',
  content: `
    <p>给定两个大小分别为 <code>m</code> 和 <code>n</code> 的正序（从小到大）数组 <code>nums1</code> 和 <code>nums2</code>。请你找出并返回这两个正序数组的 <strong>中位数</strong> 。</p>
    <p>算法的时间复杂度应该为 <code>O(log (m+n))</code> 。</p>
    <ul>
      <li>数组长度范围: [0, 1000]</li>
      <li>数值范围: [-10^6, 10^6]</li>
    </ul>
  `,
  examples: [
    { input: 'nums1 = [1,3], nums2 = [2]', output: '2.00000' },
    { input: 'nums1 = [1,2], nums2 = [3,4]', output: '2.50000' },
  ],
})

// --- Mock Data: Battle State ---
const timer = ref(1800)
const myProgress = ref(0)
const opponent = ref({
  name: 'AlgorithmMaster_99',
  avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=John',
  progress: 0,
})

// --- Mock Data: Logs ---
type LogType = 'info' | 'success' | 'danger' | 'warning'
interface BattleLog {
  id: number
  time: string
  message: string
  type: LogType
}

const battleLogs = ref<BattleLog[]>([
  { id: 1, time: '10:00', message: 'Battle Started!', type: 'info' },
  { id: 2, time: '10:02', message: 'You compiled successfully.', type: 'success' },
])

const logColorMap: Record<LogType, string> = {
  info: 'text-zinc-400',
  success: 'text-emerald-400',
  danger: 'text-rose-400',
  warning: 'text-amber-400',
}

// --- Logic: Timer & Simulation ---
let intervalId: number
const formattedTime = computed(() => {
  const m = Math.floor(timer.value / 60)
    .toString()
    .padStart(2, '0')
  const s = (timer.value % 60).toString().padStart(2, '0')
  return `${m}:${s}`
})

onMounted(() => {
  intervalId = window.setInterval(() => {
    if (timer.value > 0) timer.value--
    if (Math.random() > 0.8 && opponent.value.progress < 100) {
      opponent.value.progress += 5
      addLog(`Opponent passed test case #${Math.floor(opponent.value.progress / 10)}`, 'warning')
    }
    if (Math.random() > 0.9 && myProgress.value < 100) {
      myProgress.value += 5
    }
  }, 1000)
})

onUnmounted(() => {
  clearInterval(intervalId)
})

function addLog(msg: string, type: LogType = 'info') {
  const now = new Date()
  const timeStr = `${now.getHours()}:${now.getMinutes().toString().padStart(2, '0')}`
  battleLogs.value.unshift({
    id: Date.now(),
    time: timeStr,
    message: msg,
    type,
  })
}
</script>

<style scoped>
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
.list-enter-active,
.list-leave-active {
  transition: all 0.3s ease;
}
.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateX(-10px);
}
</style>
