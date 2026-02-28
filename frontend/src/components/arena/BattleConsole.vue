<template>
  <Transition name="slide-up">
    <div
      v-if="modelValue"
      class="absolute bottom-0 left-0 right-0 z-40 h-72 flex flex-col bg-[#09090b]/95 backdrop-blur-xl border-t border-white/10 shadow-[0_-10px_40px_rgba(0,0,0,0.5)] font-sans"
    >
      <div
        class="h-10 shrink-0 flex items-center justify-between px-4 border-b border-white/5 bg-white/[0.02]"
      >
        <div class="flex items-center h-full">
          <div v-if="mode === 'TEST'" class="flex h-full">
            <button
              v-for="tab in ['INPUT', 'OUTPUT']"
              :key="tab"
              @click="activeTab = tab"
              class="relative h-full px-4 text-xs font-bold tracking-widest uppercase transition-colors duration-300 flex items-center gap-2"
              :class="activeTab === tab ? 'text-white' : 'text-zinc-500 hover:text-zinc-300'"
            >
              <Terminal v-if="tab === 'INPUT'" class="w-3.5 h-3.5" />
              <Activity v-else class="w-3.5 h-3.5" />
              {{ tab === 'INPUT' ? '测试用例' : '运行结果' }}

              <div
                v-if="activeTab === tab"
                class="absolute bottom-0 left-0 right-0 h-[2px] bg-[#FF4C00] shadow-[0_-2px_8px_rgba(255,76,0,0.5)]"
              ></div>
            </button>
          </div>

          <div v-else class="flex items-center gap-2 text-zinc-300 px-2">
            <Server class="w-4 h-4 text-[#FF4C00]" />
            <span class="text-xs font-bold tracking-widest uppercase"
              >评测详情 (Checkpoint Details)</span
            >
          </div>
        </div>

        <button
          @click="emit('update:modelValue', false)"
          class="p-1.5 rounded-md text-zinc-500 hover:text-white hover:bg-white/10 transition-all duration-200"
        >
          <ChevronDown class="w-4 h-4" />
        </button>
      </div>

      <div class="flex-1 overflow-hidden relative">
        <template v-if="mode === 'TEST'">
          <div v-show="activeTab === 'INPUT'" class="h-full w-full relative group">
            <textarea
              v-model="inputModel"
              class="w-full h-full bg-transparent resize-none p-4 font-mono text-sm text-zinc-300 focus:outline-none placeholder:text-zinc-700 custom-scrollbar selection:bg-[#FF4C00]/30"
              placeholder="请输入测试用例..."
              spellcheck="false"
            ></textarea>
            <div
              class="absolute bottom-3 right-4 text-[10px] text-zinc-600 font-mono pointer-events-none opacity-0 group-hover:opacity-100 transition-opacity"
            >
              INPUT STREAM
            </div>
          </div>

          <div
            v-show="activeTab === 'OUTPUT'"
            class="h-full w-full p-4 overflow-y-auto custom-scrollbar"
          >
            <div
              v-if="status === 'RUNNING'"
              class="h-full flex flex-col items-center justify-center gap-4"
            >
              <Loader2 class="w-8 h-8 text-[#FF4C00] animate-spin" />
              <p class="text-xs font-mono text-zinc-500 animate-pulse">Running Test Case...</p>
            </div>

            <div v-else-if="status === 'ERROR'" class="h-full flex flex-col gap-2">
              <div
                class="flex items-center gap-2 text-red-500 text-xs font-bold uppercase tracking-wider"
              >
                <AlertTriangle class="w-4 h-4" />
                <span>Execution Error</span>
              </div>
              <pre
                class="flex-1 bg-red-500/5 border border-red-500/20 rounded-lg p-3 text-red-400 font-mono text-xs overflow-auto"
                >{{ message }}</pre
              >
            </div>

            <div
              v-else-if="status === 'SUCCESS' || status === 'IDLE'"
              class="h-full flex flex-col gap-4"
            >
              <div class="flex items-center justify-between shrink-0">
                <div
                  class="flex items-center gap-2 px-3 py-1 rounded-full text-xs font-bold tracking-wider border"
                  :class="
                    isAccepted
                      ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-500'
                      : 'bg-red-500/10 border-red-500/30 text-red-500'
                  "
                >
                  <Check v-if="isAccepted" class="w-3.5 h-3.5" />
                  <X v-else class="w-3.5 h-3.5" />
                  {{ isAccepted ? 'ACCEPTED' : 'WRONG ANSWER' }}
                </div>
                <span class="text-[10px] font-mono text-zinc-600">TIME: 24ms</span>
              </div>

              <div class="grid grid-cols-2 gap-4 flex-1 min-h-0">
                <div class="flex flex-col gap-2 min-h-0">
                  <span class="text-[10px] text-zinc-500 uppercase font-bold">Your Output</span>
                  <div
                    class="flex-1 bg-zinc-900/50 border border-white/5 rounded-lg p-3 font-mono text-xs text-zinc-300 overflow-auto whitespace-pre-wrap"
                  >
                    {{ testOutput || '// No output' }}
                  </div>
                </div>
                <div class="flex flex-col gap-2 min-h-0">
                  <span class="text-[10px] text-zinc-500 uppercase font-bold">Expected Output</span>
                  <div
                    class="flex-1 bg-zinc-900/50 border border-white/5 rounded-lg p-3 font-mono text-xs text-zinc-400 overflow-auto whitespace-pre-wrap"
                  >
                    {{ expectedOutput || '// Unknown' }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>

        <template v-else>
          <div v-if="status === 'ERROR'" class="h-full p-4 flex flex-col gap-3">
            <div
              class="flex items-center gap-2 text-red-500 text-xs font-bold uppercase tracking-wider"
            >
              <AlertCircle class="w-4 h-4" />
              <span>Compilation Error</span>
            </div>
            <pre
              class="flex-1 bg-red-500/5 border border-red-500/20 rounded-lg p-4 text-red-400 font-mono text-xs overflow-auto custom-scrollbar leading-relaxed"
              >{{ message }}</pre
            >
          </div>

          <div v-else class="h-full p-4 overflow-y-auto custom-scrollbar">
            <div
              v-if="checkpoints && checkpoints.length > 0"
              class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-5 gap-3"
            >
              <div
                v-for="cp in checkpoints"
                :key="cp.id"
                class="relative h-20 rounded-lg border flex flex-col items-center justify-center gap-2 transition-all duration-300 overflow-hidden"
                :class="getCheckpointStyle(cp.status)"
              >
                <component
                  :is="getCheckpointIcon(cp.status)"
                  class="w-5 h-5"
                  :class="{ 'animate-spin': cp.status === 'PENDING' }"
                />

                <span class="text-xs font-mono font-bold tracking-wider">Test #{{ cp.id }}</span>

                <div
                  v-if="cp.status !== 'PENDING' && cp.time"
                  class="absolute bottom-1 w-full text-center text-[10px] font-mono opacity-70"
                >
                  {{ cp.time }} / {{ cp.memory }}
                </div>

                <div
                  v-if="cp.status === 'PENDING'"
                  class="absolute inset-0 bg-white/5 animate-pulse"
                ></div>
              </div>
            </div>

            <div
              v-if="status === 'RUNNING' && checkpoints?.some((c) => c.status === 'PENDING')"
              class="mt-8 flex justify-center"
            >
              <div class="flex items-center gap-2 text-xs font-mono text-zinc-500">
                <Loader2 class="w-3 h-3 animate-spin" />
                <span>Evaluating remaining test cases...</span>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import {
  ChevronDown,
  Terminal,
  Activity,
  Loader2,
  Check,
  X,
  Clock,
  AlertTriangle,
  AlertCircle,
  Server,
  Zap,
  Cpu,
} from 'lucide-vue-next'

// --- 1. Props 定义 ---
export interface Checkpoint {
  id: number
  status: 'PENDING' | 'AC' | 'WA' | 'TLE' | 'RE' | 'MLE'
  time?: string
  memory?: string
}

const props = defineProps<{
  modelValue: boolean
  mode: 'TEST' | 'SUBMIT'
  status: 'IDLE' | 'RUNNING' | 'SUCCESS' | 'ERROR'
  testInput: string
  testOutput?: string
  expectedOutput?: string
  message?: string
  checkpoints?: Checkpoint[]
}>()

const emit = defineEmits(['update:modelValue', 'update:testInput'])

// --- 2. 状态管理 ---
const activeTab = ref('INPUT') // 'INPUT' | 'OUTPUT'

// v-model binding for input
const inputModel = computed({
  get: () => props.testInput,
  set: (val) => emit('update:testInput', val),
})

// 计算是否 AC (自测模式下简单对比)
const isAccepted = computed(() => {
  if (!props.testOutput || !props.expectedOutput) return false
  return props.testOutput.trim() === props.expectedOutput.trim()
})

// 自动切换 Tab: 当开始运行或得出结果时，自动跳到输出页
watch(
  () => props.status,
  (newVal) => {
    if (
      props.mode === 'TEST' &&
      (newVal === 'RUNNING' || newVal === 'SUCCESS' || newVal === 'ERROR')
    ) {
      activeTab.value = 'OUTPUT'
    }
  },
)

// --- 3. 样式逻辑 (Checkpoint) ---
const getCheckpointStyle = (status: Checkpoint['status']) => {
  switch (status) {
    case 'PENDING':
      return 'bg-zinc-900 border-zinc-700 text-zinc-500'
    case 'AC':
      return 'bg-emerald-500/10 border-emerald-500/50 text-emerald-500 shadow-[0_0_15px_rgba(16,185,129,0.2)]'
    case 'WA':
      return 'bg-red-500/10 border-red-500/50 text-red-500 shadow-[0_0_15px_rgba(239,68,68,0.2)]'
    case 'TLE':
      return 'bg-amber-500/10 border-amber-500/50 text-amber-500'
    case 'MLE':
      return 'bg-purple-500/10 border-purple-500/50 text-purple-500'
    case 'RE':
      return 'bg-rose-500/10 border-rose-500/50 text-rose-500'
    default:
      return 'bg-zinc-900 border-zinc-800'
  }
}

const getCheckpointIcon = (status: Checkpoint['status']) => {
  switch (status) {
    case 'PENDING':
      return Loader2
    case 'AC':
      return Check
    case 'WA':
      return X
    case 'TLE':
      return Clock
    case 'MLE':
      return Cpu
    case 'RE':
      return Zap
    default:
      return Activity
  }
}
</script>

<style scoped>
/* 展开/收起动画 */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: transform 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.slide-up-enter-from,
.slide-up-leave-to {
  transform: translateY(100%);
}

/* 自定义滚动条 */
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.2);
}
</style>
