<template>
  <div
    class="min-h-[calc(100vh-80px)] bg-zinc-50 dark:bg-zinc-950 bg-grid-black/[0.02] dark:bg-grid-white/[0.02] p-8"
  >
    <div class="relative z-10 space-y-6">
      <!-- 页面标题 -->
      <div
        class="flex items-center gap-3"
        v-motion
        :initial="{ opacity: 0, y: -50 }"
        :visible="{ opacity: 1, y: 0, transition: { duration: 400 } }"
      >
        <div
          class="p-2 bg-zinc-100 dark:bg-zinc-900/50 rounded-xl border border-zinc-200 dark:border-white/10 shadow-sm backdrop-blur-sm"
        >
          <Users class="w-6 h-6 text-[#FF4C00]" />
        </div>
        <div>
          <h1 class="text-2xl font-bold text-zinc-900 dark:text-white">用户管理</h1>
          <p class="text-zinc-500 dark:text-zinc-500 text-sm mt-0.5">查看、搜索与管理平台用户</p>
        </div>
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
            placeholder="搜索用户名 / 昵称..."
            class="w-full bg-transparent text-zinc-900 dark:text-white pl-11 pr-4 py-3 outline-none placeholder-zinc-400 dark:placeholder-zinc-600 text-sm font-medium transition-all"
            @input="handleSearchDebounced"
          />
        </div>

        <div
          class="flex items-center gap-2 p-1 border-t md:border-t-0 md:border-l border-zinc-200 dark:border-white/5 w-full md:w-auto"
        >
          <Filter class="w-4 h-4 text-zinc-500 ml-3 shrink-0 hidden md:block" />
          <div class="relative" ref="roleDropdownRef">
            <button
              @click="showRoleDropdown = !showRoleDropdown"
              class="flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold border transition-all"
              :class="
                searchRole
                  ? roleOptions.find((r) => r.value === searchRole)?.activeClass
                  : 'bg-zinc-50 dark:bg-white/5 border-zinc-200 dark:border-white/10 text-zinc-600 dark:text-zinc-400 hover:border-zinc-300 dark:hover:border-white/20'
              "
            >
              <component
                :is="roleOptions.find((r) => r.value === searchRole)?.icon || Users"
                class="w-3.5 h-3.5"
              />
              {{ roleOptions.find((r) => r.value === searchRole)?.label || '全部角色' }}
              <ChevronDown
                class="w-3.5 h-3.5 transition-transform duration-200"
                :class="{ 'rotate-180': showRoleDropdown }"
              />
            </button>
            <Transition name="dropdown">
              <div
                v-if="showRoleDropdown"
                class="absolute right-0 top-full mt-2 w-40 bg-white dark:bg-zinc-900/95 backdrop-blur-xl border border-zinc-200 dark:border-white/10 rounded-xl p-1.5 shadow-lg dark:shadow-2xl dark:shadow-black/50 z-50"
              >
                <button
                  v-for="role in roleOptions"
                  :key="role.value"
                  @click="selectRole(role.value)"
                  class="w-full flex items-center gap-2.5 px-3 py-2 rounded-lg text-xs font-bold transition-all duration-200"
                  :class="
                    searchRole === role.value
                      ? role.activeClass
                      : 'text-zinc-600 dark:text-zinc-400 hover:bg-zinc-50 dark:hover:bg-white/5 hover:text-zinc-900 dark:hover:text-zinc-200'
                  "
                >
                  <component :is="role.icon" class="w-3.5 h-3.5" />
                  {{ role.label }}
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
          <!-- 积分列（管理员显示 -） -->
          <template #cell-ratingScore="{ row }">
            <span class="text-zinc-700 dark:text-zinc-300 font-mono text-sm">
              {{ row.role === 'admin' ? '-' : (row.ratingScore ?? 0) }}
            </span>
          </template>

          <!-- 排名列（管理员显示 -） -->
          <template #cell-globalRank="{ row }">
            <span class="text-zinc-700 dark:text-zinc-300 font-mono text-sm">
              {{ row.role === 'admin' ? '-' : (row.globalRank ?? '-') }}
            </span>
          </template>

          <!-- 角色列 -->
          <template #cell-role="{ row }">
            <span
              :class="cn('px-2.5 py-0.5 rounded-full text-xs font-bold', roleTagClass(row.role))"
            >
              {{ roleLabel(row.role) }}
            </span>
          </template>

          <!-- 操作列 -->
          <template #cell-actions="{ row }">
            <div class="flex items-center gap-2">
              <button
                @click="openDetail(row)"
                class="px-3 py-1.5 text-xs font-medium text-zinc-700 dark:text-zinc-300 bg-zinc-100 dark:bg-white/5 rounded-lg hover:bg-zinc-200 dark:hover:bg-white/10 transition-colors"
              >
                详情
              </button>
              <button
                v-if="row.role !== 'ban'"
                @click="handleBan(row)"
                class="px-3 py-1.5 text-xs font-medium text-rose-400 bg-rose-500/10 rounded-lg hover:bg-rose-500/20 transition-colors"
              >
                封禁
              </button>
              <button
                v-else
                @click="handleUnban(row)"
                class="px-3 py-1.5 text-xs font-medium text-emerald-400 bg-emerald-500/10 rounded-lg hover:bg-emerald-500/20 transition-colors"
              >
                解封
              </button>
              <button
                @click="openRoleChange(row)"
                class="px-3 py-1.5 text-xs font-medium text-blue-400 bg-blue-500/10 rounded-lg hover:bg-blue-500/20 transition-colors"
              >
                改角色
              </button>
            </div>
          </template>
        </AdminDataTable>
      </div>

      <!-- 用户详情弹窗 -->
      <AdminUserDetailDialog v-model="showDetailDialog" :user="selectedUser" />

      <!-- 修改角色弹窗 -->
      <ArenaDialog
        v-model="showRoleDialog"
        title="修改用户角色"
        confirm-text="确认修改"
        @confirm="confirmRoleChange"
      >
        <div class="space-y-4">
          <p class="text-zinc-500 dark:text-zinc-400 text-sm">
            当前用户:
            <span class="text-zinc-900 dark:text-white font-medium">{{
              roleChangeTarget?.nickname || roleChangeTarget?.username
            }}</span>
          </p>
          <div class="flex gap-3">
            <button
              v-for="role in ['user', 'admin']"
              :key="role"
              @click="newRole = role"
              :class="
                cn(
                  'flex-1 py-2.5 rounded-xl text-sm font-bold border transition-all',
                  newRole === role
                    ? 'bg-[#FF4C00]/20 border-[#FF4C00]/50 text-[#FF4C00]'
                    : 'bg-zinc-100 dark:bg-black/30 border-zinc-200 dark:border-white/10 text-zinc-600 dark:text-zinc-400 hover:border-zinc-300 dark:hover:border-white/20',
                )
              "
            >
              {{ role === 'user' ? '普通用户' : '管理员' }}
            </button>
          </div>
        </div>
      </ArenaDialog>

      <!-- 封禁确认弹窗 -->
      <ArenaDialog
        v-model="showBanDialog"
        title="确认封禁"
        confirm-text="确认封禁"
        @confirm="confirmBan"
      >
        <p class="text-zinc-500 dark:text-zinc-400 text-sm text-center">
          确定要封禁用户
          <span class="text-zinc-900 dark:text-white font-medium">{{
            banTarget?.nickname || banTarget?.username
          }}</span>
          吗？封禁后该用户将无法登录平台。
        </p>
      </ArenaDialog>
    </div>

    <BackToTop />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Users, ChevronDown, Search, Filter, Shield, ShieldBan } from 'lucide-vue-next'
