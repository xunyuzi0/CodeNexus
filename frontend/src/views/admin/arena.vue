<template>
  <div class="min-h-[calc(100vh-80px)] bg-zinc-950 bg-grid-white/[0.02] p-8">
    <div class="relative z-10 max-w-7xl mx-auto space-y-6">
      <!-- 页面标题 -->
      <div
        class="flex items-center gap-3"
        v-motion
        :initial="{ opacity: 0, y: -50 }"
        :visible="{ opacity: 1, y: 0, transition: { duration: 400 } }"
      >
        <div
          class="p-2 bg-zinc-900/50 rounded-xl border border-white/10 shadow-sm backdrop-blur-sm"
        >
          <Swords class="w-6 h-6 text-[#FF4C00]" />
        </div>
        <div>
          <h1 class="text-2xl font-bold text-white">对战记录管理</h1>
          <p class="text-zinc-500 text-sm mt-0.5">查看与管理所有对战房间</p>
        </div>
      </div>

      <!-- 筛选栏 -->
      <div
        class="relative z-20 bg-zinc-900/40 backdrop-blur-md border border-white/5 rounded-2xl p-1.5 flex flex-col md:flex-row items-center gap-2 shadow-lg shadow-black/20 hover:border-[#FF4C00]/30 transition-colors duration-500"
        v-motion
        :initial="{ opacity: 0, y: -10 }"
        :visible="{ opacity: 1, y: 0, transition: { duration: 400, delay: 100 } }"
      >
        <div class="flex items-center gap-2 p-1 w-full md:w-auto">
          <Filter class="w-4 h-4 text-zinc-500 ml-3 shrink-0 hidden md:block" />
          <!-- 状态下拉 -->
          <div class="relative" ref="statusDropdownRef">
            <button
              @click="showStatusDropdown = !showStatusDropdown"
              class="flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold border transition-all"
              :class="
                filterStatus
                  ? statusFilterOptions.find((s) => s.value === filterStatus)?.activeClass
                  : 'bg-white/5 border-white/10 text-zinc-400 hover:border-white/20'
              "
            >
              {{ statusFilterOptions.find((s) => s.value === filterStatus)?.label || '全部状态' }}
              <ChevronDown
                class="w-3.5 h-3.5 transition-transform duration-200"
                :class="{ 'rotate-180': showStatusDropdown }"
              />
            </button>
            <Transition name="dropdown">
              <div
                v-if="showStatusDropdown"
                class="absolute left-0 top-full mt-2 w-36 bg-zinc-900/95 backdrop-blur-xl border border-white/10 rounded-xl p-1.5 shadow-2xl shadow-black/50 z-50"
              >
                <button
                  v-for="st in statusFilterOptions"
                  :key="st.value"
                  @click="selectStatusFilter(st.value)"
                  class="w-full flex items-center gap-2.5 px-3 py-2 rounded-lg text-xs font-bold transition-all duration-200"
                  :class="
                    filterStatus === st.value
                      ? st.activeClass
                      : 'text-zinc-400 hover:bg-white/5 hover:text-zinc-200'
                  "
                >
                  {{ st.label }}
                </button>
              </div>
            </Transition>
          </div>
          <!-- 类型下拉 -->
          <div class="relative" ref="typeDropdownRef">
            <button
              @click="showTypeDropdown = !showTypeDropdown"
              class="flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold border transition-all"
              :class="
                filterRoomType !== ''
                  ? typeFilterOptions.find((t) => t.value === filterRoomType)?.activeClass
                  : 'bg-white/5 border-white/10 text-zinc-400 hover:border-white/20'
              "
            >
              {{ typeFilterOptions.find((t) => t.value === filterRoomType)?.label || '全部类型' }}
              <ChevronDown
                class="w-3.5 h-3.5 transition-transform duration-200"
                :class="{ 'rotate-180': showTypeDropdown }"
              />
            </button>
            <Transition name="dropdown">
              <div
                v-if="showTypeDropdown"
                class="absolute left-0 top-full mt-2 w-36 bg-zinc-900/95 backdrop-blur-xl border border-white/10 rounded-xl p-1.5 shadow-2xl shadow-black/50 z-50"
              >
                <button
                  v-for="tp in typeFilterOptions"
                  :key="tp.value"
                  @click="selectTypeFilter(tp.value)"
                  class="w-full flex items-center gap-2.5 px-3 py-2 rounded-lg text-xs font-bold transition-all duration-200"
                  :class="
                    filterRoomType === tp.value
                      ? tp.activeClass
                      : 'text-zinc-400 hover:bg-white/5 hover:text-zinc-200'
                  "
                >
                  {{ tp.label }}
                </button>
              </div>
            </Transition>
          </div>
        </div>

        <div class="flex-1"></div>

        <!-- 异常对局切换 -->
        <div class="p-1">
          <button
            @click="toggleAbnormal"
            :class="
              cn(
                'px-4 py-2 text-xs font-bold rounded-xl border transition-all flex items-center gap-2',
                abnormalMode
                  ? 'bg-rose-500/15 border-rose-500/30 text-rose-400'
                  : 'bg-white/5 border-white/10 text-zinc-400 hover:border-white/20',
              )
            "
          >
            <AlertTriangle class="w-3.5 h-3.5" />
            异常对局
          </button>
        </div>
      </div>

      <!-- 异常模式提示 -->
      <div
        v-if="abnormalMode"
        class="bg-rose-500/10 border border-rose-500/20 rounded-xl px-4 py-3 flex items-center gap-3"
        v-motion
        :initial="{ opacity: 0, y: -50 }"
        :visible="{ opacity: 1, y: 0, transition: { duration: 400 } }"
      >
        <AlertTriangle class="w-5 h-5 text-rose-400 shrink-0" />
        <p class="text-rose-300 text-sm">
          当前正在查看异常对局记录，这些对局可能存在超时、断线或其他异常情况。
        </p>
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
          <!-- 房间类型列 -->
          <template #cell-roomType="{ row }">
            <span
              :class="
                cn('px-2.5 py-0.5 rounded-full text-xs font-bold', roomTypeClass(row.roomType))
              "
            >
              {{ roomTypeLabel(row.roomType) }}
            </span>
          </template>

          <!-- 状态列 -->
          <template #cell-status="{ row }">
            <span
              :class="
                cn('px-2.5 py-0.5 rounded-full text-xs font-bold', roomStatusClass(row.status))
              "
            >
              {{ roomStatusLabel(row.status) }}
            </span>
          </template>

          <!-- 题目列 -->
          <template #cell-problemTitle="{ row }">
            <span class="text-zinc-300 text-sm line-clamp-1">{{ row.problemTitle || '-' }}</span>
          </template>

          <!-- 操作列 -->
          <template #cell-actions="{ row }">
            <button
              @click="openDetail(row)"
              class="px-3 py-1.5 text-xs font-medium text-zinc-300 bg-white/5 rounded-lg hover:bg-white/10 transition-colors"
            >
              详情
            </button>
          </template>
        </AdminDataTable>
      </div>

      <!-- 房间详情弹窗 -->
      <ArenaDialog
        v-model="showDetailDialog"
        title="房间详情"
        confirm-text="关闭"
        @confirm="showDetailDialog = false"
      >
        <div v-if="detailRoom" class="space-y-4">
          <div class="grid grid-cols-2 gap-3">
            <div class="bg-black/30 rounded-xl p-3 border border-white/5">
              <p class="text-zinc-500 text-xs mb-1">房间码</p>
              <p class="text-white font-mono font-bold">{{ detailRoom.roomCode }}</p>
            </div>
            <div class="bg-black/30 rounded-xl p-3 border border-white/5">
              <p class="text-zinc-500 text-xs mb-1">类型</p>
              <p>
                <span
                  :class="
                    cn(
                      'px-2 py-0.5 rounded-full text-xs font-bold',
                      roomTypeClass(detailRoom.roomType),
                    )
                  "
                >
                  {{ roomTypeLabel(detailRoom.roomType) }}
                </span>
              </p>
            </div>
            <div class="bg-black/30 rounded-xl p-3 border border-white/5">
              <p class="text-zinc-500 text-xs mb-1">状态</p>
              <p>
                <span
                  :class="
                    cn(
                      'px-2 py-0.5 rounded-full text-xs font-bold',
                      roomStatusClass(detailRoom.status),
                    )
                  "
                >
                  {{ roomStatusLabel(detailRoom.status) }}
                </span>
              </p>
            </div>
            <div class="bg-black/30 rounded-xl p-3 border border-white/5">
              <p class="text-zinc-500 text-xs mb-1">题目</p>
              <p class="text-white text-sm line-clamp-1">{{ detailRoom.problemTitle || '-' }}</p>
            </div>
          </div>
          <div class="bg-black/30 rounded-xl p-3 border border-white/5">
            <p class="text-zinc-500 text-xs mb-1">创建时间</p>
            <p class="text-zinc-300 font-mono text-sm">{{ detailRoom.createTime }}</p>
          </div>
        </div>
      </ArenaDialog>
    </div>

    <BackToTop />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Swords, AlertTriangle, ChevronDown, Filter } from 'lucide-vue-next'
