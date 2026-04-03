<template>
  <div class="flex flex-col h-full w-full bg-[#1e1e1e] relative group/workspace overflow-hidden">
    <div class="flex-1 min-h-0 relative z-0">
      <splitpanes class="zeekr-theme" horizontal @resize="handleResize">
        <pane
          :size="paneSizes.editor"
          :min-size="isConsoleOpen ? 10 : 100"
          class="flex flex-col min-h-0 relative transition-[height] duration-300 ease-in-out"
        >
          <div
            v-if="readOnly && isBattleMode"
            class="absolute top-0 left-0 right-0 z-50 bg-amber-500/90 text-black text-xs font-bold px-3 py-1.5 flex items-center justify-center gap-2 shadow-lg backdrop-blur-sm"
          >
            <Eye class="w-3.5 h-3.5" />
            <span>正在查看对手代码 (只读模式)</span>
          </div>

          <div
            class="flex-1 relative overflow-hidden"
            :class="{ 'ring-2 ring-amber-500/50': readOnly && isBattleMode }"
          >
            <CodeEditor
              v-model="modelValue"
              language="java"
              class="h-full w-full"
              :is-maximized="isMaximized"
              :read-only="readOnly"
              @toggle-maximize="$emit('toggle-maximize')"
            />
          </div>
        </pane>

        <pane
          v-if="isConsoleOpen"
          :size="paneSizes.console"
          class="bg-zinc-900 flex flex-col min-h-0 border-t border-white/5 relative transition-[height] duration-300 ease-in-out z-10"
        >
          <div
            class="h-9 shrink-0 flex items-center justify-between px-2 bg-zinc-950 border-b border-white/5 select-none"
          >
            <div class="flex items-center h-full gap-1">
              <button
                v-for="tab in ['testcase', 'result'] as const"
                :key="tab"
                @click="activeConsoleTab = tab"
                class="h-full px-4 text-xs font-medium relative transition-colors flex items-center gap-2 group"
                :class="
                  activeConsoleTab === tab ? 'text-white' : 'text-zinc-500 hover:text-zinc-300'
                "
              >
                <component :is="tab === 'testcase' ? FlaskConical : Terminal" class="w-3.5 h-3.5" />
                {{ tab === 'testcase' ? '测试用例' : '执行结果' }}
                <div
                  v-if="activeConsoleTab === tab"
                  class="absolute bottom-0 left-0 right-0 h-[2px] bg-[#FF4C00] shadow-[0_-2px_6px_rgba(255,76,0,0.4)]"
                ></div>
              </button>
            </div>
            <button
              @click="toggleConsole"
              class="mr-2 p-1.5 rounded-lg text-zinc-500 hover:text-white hover:bg-white/5 transition-colors"
              title="收起控制台"
            >
              <ChevronDown class="w-4 h-4" />
            </button>
          </div>

          <div class="flex-1 overflow-y-auto custom-scrollbar p-4 bg-zinc-900/50">
            <div
              v-show="activeConsoleTab === 'testcase'"
              class="space-y-5 animate-in fade-in slide-in-from-bottom-2 duration-300 h-full flex flex-col"
            >
              <div class="flex items-center gap-2 overflow-x-auto no-scrollbar shrink-0">
                <div v-for="(c, index) in testCases" :key="index" class="flex items-center">
                  <button
                    @click="activeCaseIndex = index"
                    class="px-3 py-1.5 rounded-lg text-xs font-mono transition-all border flex items-center gap-1.5 group/tab"
                    :class="
                      activeCaseIndex === index
                        ? 'bg-zinc-800 text-white border-white/10 shadow-sm'
                        : 'border-transparent text-zinc-500 hover:bg-zinc-800/50 hover:text-zinc-300'
                    "
                  >
                    用例 {{ index + 1 }}
                    <X
                      v-if="activeCaseIndex === index && testCases.length > 1"
                      @click.stop="handleRemoveTestCase(index)"
                      class="w-3 h-3 text-zinc-500 hover:text-red-500 cursor-pointer transition-colors"
                    />
                  </button>
                </div>
                <button
                  @click="handleAddTestCase"
                  class="p-1.5 rounded-lg text-zinc-500 hover:text-white hover:bg-white/10 transition-colors border border-dashed border-zinc-700"
                  title="添加测试用例"
                  :disabled="readOnly"
                >
                  <Plus class="w-3.5 h-3.5" />
                </button>
              </div>

              <div class="space-y-4 flex-1 overflow-y-auto pr-2 pb-4">
                <template v-if="testCases[activeCaseIndex]">
                  <div
                    v-for="(_, key) in testCases[activeCaseIndex]!.inputs"
                    :key="key"
                    class="space-y-2 flex flex-col shrink-0"
                  >
                    <label
                      class="block text-xs font-medium text-zinc-500 uppercase tracking-wider font-mono pl-1"
                      >{{ key }}</label
                    >
                    <textarea
                      v-model="testCases[activeCaseIndex]!.inputs[key]"
                      class="w-full min-h-[80px] bg-black/30 border border-white/10 rounded-lg px-4 py-3 font-mono text-sm text-zinc-300 placeholder-zinc-700 focus:border-[#FF4C00] focus:ring-1 focus:ring-[#FF4C00]/20 outline-none transition-all resize-y"
                      spellcheck="false"
                      :placeholder="`请输入 ${key} 的纯数据`"
                      :disabled="readOnly"
                    ></textarea>
                  </div>
                </template>
              </div>
            </div>

            <div v-show="activeConsoleTab === 'result'" class="h-full">
              <div
                v-if="isRunning && !showJudgePanel"
                class="h-full flex flex-col items-center justify-center space-y-4"
              >
                <Loader2 class="w-8 h-8 text-[#FF4C00] animate-spin" />
                <p class="text-xs text-zinc-500 font-mono animate-pulse">正在沙箱中执行代码...</p>
              </div>
              <div
                v-else-if="runResults.length === 0"
                class="h-full flex flex-col items-center justify-center text-zinc-600 space-y-3"
              >
                <div class="w-12 h-12 rounded-2xl bg-zinc-800/50 flex items-center justify-center">
                  <Terminal class="w-6 h-6 opacity-40" />
                </div>
                <p class="text-xs font-medium">请点击“运行”以执行自测代码</p>
              </div>
              <div
                v-else
                class="animate-in fade-in slide-in-from-bottom-2 flex flex-col h-full gap-4"
              >
                <div class="flex items-center gap-2 overflow-x-auto no-scrollbar shrink-0">
                  <button
                    v-for="(res, idx) in runResults"
                    :key="idx"
                    @click="activeResultIndex = idx"
                    class="px-3 py-1.5 rounded-lg text-xs font-mono transition-all border flex items-center gap-2"
                    :class="[
                      activeResultIndex === idx
                        ? res?.status === 'AC'
                          ? 'bg-emerald-500/10 text-emerald-500 border-emerald-500/20 shadow-sm'
                          : 'bg-rose-500/10 text-rose-500 border-rose-500/20 shadow-sm'
                        : 'border-transparent text-zinc-500 hover:bg-zinc-800/50 hover:text-zinc-300',
                    ]"
                  >
                    <span
                      class="w-1.5 h-1.5 rounded-full"
                      :class="
                        res?.status === 'AC'
                          ? 'bg-emerald-500 shadow-[0_0_8px_rgba(16,185,129,0.8)]'
                          : 'bg-rose-500 shadow-[0_0_8px_rgba(244,63,94,0.8)]'
                      "
                    ></span>
                    用例 {{ idx + 1 }}
                  </button>
                </div>

                <div v-if="runResults[activeResultIndex]" class="flex-1 overflow-y-auto pr-2 pb-2">
                  <div
                    class="flex items-center gap-3 px-4 py-3 rounded-xl border mb-4"
                    :class="
                      runResults[activeResultIndex]!.status === 'AC'
                        ? 'bg-emerald-500/10 border-emerald-500/20'
                        : 'bg-rose-500/10 border-rose-500/20'
                    "
                  >
                    <span
                      class="text-sm font-bold tracking-wider"
                      :class="
                        runResults[activeResultIndex]!.status === 'AC'
                          ? 'text-emerald-500'
                          : 'text-rose-500'
                      "
                      >{{ getStatusText(runResults[activeResultIndex]!.status) }}</span
                    >
                    <span
                      class="w-[1px] h-3"
                      :class="
                        runResults[activeResultIndex]!.status === 'AC'
                          ? 'bg-emerald-500/20'
                          : 'bg-rose-500/20'
                      "
                    ></span>
                    <span
                      class="text-xs font-mono"
                      :class="
                        runResults[activeResultIndex]!.status === 'AC'
                          ? 'text-emerald-400/80'
                          : 'text-rose-400/80'
                      "
                    >
                      执行耗时: {{ runResults[activeResultIndex]!.runtime || '无' }}
                    </span>
                  </div>

                  <div
                    class="bg-black/30 border border-white/10 rounded-xl p-4 font-mono text-xs space-y-4 shadow-inner"
                  >
                    <div class="space-y-1.5">
                      <span class="text-zinc-500 block tracking-wider scale-90 origin-left"
                        >输入内容</span
                      >
                      <div
                        class="text-zinc-300 bg-zinc-800/50 border border-white/5 px-3 py-2.5 rounded-lg whitespace-pre-wrap leading-relaxed"
                      >
                        {{ runResults[activeResultIndex]!.input }}
                      </div>
                    </div>
                    <div class="space-y-1.5">
                      <span class="text-zinc-500 block tracking-wider scale-90 origin-left"
                        >实际输出</span
                      >
                      <div
                        class="bg-zinc-800/50 border border-white/5 px-3 py-2.5 rounded-lg"
                        :class="
                          runResults[activeResultIndex]!.status === 'AC'
                            ? 'text-emerald-500'
                            : 'text-rose-500'
                        "
                      >
                        {{ runResults[activeResultIndex]!.output || '无输出' }}
                      </div>
                    </div>
                    <div class="space-y-1.5">
                      <span class="text-zinc-500 block tracking-wider scale-90 origin-left"
                        >期望输出</span
                      >
                      <div
                        class="text-emerald-500 bg-zinc-800/50 border border-white/5 px-3 py-2.5 rounded-lg opacity-80"
                      >
                        {{ runResults[activeResultIndex]!.expected ?? '无 (自测模式无对照组)' }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </pane>
      </splitpanes>
    </div>

    <div
      class="h-14 shrink-0 bg-zinc-950/80 backdrop-blur-xl border-t border-white/5 px-6 flex items-center justify-between z-30"
    >
      <div class="flex items-center gap-2">
        <button
          v-if="!isConsoleOpen"
          @click="toggleConsole"
          class="flex items-center gap-2 px-3 py-1.5 rounded-lg text-zinc-400 hover:text-white hover:bg-white/5 transition-colors text-xs font-medium active:scale-95 duration-200 group"
        >
          <div class="relative">
            <SquareTerminal class="w-4 h-4 group-hover:text-[#FF4C00] transition-colors" />
            <span class="absolute -top-0.5 -right-0.5 flex h-1.5 w-1.5">
              <span
                class="animate-ping absolute inline-flex h-full w-full rounded-full bg-[#FF4C00] opacity-75"
              ></span>
              <span class="relative inline-flex rounded-full h-1.5 w-1.5 bg-[#FF4C00]"></span>
            </span>
          </div>
          <span>控制台</span>
          <ChevronUp class="w-3 h-3 ml-1 opacity-50" />
        </button>
      </div>

      <div class="flex items-center gap-3">
        <button
          @click="handleRun"
          :disabled="isRunning || isSubmitting || readOnly"
          class="px-5 py-2 rounded-lg bg-zinc-800 text-zinc-200 border border-white/5 hover:bg-zinc-700 hover:text-white transition-all font-bold text-sm flex items-center gap-2 active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed group"
        >
          <Play class="w-4 h-4 fill-zinc-400 group-hover:fill-white transition-colors" /> 运行
        </button>

        <button
          @click="handleSubmit"
          :disabled="isRunning || isSubmitting || readOnly"
          class="px-8 py-2 rounded-lg bg-[#FF4C00] text-white font-bold text-sm flex items-center gap-2 shadow-[0_0_15px_rgba(255,76,0,0.3)] hover:shadow-[0_0_25px_rgba(255,76,0,0.5)] hover:bg-[#ff5f1f] transition-all active:scale-95 disabled:opacity-50 disabled:shadow-none disabled:cursor-not-allowed"
        >
          <Loader2 v-if="isSubmitting" class="w-4 h-4 animate-spin" />
          <CloudUpload v-else class="w-4 h-4" />
          {{ isSubmitting ? '判题中...' : '提交' }}
        </button>
      </div>
    </div>

    <JudgePanel
      :show="showJudgePanel"
      :status="judgeStatus"
      :checkpoints="checkpoints"
      :mode="isBattleMode ? 'battle' : 'practice'"
      @close="showJudgePanel = false"
      @to-problems="router.push('/problems')"
      @next-problem="handleNextProblem"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Splitpanes, Pane } from 'splitpanes'
