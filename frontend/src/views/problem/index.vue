<template>
  <div class="flex flex-col relative min-h-[80vh] pb-10">
    <div
      class="fixed top-[-20%] left-1/2 -translate-x-1/2 w-[80%] h-[600px] bg-[#FF4C00]/10 blur-[120px] rounded-full pointer-events-none z-0"
    ></div>

    <div class="sticky top-0 z-30 pt-1 pb-0.5">
      <div
        class="relative bg-white/80 dark:bg-zinc-900/80 backdrop-blur-xl border border-zinc-200/50 dark:border-white/5 rounded-2xl p-5 shadow-2xl shadow-zinc-200/20 dark:shadow-black/20 flex items-center justify-between transition-all duration-300 group"
      >
        <div
          class="absolute inset-x-4 top-0 h-[1px] bg-gradient-to-r from-transparent via-white/10 to-transparent"
        ></div>

        <div>
          <h1
            class="text-3xl font-bold tracking-tight text-zinc-900 dark:text-white flex items-center gap-3"
          >
            <div
              class="p-2 bg-zinc-100 dark:bg-white/5 rounded-xl border border-zinc-200 dark:border-white/10 shadow-sm"
            >
              <Code2 class="w-6 h-6 text-zinc-900 dark:text-white" />
            </div>
            <span class="relative"> 题库中心 </span>
          </h1>
        </div>

        <div class="flex items-center gap-6">
          <div class="text-right hidden sm:block">
            <p class="text-[10px] uppercase text-zinc-500 tracking-[0.2em] font-bold">收录题目</p>
            <div class="flex items-baseline justify-end gap-1">
              <span
                class="text-3xl font-black text-zinc-900 dark:text-white font-mono tracking-tighter"
                >1,024</span
              >
              <span class="text-xs text-zinc-600 dark:text-zinc-500 font-mono">/ ∞</span>
            </div>
          </div>
          <div
            class="w-12 h-12 rounded-xl bg-zinc-100 dark:bg-white/5 flex items-center justify-center group-hover:bg-[#FF4C00]/10 transition-colors duration-500"
          >
            <TrendingUp
              class="w-6 h-6 text-zinc-400 dark:text-zinc-500 group-hover:text-[#FF4C00] transition-colors duration-500"
            />
          </div>
        </div>
      </div>
    </div>

    <div
      class="sticky top-[106px] z-20 pb-4"
      v-motion
      :initial="{ opacity: 0, y: -10 }"
      :enter="{ opacity: 1, y: 0, transition: { duration: 400 } }"
    >
      <div
        class="bg-white/80 dark:bg-zinc-900/80 backdrop-blur-xl border border-zinc-200 dark:border-white/5 rounded-2xl p-2 flex flex-col md:flex-row items-center gap-2 shadow-lg shadow-zinc-200/20 dark:shadow-black/40"
      >
        <div class="relative flex-1 w-full group">
          <div class="absolute left-4 top-1/2 -translate-y-1/2 flex items-center justify-center">
            <Search
              class="w-4 h-4 text-zinc-400 group-focus-within:text-[#FF4C00] transition-colors"
            />
          </div>
          <input
            v-model="queryParams.keyword"
            type="text"
            placeholder="搜索题目 ID、标题或算法标签..."
            class="w-full bg-transparent text-zinc-900 dark:text-zinc-100 pl-11 pr-4 py-3 !outline-none !ring-0 !border-none !shadow-none focus:ring-offset-0 placeholder-zinc-400 text-sm font-medium transition-all"
            @input="handleSearch"
          />
          <div
            class="absolute bottom-0 left-4 right-4 h-[1px] bg-zinc-200 dark:bg-white/5 group-focus-within:bg-[#FF4C00] transition-colors duration-300"
          ></div>
        </div>

        <div
          class="flex items-center gap-1 p-1 overflow-x-auto no-scrollbar border-t md:border-t-0 md:border-l border-zinc-200 dark:border-white/5 w-full md:w-auto"
        >
          <Filter class="w-4 h-4 text-zinc-400 ml-3 mr-2 shrink-0 hidden md:block" />

          <button
            v-for="diff in difficultyOptions"
            :key="diff.value"
            @click="handleFilter(diff.value)"
            class="px-4 py-2 rounded-xl text-xs font-bold transition-all duration-300 whitespace-nowrap"
            :class="[
              queryParams.difficulty === diff.value
                ? diff.activeClass
                : 'text-zinc-500 dark:text-zinc-400 hover:bg-zinc-100 dark:hover:bg-white/5',
            ]"
          >
            {{ diff.label }}
          </button>
        </div>
      </div>
    </div>

    <div class="relative z-10 space-y-3">
      <div v-if="loading" class="flex flex-col items-center justify-center h-64 space-y-4">
        <div class="relative w-12 h-12">
          <div class="absolute inset-0 border-2 border-zinc-800 rounded-full"></div>
          <div
            class="absolute inset-0 border-2 border-[#FF4C00] border-t-transparent rounded-full animate-spin"
          ></div>
        </div>
        <p class="text-zinc-500 text-sm font-mono animate-pulse">正在连接 CodeNexus 神经元...</p>
      </div>

      <div
        v-else-if="problemList.length === 0"
        class="flex flex-col items-center justify-center h-64 text-zinc-500"
      >
        <Zap class="w-12 h-12 mb-4 opacity-20" />
        <p>未在扇区内扫描到相关题目</p>
      </div>

      <template v-else>
        <div
          v-for="(item, index) in problemList"
          :key="item.id"
          @click="handleEnterProblem(item.id)"
          class="group relative flex items-center justify-between p-4 pl-6 rounded-2xl border border-zinc-200 dark:border-white/5 bg-white dark:bg-[#0A0A0B]/40 hover:bg-zinc-50 dark:hover:bg-zinc-800/60 transition-all duration-300 cursor-pointer shadow-sm hover:shadow-lg hover:shadow-[#FF4C00]/5 hover:-translate-y-[2px] will-change-transform"
          v-motion
          :initial="{ opacity: 0, y: 20 }"
          :enter="{ opacity: 1, y: 0, transition: { duration: 400, delay: index * 50 } }"
        >
          <div
            class="absolute left-0 top-4 bottom-4 w-1 bg-[#FF4C00] rounded-r-full opacity-0 group-hover:opacity-100 transition-opacity duration-300 shadow-[0_0_12px_#FF4C00]"
          ></div>

          <div class="flex items-center gap-5 overflow-hidden">
            <div class="shrink-0">
              <CheckCircle2
                v-if="item.status === 'PASSED'"
                class="w-5 h-5 text-emerald-500/80 drop-shadow-[0_0_8px_rgba(16,185,129,0.4)]"
              />
              <div
                v-else-if="item.status === 'ATTEMPTED'"
                class="w-4 h-4 rounded-full border-[3px] border-amber-500/30"
              ></div>
              <Circle v-else class="w-5 h-5 text-zinc-300 dark:text-zinc-700" />
            </div>

            <div class="flex flex-col">
              <div class="flex items-baseline gap-2">
                <span
                  class="text-zinc-400 dark:text-zinc-600 font-mono text-xs group-hover:text-zinc-500 dark:group-hover:text-zinc-500 transition-colors"
                >
                  #{{ item.displayId }}
                </span>
                <h3
                  class="text-zinc-900 dark:text-zinc-200 font-bold text-base truncate group-hover:text-[#FF4C00] dark:group-hover:text-[#FF4C00] transition-colors"
                >
                  {{ item.title }}
                </h3>
              </div>

              <div class="flex items-center gap-2 mt-1.5">
                <span
                  v-for="tag in item.tags"
                  :key="tag"
                  class="border border-zinc-100 dark:border-zinc-800 bg-zinc-50 dark:bg-zinc-900/50 text-[10px] text-zinc-500 dark:text-zinc-500 px-2 py-0.5 rounded-md font-medium"
                >
                  {{ tag }}
                </span>
              </div>
            </div>
          </div>

          <div class="hidden md:flex items-center gap-10 shrink-0 pr-4">
            <div class="text-right">
              <p
                class="text-zinc-400 dark:text-zinc-600 text-[10px] uppercase font-bold tracking-wider mb-0.5"
              >
                Acceptance
              </p>
              <p class="text-zinc-700 dark:text-zinc-300 font-mono text-sm">{{ item.passRate }}%</p>
            </div>

            <div class="w-16 flex justify-end">
              <span
                class="text-xs font-black px-2 py-1 rounded-md select-none tracking-wide"
                :class="difficultyColorMap[item.difficulty]"
              >
                {{ difficultyTextMap[item.difficulty] }}
              </span>
            </div>

            <MoreHorizontal
              class="w-5 h-5 text-zinc-300 dark:text-zinc-700 group-hover:text-zinc-600 dark:group-hover:text-zinc-300 transition-colors"
            />
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useDebounceFn } from '@vueuse/core'
import {
  Search,
  Filter,
  CheckCircle2,
  Circle,
  TrendingUp,
  Zap,
  MoreHorizontal,
  Code2,
} from 'lucide-vue-next'
import {
  getProblemList,
  type Problem,
  type ProblemQuery,
  type ProblemDifficulty,
} from '@/api/problem'

