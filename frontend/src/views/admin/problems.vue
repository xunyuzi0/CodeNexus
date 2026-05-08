<template>
  <div
    class="min-h-[calc(100vh-80px)] bg-zinc-50 dark:bg-zinc-950 bg-grid-black/[0.02] dark:bg-grid-white/[0.02] p-8"
  >
    <div class="relative z-10 max-w-7xl mx-auto space-y-6">
      <!-- 页面标题 -->
      <div
        class="flex items-center justify-between"
        v-motion
        :initial="{ opacity: 0, y: -50 }"
        :visible="{ opacity: 1, y: 0, transition: { duration: 400 } }"
      >
        <div class="flex items-center gap-3">
          <div
            class="p-2 bg-zinc-100 dark:bg-zinc-900/50 rounded-xl border border-zinc-200 dark:border-white/10 shadow-sm backdrop-blur-sm"
          >
            <FileCode class="w-6 h-6 text-[#FF4C00]" />
          </div>
          <div>
            <h1 class="text-2xl font-bold text-zinc-900 dark:text-white">题库管理</h1>
            <p class="text-zinc-500 dark:text-zinc-500 text-sm mt-0.5">管理平台题库的增删改查</p>
          </div>
        </div>
        <button
          @click="openCreateForm"
          class="px-5 py-2.5 bg-[#FF4C00] text-white text-sm font-bold rounded-xl hover:bg-[#ff5f1f] transition-colors shadow-[0_0_15px_rgba(255,76,0,0.2)] flex items-center gap-2"
        >
          <Plus class="w-4 h-4" />
          新建题目
        </button>
      </div>

      <!-- 搜索栏 -->
      <div
        class="relative z-20 bg-white dark:bg-zinc-900/40 backdrop-blur-md border border-zinc-200 dark:border-white/5 rounded-2xl p-1.5 flex flex-col md:flex-row items-center gap-2 shadow-sm dark:shadow-lg dark:shadow-black/20 group/search hover:border-[#FF4C00]/30 transition-colors duration-500"
        v-motion
        :initial="{ opacity: 0, y: -10 }"
        :visible="{ opacity: 1, y: 0, transition: { duration: 400, delay: 100 } }"
      >
        <div class="relative flex-1 w-full">
          <div class="absolute left-4 top-1/2 -translate-y-1/2 flex items-center justify-center">
            <Search
              class="w-4 h-4 text-zinc-500 group-focus-within/search:text-[#FF4C00] transition-colors"
            />
          </div>
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索题目标题 / 编号..."
            class="w-full bg-transparent text-zinc-900 dark:text-white pl-11 pr-4 py-3 outline-none placeholder-zinc-400 dark:placeholder-zinc-600 text-sm font-medium transition-all"
            @input="handleSearchDebounced"
          />
        </div>

        <div
          class="flex items-center gap-2 p-1 border-t md:border-t-0 md:border-l border-zinc-200 dark:border-white/5 w-full md:w-auto"
        >
          <Filter class="w-4 h-4 text-zinc-500 ml-3 shrink-0 hidden md:block" />
          <!-- 难度下拉 -->
          <div class="relative" ref="difficultyDropdownRef">
            <button
              @click="showDifficultyDropdown = !showDifficultyDropdown"
              class="flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold border transition-all"
              :class="
                searchDifficulty !== ''
                  ? difficultyOptions.find((d) => d.value === searchDifficulty)?.activeClass
                  : 'bg-zinc-50 dark:bg-white/5 border-zinc-200 dark:border-white/10 text-zinc-600 dark:text-zinc-400 hover:border-zinc-300 dark:hover:border-white/20'
              "
            >
              {{ difficultyOptions.find((d) => d.value === searchDifficulty)?.label || '全部难度' }}
              <ChevronDown
                class="w-3.5 h-3.5 transition-transform duration-200"
                :class="{ 'rotate-180': showDifficultyDropdown }"
              />
            </button>
            <Transition name="dropdown">
              <div
                v-if="showDifficultyDropdown"
                class="absolute right-0 top-full mt-2 w-36 bg-white dark:bg-zinc-900/95 backdrop-blur-xl border border-zinc-200 dark:border-white/10 rounded-xl p-1.5 shadow-lg dark:shadow-2xl dark:shadow-black/50 z-50"
              >
                <button
                  v-for="diff in difficultyOptions"
                  :key="diff.value"
                  @click="selectDifficulty(diff.value)"
                  class="w-full flex items-center gap-2.5 px-3 py-2 rounded-lg text-xs font-bold transition-all duration-200"
                  :class="
                    searchDifficulty === diff.value
                      ? diff.activeClass
                      : 'text-zinc-600 dark:text-zinc-400 hover:bg-zinc-50 dark:hover:bg-white/5 hover:text-zinc-900 dark:hover:text-zinc-200'
                  "
                >
                  {{ diff.label }}
                </button>
              </div>
            </Transition>
          </div>
          <!-- 状态下拉 -->
          <div class="relative" ref="statusDropdownRef">
            <button
              @click="showStatusDropdown = !showStatusDropdown"
              class="flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold border transition-all"
              :class="
                searchStatus !== ''
                  ? statusOptions.find((s) => s.value === searchStatus)?.activeClass
                  : 'bg-zinc-50 dark:bg-white/5 border-zinc-200 dark:border-white/10 text-zinc-600 dark:text-zinc-400 hover:border-zinc-300 dark:hover:border-white/20'
              "
            >
              {{ statusOptions.find((s) => s.value === searchStatus)?.label || '全部状态' }}
              <ChevronDown
                class="w-3.5 h-3.5 transition-transform duration-200"
                :class="{ 'rotate-180': showStatusDropdown }"
              />
            </button>
            <Transition name="dropdown">
              <div
                v-if="showStatusDropdown"
                class="absolute right-0 top-full mt-2 w-32 bg-white dark:bg-zinc-900/95 backdrop-blur-xl border border-zinc-200 dark:border-white/10 rounded-xl p-1.5 shadow-lg dark:shadow-2xl dark:shadow-black/50 z-50"
              >
                <button
                  v-for="st in statusOptions"
                  :key="st.value"
                  @click="selectStatus(st.value)"
                  class="w-full flex items-center gap-2.5 px-3 py-2 rounded-lg text-xs font-bold transition-all duration-200"
                  :class="
                    searchStatus === st.value
                      ? st.activeClass
                      : 'text-zinc-600 dark:text-zinc-400 hover:bg-zinc-50 dark:hover:bg-white/5 hover:text-zinc-900 dark:hover:text-zinc-200'
                  "
                >
                  {{ st.label }}
                </button>
              </div>
            </Transition>
          </div>
        </div>
      </div>

      <!-- 数据表格 -->
      <div
        v-motion
        :initial="{ opacity: 0, y: -50 }"
        :visible="{ opacity: 1, y: 0, transition: { duration: 400, delay: 200 } }"
      >
        <AdminDataTable
          :columns="columns"
          :data="tableData"
          :total="total"
          :current="currentPage"
          :page-size="pageSize"
          :loading="loading"
          @update:current="handlePageChange"
          @update:pageSize="handlePageSizeChange"
        >
          <!-- 难度列 -->
          <template #cell-difficulty="{ row }">
            <span
              :class="
                cn('px-2.5 py-0.5 rounded-full text-xs font-bold', difficultyClass(row.difficulty))
              "
            >
              {{ difficultyLabel(row.difficulty) }}
            </span>
          </template>

          <!-- 状态列 -->
          <template #cell-status="{ row }">
            <span
              :class="cn('px-2.5 py-0.5 rounded-full text-xs font-bold', statusClass(row.status))"
            >
              {{ statusLabel(row.status) }}
            </span>
          </template>

          <!-- 通过率列 -->
          <template #cell-passRate="{ row }">
            <span class="text-zinc-700 dark:text-zinc-300 font-mono text-sm"
              >{{ (row.passRate * 100).toFixed(1) }}%</span
            >
          </template>

          <!-- 操作列 -->
          <template #cell-actions="{ row }">
            <div class="flex items-center gap-2">
              <button
                @click="openEditForm(row)"
                class="px-3 py-1.5 text-xs font-medium text-zinc-700 dark:text-zinc-300 bg-zinc-100 dark:bg-white/5 rounded-lg hover:bg-zinc-200 dark:hover:bg-white/10 transition-colors"
              >
                编辑
              </button>
              <button
                @click="openSolutionDrawer(row)"
                class="px-3 py-1.5 text-xs font-medium text-blue-400 bg-blue-500/10 rounded-lg hover:bg-blue-500/20 transition-colors"
              >
                题解
              </button>
              <button
                v-if="row.status !== 1"
                @click="handlePublish(row)"
                class="px-3 py-1.5 text-xs font-medium text-emerald-400 bg-emerald-500/10 rounded-lg hover:bg-emerald-500/20 transition-colors"
              >
                上架
              </button>
              <button
                v-if="row.status === 1"
                @click="handleOffline(row)"
                class="px-3 py-1.5 text-xs font-medium text-yellow-400 bg-yellow-500/10 rounded-lg hover:bg-yellow-500/20 transition-colors"
              >
                下架
              </button>
              <button
                @click="handleDelete(row)"
                class="px-3 py-1.5 text-xs font-medium text-rose-400 bg-rose-500/10 rounded-lg hover:bg-rose-500/20 transition-colors"
              >
                删除
              </button>
            </div>
          </template>
        </AdminDataTable>
      </div>

      <!-- 新建 / 编辑弹窗 -->
      <AdminProblemFormDialog
        v-model="showFormDialog"
        :problem="editingProblem"
        @submit="handleFormSubmit"
      />

      <!-- 下架确认弹窗 -->
      <ArenaDialog
        v-model="showOfflineDialog"
        title="确认下架"
        confirm-text="下架"
        @confirm="confirmOffline"
      >
        <p class="text-zinc-500 dark:text-zinc-400 text-sm text-center">
          确定要下架题目
          <span class="text-zinc-900 dark:text-white font-medium">{{ offlineTarget?.title }}</span>
          吗？下架后用户将无法看到该题目。
        </p>
      </ArenaDialog>

      <!-- 删除确认弹窗 -->
      <ArenaDialog
        v-model="showDeleteDialog"
        title="确认删除"
        confirm-text="删除"
        @confirm="confirmDelete"
      >
        <p class="text-zinc-500 dark:text-zinc-400 text-sm text-center">
          确定要删除题目
          <span class="text-zinc-900 dark:text-white font-medium">{{ deleteTarget?.title }}</span>
          吗？此操作不可撤销。
        </p>
      </ArenaDialog>

      <!-- 题解管理抽屉 -->
      <AdminSolutionDrawer
        v-model="showSolutionDrawer"
        :problem-id="solutionTarget?.id ?? null"
        :problem-title="solutionTarget?.title ?? ''"
        @deleted="fetchData"
      />
    </div>

    <BackToTop />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { FileCode, Plus, ChevronDown, Search, Filter } from 'lucide-vue-next'
