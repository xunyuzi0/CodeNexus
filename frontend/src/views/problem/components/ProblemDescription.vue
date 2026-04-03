<template>
  <div class="flex flex-col h-full min-w-0 bg-zinc-900/30 backdrop-blur-sm relative group/pane">
    <div
      class="h-10 shrink-0 flex items-center justify-between px-2 bg-zinc-950/80 border-b border-white/5 select-none backdrop-blur-md z-20"
    >
      <div class="flex items-center h-full gap-1">
        <button
          v-for="tab in filteredTabs"
          :key="tab.id"
          @click="currentTab = tab.id"
          class="relative h-full px-4 text-xs font-medium transition-all duration-300 flex items-center gap-2 group"
          :class="[
            currentTab === tab.id
              ? 'text-white'
              : 'text-zinc-500 hover:text-zinc-300 hover:bg-white/5',
          ]"
        >
          <component
            :is="tab.icon"
            class="w-3.5 h-3.5 transition-colors"
            :class="
              currentTab === tab.id ? 'text-[#FF4C00]' : 'text-zinc-600 group-hover:text-zinc-400'
            "
          />
          {{ tab.label }}

          <div
            v-if="currentTab === tab.id"
            class="absolute bottom-0 left-0 right-0 h-[2px] bg-[#FF4C00] shadow-[0_-2px_6px_rgba(255,76,0,0.4)]"
            v-motion
            :initial="{ opacity: 0, scaleX: 0 }"
            :enter="{ opacity: 1, scaleX: 1 }"
          ></div>
        </button>
      </div>

      <div class="flex items-center gap-1 pr-1">
        <button
          @click="$emit('toggle-maximize')"
          class="p-1.5 rounded-md text-zinc-500 hover:text-white hover:bg-white/10 transition-colors"
          :title="isMaximized ? '还原视图' : '最大化阅读模式'"
        >
          <Minimize2 v-if="isMaximized" class="w-3.5 h-3.5" />
          <Maximize2 v-else class="w-3.5 h-3.5" />
        </button>
      </div>
    </div>

    <div class="flex-1 overflow-y-auto custom-scrollbar p-6 relative scroll-smooth">
      <Transition name="fade" mode="out-in">
        <div
          v-if="currentTab === 'desc'"
          key="desc"
          class="space-y-6"
          v-motion-slide-visible-bottom
        >
          <div class="space-y-3 pb-4 border-b border-white/5">
            <h1 class="text-xl font-bold text-white tracking-tight leading-snug">
              {{ problem.title }}
            </h1>
            <div class="flex items-center gap-3 text-xs">
              <span
                class="px-2 py-0.5 rounded text-[10px] font-bold border tracking-wider uppercase"
                :class="[
                  problem.difficulty === 'EASY'
                    ? 'border-emerald-500/20 bg-emerald-500/10 text-emerald-500'
                    : problem.difficulty === 'MEDIUM'
                      ? 'border-amber-500/20 bg-amber-500/10 text-amber-500'
                      : 'border-rose-500/20 bg-rose-500/10 text-rose-500',
                ]"
              >
                {{ problem.difficulty }}
              </span>
              <span class="text-zinc-500 flex items-center gap-1">
                <CheckCircle2 class="w-3 h-3" /> 通过率 {{ problem.passRate }}%
              </span>
            </div>
          </div>

          <div
            class="prose prose-invert prose-sm max-w-none text-zinc-400 leading-relaxed prose-headings:text-zinc-100 prose-headings:font-bold prose-headings:tracking-tight prose-p:my-4 prose-p:leading-7 prose-strong:text-zinc-200 prose-strong:font-semibold prose-code:text-[#FF4C00] prose-code:bg-zinc-800/50 prose-code:px-1.5 prose-code:py-0.5 prose-code:rounded prose-code:font-mono prose-code:text-xs prose-code:before:content-none prose-code:after:content-none prose-pre:bg-[#0d0d0d] prose-pre:border prose-pre:border-white/10 prose-pre:rounded-xl prose-pre:p-4 prose-a:text-[#FF4C00] prose-a:no-underline hover:prose-a:underline prose-li:marker:text-zinc-600"
            v-html="parsedContent"
          ></div>

          <div v-if="problem.examples && problem.examples.length > 0" class="mt-8 space-y-4">
            <div
              v-for="(ex, i) in problem.examples"
              :key="i"
              class="bg-zinc-900/40 border border-white/5 rounded-xl overflow-hidden hover:border-white/10 transition-colors group/card"
            >
              <div
                class="px-4 py-2 bg-white/[0.02] border-b border-white/5 flex items-center justify-between"
              >
                <span
                  class="text-[10px] font-bold text-zinc-500 uppercase tracking-wider flex items-center gap-2"
                >
                  <div
                    class="w-1.5 h-1.5 rounded-full bg-zinc-700 group-hover/card:bg-[#FF4C00] transition-colors"
                  ></div>
                  Example {{ i + 1 }}
                </span>
              </div>
              <div class="p-4 space-y-3 font-mono text-sm text-zinc-300">
                <div class="space-y-1">
                  <span
                    class="text-[10px] font-bold text-zinc-500 uppercase tracking-wider block select-none"
                    >Input</span
                  >
                  <div
                    class="bg-[#0d0d0d] border border-white/5 rounded-lg px-3 py-2 text-zinc-300"
                  >
                    {{ ex.input }}
                  </div>
                </div>
                <div class="space-y-1">
                  <span
                    class="text-[10px] font-bold text-zinc-500 uppercase tracking-wider block select-none"
                    >Output</span
                  >
                  <div
                    class="bg-[#0d0d0d] border border-white/5 rounded-lg px-3 py-2 text-white font-bold"
                  >
                    {{ ex.output }}
                  </div>
                </div>
                <div v-if="ex.explanation" class="space-y-1 pt-1">
                  <span
                    class="text-[10px] font-bold text-zinc-500 uppercase tracking-wider block select-none"
                    >Explanation</span
                  >
                  <div class="text-zinc-400 text-xs italic leading-relaxed px-1">
                    {{ ex.explanation }}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="mt-8 pt-8 border-t border-white/5">
            <div class="flex items-center gap-3 flex-wrap">
              <span class="text-xs text-zinc-500 select-none flex items-center gap-1">
                <Tag class="w-3.5 h-3.5" /> 相关标签:
              </span>
              <span
                v-for="tag in problem.tags"
                :key="tag"
                class="px-3 py-1 text-xs font-medium rounded-full bg-zinc-800/50 border border-white/5 text-zinc-400 hover:text-white hover:border-[#FF4C00]/30 hover:bg-[#FF4C00]/10 cursor-pointer transition-all duration-300"
              >
                {{ tag }}
              </span>
            </div>
          </div>
        </div>

        <div
          v-else-if="currentTab === 'solution'"
          key="solution"
          class="space-y-4 animate-in fade-in slide-in-from-bottom-2"
        >
          <div class="flex items-center justify-between pb-3 border-b border-white/5">
            <h2 class="text-sm font-bold text-white flex items-center gap-2">
              官方与社区题解
              <span class="text-xs font-normal text-zinc-500">({{ solutionsList.length }})</span>
            </h2>
            <Button
              size="sm"
              class="bg-[#FF4C00] hover:bg-[#ff5f1f] text-white h-8 text-xs px-3 rounded-lg flex items-center"
              @click="openPublishDialog"
            >
              <PenSquare class="w-3.5 h-3.5 mr-1.5" /> 发布题解
            </Button>
          </div>

          <div
            v-if="solutionLoading"
            class="h-32 w-full bg-zinc-800/20 border border-white/5 rounded-xl flex items-center justify-center"
          >
            <Loader2 class="w-6 h-6 animate-spin text-[#FF4C00]" />
          </div>

          <div v-else-if="solutionsList.length > 0" class="space-y-3">
            <div
              v-for="sol in solutionsList"
              :key="sol.id"
              class="bg-zinc-900/40 border rounded-xl overflow-hidden transition-all duration-300"
              :class="
                expandedSolutions.has(sol.id)
                  ? 'border-[#FF4C00]/30 shadow-[0_0_15px_rgba(255,76,0,0.05)]'
                  : 'border-white/5 hover:border-white/10'
              "
            >
              <div
                class="p-4 cursor-pointer hover:bg-white/[0.02] flex items-center justify-between transition-colors group/head"
                @click="toggleExpand(sol)"
              >
                <div class="flex-1 pr-4">
                  <h3 class="text-white font-bold text-[15px] leading-snug truncate">
                    {{ sol.title }}
                  </h3>
                  <div class="flex items-center gap-2 text-xs text-zinc-500 mt-1.5">
                    <span class="font-medium text-zinc-300">{{ sol.authorName }}</span>
                    <span class="w-1 h-1 rounded-full bg-zinc-700"></span>
                    <span>{{ sol.createTime }}</span>
                    <span class="w-1 h-1 rounded-full bg-zinc-700"></span>
                    <span
                      >阅读
                      {{
                        sol.viewCount >= 1000
                          ? (sol.viewCount / 1000).toFixed(1) + 'k'
                          : sol.viewCount
                      }}</span
                    >
                  </div>
                </div>

                <div class="flex items-center gap-3">
                  <div
                    v-if="sol.authorId === currentUserId"
                    class="flex items-center gap-1 opacity-0 group-hover/head:opacity-100 transition-opacity"
                    @click.stop
                  >
                    <button
                      @click="openEditDialog(sol)"
                      class="text-zinc-400 hover:text-blue-400 p-1.5 rounded-lg hover:bg-white/5 transition-colors"
                      title="修改"
                    >
                      <Edit2 class="w-3.5 h-3.5" />
                    </button>
                    <button
                      @click="openDeleteConfirm(sol.id)"
                      class="text-zinc-400 hover:text-red-500 p-1.5 rounded-lg hover:bg-white/5 transition-colors"
                      title="删除"
                    >
                      <Trash class="w-3.5 h-3.5" />
                    </button>
                  </div>

                  <div
                    class="shrink-0 w-8 h-8 rounded-full flex items-center justify-center bg-zinc-800/50"
                  >
                    <ChevronUp
                      v-if="expandedSolutions.has(sol.id)"
                      class="w-4 h-4 text-[#FF4C00]"
                    />
                    <ChevronDown v-else class="w-4 h-4 text-zinc-500" />
                  </div>
                </div>
              </div>

              <div
                v-show="expandedSolutions.has(sol.id)"
                class="p-5 border-t border-white/5 bg-zinc-950/50 space-y-6"
              >
                <section v-if="sol.content">
                  <div class="flex items-center gap-2 mb-3">
                    <div class="w-1 h-3 bg-[#FF4C00] rounded-full"></div>
                    <h4 class="text-xs font-bold text-zinc-300 uppercase tracking-widest">
                      解题思路
                    </h4>
                  </div>
                  <div
                    class="prose prose-invert prose-sm max-w-none text-zinc-400 leading-relaxed prose-headings:text-zinc-100 prose-headings:font-bold prose-headings:tracking-tight prose-p:my-4 prose-p:leading-7 prose-strong:text-zinc-200 prose-strong:font-semibold prose-code:text-[#FF4C00] prose-code:bg-zinc-800/50 prose-code:px-1.5 prose-code:py-0.5 prose-code:rounded prose-code:font-mono prose-code:text-xs prose-code:before:content-none prose-code:after:content-none prose-pre:bg-[#0d0d0d] prose-pre:border prose-pre:border-white/10 prose-pre:rounded-xl prose-pre:p-4 prose-a:text-[#FF4C00] prose-a:no-underline hover:prose-a:underline prose-li:marker:text-zinc-600"
                    v-html="parseMarkdown(sol.content)"
                  ></div>
                </section>

                <section v-if="sol.code">
                  <div class="flex items-center justify-between mb-3">
                    <div class="flex items-center gap-2">
                      <div class="w-1 h-3 bg-[#FF4C00] rounded-full"></div>
                      <h4 class="text-xs font-bold text-zinc-300 uppercase tracking-widest">
                        代码实现
                      </h4>
                    </div>
                    <button
                      @click.stop="copyToClipboard(sol.code, sol.id)"
                      class="flex items-center gap-1.5 px-2 py-1 rounded bg-white/5 hover:bg-white/10 text-[10px] text-zinc-400 hover:text-white transition-all border border-white/5"
                    >
                      <component
                        :is="copiedId === sol.id ? Check : Copy"
                        class="w-3 h-3"
                        :class="copiedId === sol.id ? 'text-emerald-500' : ''"
                      />
                      {{ copiedId === sol.id ? '已复制' : '复制代码' }}
                    </button>
                  </div>
                  <div class="relative group/code">
                    <pre
                      class="bg-[#0d0d0d] border border-white/10 rounded-xl p-4 overflow-x-auto custom-scrollbar font-mono text-xs leading-relaxed text-zinc-300"
                    ><code>{{ sol.code }}</code></pre>
                  </div>
                </section>
              </div>
            </div>
          </div>

          <div
            v-else
            class="h-32 w-full bg-zinc-800/20 border border-white/5 rounded-xl flex flex-col items-center justify-center gap-2"
          >
            <FlaskConical class="w-8 h-8 text-zinc-700" />
            <p class="text-zinc-500 text-sm">暂无题解，来做第一个分享者吧！</p>
          </div>
        </div>

        <div v-else key="submissions" class="space-y-3 animate-in fade-in slide-in-from-bottom-2">
          <h2 class="text-lg font-bold text-white mb-4">我的提交</h2>
          <div v-if="submissionsLoading" class="h-32 flex items-center justify-center">
            <Loader2 class="w-6 h-6 animate-spin text-[#FF4C00]" />
          </div>
          <template v-else-if="submissionsList.length > 0">
            <div
              v-for="sub in submissionsList"
              :key="sub.id"
              class="p-4 bg-zinc-900/40 border border-white/5 rounded-xl flex items-center justify-between group hover:border-white/10 transition-colors"
            >
              <div class="flex items-center gap-3">
                <div class="w-2 h-2 rounded-full" :class="getStatusConfig(sub.status).bg"></div>
                <div>
                  <p class="text-sm font-medium" :class="getStatusConfig(sub.status).color">
                    {{ getStatusConfig(sub.status).text }}
                  </p>
                  <p class="text-xs text-zinc-500">{{ sub.submitTime }}</p>
                </div>
              </div>
              <div class="text-right">
                <span class="text-xs font-mono text-zinc-400 block"
                  >{{ sub.timeCost }}ms / {{ sub.memoryCost }}MB</span
                >
                <span class="text-[10px] text-zinc-500 uppercase mt-0.5 inline-block">{{
                  sub.language
                }}</span>
              </div>
            </div>
          </template>
          <div
            v-else
            class="h-32 w-full flex items-center justify-center bg-zinc-900/20 border border-white/5 rounded-xl"
          >
            <p class="text-zinc-500 text-sm">暂无提交记录</p>
          </div>
        </div>
      </Transition>
    </div>

    <ArenaDialog
      v-model="showPublishDialog"
      :title="isEditMode ? '修改知识脉络' : '发布知识脉络'"
      :confirm-text="isPublishing ? '信号上传中...' : isEditMode ? '保存修改' : '确认发布'"
      @confirm="submitSolution"
    >
      <div class="space-y-4 pt-2">
        <p class="text-zinc-400 text-sm leading-relaxed mb-4">
          分享你的解题思路，帮助其他特工破解这道难题。支持使用 Markdown 语法进行代码高亮排版。
        </p>

        <div>
          <label
            class="text-[11px] font-bold text-zinc-500 uppercase tracking-wider block mb-1.5 ml-1"
            >题解主旨</label
          >
          <input
            v-model="publishForm.title"
            type="text"
            :disabled="isPublishing"
            class="w-full bg-zinc-900/50 border border-zinc-800 rounded-xl px-4 py-3 text-sm text-white focus:border-[#FF4C00] focus:bg-zinc-900 transition-colors outline-none placeholder-zinc-700 disabled:opacity-50"
            placeholder="例如：双指针算法优化空间复杂度，附详细注释"
          />
        </div>

        <div>
          <label
            class="text-[11px] font-bold text-zinc-500 uppercase tracking-wider block mb-1.5 ml-1"
            >解题思路 (Markdown)</label
          >
          <textarea
            v-model="publishForm.content"
            rows="6"
            :disabled="isPublishing"
            class="w-full bg-zinc-900/50 border border-zinc-800 rounded-xl px-4 py-3 text-sm text-zinc-300 focus:border-[#FF4C00] focus:bg-zinc-900 transition-colors outline-none placeholder-zinc-700 custom-scrollbar disabled:opacity-50 resize-none font-mono"
            placeholder="在此键入你的解题策略推导..."
          ></textarea>
        </div>

        <div>
          <label
            class="text-[11px] font-bold text-zinc-500 uppercase tracking-wider block mb-1.5 ml-1"
            >代码实现</label
          >
          <textarea
            v-model="publishForm.code"
            rows="8"
            :disabled="isPublishing"
            class="w-full bg-zinc-900/50 border border-zinc-800 rounded-xl px-4 py-3 text-xs text-zinc-300 focus:border-[#FF4C00] focus:bg-zinc-900 transition-colors outline-none placeholder-zinc-700 custom-scrollbar disabled:opacity-50 resize-none font-mono"
            placeholder="在此粘贴你的核心算法代码..."
          ></textarea>
        </div>

        <p
          v-if="publishError"
          class="text-xs text-red-500 font-bold animate-pulse text-center mt-2"
        >
          ⚠️ {{ publishError }}
        </p>
      </div>
    </ArenaDialog>

    <ArenaDialog
      v-model="showDeleteDialog"
      title="警告：销毁记录"
      :confirm-text="isDeleting ? '正在销毁...' : '确认摧毁'"
      @confirm="confirmDelete"
    >
      <div class="text-center py-4">
        <div
          class="w-12 h-12 mx-auto rounded-full bg-red-500/10 border border-red-500/20 flex items-center justify-center mb-4"
        >
          <Trash class="w-6 h-6 text-red-500" />
        </div>
        <p class="text-zinc-400 text-sm">
          一旦销毁，这条解题记录将<strong class="text-red-500">永久从服务器抹除</strong
          >。<br />你确定要这么做吗？
        </p>
      </div>
    </ArenaDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { marked } from 'marked'