import 'splitpanes/dist/splitpanes.css'
import {
  ChevronDown,
  ChevronUp,
  Play,
  CloudUpload,
  SquareTerminal,
  Plus,
  Terminal,
  FlaskConical,
  Loader2,
  X,
  Eye,
} from 'lucide-vue-next'
import CodeEditor from '@/components/code/CodeEditor.vue'
import JudgePanel from '@/components/code/JudgePanel.vue'

import {
  runCode,
  submitCode,
  getSubmissionStatus,
  getDailyRecommendProblem,
  type Problem,
} from '@/api/problem'
import type { RunCodeResult } from '@/api/problem'

interface TestCase {
  inputs: Record<string, string>
}

const router = useRouter()
const route = useRoute()
const modelValue = defineModel<string>({ required: true })

const props = withDefaults(
  defineProps<{
    isMaximized?: boolean
    problem?: any
    readOnly?: boolean
    isBattleMode?: boolean
  }>(),
  {
    isMaximized: false,
    readOnly: false,
    isBattleMode: false,
  },
)

const emit = defineEmits([
  'toggle-maximize',
  'success',
  'submit-start',
  'submit-fail',
  'run-start',
  'run-success',
  'run-fail',
  'run-end',
])

const isConsoleOpen = ref(true)
const paneSizes = reactive({ editor: 70, console: 30 })
const lastConsoleSize = ref(30)
const activeConsoleTab = ref<'testcase' | 'result'>('testcase')