import { onClickOutside } from '@vueuse/core'
import { cn } from '@/lib/utils'
import AdminDataTable from '@/components/admin/AdminDataTable.vue'
import ArenaDialog from '@/components/arena/ArenaDialog.vue'
import BackToTop from '@/components/ui/BackToTop.vue'
import { getAdminArenaRooms, getAdminArenaRoomDetail, getAbnormalArenaRooms } from '@/api/admin'
import type { AdminArenaRoomVO } from '@/types/api'

// --- 表格列定义 ---
const columns = [
  { key: 'roomCode', title: '房间码', width: '120px' },
  { key: 'roomType', title: '类型', width: '100px' },
  { key: 'problemTitle', title: '题目' },
  { key: 'creatorName', title: '创建者', width: '120px' },
  { key: 'playerCount', title: '人数', width: '70px' },
  { key: 'status', title: '状态', width: '100px' },
  { key: 'createTime', title: '创建时间', width: '170px' },
  { key: 'actions', title: '操作', width: '90px' },
]

// --- 筛选 ---
const filterStatus = ref('')
const filterRoomType = ref<number | ''>('')
const abnormalMode = ref(false)
const showStatusDropdown = ref(false)
const showTypeDropdown = ref(false)
const statusDropdownRef = ref<HTMLElement | null>(null)
const typeDropdownRef = ref<HTMLElement | null>(null)