import {
  FileText,
  FlaskConical,
  History,
  Maximize2,
  Minimize2,
  CheckCircle2,
  Tag,
  Loader2,
  PenSquare,
  ChevronDown,
  ChevronUp,
  Edit2,
  Trash,
  Copy,
  Check,
} from 'lucide-vue-next'
import { Button } from '@/components/ui/button'
import ArenaDialog from '@/components/arena/ArenaDialog.vue'

import {
  getProblemSolutions,
  getProblemSubmissions,
  publishProblemSolution,
  updateProblemSolution,
  deleteProblemSolution,
  recordSolutionView,
} from '@/api/problem'
import type { Problem, ProblemSolution, SubmissionHistory } from '@/api/problem'

// 引入用户状态，用于鉴权
import { useUserStore } from '@/stores/user'

interface Props {
  problem: Problem
  isMaximized: boolean
  hideTabs?: boolean
}

const props = withDefaults(defineProps<Props>(), { hideTabs: false })
defineEmits(['toggle-maximize'])

const currentTab = ref('desc')
const userStore = useUserStore()
const currentUserId = computed(() => userStore.userInfo?.id)

const tabs = [
  { id: 'desc', label: '描述', icon: FileText },
  { id: 'solution', label: '题解', icon: FlaskConical },
  { id: 'submissions', label: '记录', icon: History },
]

