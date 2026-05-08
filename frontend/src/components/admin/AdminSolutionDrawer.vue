<template>
  <Teleport to="body">
    <Transition name="fade">
      <div
        v-if="modelValue"
        class="fixed inset-0 z-[100] bg-black/80 backdrop-blur-sm"
        @click="close"
      />
    </Transition>
    <Transition name="slide-right">
      <div
        v-if="modelValue"
        class="fixed top-0 right-0 bottom-0 z-[101] w-full max-w-2xl bg-white dark:bg-[#09090b] border-l border-zinc-200 dark:border-white/10 shadow-xl dark:shadow-[0_0_50px_rgba(0,0,0,0.8)] flex flex-col"
      >
        <!-- 顶部装饰条 -->
        <div
          class="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-[#FF4C00] via-[#FF4C00] to-transparent opacity-50"
        />

        <!-- 头部 -->
        <div class="relative z-10 p-6 pb-4 border-b border-zinc-100 dark:border-white/5">
          <div class="flex items-center justify-between">
            <div>
              <h3 class="text-xl font-bold text-zinc-900 dark:text-white">题解管理</h3>
              <p class="text-zinc-500 dark:text-zinc-500 text-sm mt-1">
                题目：<span class="text-zinc-700 dark:text-zinc-300">{{ problemTitle }}</span>
                <span class="text-zinc-300 dark:text-zinc-600 mx-2">|</span>
                共 <span class="text-[#FF4C00] font-medium">{{ solutions.length }}</span> 条题解
              </p>
            </div>
            <div class="flex items-center gap-2">
              <button
                class="h-8 px-3 rounded-lg flex items-center gap-2 text-xs font-medium bg-[#FF4C00] hover:bg-[#ff5f1f] text-white transition-colors"
                @click="openPublishDialog"
              >
                <Plus class="w-3.5 h-3.5" />
                发布官方题解
              </button>
              <button
                class="w-8 h-8 rounded-lg flex items-center justify-center text-zinc-500 hover:text-white hover:bg-white/5 transition-colors"
                @click="close"
              >
                <X class="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>

        <!-- 题解列表 -->
        <div class="flex-1 overflow-y-auto p-6 space-y-4">
          <!-- 加载状态 -->
          <div v-if="loading" class="flex items-center justify-center py-12">
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
            <span class="text-zinc-400 dark:text-zinc-500 text-sm ml-3">加载题解列表...</span>
          </div>

          <!-- 空状态 -->
          <div
            v-else-if="solutions.length === 0"
            class="flex flex-col items-center justify-center py-12 text-zinc-400 dark:text-zinc-500"
          >
            <FileCode class="w-12 h-12 text-zinc-300 dark:text-zinc-700 mb-3" />
            <p class="text-sm">暂无题解</p>
          </div>

          <!-- 题解列表 -->
          <div
            v-for="solution in solutions"
            :key="solution.id"
            class="border border-zinc-200 dark:border-white/10 rounded-xl bg-zinc-50 dark:bg-zinc-800/20 overflow-hidden"
          >
            <!-- 题解头部 -->
            <div
              class="flex items-center justify-between p-4 cursor-pointer hover:bg-zinc-50 dark:hover:bg-white/5 transition-colors"
              @click="toggleExpand(solution.id)"
            >
              <div class="flex-1 min-w-0">
                <div class="flex items-center gap-3">
                  <h4 class="text-sm font-medium text-zinc-900 dark:text-white truncate">
                    {{ solution.title }}
                  </h4>
                  <span
                    v-if="solution.isOfficial === 1"
                    class="px-1.5 py-0.5 text-[10px] font-bold bg-[#FF4C00]/20 text-[#FF4C00] rounded shrink-0"
                  >
                    官方
                  </span>
                  <span class="text-xs text-zinc-500 shrink-0">
                    <Eye class="w-3 h-3 inline mr-1" />
                    {{ solution.viewCount }}
                  </span>
                </div>
                <div class="flex items-center gap-2 mt-1.5">
                  <span
                    class="text-xs"
                    :class="
                      solution.isOfficial === 1 ? 'text-[#FF4C00] font-medium' : 'text-zinc-500'
                    "
                  >
                    <User class="w-3 h-3 inline mr-1" />
                    {{ solution.authorName }}
                  </span>
                  <span class="text-xs text-zinc-400 dark:text-zinc-600">{{
                    formatTime(solution.createTime)
                  }}</span>
                </div>
              </div>
              <div class="flex items-center gap-2 ml-4">
                <button
                  @click.stop="handleDelete(solution)"
                  class="p-2 text-rose-400 hover:text-rose-300 hover:bg-rose-500/10 rounded-lg transition-colors"
                  title="删除题解"
                >
                  <Trash2 class="w-4 h-4" />
                </button>
                <ChevronDown
                  class="w-4 h-4 text-zinc-500 transition-transform duration-200"
                  :class="{ 'rotate-180': expandedIds.has(solution.id) }"
                />
              </div>
            </div>

            <!-- 题解详情（展开） -->
            <Transition name="expand">
              <div
                v-if="expandedIds.has(solution.id)"
                class="border-t border-zinc-100 dark:border-white/5"
              >
                <!-- 解题思路 -->
                <div
                  v-if="solution.content"
                  class="p-4 border-b border-zinc-100 dark:border-white/5"
                >
                  <div class="flex items-center justify-between mb-2">
                    <span class="text-xs text-zinc-400 dark:text-zinc-500 font-medium"
                      >解题思路</span
                    >
                  </div>
                  <div
                    class="prose prose-sm max-w-none text-zinc-700 dark:text-zinc-300 text-sm leading-relaxed"
                    v-html="renderMarkdown(solution.content)"
                  />
                </div>

                <!-- 代码实现 -->
                <div v-if="solution.code" class="p-4">
                  <div class="flex items-center justify-between mb-2">
                    <span class="text-xs text-zinc-400 dark:text-zinc-500 font-medium"
                      >代码实现</span
                    >
                    <button
                      @click="copyCode(solution.code)"
                      class="text-xs text-zinc-500 dark:text-zinc-400 hover:text-[#FF4C00] transition-colors flex items-center gap-1"
                    >
                      <Copy class="w-3 h-3" />
                      {{ copiedId === solution.id ? '已复制' : '复制' }}
                    </button>
                  </div>
                  <pre
                    class="bg-zinc-100 dark:bg-zinc-900/50 rounded-lg p-4 overflow-x-auto text-sm font-mono text-emerald-600 dark:text-emerald-400 leading-relaxed"
                  ><code>{{ solution.code }}</code></pre>
                </div>
              </div>
            </Transition>
          </div>
        </div>
      </div>
    </Transition>

    <!-- 发布官方题解弹窗 -->
    <Transition name="fade">
      <div
        v-if="showPublishDialog"
        class="fixed inset-0 z-[102] flex items-center justify-center bg-black/80 backdrop-blur-sm"
        @click.self="closePublishDialog"
      >
        <div
          class="w-full max-w-2xl bg-white dark:bg-[#09090b] border border-zinc-200 dark:border-white/10 rounded-2xl shadow-xl dark:shadow-2xl overflow-hidden"
        >
          <!-- 弹窗头部 -->
          <div class="p-6 pb-4 border-b border-zinc-100 dark:border-white/5">
            <div class="flex items-center justify-between">
              <h3 class="text-lg font-bold text-zinc-900 dark:text-white">发布官方题解</h3>
              <button
                class="w-8 h-8 rounded-lg flex items-center justify-center text-zinc-500 hover:text-white hover:bg-white/5 transition-colors"
                @click="closePublishDialog"
              >
                <X class="w-4 h-4" />
              </button>
            </div>
          </div>

          <!-- 弹窗内容 -->
          <div class="p-6 space-y-4 max-h-[60vh] overflow-y-auto">
            <!-- 标题 -->
            <div>
              <label class="block text-xs text-zinc-500 dark:text-zinc-400 mb-2">题解标题</label>
              <input
                v-model="publishForm.title"
                type="text"
                placeholder="请输入题解标题"
                class="w-full h-10 px-4 bg-zinc-50 dark:bg-zinc-900/50 border border-zinc-200 dark:border-white/10 rounded-lg text-sm text-zinc-900 dark:text-white placeholder-zinc-400 dark:placeholder-zinc-600 focus:outline-none focus:border-[#FF4C00]/50 transition-colors"
              />
            </div>

            <!-- 解题思路 -->
            <div>
              <label class="block text-xs text-zinc-500 dark:text-zinc-400 mb-2"
                >解题思路（支持 Markdown）</label
              >
              <textarea
                v-model="publishForm.content"
                placeholder="请输入解题思路..."
                rows="6"
                class="w-full px-4 py-3 bg-zinc-50 dark:bg-zinc-900/50 border border-zinc-200 dark:border-white/10 rounded-lg text-sm text-zinc-900 dark:text-white placeholder-zinc-400 dark:placeholder-zinc-600 focus:outline-none focus:border-[#FF4C00]/50 transition-colors resize-none"
              />
            </div>

            <!-- 代码实现 -->
            <div>
              <label class="block text-xs text-zinc-500 dark:text-zinc-400 mb-2">代码实现</label>
              <textarea
                v-model="publishForm.code"
                placeholder="请输入代码实现..."
                rows="8"
                class="w-full px-4 py-3 bg-zinc-50 dark:bg-zinc-900/50 border border-zinc-200 dark:border-white/10 rounded-lg text-sm text-zinc-900 dark:text-white placeholder-zinc-400 dark:placeholder-zinc-600 focus:outline-none focus:border-[#FF4C00]/50 transition-colors resize-none font-mono"
              />
            </div>
          </div>

          <!-- 弹窗底部 -->
          <div
            class="p-6 pt-4 border-t border-zinc-100 dark:border-white/5 flex items-center justify-end gap-3"
          >
            <button
              class="h-9 px-4 rounded-lg text-sm text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-white hover:bg-zinc-50 dark:hover:bg-white/5 transition-colors"
              @click="closePublishDialog"
            >
              取消
            </button>
            <button
              class="h-9 px-6 rounded-lg text-sm font-medium bg-[#FF4C00] hover:bg-[#ff5f1f] text-white transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              :disabled="
                publishing || !publishForm.title || !publishForm.content || !publishForm.code
              "
              @click="handlePublish"
            >
              {{ publishing ? '发布中...' : '确认发布' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { X, Eye, User, Trash2, ChevronDown, Copy, FileCode, Plus } from 'lucide-vue-next'
import { getProblemSolutions, deleteSolution, publishOfficialSolution } from '@/api/admin'
import type { SolutionVO } from '@/types/api'
import { marked } from 'marked'

const props = defineProps<{
  modelValue: boolean
  problemId: number | null
  problemTitle: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  deleted: []
}>()

const loading = ref(false)
const solutions = ref<SolutionVO[]>([])
const expandedIds = ref<Set<number>>(new Set())
const copiedId = ref<number | null>(null)

// 发布官方题解相关状态
const showPublishDialog = ref(false)
const publishing = ref(false)
const publishForm = reactive({
  title: '',
  content: '',
  code: '',
})

const loadSolutions = async () => {
  if (!props.problemId) return
  loading.value = true
  try {
    solutions.value = await getProblemSolutions(props.problemId)
  } catch {
    solutions.value = []
  } finally {
    loading.value = false
  }
}

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      expandedIds.value.clear()
      copiedId.value = null
      loadSolutions()
    }
  },
)

