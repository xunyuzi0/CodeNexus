<template>
  <div class="min-h-screen w-full relative p-6 md:p-12 pb-32">
    <div class="absolute inset-0 bg-grid-white/[0.02] pointer-events-none fixed"></div>
    <div
      class="absolute top-0 left-1/2 -translate-x-1/2 w-[800px] h-[600px] bg-[#FF4C00]/5 blur-[120px] rounded-full pointer-events-none fixed"
    ></div>

    <div
      class="relative z-10 mb-8 flex flex-col md:flex-row md:items-end justify-between gap-4"
      v-motion-slide-visible-top
    >
      <div>
        <div class="flex items-center gap-3 mb-2">
          <div
            class="p-2 bg-zinc-900/50 rounded-xl border border-white/10 shadow-sm backdrop-blur-sm"
          >
            <Code2 class="w-6 h-6 text-[#FF4C00]" />
          </div>
          <h1 class="text-3xl font-black tracking-tight text-white">题库中心</h1>
        </div>
        <p class="text-zinc-500 text-sm font-medium tracking-wide pl-1">
          探索海量算法挑战，磨练编程技艺
        </p>
      </div>

      <div class="flex items-center gap-4">
        <div class="text-right hidden sm:block">
          <div class="flex items-baseline justify-end gap-1">
            <span class="text-3xl font-black text-white font-mono tracking-tighter">
              {{ totalProblems }}
            </span>
            <span class="text-xs text-zinc-500 font-mono font-bold uppercase tracking-wider"
              >Total Problems</span
            >
          </div>
        </div>
        <div
          class="w-10 h-10 rounded-full bg-zinc-900/50 border border-white/5 flex items-center justify-center"
        >
          <TrendingUp class="w-5 h-5 text-emerald-500" />
        </div>
      </div>
    </div>

    <div
      class="relative z-10 mb-8"
      v-motion
      :initial="{ opacity: 0, y: -10 }"
      :enter="{ opacity: 1, y: 0, transition: { duration: 400, delay: 100 } }"
    >
      <div
        class="bg-zinc-900/40 backdrop-blur-md border border-white/5 rounded-2xl p-1.5 flex flex-col md:flex-row items-center gap-2 shadow-lg shadow-black/20 group/search hover:border-[#FF4C00]/30 transition-colors duration-500"
      >
        <div class="relative flex-1 w-full">
          <div class="absolute left-4 top-1/2 -translate-y-1/2 flex items-center justify-center">
            <Search
              class="w-4 h-4 text-zinc-500 group-focus-within/search:text-[#FF4C00] transition-colors"
            />
          </div>
          <input
            v-model="queryParams.keyword"
            type="text"
            placeholder="搜索题目 ID、标题或算法标签..."
            class="w-full bg-transparent text-white pl-11 pr-4 py-3 outline-none placeholder-zinc-600 text-sm font-medium transition-all"
            @input="handleSearch"
          />
        </div>

        <div
          class="flex items-center gap-1 p-1 overflow-x-auto no-scrollbar border-t md:border-t-0 md:border-l border-white/5 w-full md:w-auto"
        >
          <Filter class="w-4 h-4 text-zinc-500 ml-3 mr-2 shrink-0 hidden md:block" />
          <button
            v-for="diff in difficultyOptions"
            :key="diff.value"
            @click="handleFilter(diff.value)"
            class="px-4 py-2 rounded-xl text-xs font-bold transition-all duration-300 whitespace-nowrap border"
            :class="[
              queryParams.difficulty === diff.value
                ? diff.activeClass
                : 'border-transparent text-zinc-500 hover:bg-white/5 hover:text-zinc-300',
            ]"
          >
            {{ diff.label }}
          </button>
        </div>
      </div>
    </div>

    <div class="relative z-10 space-y-3 min-h-[400px]">
      <div v-if="loading" class="flex flex-col items-center justify-center h-64 space-y-4">
        <div
          class="w-10 h-10 border-2 border-zinc-800 border-t-[#FF4C00] rounded-full animate-spin"
        ></div>
        <p class="text-zinc-600 text-sm font-mono animate-pulse">正在连接 CodeNexus 神经元...</p>
      </div>

      <div
        v-else-if="problemList.length === 0"
        class="flex flex-col items-center justify-center h-64 text-zinc-600"
      >
        <Zap class="w-12 h-12 mb-4 opacity-20" />
        <p>未在扇区内扫描到相关题目</p>
      </div>

      <template v-else>
        <ProblemList
          :problems="problemList"
          action-type="favorite"
          :collected-ids="collectedProblemIds"
          @click="handleEnterProblem"
          @action="openCollectDialog"
        />
      </template>
    </div>

    <div v-if="!loading && totalPages > 0" class="mt-12 flex items-center justify-center gap-2">
      <button
        @click="handlePageChange(queryParams.pageNum - 1)"
        :disabled="queryParams.pageNum <= 1"
        class="w-9 h-9 flex items-center justify-center rounded-lg bg-zinc-900 border border-white/10 text-zinc-400 hover:text-white hover:border-[#FF4C00] transition-all disabled:opacity-50 disabled:cursor-not-allowed"
      >
        <ChevronLeft class="w-4 h-4" />
      </button>

      <div class="flex items-center gap-2 px-2">
        <button
          v-for="page in totalPages"
          :key="page"
          @click="handlePageChange(page)"
          class="w-9 h-9 flex items-center justify-center rounded-lg text-sm font-bold font-mono transition-all border"
          :class="
            queryParams.pageNum === page
              ? 'bg-[#FF4C00] border-[#FF4C00] text-white shadow-[0_0_15px_#FF4C00]'
              : 'bg-transparent border-transparent text-zinc-500 hover:bg-white/5 hover:text-zinc-300'
          "
        >
          {{ page }}
        </button>
      </div>

      <button
        @click="handlePageChange(queryParams.pageNum + 1)"
        :disabled="queryParams.pageNum >= totalPages"
        class="w-9 h-9 flex items-center justify-center rounded-lg bg-zinc-900 border border-white/10 text-zinc-400 hover:text-white hover:border-[#FF4C00] transition-all disabled:opacity-50 disabled:cursor-not-allowed"
      >
        <ChevronRight class="w-4 h-4" />
      </button>
    </div>

    <ArenaDialog
      v-model="collectDialog.show"
      title="收藏题目 (可多选)"
      :confirm-text="isSubmitting ? '处理中...' : '保存更改'"
      @confirm="confirmCollect"
    >
      <div class="flex flex-col gap-4 min-h-[150px]">
        <div v-if="collectDialog.loading" class="flex-1 flex items-center justify-center">
          <div
            class="w-6 h-6 border-2 border-zinc-800 border-t-[#FF4C00] rounded-full animate-spin"
          ></div>
        </div>

        <template v-else>
          <div class="flex justify-between items-end">
            <p class="text-xs text-zinc-500">请选择目标文件夹：</p>
          </div>

          <p v-if="errorMessage" class="text-xs text-red-500 font-bold animate-pulse">
            {{ errorMessage }}
          </p>

          <div class="space-y-2 max-h-[200px] overflow-y-auto custom-scrollbar pr-2">
            <div
              v-for="folder in collectDialog.folders"
              :key="folder.id"
              @click="toggleFolderSelection(folder.id)"
              class="flex items-center justify-between p-3 rounded-xl border cursor-pointer transition-all select-none group"
              :class="
                collectDialog.selectedFolderIds.includes(folder.id)
                  ? 'bg-[#FF4C00]/10 border-[#FF4C00] text-white shadow-[0_0_10px_rgba(255,76,0,0.1)]'
                  : 'bg-zinc-900/50 border-white/5 text-zinc-400 hover:bg-white/5 hover:border-white/10'
              "
            >
              <div class="flex items-center gap-3">
                <Folder
                  class="w-4 h-4 transition-colors"
                  :class="
                    collectDialog.selectedFolderIds.includes(folder.id)
                      ? 'text-[#FF4C00]'
                      : 'text-zinc-600 group-hover:text-zinc-400'
                  "
                />
                <span class="text-sm font-medium">{{ folder.name }}</span>
              </div>

              <div class="relative w-4 h-4 flex items-center justify-center">
                <transition name="scale">
                  <div
                    v-if="collectDialog.selectedFolderIds.includes(folder.id)"
                    class="w-4 h-4 rounded bg-[#FF4C00] flex items-center justify-center"
                  >
                    <Check class="w-3 h-3 text-white font-bold" />
                  </div>
                  <div
                    v-else
                    class="w-4 h-4 rounded border border-zinc-700 group-hover:border-zinc-500"
                  ></div>
                </transition>
              </div>
            </div>
          </div>

          <div class="w-full h-[1px] bg-white/5 my-1"></div>

          <div class="space-y-2">
            <p class="text-xs text-zinc-500">或新建文件夹：</p>
            <div class="flex gap-2">
              <div class="relative flex-1">
                <Plus class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-zinc-500" />
                <input
                  v-model="collectDialog.newFolderName"
                  type="text"
                  :disabled="isSubmitting"
                  placeholder="例如：面试冲刺 (回车创建)"
                  class="w-full bg-zinc-900/50 border border-zinc-700 rounded-xl py-2 pl-9 pr-4 text-sm text-white focus:outline-none focus:border-[#FF4C00] transition-colors placeholder-zinc-600 disabled:opacity-50"
                  @keyup.enter="handleCreateFolder"
                />
              </div>
              <button
                @click="handleCreateFolder"
                :disabled="!collectDialog.newFolderName.trim() || isSubmitting"
                class="px-3 py-2 rounded-xl bg-zinc-800 text-xs font-bold text-zinc-400 hover:text-white hover:bg-[#FF4C00] transition-all disabled:opacity-50 disabled:hover:bg-zinc-800 disabled:hover:text-zinc-400"
              >
                创建
              </button>
            </div>
          </div>
        </template>
      </div>
    </ArenaDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useScroll, useDebounceFn } from '@vueuse/core'