import { useDebounceFn, onClickOutside } from '@vueuse/core'
import { cn } from '@/lib/utils'
import AdminDataTable from '@/components/admin/AdminDataTable.vue'
import AdminProblemFormDialog from '@/components/admin/AdminProblemFormDialog.vue'
import AdminSolutionDrawer from '@/components/admin/AdminSolutionDrawer.vue'
import ArenaDialog from '@/components/arena/ArenaDialog.vue'
import BackToTop from '@/components/ui/BackToTop.vue'
import {
  getAdminProblems,
  createProblem,
  updateProblem,
  deleteProblem as deleteProblemApi,
  publishProblem,
  offlineProblem,
} from '@/api/admin'
import type { AdminProblemVO, ProblemUpsertRequest } from '@/types/api'

// --- 表格列定义 ---
const columns = [
  { key: 'id', title: 'ID', width: '60px' },
  { key: 'displayId', title: '编号', width: '90px' },
  { key: 'title', title: '标题' },
  { key: 'difficulty', title: '难度', width: '100px' },
  { key: 'status', title: '状态', width: '100px' },
  { key: 'submitNum', title: '提交数', width: '100px' },
  { key: 'passRate', title: '首次通过率', width: '100px' },
  { key: 'actions', title: '操作', width: '240px' },
]