const testCases = ref<TestCase[]>([])
const activeCaseIndex = ref(0)

const parseInputString = (str: string): Record<string, string> => {
  const result: Record<string, string> = {}
  if (!str) return { 'Standard Input': '' }

  if (!str.includes('=')) {
    return { 'Standard Input': str.trim() }
  }

  const parts = str.split(/,\s*(?=[a-zA-Z_0-9]+\s*=)/)

  let parsedCount = 0
  parts.forEach((part) => {
    const match = part.match(/^([a-zA-Z_0-9]+)\s*=\s*([\s\S]*)$/)
    if (match) {
      result[match[1].trim()] = match[2].trim()
      parsedCount++
    }
  })

  if (parsedCount === 0) {
    return { 'Standard Input': str.trim() }
  }

  return result
}

watch(
  () => props.problem,
  (newProblem) => {
    if (newProblem && newProblem.examples && newProblem.examples.length > 0) {
      testCases.value = newProblem.examples.map((ex: any) => ({
        inputs: parseInputString(ex.input || ''),
      }))
    } else {
      testCases.value = [{ inputs: { 'Standard Input': '' } }]
    }
  },
  { immediate: true },
)

const isRunning = ref(false)
const isSubmitting = ref(false)
const runResults = ref<RunCodeResult[]>([])
const activeResultIndex = ref(0)