const toggleExpand = (id: number) => {
  if (expandedIds.value.has(id)) {
    expandedIds.value.delete(id)
  } else {
    expandedIds.value.add(id)
  }
}

const handleDelete = async (solution: SolutionVO) => {
  if (!confirm(`确定要删除题解「${solution.title}」吗？此操作不可撤销。`)) return
  await deleteSolution(solution.id)
  solutions.value = solutions.value.filter((s) => s.id !== solution.id)
  expandedIds.value.delete(solution.id)
  emit('deleted')
}

const openPublishDialog = () => {
  publishForm.title = ''
  publishForm.content = ''
  publishForm.code = ''
  showPublishDialog.value = true
}

const closePublishDialog = () => {
  showPublishDialog.value = false
}

const handlePublish = async () => {
  if (!props.problemId) return
  publishing.value = true
  try {
    await publishOfficialSolution(props.problemId, {
      title: publishForm.title,
      content: publishForm.content,
      code: publishForm.code,
    })
    closePublishDialog()
    await loadSolutions()
    emit('deleted') // 触发父组件刷新
  } catch {
    // 错误处理
  } finally {
    publishing.value = false
  }
}

const copyCode = async (code: string) => {
  try {
    await navigator.clipboard.writeText(code)
    const targetId = solutions.value.find((s) => s.code === code)?.id
    if (targetId) {
      copiedId.value = targetId
      setTimeout(() => (copiedId.value = null), 2000)
    }
  } catch {
    // 降级方案
    const textarea = document.createElement('textarea')
    textarea.value = code
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
  }
}

const renderMarkdown = (content: string) => {
  try {
    return marked(content, { breaks: true })
  } catch {
    return content
  }
}

const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes} 分钟前`
  if (hours < 24) return `${hours} 小时前`
  if (days < 30) return `${days} 天前`
  return time.split(' ')[0]
}

const close = () => {
  emit('update:modelValue', false)
}
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
.slide-right-enter-active,
.slide-right-leave-active {
  transition: transform 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}
.slide-right-enter-from,
.slide-right-leave-to {
  transform: translateX(100%);
}
.expand-enter-active,
.expand-leave-active {
  transition: all 0.2s ease;
  overflow: hidden;
}
.expand-enter-from,
.expand-leave-to {
  opacity: 0;
  max-height: 0;
}
.expand-enter-to,
.expand-leave-from {
  opacity: 1;
  max-height: 1000px;
}
</style>