const filteredTabs = computed(() => (props.hideTabs ? tabs.filter((t) => t.id === 'desc') : tabs))

const parsedContent = computed(() =>
  props.problem?.content ? marked.parse(props.problem.content) : '',
)
const parseMarkdown = (content: string) => (content ? marked.parse(content) : '')

// --- 题解控制核心引擎 ---
const solutionsList = ref<ProblemSolution[]>([])
const solutionLoading = ref(false)
const expandedSolutions = ref<Set<number>>(new Set())
const viewedInSession = new Set<number>() // 本地防并发刷量集合

// 展开/折叠题解，并触发阅读记录
const toggleExpand = async (sol: ProblemSolution) => {
  if (expandedSolutions.value.has(sol.id)) {
    expandedSolutions.value.delete(sol.id)
  } else {
    expandedSolutions.value.add(sol.id)

    // 如果本次会话未记录过，则向后端发送信号
    if (!viewedInSession.has(sol.id)) {
      viewedInSession.add(sol.id)
      try {
        await recordSolutionView(sol.id)
        // 乐观更新 UI，让用户立刻看到变化
        sol.viewCount++
      } catch (error) {
        // 后端可能判定已经阅读过或网络异常，静默忽略
      }
    }
  }
}

// 加载题解列表
const fetchSolutions = async () => {
  if (!props.problem?.id) return
  solutionLoading.value = true
  try {
    solutionsList.value = await getProblemSolutions(props.problem.id)
  } catch (error) {
    console.error('获取题解失败:', error)
  } finally {
    solutionLoading.value = false
  }
}