import { useDebounceFn, onClickOutside } from '@vueuse/core'
import { cn } from '@/lib/utils'
import AdminDataTable from '@/components/admin/AdminDataTable.vue'
import AdminUserDetailDialog from '@/components/admin/AdminUserDetailDialog.vue'
import ArenaDialog from '@/components/arena/ArenaDialog.vue'
import BackToTop from '@/components/ui/BackToTop.vue'
import { getAdminUsers, getAdminUserDetail, banUser, unbanUser, updateUserRole } from '@/api/admin'
import type { AdminUserVO, AdminUserDetailVO } from '@/types/api'

// --- 表格列定义 ---
const columns = [
  { key: 'id', title: 'ID', width: '7%' },
  { key: 'username', title: '用户名', width: '16%' },
  { key: 'nickname', title: '昵称', width: '16%' },
  { key: 'role', title: '角色', width: '10%' },
  { key: 'ratingScore', title: '积分', width: '9%' },
  { key: 'globalRank', title: '排名', width: '9%' },
  { key: 'createTime', title: '注册时间', width: '17%' },
  { key: 'actions', title: '操作', width: '16%' },
]

// --- 搜索 ---
const searchKeyword = ref('')
const searchRole = ref('')
const showRoleDropdown = ref(false)
const roleDropdownRef = ref<HTMLElement | null>(null)