import {
  Code2,
  TrendingUp,
  Search,
  Filter,
  Zap,
  ChevronLeft,
  ChevronRight,
  Folder,
  Plus,
  Check,
} from 'lucide-vue-next'
import ArenaDialog from '@/components/arena/ArenaDialog.vue'
import ProblemList from '@/components/problem/ProblemList.vue' // 引入重构后的公共组件

// API 引入
import {
  getProblemList,
  type Problem,
  type ProblemQuery,
  type ProblemDifficulty,
} from '@/api/problem'
import {
  getFolders,
  createFolder,
  addFavoriteProblem,
  removeFavoriteProblem,
  getFolderDetail,
  type FavoriteFolder,
} from '@/api/favorites'

const router = useRouter()

const scrollContainer = ref<HTMLElement | null>(null)
const { y } = useScroll(scrollContainer)

// --- State ---
const loading = ref(false)
const problemList = ref<Problem[]>([])
const totalProblems = ref(0)

// 维护一对多：题目ID -> 收藏夹ID数组
const collectedMap = ref<Map<number, number[]>>(new Map())

// 计算出所有被收藏的题目 ID 集合，供 ProblemList 组件点亮星星
const collectedProblemIds = computed(() => {
  const ids: number[] = []
  collectedMap.value.forEach((folders, problemId) => {
    if (folders && folders.length > 0) {
      ids.push(problemId)
    }
  })
  return ids
})