const showJudgePanel = ref(false)
const judgeStatus = ref<'judging' | 'accepted' | 'rejected'>('judging')
const checkpoints = ref<any[]>([])

let pollTimer: number | null = null

const clearPollTimer = () => {
  if (pollTimer) {
    window.clearInterval(pollTimer)
    pollTimer = null
  }
}

onUnmounted(() => {
  clearPollTimer()
})

// 监听路由变化，清空沙箱状态
watch(
  () => route.params.id,
  () => {
    runResults.value = []
    checkpoints.value = []
    showJudgePanel.value = false
    activeConsoleTab.value = 'testcase'
    activeResultIndex.value = 0
  },
)

const handleResize = (event: { min: number; max: number; size: number }[]) => {
  if (event && event.length >= 2 && event[0] && event[1]) {
    paneSizes.editor = event[0].size
    paneSizes.console = event[1].size
    if (paneSizes.console > 5) {
      lastConsoleSize.value = paneSizes.console
    } else {
      toggleConsole()
    }
  }
}

const toggleConsole = () => {
  if (isConsoleOpen.value) {
    isConsoleOpen.value = false
    paneSizes.editor = 100
    paneSizes.console = 0
  } else {
    isConsoleOpen.value = true
    const targetSize = lastConsoleSize.value < 10 ? 30 : lastConsoleSize.value
    paneSizes.editor = 100 - targetSize
    paneSizes.console = targetSize
  }
}

