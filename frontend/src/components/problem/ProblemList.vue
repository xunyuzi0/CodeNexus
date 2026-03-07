<template>
  <div class="flex flex-col gap-3">
    <div
      v-for="(item, index) in problems"
      :key="item.id"
      @click="emit('click', item.id)"
      class="group relative flex items-center justify-between bg-zinc-900/50 backdrop-blur-md border border-white/5 rounded-xl p-4 transition-all duration-300 cursor-pointer hover:border-[#FF4C00]/50 hover:bg-zinc-800/80 hover:shadow-[0_0_15px_rgba(255,76,0,0.15)] hover:-translate-y-0.5"
      v-motion
      :initial="{ opacity: 0, y: 30 }"
      :enter="{
        opacity: 1,
        y: 0,
        transition: {
          duration: 350,
          ease: 'easeOut',
          delay: Math.min(index * 40, 400),
        },
      }"
    >
      <div
        class="absolute left-0 top-4 bottom-4 w-1 bg-[#FF4C00] rounded-r-full opacity-0 group-hover:opacity-100 transition-opacity duration-300 shadow-[0_0_12px_#FF4C00]"
      ></div>

      <div class="flex items-center gap-5 overflow-hidden flex-1 pl-2">
        <div class="shrink-0" v-if="item.status !== undefined">
          <CheckCircle2
            v-if="item.status === 'PASSED'"
            class="w-5 h-5 text-emerald-500/80 drop-shadow-[0_0_8px_rgba(16,185,129,0.4)]"
          />
          <div
            v-else-if="item.status === 'ATTEMPTED'"
            class="w-4 h-4 rounded-full border-[3px] border-amber-500/30"
          ></div>
          <Circle v-else class="w-5 h-5 text-zinc-700" />
        </div>

        <div class="flex flex-col min-w-0">
          <div class="flex items-baseline gap-3">
            <span
              class="text-zinc-500 font-mono text-sm font-bold group-hover:text-zinc-400 transition-colors"
            >
              #{{ item.displayId }}
            </span>
            <h3
              class="text-zinc-200 font-bold text-base truncate group-hover:text-white transition-colors"
            >
              {{ item.title }}
            </h3>
          </div>

          <div class="flex items-center gap-2 mt-1.5">
            <span
              v-for="tag in item.tags"
              :key="tag"
              class="border border-white/5 bg-white/5 text-xs text-zinc-500 px-2.5 py-1 rounded-md font-medium group-hover:border-white/10 group-hover:text-zinc-400 transition-colors"
            >
              {{ tag }}
            </span>
            <span
              v-if="item.joinTime"
              class="text-xs text-zinc-600 font-mono ml-2 border-l border-white/10 pl-2"
            >
              Added: {{ item.joinTime.split(' ')[0] }}
            </span>
          </div>
        </div>
      </div>

      <div class="hidden md:flex items-center gap-8 shrink-0 pr-2">
        <div class="text-right" v-if="item.passRate !== undefined">
          <p class="text-zinc-600 text-xs uppercase font-bold tracking-wider mb-0.5">首次通过率</p>
          <p class="text-zinc-300 font-mono text-sm">{{ item.passRate }}%</p>
        </div>

        <div class="w-16 flex justify-end">
          <span
            class="text-xs font-black px-2 py-1 rounded-md select-none tracking-wide border"
            :class="difficultyColorMap[item.difficulty]"
          >
            {{ difficultyTextMap[item.difficulty] }}
          </span>
        </div>

        <button
          @click.stop="emit('action', item.id)"
          class="w-8 h-8 flex items-center justify-center rounded-full hover:bg-white/5 transition-all active:scale-90"
          :title="actionType === 'favorite' ? '操作收藏' : '移除收藏'"
        >
          <Star
            v-if="actionType === 'favorite'"
            class="w-5 h-5 transition-colors duration-300"
            :class="
              collectedIds.includes(item.id)
                ? 'text-[#FF4C00] fill-[#FF4C00] drop-shadow-[0_0_8px_rgba(255,76,0,0.5)]'
                : 'text-zinc-600 hover:text-[#FF4C00]'
            "
          />
          <Trash2
            v-else-if="actionType === 'remove'"
            class="w-5 h-5 text-zinc-600 hover:text-red-500 hover:drop-shadow-[0_0_8px_rgba(239,68,68,0.5)] transition-colors duration-300"
          />
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { CheckCircle2, Circle, Star, Trash2 } from 'lucide-vue-next'
import type { ProblemDifficulty } from '@/api/problem'

// 聚合类型：兼容题库的 Problem 和 收藏夹的 FavoriteProblem
export interface BaseProblemItem {
  id: number
  displayId: string
  title: string
  difficulty: ProblemDifficulty
  tags: string[]
  status?: string // 题库特有
  passRate?: number // 题库特有
  joinTime?: string // 收藏夹特有
}

const props = withDefaults(
  defineProps<{
    problems: BaseProblemItem[]
    actionType?: 'favorite' | 'remove'
    collectedIds?: number[] // 用于判断题库中哪些题目被收藏了以标红星星
  }>(),
  {
    actionType: 'favorite',
    collectedIds: () => [],
  },
)

const emit = defineEmits<{
  (e: 'click', id: number): void
  (e: 'action', id: number): void
}>()

// --- 难度配置字典 ---
const difficultyColorMap: Record<ProblemDifficulty, string> = {
  EASY: 'text-emerald-500 border-emerald-500/20 bg-emerald-500/10',
  MEDIUM: 'text-amber-500 border-amber-500/20 bg-amber-500/10',
  HARD: 'text-rose-500 border-rose-500/20 bg-rose-500/10',
}

const difficultyTextMap: Record<ProblemDifficulty, string> = {
  EASY: '简单',
  MEDIUM: '中等',
  HARD: '困难',
}
</script>