const queryParams = reactive<ProblemQuery>({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  difficulty: '',
})

const totalPages = computed(() => {
  return Math.ceil(totalProblems.value / queryParams.pageSize) || 0
})

// --- Collection Dialog State ---
const collectDialog = reactive({
  show: false,
  loading: false,
  currentProblemId: -1,
  selectedFolderIds: [] as number[],
  newFolderName: '',
  folders: [] as FavoriteFolder[],
})

const isSubmitting = ref(false)
const errorMessage = ref('')

// --- Configurations ---
const difficultyOptions: { label: string; value: ProblemDifficulty | ''; activeClass: string }[] = [
  {
    label: '全部',
    value: '',
    activeClass: 'bg-[#FF4C00] border-[#FF4C00] text-white shadow-lg shadow-[#FF4C00]/30',
  },
  {
    label: '简单',
    value: 'EASY',
    activeClass: 'bg-emerald-500 border-emerald-500 text-white shadow-lg shadow-emerald-500/30',
  },
  {
    label: '中等',
    value: 'MEDIUM',
    activeClass: 'bg-amber-500 border-amber-500 text-white shadow-lg shadow-amber-500/30',
  },
  {
    label: '困难',
    value: 'HARD',
    activeClass: 'bg-rose-500 border-rose-500 text-white shadow-lg shadow-rose-500/30',
  },
]

// --- Methods ---

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getProblemList(queryParams)
    problemList.value = res.list
    totalProblems.value = res.total
  } catch (err) {
    console.error('Failed to fetch problems:', err)
  } finally {
    loading.value = false
  }
}