const handleAddTestCase = () => {
  const newInputs: Record<string, string> = {}
  if (testCases.value.length > 0) {
    Object.keys(testCases.value[0]!.inputs).forEach((key) => {
      newInputs[key] = ''
    })
  } else {
    newInputs['Standard Input'] = ''
  }
  testCases.value.push({ inputs: newInputs })
  activeCaseIndex.value = testCases.value.length - 1
}

const handleRemoveTestCase = (index: number) => {
  if (testCases.value.length <= 1) return
  testCases.value.splice(index, 1)
  if (activeCaseIndex.value >= testCases.value.length) {
    activeCaseIndex.value = testCases.value.length - 1
  } else if (activeCaseIndex.value > index) {
    activeCaseIndex.value--
  }
}

const handleRun = async () => {
  if (isRunning.value || props.readOnly) return
  const problemId = props.problem?.id || route.params.id
  if (!problemId) return

  if (!isConsoleOpen.value) toggleConsole()
  activeConsoleTab.value = 'result'
  isRunning.value = true
  runResults.value = []
  activeResultIndex.value = 0

  emit('run-start')

  try {
    const formattedInputs = testCases.value.map((tc) => Object.values(tc.inputs).join('\n'))

    const res = await runCode(problemId, {
      code: modelValue.value,
      language: 'java',
      inputs: formattedInputs,
    })
    runResults.value = res

    if (res && res.length > 0) {
      const isAllAC = res.every((r: any) => r.status === 'AC')
      if (isAllAC) {
        emit('run-success')
      } else {
        emit('run-fail')
      }
    } else {
      emit('run-fail')
    }
  } catch (error) {
    console.error('运行代码失败:', error)
    emit('run-fail')
  } finally {
    isRunning.value = false
    emit('run-end')
  }
}

