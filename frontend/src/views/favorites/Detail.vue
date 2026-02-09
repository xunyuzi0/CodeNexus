<template>
  <div class="min-h-[85vh] w-full p-6 md:p-12">
    <div class="flex items-center gap-4 mb-8">
      <button
        @click="router.back()"
        class="w-10 h-10 rounded-xl bg-zinc-900 border border-white/10 flex items-center justify-center text-zinc-400 hover:text-white hover:border-[#FF4C00] hover:bg-[#FF4C00]/10 transition-all group"
      >
        <ChevronLeft class="w-5 h-5 group-hover:-translate-x-0.5 transition-transform" />
      </button>
      <div>
        <h1 class="text-2xl font-bold text-white flex items-center gap-2">
          <FolderOpen class="w-6 h-6 text-[#FF4C00]" />
          {{ folderInfo.name }}
        </h1>
        <p class="text-xs text-zinc-500 font-mono mt-1">
          创建于 {{ folderInfo.createTime }} · 共 {{ problemList.length }} 个条目
        </p>
      </div>
    </div>

    <div v-if="loading" class="space-y-4">
      <div v-for="i in 3" :key="i" class="h-16 rounded-xl bg-zinc-900/50 animate-pulse"></div>
    </div>

    <div
      v-else-if="problemList.length === 0"
      class="flex flex-col items-center justify-center h-64 text-zinc-500 border border-dashed border-zinc-800 rounded-2xl bg-zinc-900/20"
    >
      <p>该收藏夹是空的</p>
      <button @click="router.push('/problems')" class="mt-4 text-[#FF4C00] text-sm hover:underline">
        去题库添加
      </button>
    </div>

    <div v-else class="space-y-3">
      <TransitionGroup name="list">
        <div
          v-for="item in problemList"
          :key="item.id"
          class="group flex items-center justify-between p-4 rounded-xl bg-zinc-900/40 border border-white/5 hover:bg-zinc-800/60 hover:border-[#FF4C00]/30 transition-all duration-300"
        >
          <div class="flex items-center gap-4 md:gap-8 overflow-hidden">
            <span class="font-mono text-zinc-500 text-xs w-12 shrink-0">#{{ item.displayId }}</span>
            <div class="flex flex-col gap-1 min-w-0">
              <h3
                class="text-white font-bold text-sm truncate group-hover:text-[#FF4C00] transition-colors cursor-pointer"
                @click="goToProblem(item.id)"
              >
                {{ item.title }}
              </h3>
              <div class="flex gap-2">
                <span
                  class="text-[10px] px-1.5 rounded border"
                  :class="difficultyClass(item.difficulty)"
                >
                  {{ difficultyText(item.difficulty) }}
                </span>
                <span
                  v-for="tag in item.tags"
                  :key="tag"
                  class="text-[10px] text-zinc-500 bg-white/5 px-1.5 rounded hidden md:inline-block"
                >
                  {{ tag }}
                </span>
              </div>
            </div>
          </div>

          <div class="flex items-center gap-4 shrink-0 pl-4">
            <span class="text-[10px] text-zinc-600 font-mono hidden md:block">
              {{ item.joinTime }} 加入
            </span>
            <button
              @click="triggerRemove(item)"
              class="p-2 rounded-lg text-zinc-500 hover:text-red-500 hover:bg-red-500/10 transition-colors"
              title="取消收藏"
            >
              <Trash2 class="w-4 h-4" />
            </button>
          </div>
        </div>
      </TransitionGroup>
    </div>

    <ArenaDialog
      v-model="showRemoveDialog"
      title="移出收藏夹"
      confirm-text="确认移出"
      @confirm="confirmRemove"
    >
      <p class="text-zinc-300 text-sm text-center">
        确定将题目
        <span class="text-white font-bold">"{{ targetProblem?.title }}"</span> 移出该收藏夹吗？
      </p>
    </ArenaDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ChevronLeft, FolderOpen, Trash2 } from 'lucide-vue-next'
import ArenaDialog from '@/components/arena/ArenaDialog.vue'
import {
  getFolderDetail,
  removeFavoriteProblem,
  type FavoriteProblem,
  type FavoriteFolder,
} from '@/api/favorites'
import type { ProblemDifficulty } from '@/api/problem'

const route = useRoute()
const router = useRouter()
const folderId = Number(route.params.id)

const loading = ref(false)
const folderInfo = ref<Partial<FavoriteFolder>>({ name: '加载中...' })
const problemList = ref<FavoriteProblem[]>([])

// 移除逻辑
const showRemoveDialog = ref(false)
const targetProblem = ref<FavoriteProblem | null>(null)

// 样式映射
const difficultyClass = (diff: ProblemDifficulty) => {
  const map = {
    EASY: 'text-emerald-500 border-emerald-500/20 bg-emerald-500/10',
    MEDIUM: 'text-amber-500 border-amber-500/20 bg-amber-500/10',
    HARD: 'text-rose-500 border-rose-500/20 bg-rose-500/10',
  }
  return map[diff]
}

const difficultyText = (diff: ProblemDifficulty) => {
  return diff === 'EASY' ? '简单' : diff === 'MEDIUM' ? '中等' : '困难'
}

// 初始化
const init = async () => {
  loading.value = true
  try {
    const res = await getFolderDetail(folderId)
    folderInfo.value = res.folder
    problemList.value = res.list
  } finally {
    loading.value = false
  }
}

// 移除操作
const triggerRemove = (item: FavoriteProblem) => {
  targetProblem.value = item
  showRemoveDialog.value = true
}

const confirmRemove = async () => {
  if (!targetProblem.value) return
  showRemoveDialog.value = false

  await removeFavoriteProblem(folderId, targetProblem.value.id)

  // 前端动画移除
  problemList.value = problemList.value.filter((p) => p.id !== targetProblem.value?.id)
}

const goToProblem = (id: number) => {
  router.push(`/problems/${id}`)
}

onMounted(() => {
  init()
})
</script>

<style scoped>
/* 列表移除动画 */
.list-move,
.list-enter-active,
.list-leave-active {
  transition: all 0.4s ease;
}

.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

.list-leave-active {
  position: absolute;
  width: 100%;
}
</style>