const router = useRouter()

// --- State ---
const loading = ref(false)
const problemList = ref<Problem[]>([])

const queryParams = reactive<ProblemQuery>({
  pageNum: 1,
  pageSize: 20,
  keyword: '',
  difficulty: '',
})

// --- Configurations ---
const difficultyOptions: { label: string; value: ProblemDifficulty | ''; activeClass: string }[] = [
  {
    label: '全部',
    value: '',
    activeClass: 'bg-[#FF4C00] text-white shadow-lg shadow-[#FF4C00]/30',
  },
  {
    label: '简单',
    value: 'EASY',
    activeClass: 'bg-emerald-500 text-white shadow-lg shadow-emerald-500/30',
  },
  {
    label: '中等',
    value: 'MEDIUM',
    activeClass: 'bg-amber-500 text-white shadow-lg shadow-amber-500/30',
  },
  {
    label: '困难',
    value: 'HARD',
    activeClass: 'bg-rose-500 text-white shadow-lg shadow-rose-500/30',
  },
]

const difficultyColorMap: Record<ProblemDifficulty, string> = {
  EASY: 'text-emerald-600 dark:text-emerald-400',
  MEDIUM: 'text-amber-600 dark:text-amber-400',
  HARD: 'text-rose-600 dark:text-rose-400',
}

const difficultyTextMap: Record<ProblemDifficulty, string> = {
  EASY: '简单',
  MEDIUM: '中等',
  HARD: '困难',
}

// --- Methods ---

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getProblemList(queryParams)
    problemList.value = res.list
  } catch (err) {
    console.error('Failed to fetch problems:', err)
  } finally {
    loading.value = false
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
  router.push({
    name: 'ProblemDetail',
    params: { id: id.toString() },
  })
}

// --- Lifecycle ---
onMounted(() => {
  fetchData()
})
</script>