// 复制代码功能
const copiedId = ref<number | null>(null)
const copyToClipboard = async (text: string, id: number) => {
  if (!text) return
  try {
    await navigator.clipboard.writeText(text)
    copiedId.value = id
    setTimeout(() => {
      copiedId.value = null
    }, 2000)
  } catch (err) {
    console.error('复制失败:', err)
  }
}

// --- 发布与修改弹窗 ---
const showPublishDialog = ref(false)
const isPublishing = ref(false)
const isEditMode = ref(false)
const editSolutionId = ref<number | null>(null)
const publishError = ref('')
const publishForm = reactive({ title: '', content: '', code: '' })

// 开启发布模式
const openPublishDialog = () => {
  isEditMode.value = false
  editSolutionId.value = null
  publishForm.title = ''
  publishForm.content = ''
  publishForm.code = ''
  publishError.value = ''
  showPublishDialog.value = true
}

// 开启修改模式
const openEditDialog = (sol: ProblemSolution) => {
  isEditMode.value = true
  editSolutionId.value = sol.id
  publishForm.title = sol.title
  publishForm.content = sol.content || ''
  publishForm.code = sol.code || ''
  publishError.value = ''
  showPublishDialog.value = true
}

// 提交变更
const submitSolution = async () => {
  if (!publishForm.title.trim() || !publishForm.content.trim() || !publishForm.code.trim()) {
    publishError.value = '通信遭到拒绝：主旨、思路及代码实现均不能为空。'
    return
  }
  isPublishing.value = true
  publishError.value = ''
  try {
    const payload = {
      title: publishForm.title.trim(),
      content: publishForm.content.trim(),
      code: publishForm.code.trim(),
    }

    if (isEditMode.value && editSolutionId.value) {
      await updateProblemSolution(editSolutionId.value, payload)
    } else {
      await publishProblemSolution(props.problem.id, payload)
    }
    showPublishDialog.value = false
    expandedSolutions.value.clear()
    await fetchSolutions() // 刷新列表
  } catch (error: any) {
    publishError.value = error.message || '操作失败，网络连接可能不稳定。'
  } finally {
    isPublishing.value = false
  }
}