// --- 搜索 ---
const searchKeyword = ref('')
const searchDifficulty = ref<number | ''>('')
const searchStatus = ref<number | ''>('')
const showDifficultyDropdown = ref(false)
const showStatusDropdown = ref(false)
const difficultyDropdownRef = ref<HTMLElement | null>(null)
const statusDropdownRef = ref<HTMLElement | null>(null)

onClickOutside(difficultyDropdownRef, () => (showDifficultyDropdown.value = false))
onClickOutside(statusDropdownRef, () => (showStatusDropdown.value = false))

const difficultyOptions = [
  {
    label: '全部难度',
    value: '' as number | '',
    activeClass: 'bg-[#FF4C00]/20 border-[#FF4C00]/50 text-[#FF4C00]',
  },
  {
    label: 'Easy',
    value: 1 as number | '',
    activeClass: 'bg-emerald-500/15 border-emerald-500/40 text-emerald-400',
  },
  {
    label: 'Medium',
    value: 2 as number | '',
    activeClass: 'bg-amber-500/15 border-amber-500/40 text-amber-400',
  },
  {
    label: 'Hard',
    value: 3 as number | '',
    activeClass: 'bg-rose-500/15 border-rose-500/40 text-rose-400',
  },
]

const statusOptions = [
  {
    label: '全部状态',
    value: '' as number | '',
    activeClass: 'bg-[#FF4C00]/20 border-[#FF4C00]/50 text-[#FF4C00]',
  },
  {
    label: '草稿',
    value: 0 as number | '',
    activeClass: 'bg-zinc-500/15 border-zinc-500/40 text-zinc-400',
  },
  {
    label: '已上架',
    value: 1 as number | '',
    activeClass: 'bg-emerald-500/15 border-emerald-500/40 text-emerald-400',
  },
  {
    label: '已下架',
    value: 2 as number | '',
    activeClass: 'bg-rose-500/15 border-rose-500/40 text-rose-400',
  },
]