onClickOutside(roleDropdownRef, () => {
  showRoleDropdown.value = false
})

const roleOptions = [
  {
    label: '全部角色',
    value: '',
    icon: Users,
    activeClass: 'bg-[#FF4C00]/20 border-[#FF4C00]/50 text-[#FF4C00]',
  },
  {
    label: '普通用户',
    value: 'user',
    icon: Users,
    activeClass: 'bg-emerald-500/15 border-emerald-500/40 text-emerald-400',
  },
  {
    label: '管理员',
    value: 'admin',
    icon: Shield,
    activeClass: 'bg-orange-500/15 border-orange-500/40 text-orange-400',
  },
  {
    label: '已封禁',
    value: 'ban',
    icon: ShieldBan,
    activeClass: 'bg-rose-500/15 border-rose-500/40 text-rose-400',
  },
]

const selectRole = (value: string) => {
  searchRole.value = value
  showRoleDropdown.value = false
  handleSearch()
}

// --- 表格状态 ---
const tableData = ref<AdminUserVO[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(15)
const loading = ref(false)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getAdminUsers({
      current: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value || undefined,
      role: searchRole.value || undefined,
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

const handleSearchDebounced = useDebounceFn(() => {
  handleSearch()
}, 300)

const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchData()
}

const handlePageSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchData()
}

// --- 角色标签 ---
const roleLabel = (role: string) => {
  const map: Record<string, string> = { user: '用户', admin: '管理员', ban: '已封禁' }
  return map[role] || role
}

const roleTagClass = (role: string) => {
  if (role === 'admin') return 'bg-orange-500/15 text-orange-400'
  if (role === 'ban') return 'bg-rose-500/15 text-rose-400'
  return 'bg-emerald-500/15 text-emerald-400'
}

// --- 用户详情 ---
const showDetailDialog = ref(false)
const selectedUser = ref<AdminUserDetailVO | null>(null)

const openDetail = async (row: AdminUserVO) => {
  try {
    selectedUser.value = await getAdminUserDetail(row.id)
  } catch {
    selectedUser.value = row as AdminUserDetailVO
  }
  showDetailDialog.value = true
}

// --- 封禁 / 解封 ---
const showBanDialog = ref(false)
const banTarget = ref<AdminUserVO | null>(null)

const handleBan = (row: AdminUserVO) => {
  banTarget.value = row
  showBanDialog.value = true
}

const confirmBan = async () => {
  if (!banTarget.value) return
  await banUser(banTarget.value.id)
  showBanDialog.value = false
  fetchData()
}

const handleUnban = async (row: AdminUserVO) => {
  await unbanUser(row.id)
  fetchData()
}

// --- 修改角色 ---
const showRoleDialog = ref(false)
const roleChangeTarget = ref<AdminUserVO | null>(null)
const newRole = ref('user')

const openRoleChange = (row: AdminUserVO) => {
  roleChangeTarget.value = row
  newRole.value = row.role === 'admin' ? 'admin' : 'user'
  showRoleDialog.value = true
}

const confirmRoleChange = async () => {
  if (!roleChangeTarget.value) return
  await updateUserRole(roleChangeTarget.value.id, newRole.value)
  showRoleDialog.value = false
  fetchData()
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