const handleSubmit = async () => {
  if (isSubmitting.value || props.readOnly) return
  const problemId = props.problem?.id || route.params.id
  if (!problemId) return

  showJudgePanel.value = true
  judgeStatus.value = 'judging'
  isSubmitting.value = true
  checkpoints.value = []
  clearPollTimer()

  emit('submit-start')

  try {
    const submissionId = await submitCode(problemId, {
      code: modelValue.value,
      language: 'java',
    })

    pollTimer = window.setInterval(async () => {
      try {
        const pollRes = await getSubmissionStatus(submissionId)

        checkpoints.value = (pollRes.checkpoints || []).map((cp) => ({
          ...cp,
          status:
            cp.status === 'PENDING' ? 'pending' : cp.status === 'RUNNING' ? 'running' : cp.status,
        }))

        if (pollRes.status === 'OK') {
          clearPollTimer()
          isSubmitting.value = false

          const hasError = checkpoints.value.some(
            (cp) => cp.status !== 'AC' && cp.status !== 'pending' && cp.status !== 'running',
          )

          judgeStatus.value = hasError ? 'rejected' : 'accepted'

          if (hasError) {
            emit('submit-fail')
          } else {
            if (props.isBattleMode) {
              setTimeout(() => {
                showJudgePanel.value = false
              }, 1500)
            }
            emit('success')
          }
        }
      } catch (err) {
        console.error('轮询状态异常:', err)
        clearPollTimer()
        isSubmitting.value = false
        judgeStatus.value = 'rejected'
        emit('submit-fail')
      }
    }, 1000)
  } catch (error) {
    console.error('提交失败:', error)
    isSubmitting.value = false
    judgeStatus.value = 'rejected'
    emit('submit-fail')
  }
}

const isFetchingNext = ref(false)

const handleNextProblem = async () => {
  if (isFetchingNext.value || props.readOnly) return
  isFetchingNext.value = true

  try {
    const nextId = await getDailyRecommendProblem()

    // 如果成功获取到题目 ID，则隐藏弹窗并路由跳转
    if (nextId) {
      showJudgePanel.value = false
      router.push(`/problems/${nextId}`)
    } else {
      console.warn('获取挑战题目失败，未返回有效 ID')
    }
  } catch (err) {
    console.error('获取随机题目失败:', err)
  } finally {
    isFetchingNext.value = false
  }
}

// 🎯 汉化点：将底部的运行结果状态翻译为纯正的中文提示
const getStatusText = (status: string) => {
  if (status === 'AC') return '执行通过'
  if (status === 'WA') return '解答错误'
  if (status === 'TLE') return '超出时间限制'
  if (status === 'ERROR') return '运行错误'
  return status
}
</script>

<style scoped>
:deep(.splitpanes__pane) {
  overflow: hidden;
}

.no-scrollbar::-webkit-scrollbar {
  display: none;
}
.no-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>

<style>
.zeekr-theme.splitpanes {
  background-color: transparent !important;
}
.zeekr-theme .splitpanes__pane {
  background-color: transparent !important;
  overflow: hidden;
}
.zeekr-theme .splitpanes__splitter {
  background-color: #09090b !important;
  border: none !important;
  box-sizing: border-box;
  position: relative;
  z-index: 20;
  transition: all 0.2s ease;
}
.zeekr-theme.splitpanes--vertical > .splitpanes__splitter {
  width: 6px !important;
  border-left: 1px solid rgba(255, 255, 255, 0.05) !important;
  border-right: 1px solid rgba(255, 255, 255, 0.05) !important;
  margin-left: -1px;
}
.zeekr-theme.splitpanes--horizontal > .splitpanes__splitter {
  height: 6px !important;
  border-top: 1px solid rgba(255, 255, 255, 0.05) !important;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05) !important;
  margin-top: -1px;
}
.zeekr-theme .splitpanes__splitter::after {
  content: '';
  position: absolute;
  background-color: #ff4c00;
  opacity: 0;
  transition: opacity 0.2s ease;
  pointer-events: none;
}
.zeekr-theme.splitpanes--vertical > .splitpanes__splitter::after {
  top: 10%;
  bottom: 10%;
  left: 50%;
  width: 1px;
  transform: translateX(-50%);
}
.zeekr-theme.splitpanes--horizontal > .splitpanes__splitter::after {
  left: 10%;
  right: 10%;
  top: 50%;
  height: 1px;
  transform: translateY(-50%);
}
.zeekr-theme .splitpanes__splitter:hover::after,
.zeekr-theme .splitpanes__splitter:active::after {
  opacity: 1;
  box-shadow: 0 0 8px rgba(255, 76, 0, 0.6);
}
</style>