const selectDifficulty = (value: number | '') => {
  searchDifficulty.value = value
  showDifficultyDropdown.value = false
  handleSearch()
}

const selectStatus = (value: number | '') => {
  searchStatus.value = value
  showStatusDropdown.value = false
  handleSearch()
}

// --- 表格状态 ---
const tableData = ref<AdminProblemVO[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(15)
const loading = ref(false)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getAdminProblems({
      current: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value || undefined,
      difficulty: searchDifficulty.value !== '' ? searchDifficulty.value : undefined,
      status: searchStatus.value !== '' ? searchStatus.value : undefined,
    })
    tableData.value = res.records
    total.value = res.total
  } catch {
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchData()
}

const handleSearchDebounced = useDebounceFn(() => handleSearch(), 300)

const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchData()
}

const handlePageSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchData()
}

// --- 难度 / 状态标签 ---
const difficultyLabel = (d: number) => {
  const map: Record<number, string> = { 1: 'Easy', 2: 'Medium', 3: 'Hard' }
  return map[d] || '未知'
}

const difficultyClass = (d: number) => {
  if (d === 1) return 'bg-emerald-500/15 text-emerald-400'
  if (d === 2) return 'bg-yellow-500/15 text-yellow-400'
  return 'bg-rose-500/15 text-rose-400'
}

const statusLabel = (s: number) => {
  const map: Record<number, string> = { 0: '草稿', 1: '已上架', 2: '已下架' }
  return map[s] || '未知'
}

const statusClass = (s: number) => {
  if (s === 0) return 'bg-zinc-500/15 text-zinc-400'
  if (s === 1) return 'bg-emerald-500/15 text-emerald-400'
  return 'bg-rose-500/15 text-rose-400'
}

// --- 新建 / 编辑 ---
const showFormDialog = ref(false)
const editingProblem = ref<AdminProblemVO | null>(null)

const openCreateForm = () => {
  editingProblem.value = null
  showFormDialog.value = true
}

const openEditForm = (row: AdminProblemVO) => {
  editingProblem.value = row
  showFormDialog.value = true
}

const handleFormSubmit = async (data: ProblemUpsertRequest) => {
  if (editingProblem.value) {
    await updateProblem(editingProblem.value.id, data)
  } else {
    await createProblem(data)
  }
  showFormDialog.value = false
  fetchData()
}

// --- 上架 / 下架 ---
const handlePublish = async (row: AdminProblemVO) => {
  await publishProblem(row.id)
  fetchData()
}

const showOfflineDialog = ref(false)
const offlineTarget = ref<AdminProblemVO | null>(null)

const handleOffline = (row: AdminProblemVO) => {
  offlineTarget.value = row
  showOfflineDialog.value = true
}

const confirmOffline = async () => {
  if (!offlineTarget.value) return
  await offlineProblem(offlineTarget.value.id)
  showOfflineDialog.value = false
  fetchData()
}

// --- 删除 ---
const showDeleteDialog = ref(false)
const deleteTarget = ref<AdminProblemVO | null>(null)

const handleDelete = (row: AdminProblemVO) => {
  deleteTarget.value = row
  showDeleteDialog.value = true
}

const confirmDelete = async () => {
  if (!deleteTarget.value) return
  await deleteProblemApi(deleteTarget.value.id)
  showDeleteDialog.value = false
  fetchData()
}

// --- 题解管理 ---
const showSolutionDrawer = ref(false)
const solutionTarget = ref<AdminProblemVO | null>(null)

const openSolutionDrawer = (row: AdminProblemVO) => {
  solutionTarget.value = row
  showSolutionDrawer.value = true
}

// --- 初始化 ---
onMounted(fetchData)
</script>

<style scoped>
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