// --- 删除鉴权弹窗 ---
const showDeleteDialog = ref(false)
const deletingId = ref<number | null>(null)
const isDeleting = ref(false)

const openDeleteConfirm = (id: number) => {
  deletingId.value = id
  showDeleteDialog.value = true
}

const confirmDelete = async () => {
  if (!deletingId.value) return
  isDeleting.value = true
  try {
    await deleteProblemSolution(deletingId.value)
    showDeleteDialog.value = false
    expandedSolutions.value.clear()
    await fetchSolutions() // 刷新列表
  } catch (error: any) {
    console.error('销毁失败:', error)
  } finally {
    isDeleting.value = false
    deletingId.value = null
  }
}

// --- 我的提交控制区 ---
const submissionsList = ref<SubmissionHistory[]>([])
const submissionsLoading = ref(false)

const fetchSubmissions = async () => {
  if (submissionsList.value.length > 0 || !props.problem?.id) return
  submissionsLoading.value = true
  try {
    const res = await getProblemSubmissions(props.problem.id, { current: 1, pageSize: 10 })
    submissionsList.value = res.list
  } catch (error) {
    console.error('获取提交记录失败:', error)
  } finally {
    submissionsLoading.value = false
  }
}

const getStatusConfig = (status: number) => {
  switch (status) {
    case 1:
      return { text: 'Accepted', color: 'text-emerald-500', bg: 'bg-emerald-500' }
    case 2:
      return { text: 'Wrong Answer', color: 'text-red-500', bg: 'bg-red-500' }
    case 3:
      return { text: 'Time Limit Exceeded', color: 'text-orange-500', bg: 'bg-orange-500' }
    case 4:
      return { text: 'Memory Limit Exceeded', color: 'text-yellow-500', bg: 'bg-yellow-500' }
    case 5:
      return { text: 'Runtime Error', color: 'text-purple-500', bg: 'bg-purple-500' }
    case 6:
      return { text: 'Compile Error', color: 'text-zinc-500', bg: 'bg-zinc-500' }
    default:
      return { text: 'Pending', color: 'text-blue-500', bg: 'bg-blue-500' }
  }
}

watch(currentTab, (newTab) => {
  if (newTab === 'solution') fetchSolutions()
  else if (newTab === 'submissions') fetchSubmissions()
})
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition:
    opacity 0.2s ease,
    transform 0.2s ease;
}
.fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}
.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
::selection {
  background: rgba(255, 76, 0, 0.3);
  color: white;
}
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 6px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.2);
}
</style>
