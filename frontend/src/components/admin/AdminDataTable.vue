<template>
  <div class="bg-zinc-900/60 backdrop-blur-xl border border-white/5 rounded-2xl overflow-hidden">
    <!-- 表格 -->
    <div>
      <table style="width: 100%; table-layout: fixed">
        <thead>
          <tr class="bg-zinc-800/50 border-b border-white/5">
            <th
              v-for="col in columns"
              :key="col.key"
              :style="col.width ? { width: col.width } : undefined"
              class="text-zinc-400 text-xs uppercase tracking-wider font-medium text-left px-4 py-3 whitespace-nowrap"
            >
              {{ col.title ?? col.label }}
            </th>
          </tr>
        </thead>
        <tbody>
          <!-- 加载状态 -->
          <tr v-if="loading">
            <td :colspan="columns.length" class="px-6 py-20 text-center">
              <div class="flex items-center justify-center gap-3 text-zinc-500">
                <svg class="w-5 h-5 animate-spin" viewBox="0 0 24 24" fill="none">
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
                <span class="text-sm">加载中...</span>
              </div>
            </td>
          </tr>

          <!-- 空状态 -->
          <tr v-else-if="data.length === 0">
            <td :colspan="columns.length" class="px-6 py-20 text-center">
              <div class="flex flex-col items-center gap-2 text-zinc-500">
                <svg
                  class="w-10 h-10 opacity-40"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.5"
                >
                  <path d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
                </svg>
                <span class="text-sm">暂无数据</span>
              </div>
            </td>
          </tr>

          <!-- 数据行 -->
          <tr
            v-else
            v-for="(row, index) in data"
            :key="index"
            class="border-b border-white/5 hover:bg-white/[0.08] transition-colors group"
          >
            <td
              v-for="col in columns"
              :key="col.key"
              class="px-4 py-3 text-sm text-white whitespace-nowrap"
            >
              <slot :name="`cell-${col.key}`" :row="row" :value="row[col.key]">
                {{ row[col.key] ?? '-' }}
              </slot>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div
      v-if="total > 0"
      class="flex items-center justify-between px-6 py-4 border-t border-white/5"
    >
      <div class="flex items-center gap-3">
        <span class="text-zinc-500 text-sm">共 {{ total }} 条</span>
        <div class="relative" ref="pageSizeDropdownRef">
          <button
            @click="showPageSizeDropdown = !showPageSizeDropdown"
            class="flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-bold border bg-white/5 border-white/10 text-zinc-400 hover:border-white/20 transition-all"
          >
            {{ pageSize }} 条/页
            <ChevronDown
              class="w-3.5 h-3.5 transition-transform duration-200"
              :class="{ 'rotate-180': showPageSizeDropdown }"
            />
          </button>
          <Transition name="dropdown">
            <div
              v-if="showPageSizeDropdown"
              class="absolute left-0 bottom-full mb-2 w-28 bg-zinc-900/95 backdrop-blur-xl border border-white/10 rounded-xl p-1.5 shadow-2xl shadow-black/50 z-50"
            >
              <button
                v-for="size in pageSizeOptions"
                :key="size"
                @click="selectPageSize(size)"
                class="w-full px-3 py-2 rounded-lg text-xs font-bold transition-all duration-200"
                :class="
                  pageSize === size
                    ? 'bg-[#FF4C00]/20 border-[#FF4C00]/50 text-[#FF4C00]'
                    : 'text-zinc-400 hover:bg-white/5 hover:text-zinc-200'
                "
              >
                {{ size }} 条/页
              </button>
            </div>
          </Transition>
        </div>
      </div>
      <div class="flex items-center gap-2">
        <button
          :disabled="current <= 1"
          class="px-3 py-1.5 rounded-lg text-sm border border-white/10 text-zinc-400 hover:text-white hover:bg-white/5 transition-colors disabled:opacity-30 disabled:cursor-not-allowed"
          @click="emit('update:current', current - 1)"
        >
          上一页
        </button>

        <template v-for="page in displayPages" :key="page">
          <span v-if="page === '...'" class="px-2 text-zinc-600 text-sm">...</span>
          <button
            v-else
            :class="
              cn(
                'w-9 h-9 rounded-lg text-sm transition-colors',
                page === current
                  ? 'bg-[#FF4C00] text-white font-medium'
                  : 'text-zinc-400 hover:text-white hover:bg-white/5',
              )
            "
            @click="emit('update:current', page as number)"
          >
            {{ page }}
          </button>
        </template>

        <button
          :disabled="current >= totalPages"
          class="px-3 py-1.5 rounded-lg text-sm border border-white/10 text-zinc-400 hover:text-white hover:bg-white/5 transition-colors disabled:opacity-30 disabled:cursor-not-allowed"
          @click="emit('update:current', current + 1)"
        >
          下一页
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ChevronDown } from 'lucide-vue-next'
import { onClickOutside } from '@vueuse/core'
import { cn } from '@/lib/utils'

export interface TableColumn {
  key: string
  label?: string
  title?: string
  width?: string
}

const props = withDefaults(
  defineProps<{
    columns: TableColumn[]
    data: any[]
    total: number
    current: number
    pageSize: number
    loading?: boolean
  }>(),
  {
    loading: false,
  },
)

const emit = defineEmits<{
  'update:current': [page: number]
  'update:pageSize': [size: number]
}>()

const showPageSizeDropdown = ref(false)
const pageSizeDropdownRef = ref<HTMLElement | null>(null)
const pageSizeOptions = [10, 15, 20, 50]

onClickOutside(pageSizeDropdownRef, () => (showPageSizeDropdown.value = false))

const selectPageSize = (size: number) => {
  showPageSizeDropdown.value = false
  emit('update:pageSize', size)
}

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.pageSize)))

const displayPages = computed(() => {
  const pages: (number | string)[] = []
  const total = totalPages.value
  const cur = props.current

  if (total <= 7) {
    for (let i = 1; i <= total; i++) pages.push(i)
    return pages
  }

  pages.push(1)

  if (cur > 3) pages.push('...')

  const start = Math.max(2, cur - 1)
  const end = Math.min(total - 1, cur + 1)
  for (let i = start; i <= end; i++) pages.push(i)

  if (cur < total - 2) pages.push('...')

  pages.push(total)
  return pages
})
</script>

<style scoped>
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}
.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(4px) scale(0.95);
}
</style>