onClickOutside(statusDropdownRef, () => (showStatusDropdown.value = false))
onClickOutside(typeDropdownRef, () => (showTypeDropdown.value = false))

const statusFilterOptions = [
  {
    label: '全部状态',
    value: '',
    activeClass: 'bg-[#FF4C00]/20 border-[#FF4C00]/50 text-[#FF4C00]',
  },
  {
    label: '等待中',
    value: 'WAITING',
    activeClass: 'bg-yellow-500/15 border-yellow-500/40 text-yellow-400',
  },
  {
    label: '对战中',
    value: 'FIGHTING',
    activeClass: 'bg-blue-500/15 border-blue-500/40 text-blue-400',
  },
  {
    label: '已结束',
    value: 'FINISHED',
    activeClass: 'bg-emerald-500/15 border-emerald-500/40 text-emerald-400',
  },
  {
    label: '已解散',
    value: 'DISMISSED',
    activeClass: 'bg-zinc-500/15 border-zinc-500/40 text-zinc-400',
  },
]

const typeFilterOptions = [
  {
    label: '全部类型',
    value: '' as number | '',
    activeClass: 'bg-[#FF4C00]/20 border-[#FF4C00]/50 text-[#FF4C00]',
  },
  {
    label: '私人房间',
    value: 1 as number | '',
    activeClass: 'bg-blue-500/15 border-blue-500/40 text-blue-400',
  },
  {
    label: '天梯匹配',
    value: 3 as number | '',
    activeClass: 'bg-orange-500/15 border-orange-500/40 text-orange-400',
  },
]

const selectStatusFilter = (value: string) => {
  filterStatus.value = value
  showStatusDropdown.value = false
  handleFilter()
}

const selectTypeFilter = (value: number | '') => {
  filterRoomType.value = value
  showTypeDropdown.value = false
  handleFilter()
}

// --- 表格状态 ---
const tableData = ref<AdminArenaRoomVO[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(15)
const loading = ref(false)

const fetchData = async () => {
  loading.value = true
  try {
    let res
    if (abnormalMode.value) {
      res = await getAbnormalArenaRooms({
        current: currentPage.value,
        pageSize: pageSize.value,
      })
    } else {
      res = await getAdminArenaRooms({
        current: currentPage.value,
        pageSize: pageSize.value,
        status: filterStatus.value || undefined,
        roomType: filterRoomType.value !== '' ? filterRoomType.value : undefined,
      })
    }
    tableData.value = res.records
    total.value = res.total
  } catch {
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleFilter = () => {
  abnormalMode.value = false
  currentPage.value = 1
  fetchData()
}

const toggleAbnormal = () => {
  abnormalMode.value = !abnormalMode.value
  currentPage.value = 1
  fetchData()
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchData()
}

const handlePageSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchData()
}

// --- 房间类型标签 ---
const roomTypeLabel = (t: number) => {
  const map: Record<number, string> = { 1: '私人', 2: '公开', 3: '天梯' }
  return map[t] || '未知'
}

const roomTypeClass = (t: number) => {
  if (t === 1) return 'bg-blue-500/15 text-blue-400'
  if (t === 2) return 'bg-emerald-500/15 text-emerald-400'
  return 'bg-orange-500/15 text-orange-400'
}

// --- 房间状态标签 ---
const roomStatusLabel = (s: string) => {
  const map: Record<string, string> = {
    WAITING: '等待中',
    FIGHTING: '对战中',
    FINISHED: '已结束',
    DISMISSED: '已解散',
  }
  return map[s] || s
}

const roomStatusClass = (s: string) => {
  if (s === 'WAITING') return 'bg-yellow-500/15 text-yellow-400'
  if (s === 'FIGHTING') return 'bg-blue-500/15 text-blue-400'
  if (s === 'FINISHED') return 'bg-emerald-500/15 text-emerald-400'
  return 'bg-zinc-500/15 text-zinc-400'
}

// --- 房间详情 ---
const showDetailDialog = ref(false)
const detailRoom = ref<AdminArenaRoomVO | null>(null)

const openDetail = async (row: AdminArenaRoomVO) => {
  try {
    detailRoom.value = await getAdminArenaRoomDetail(row.id)
  } catch {
    detailRoom.value = row
  }
  showDetailDialog.value = true
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
