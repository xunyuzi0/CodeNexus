<template>
  <Teleport to="body">
    <Transition name="fade">
      <div
        v-if="modelValue"
        class="fixed inset-0 z-[100] flex items-center justify-center bg-black/80 backdrop-blur-sm px-4"
        @click="close"
      >
        <div
          class="w-full max-w-4xl max-h-[90vh] overflow-y-auto rounded-3xl bg-[#09090b] border border-white/10 shadow-[0_0_50px_rgba(0,0,0,0.8)] relative"
          v-motion
          :initial="{ opacity: 0, scale: 0.95, y: 20 }"
          :enter="{
            opacity: 1,
            scale: 1,
            y: 0,
            transition: { type: 'spring', stiffness: 300, damping: 25 },
          }"
          @click.stop
        >
          <!-- 顶部装饰条 -->
          <div
            class="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-transparent via-[#FF4C00] to-transparent opacity-50"
          />
          <div
            class="absolute -top-20 -right-20 w-40 h-40 bg-[#FF4C00]/10 blur-[50px] rounded-full pointer-events-none"
          />

          <!-- 头部 -->
          <div class="relative z-10 p-6 pb-0">
            <div class="flex items-center justify-between mb-6">
              <h3 class="text-xl font-bold text-white">
                {{ isEdit ? '编辑题目' : '创建题目' }}
              </h3>
              <button
                class="w-8 h-8 rounded-lg flex items-center justify-center text-zinc-500 hover:text-white hover:bg-white/5 transition-colors"
                @click="close"
              >
                <X class="w-4 h-4" />
              </button>
            </div>

            <!-- 加载状态 -->
            <div v-if="detailLoading" class="flex items-center justify-center py-12">
              <svg class="w-6 h-6 animate-spin text-[#FF4C00]" viewBox="0 0 24 24" fill="none">
                <circle
                  class="opacity-25"
                  cx="12"
                  cy="12"
                  r="10"
                  stroke="currentColor"
                  stroke-width="4"
                />
                <path
                  class="opacity-75"
                  fill="currentColor"
                  d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"
                />
              </svg>
              <span class="text-zinc-500 text-sm ml-3">加载题目详情...</span>
            </div>

            <!-- 详情加载失败提示 -->
            <div
              v-if="detailError && !detailLoading"
              class="mx-6 mb-4 px-4 py-2.5 bg-yellow-500/10 border border-yellow-500/30 rounded-xl text-yellow-400 text-xs"
            >
              无法获取完整题目详情（描述、示例等），已用列表数据回填。请检查后端服务是否已重启。
            </div>
          </div>

          <!-- 表单 -->
          <form
            v-if="!detailLoading"
            class="relative z-10 px-6 pb-6 space-y-5"
            @submit.prevent="handleSubmit"
          >
            <!-- 题目标题 -->
            <div>
              <label class="block text-sm text-zinc-400 mb-1.5"
                >题目标题 <span class="text-red-400">*</span></label
              >
              <input
                v-model="form.title"
                type="text"
                required
                placeholder="请输入题目标题"
                class="w-full px-4 py-2.5 bg-zinc-800/50 border border-white/10 rounded-xl text-white text-sm placeholder-zinc-600 outline-none focus:border-[#FF4C00]/50 focus:ring-1 focus:ring-[#FF4C00]/20 transition-colors"
              />
            </div>

            <!-- 题目描述 -->
            <div>
              <label class="block text-sm text-zinc-400 mb-1.5"
                >题目描述 <span class="text-red-400">*</span></label
              >
              <textarea
                v-model="form.description"
                :required="!isEdit"
                rows="8"
                placeholder="支持 Markdown 格式，如：&#10;&#10;## 题目描述&#10;给定一个整数数组 nums...&#10;&#10;## 示例&#10;**输入:** nums = [1,2,3]&#10;**输出:** 6"
                class="w-full px-4 py-2.5 bg-zinc-800/50 border border-white/10 rounded-xl text-white text-sm placeholder-zinc-600 outline-none focus:border-[#FF4C00]/50 focus:ring-1 focus:ring-[#FF4C00]/20 transition-colors resize-y font-mono"
              />
            </div>

            <!-- 难度 & 状态 -->
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm text-zinc-400 mb-1.5"
                  >难度 <span class="text-red-400">*</span></label
                >
                <div class="relative" ref="difficultyDropdownRef">
                  <button
                    type="button"
                    @click="showDifficultyDropdown = !showDifficultyDropdown"
                    class="w-full flex items-center justify-between px-4 py-2.5 rounded-xl text-sm border transition-all"
                    :class="
                      currentDifficulty
                        ? `${currentDifficulty.bg} ${currentDifficulty.border} ${currentDifficulty.color}`
                        : 'bg-zinc-800/50 border-white/10 text-zinc-400'
                    "
                  >
                    <span class="font-bold">{{ currentDifficulty?.label || '选择难度' }}</span>
                    <ChevronDown
                      class="w-4 h-4 transition-transform duration-200"
                      :class="{ 'rotate-180': showDifficultyDropdown }"
                    />
                  </button>
                  <Transition name="dropdown">
                    <div
                      v-if="showDifficultyDropdown"
                      class="absolute left-0 right-0 top-full mt-2 bg-zinc-900/95 backdrop-blur-xl border border-white/10 rounded-xl p-1.5 shadow-2xl shadow-black/50 z-50"
                    >
                      <button
                        v-for="diff in difficultyOptions"
                        :key="diff.value"
                        type="button"
                        @click="selectDifficulty(diff.value)"
                        class="w-full flex items-center gap-2.5 px-3 py-2.5 rounded-lg text-sm font-bold transition-all duration-200"
                        :class="
                          form.difficulty === diff.value
                            ? `${diff.bg} ${diff.border} ${diff.color}`
                            : 'text-zinc-400 hover:bg-white/5 hover:text-zinc-200'
                        "
                      >
                        {{ diff.label }}
                      </button>
                    </div>
                  </Transition>
                </div>
              </div>
              <div>
                <label class="block text-sm text-zinc-400 mb-1.5">状态</label>
                <div class="relative" ref="statusDropdownRef">
                  <button
                    type="button"
                    @click="showStatusDropdown = !showStatusDropdown"
                    class="w-full flex items-center justify-between px-4 py-2.5 rounded-xl text-sm border transition-all"
                    :class="
                      currentStatus
                        ? `${currentStatus.bg} ${currentStatus.border} ${currentStatus.color}`
                        : 'bg-zinc-800/50 border-white/10 text-zinc-400'
                    "
                  >
                    <span class="font-bold">{{ currentStatus?.label || '选择状态' }}</span>
                    <ChevronDown
                      class="w-4 h-4 transition-transform duration-200"
                      :class="{ 'rotate-180': showStatusDropdown }"
                    />
                  </button>
                  <Transition name="dropdown">
                    <div
                      v-if="showStatusDropdown"
                      class="absolute left-0 right-0 top-full mt-2 bg-zinc-900/95 backdrop-blur-xl border border-white/10 rounded-xl p-1.5 shadow-2xl shadow-black/50 z-50"
                    >
                      <button
                        v-for="st in statusOptions"
                        :key="st.value"
                        type="button"
                        @click="selectStatus(st.value)"
                        class="w-full flex items-center gap-2.5 px-3 py-2.5 rounded-lg text-sm font-bold transition-all duration-200"
                        :class="
                          form.status === st.value
                            ? `${st.bg} ${st.border} ${st.color}`
                            : 'text-zinc-400 hover:bg-white/5 hover:text-zinc-200'
                        "
                      >
                        {{ st.label }}
                      </button>
                    </div>
                  </Transition>
                </div>
              </div>
            </div>

            <!-- 标签 -->
            <div>
              <label class="block text-sm text-zinc-400 mb-1.5">标签</label>
              <input
                v-model="tagsInput"
                type="text"
                placeholder="多个标签用逗号分隔，如: 数组,双指针,排序"
                class="w-full px-4 py-2.5 bg-zinc-800/50 border border-white/10 rounded-xl text-white text-sm placeholder-zinc-600 outline-none focus:border-[#FF4C00]/50 focus:ring-1 focus:ring-[#FF4C00]/20 transition-colors"
              />
              <div v-if="parsedTags.length" class="flex flex-wrap gap-2 mt-2">
                <span
                  v-for="tag in parsedTags"
                  :key="tag"
                  class="text-xs px-2 py-0.5 bg-[#FF4C00]/10 text-[#FF4C00] rounded-full"
                >
                  {{ tag }}
                </span>
              </div>
            </div>

            <!-- 时间限制 & 内存限制 -->
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm text-zinc-400 mb-1.5">时间限制 (ms)</label>
                <input
                  v-model.number="form.timeLimit"
                  type="number"
                  min="100"
                  max="10000"
                  placeholder="默认 1000"
                  class="w-full px-4 py-2.5 bg-zinc-800/50 border border-white/10 rounded-xl text-white text-sm placeholder-zinc-600 outline-none focus:border-[#FF4C00]/50 focus:ring-1 focus:ring-[#FF4C00]/20 transition-colors"
                />
              </div>
              <div>
                <label class="block text-sm text-zinc-400 mb-1.5">内存限制 (MB)</label>
                <input
                  v-model.number="form.memoryLimit"
                  type="number"
                  min="16"
                  max="512"
                  placeholder="默认 256"
                  class="w-full px-4 py-2.5 bg-zinc-800/50 border border-white/10 rounded-xl text-white text-sm placeholder-zinc-600 outline-none focus:border-[#FF4C00]/50 focus:ring-1 focus:ring-[#FF4C00]/20 transition-colors"
                />
              </div>
            </div>

            <!-- 示例测试用例 -->
            <div>
              <div class="flex items-center justify-between mb-1.5">
                <label class="block text-sm text-zinc-400">示例测试用例</label>
                <button
                  type="button"
                  @click="addExample"
                  class="text-xs px-3 py-1 bg-[#FF4C00]/10 text-[#FF4C00] rounded-lg hover:bg-[#FF4C00]/20 transition-colors flex items-center gap-1"
                >
                  <Plus class="w-3 h-3" />
                  添加示例
                </button>
              </div>

              <div
                v-if="form.examples.length === 0"
                class="text-zinc-600 text-xs py-4 text-center border border-dashed border-white/10 rounded-xl"
              >
                暂无示例，点击上方按钮添加
              </div>

              <div
                v-for="(example, idx) in form.examples"
                :key="idx"
                class="border border-white/10 rounded-xl p-4 mb-3 space-y-3 bg-zinc-800/20"
              >
                <div class="flex items-center justify-between">
                  <span class="text-xs text-zinc-500 font-medium">示例 {{ idx + 1 }}</span>
                  <button
                    type="button"
                    @click="removeExample(idx)"
                    class="text-xs text-rose-400 hover:text-rose-300 transition-colors"
                  >
                    删除
                  </button>
                </div>
                <div class="grid grid-cols-2 gap-3">
                  <div>
                    <label class="block text-xs text-zinc-500 mb-1">输入</label>
                    <textarea
                      v-model="example.input"
                      rows="2"
                      placeholder="如: [1,2,3]"
                      class="w-full px-3 py-2 bg-zinc-800/50 border border-white/10 rounded-lg text-white text-xs placeholder-zinc-600 outline-none focus:border-[#FF4C00]/50 transition-colors resize-y font-mono"
                    />
                  </div>
                  <div>
                    <label class="block text-xs text-zinc-500 mb-1">输出</label>
                    <textarea
                      v-model="example.output"
                      rows="2"
                      placeholder="如: 6"
                      class="w-full px-3 py-2 bg-zinc-800/50 border border-white/10 rounded-lg text-white text-xs placeholder-zinc-600 outline-none focus:border-[#FF4C00]/50 transition-colors resize-y font-mono"
                    />
                  </div>
                </div>
                <div>
                  <label class="block text-xs text-zinc-500 mb-1">解释（可选）</label>
                  <input
                    v-model="example.explanation"
                    type="text"
                    placeholder="如: 1 + 2 + 3 = 6"
                    class="w-full px-3 py-2 bg-zinc-800/50 border border-white/10 rounded-lg text-white text-xs placeholder-zinc-600 outline-none focus:border-[#FF4C00]/50 transition-colors"
                  />
                </div>
              </div>
            </div>

            <!-- 判题检测点（仅编辑模式） -->
            <div v-if="isEdit" class="border-t border-white/5 pt-5">
              <div class="flex items-center justify-between mb-3">
                <div>
                  <label class="block text-sm text-zinc-400">判题检测点</label>
                  <p class="text-xs text-zinc-600 mt-0.5">用户提交后用于最终判定的隐藏测试用例</p>
                </div>
                <button
                  type="button"
                  @click="addTestcaseItem"
                  class="text-xs px-3 py-1 bg-emerald-500/10 text-emerald-400 rounded-lg hover:bg-emerald-500/20 transition-colors flex items-center gap-1"
                >
                  <Plus class="w-3 h-3" />
                  添加检测点
                </button>
              </div>

              <div v-if="testcaseLoading" class="text-zinc-600 text-xs py-4 text-center">
                加载检测点中...
              </div>

              <div
                v-else-if="testcases.length === 0"
                class="text-zinc-600 text-xs py-4 text-center border border-dashed border-white/10 rounded-xl"
              >
                暂无检测点，点击上方按钮添加
              </div>

              <div
                v-for="(tc, idx) in testcases"
                :key="idx"
                class="border border-white/10 rounded-xl p-4 mb-3 space-y-3 bg-zinc-800/20"
              >
                <div class="flex items-center justify-between">
                  <span class="text-xs text-zinc-500 font-medium">
                    检测点 {{ idx + 1 }}
                    <span v-if="tc.id" class="text-zinc-700 ml-1">#{{ tc.id }}</span>
                  </span>
                  <div class="flex items-center gap-2">
                    <button
                      v-if="!tc._editing"
                      type="button"
                      @click="tc._editing = true"
                      class="text-xs text-zinc-400 hover:text-white transition-colors"
                    >
                      编辑
                    </button>
                    <button
                      v-if="tc._editing"
                      type="button"
                      @click="saveTestcaseItem(idx)"
                      class="text-xs text-emerald-400 hover:text-emerald-300 transition-colors"
                    >
                      保存
                    </button>
                    <button
                      type="button"
                      @click="removeTestcaseItem(idx)"
                      class="text-rose-400 hover:text-rose-300 transition-colors"
                    >
                      <Trash2 class="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>
                <div class="grid grid-cols-2 gap-3">
                  <div>
                    <label class="block text-xs text-zinc-500 mb-1">输入</label>
                    <textarea
                      v-model="tc.inputData"
                      :disabled="!tc._editing"
                      rows="2"
                      placeholder="如: 3&#10;1 2 3"
                      class="w-full px-3 py-2 bg-zinc-800/50 border border-white/10 rounded-lg text-white text-xs placeholder-zinc-600 outline-none focus:border-[#FF4C00]/50 transition-colors resize-y font-mono disabled:opacity-50"
                    />
                  </div>
                  <div>
                    <label class="block text-xs text-zinc-500 mb-1">期望输出</label>
                    <textarea
                      v-model="tc.expectedOutput"
                      :disabled="!tc._editing"
                      rows="2"
                      placeholder="如: 6"
                      class="w-full px-3 py-2 bg-zinc-800/50 border border-white/10 rounded-lg text-white text-xs placeholder-zinc-600 outline-none focus:border-[#FF4C00]/50 transition-colors resize-y font-mono disabled:opacity-50"
                    />
                  </div>
                </div>
              </div>
            </div>

            <!-- 底部按钮 -->
            <div class="flex items-center gap-4 pt-2">
              <button
                type="button"
                class="flex-1 py-3 rounded-xl border border-white/10 text-zinc-400 hover:text-white hover:bg-white/5 transition-colors text-sm font-medium"
                @click="close"
              >
                取消
              </button>
              <button
                type="submit"
                class="flex-1 py-3 rounded-xl bg-[#FF4C00] text-white font-bold text-sm tracking-wide shadow-[0_0_20px_rgba(255,76,0,0.3)] hover:bg-[#ff5f1f] hover:shadow-[0_0_30px_rgba(255,76,0,0.5)] transition-all transform active:scale-95"
              >
                {{ isEdit ? '保存修改' : '创建题目' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { X, Plus, Trash2, ChevronDown } from 'lucide-vue-next'
import { onClickOutside } from '@vueuse/core'
import type { AdminProblemVO, ProblemUpsertRequest } from '@/types/api'
import {
  getAdminProblemDetail,
  getProblemTestcases,
  addTestcase,
  updateTestcase,
  deleteTestcase,
} from '@/api/admin'

const props = defineProps<{
  modelValue: boolean
  problem: AdminProblemVO | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [data: ProblemUpsertRequest]
}>()

const isEdit = computed(() => !!props.problem)
const detailLoading = ref(false)
const detailError = ref(false)

// --- 自定义下拉框 ---
const showDifficultyDropdown = ref(false)
const showStatusDropdown = ref(false)
const difficultyDropdownRef = ref<HTMLElement | null>(null)
const statusDropdownRef = ref<HTMLElement | null>(null)

onClickOutside(difficultyDropdownRef, () => (showDifficultyDropdown.value = false))
onClickOutside(statusDropdownRef, () => (showStatusDropdown.value = false))

const difficultyOptions = [
  {
    label: 'Easy',
    value: 1,
    color: 'text-emerald-400',
    bg: 'bg-emerald-500/15',
    border: 'border-emerald-500/40',
  },
  {
    label: 'Medium',
    value: 2,
    color: 'text-amber-400',
    bg: 'bg-amber-500/15',
    border: 'border-amber-500/40',
  },
  {
    label: 'Hard',
    value: 3,
    color: 'text-rose-400',
    bg: 'bg-rose-500/15',
    border: 'border-rose-500/40',
  },
]

const statusOptions = [
  {
    label: '草稿',
    value: 0,
    color: 'text-zinc-400',
    bg: 'bg-zinc-500/15',
    border: 'border-zinc-500/40',
  },
  {
    label: '已发布',
    value: 1,
    color: 'text-emerald-400',
    bg: 'bg-emerald-500/15',
    border: 'border-emerald-500/40',
  },
]

const currentDifficulty = computed(() =>
  difficultyOptions.find((d) => d.value === form.value.difficulty),
)
const currentStatus = computed(() => statusOptions.find((s) => s.value === form.value.status))

const selectDifficulty = (value: number) => {
  form.value.difficulty = value
  showDifficultyDropdown.value = false
}

const selectStatus = (value: number) => {
  form.value.status = value
  showStatusDropdown.value = false
}

interface Example {
  input: string
  output: string
  explanation: string
}

const getDefaultForm = () => ({
  title: '',
  description: '',
  difficulty: 1,
  tags: [] as string[],
  timeLimit: 1000,
  memoryLimit: 256,
  status: 0,
  examples: [] as Example[],
})

const form = ref(getDefaultForm())
const tagsInput = ref('')

const parsedTags = computed(() =>
  tagsInput.value
    .split(/[,，]/)
    .map((t) => t.trim())
    .filter(Boolean),
)

const addExample = () => {
  form.value.examples.push({ input: '', output: '', explanation: '' })
}

const removeExample = (idx: number) => {
  form.value.examples.splice(idx, 1)
}

// --- 判题检测点管理（仅编辑模式） ---
interface TestcaseItem {
  id?: number
  inputData: string
  expectedOutput: string
  isPublic: number
  sortOrder: number
  _editing?: boolean
}

const testcases = ref<TestcaseItem[]>([])
const testcaseLoading = ref(false)

const loadTestcases = async (problemId: number) => {
  testcaseLoading.value = true
  try {
    const list = await getProblemTestcases(problemId)
    testcases.value = (list ?? []).map((t: any) => ({
      id: t.id,
      inputData: t.inputData ?? '',
      expectedOutput: t.expectedOutput ?? '',
      isPublic: t.isPublic ?? 0,
      sortOrder: t.sortOrder ?? 0,
    }))
  } catch {
    testcases.value = []
  } finally {
    testcaseLoading.value = false
  }
}

const addTestcaseItem = () => {
  testcases.value.push({
    inputData: '',
    expectedOutput: '',
    isPublic: 0,
    sortOrder: testcases.value.length,
    _editing: true,
  })
}

const removeTestcaseItem = async (idx: number) => {
  const tc = testcases.value[idx]
  if (!tc) return
  if (tc.id) {
    await deleteTestcase(tc.id)
  }
  testcases.value.splice(idx, 1)
}

const saveTestcaseItem = async (idx: number) => {
  const tc = testcases.value[idx]
  if (!tc || !props.problem) return
  const data = {
    inputData: tc.inputData,
    expectedOutput: tc.expectedOutput,
    isPublic: tc.isPublic,
    sortOrder: idx,
  }
  if (tc.id) {
    await updateTestcase(tc.id, data)
  } else {
    const newId = await addTestcase(props.problem.id, data)
    tc.id = newId
  }
  tc._editing = false
}

// 编辑模式下回填表单
watch(
  () => props.modelValue,
  async (visible) => {
    if (visible) {
      detailError.value = false
      if (props.problem) {
        detailLoading.value = true
        try {
          const detail = await getAdminProblemDetail(props.problem.id)
          form.value = {
            title: detail.title,
            description: detail.description ?? '',
            difficulty: detail.difficulty,
            tags: detail.tags ?? [],
            timeLimit: detail.timeLimit ?? 1000,
            memoryLimit: detail.memoryLimit ?? 256,
            status: detail.status,
            examples: (detail.examples ?? []).map((e) => ({
              input: e.input ?? '',
              output: e.output ?? '',
              explanation: e.explanation ?? '',
            })),
          }
          tagsInput.value = (detail.tags ?? []).join(',')
          await loadTestcases(props.problem.id)
        } catch (err) {
          console.error('[AdminProblemForm] 获取题目详情失败:', err)
          detailError.value = true
          // 降级：用列表数据回填
          form.value = {
            title: props.problem.title,
            description: '',
            difficulty: props.problem.difficulty,
            tags: props.problem.tags ?? [],
            timeLimit: 1000,
            memoryLimit: 256,
            status: props.problem.status,
            examples: [],
          }
          tagsInput.value = (props.problem.tags ?? []).join(',')
        } finally {
          detailLoading.value = false
        }
      } else {
        form.value = getDefaultForm()
        tagsInput.value = ''
      }
    }
  },
)

const close = () => {
  emit('update:modelValue', false)
}

const handleSubmit = () => {
  const payload: ProblemUpsertRequest = {
    title: form.value.title,
    description: form.value.description,
    difficulty: form.value.difficulty,
    tags: parsedTags.value,
    timeLimit: form.value.timeLimit,
    memoryLimit: form.value.memoryLimit,
    status: form.value.status,
    examples: form.value.examples.filter((e) => e.input.trim() || e.output.trim()),
  }
  emit('submit', payload)
}
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}
.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px) scale(0.95);
}
</style>