// 同步所有题目的收藏状态
const syncFavoriteStatus = async () => {
  try {
    const resFolders = await getFolders()
    const map = new Map<number, number[]>()

    const promises = resFolders.map((folder) => getFolderDetail(folder.id))
    const folderDetails = await Promise.all(promises)

    folderDetails.forEach((detail) => {
      detail.list.forEach((problem) => {
        const currentFolders = map.get(problem.id) || []
        if (!currentFolders.includes(detail.folder.id)) {
          currentFolders.push(detail.folder.id)
        }
        map.set(problem.id, currentFolders)
      })
    })

    collectedMap.value = new Map(map)
  } catch (error) {
    console.warn('同步收藏状态失败', error)
  }
}

const handleSearch = useDebounceFn(() => {
  queryParams.pageNum = 1
  fetchData()
}, 300)

const handleFilter = (difficulty: ProblemDifficulty | '') => {
  queryParams.difficulty = difficulty
  queryParams.pageNum = 1
  fetchData()
}

const handleEnterProblem = (id: number) => {
  router.push({ name: 'ProblemDetail', params: { id: id.toString() } })
}

const handlePageChange = (page: number) => {
  if (page < 1 || page > totalPages.value) return
  queryParams.pageNum = page
  fetchData()
  scrollToTop()
}

const scrollToTop = () => {
  if (scrollContainer.value) {
    scrollContainer.value.scrollTo({ top: 0, behavior: 'smooth' })
  } else {
    document.querySelector('.overflow-y-auto')?.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

const openCollectDialog = async (id: number) => {
  errorMessage.value = ''
  collectDialog.currentProblemId = id
  collectDialog.show = true
  collectDialog.loading = true
  collectDialog.newFolderName = ''

  try {
    const resFolders = await getFolders()
    collectDialog.folders = resFolders

    // 回显已勾选的收藏夹
    collectDialog.selectedFolderIds = [...(collectedMap.value.get(id) || [])]
  } catch (error) {
    console.error('获取收藏夹失败', error)
  } finally {
    collectDialog.loading = false
  }
}

const toggleFolderSelection = (folderId: number) => {
  const index = collectDialog.selectedFolderIds.indexOf(folderId)
  if (index > -1) {
    collectDialog.selectedFolderIds.splice(index, 1)
  } else {
    collectDialog.selectedFolderIds.push(folderId)
  }
}

const handleCreateFolder = async () => {
  const name = collectDialog.newFolderName.trim()
  if (!name || isSubmitting.value) return

  isSubmitting.value = true
  errorMessage.value = ''

  try {
    const newFolder = await createFolder(name)
    collectDialog.folders.push(newFolder)
    collectDialog.selectedFolderIds.push(newFolder.id)
    collectDialog.newFolderName = ''
  } catch (error: any) {
    console.error('新建文件夹失败', error)
    errorMessage.value = error.message || '新建文件夹失败，请重试'
  } finally {
    isSubmitting.value = false
  }
}

// 核心修复：提交时通过求差集分别发起添加和移除请求
const confirmCollect = async () => {
  if (isSubmitting.value) return

  const targetId = collectDialog.currentProblemId
  const newFolderIds = [...collectDialog.selectedFolderIds] // 用户最终勾选的
  const oldFolderIds = collectedMap.value.get(targetId) || [] // 题目原本所在的

  isSubmitting.value = true
  errorMessage.value = ''

  try {
    // 找出需要新增和移除的
    const toAdd = newFolderIds.filter((id) => !oldFolderIds.includes(id))
    const toRemove = oldFolderIds.filter((id) => !newFolderIds.includes(id))

    // 并发发起所有变更请求
    const promises: Promise<void>[] = []
    toAdd.forEach((folderId) => promises.push(addFavoriteProblem(folderId, targetId)))
    toRemove.forEach((folderId) => promises.push(removeFavoriteProblem(folderId, targetId)))

    await Promise.all(promises)

    // 更新本地状态
    const updatedMap = new Map(collectedMap.value)
    if (newFolderIds.length > 0) {
      updatedMap.set(targetId, newFolderIds)
    } else {
      updatedMap.delete(targetId)
    }
    collectedMap.value = updatedMap

    collectDialog.show = false
  } catch (error: any) {
    console.error('收藏操作失败', error)
    errorMessage.value = error.message || '操作失败，请检查网络报错'
  } finally {
    isSubmitting.value = false
  }
}

// --- Lifecycle ---
onMounted(() => {
  fetchData()
  syncFavoriteStatus()
  nextTick(() => {
    scrollContainer.value = document.querySelector('.overflow-y-auto') as HTMLElement
  })
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
.scale-enter-active,
.scale-leave-active {
  transition: all 0.2s ease;
}
.scale-enter-from,
.scale-leave-to {
  transform: scale(0);
  opacity: 0;
}
</style>
