<template>
  <Teleport to="body">
    <Transition name="fade">
      <div
        v-if="show"
        class="fixed inset-0 z-[100] flex items-center justify-center bg-zinc-950/80 backdrop-blur-md"
        v-motion-fade
      >
        <div
          class="w-[600px] bg-zinc-900 border border-white/10 rounded-2xl shadow-2xl overflow-hidden flex flex-col"
          v-motion-pop-visible
        >
          <div
            class="px-6 py-4 border-b border-white/5 flex items-center justify-between bg-zinc-950/50"
          >
            <div class="flex items-center gap-3">
              <Loader2 v-if="status === 'judging'" class="w-5 h-5 text-[#FF4C00] animate-spin" />
              <CheckCircle2
                v-else-if="status === 'accepted'"
                class="w-6 h-6 text-emerald-500 animate-[bounce_1s_ease-in-out_infinite]"
              />
              <XCircle v-else class="w-5 h-5 text-rose-500" />

              <h2
                class="text-lg font-bold tracking-wider"
                :class="{
                  'text-[#FF4C00] animate-pulse drop-shadow-[0_0_8px_rgba(255,76,0,0.5)]':
                    status === 'judging',
                  'text-emerald-500 drop-shadow-[0_0_8px_rgba(16,185,129,0.5)]':
                    status === 'accepted',
                  'text-rose-500 drop-shadow-[0_0_8px_rgba(244,63,94,0.5)]': status === 'rejected',
                }"
              >
                {{
                  status === 'judging' ? '判题中...' : status === 'accepted' ? '通过本题' : '未通过'
                }}
              </h2>
            </div>

            <div class="flex items-center gap-4">
              <div class="text-xs font-mono text-zinc-500">
                Checkpoints:
                {{
                  checkpoints.filter((c) => c.status !== 'pending' && c.status !== 'running').length
                }}
                / {{ checkpoints.length }}
              </div>

              <button
                v-if="mode === 'battle' && status === 'rejected'"
                @click="emit('close')"
                class="p-1 rounded-md text-zinc-500 hover:text-white hover:bg-white/10 transition-colors"
                title="关闭雷达"
              >
                <X class="w-4 h-4" />
              </button>
            </div>
          </div>

          <div class="p-6 grid grid-cols-5 gap-3 bg-zinc-900/50">
            <div
              v-for="cp in checkpoints"
              :key="cp.id"
              class="relative group h-12 rounded-xl border flex items-center justify-center font-mono text-sm transition-all duration-300"
              :class="getCheckpointClass(cp.status)"
            >
              <span v-if="cp.status === 'pending'" class="opacity-30">-</span>
              <Loader2 v-else-if="cp.status === 'running'" class="w-4 h-4 animate-spin" />
              <span v-else class="font-bold">{{ cp.status }}</span>

              <div
                v-if="cp.time"
                class="absolute -top-8 left-1/2 -translate-x-1/2 px-2 py-1 bg-zinc-800 text-zinc-300 text-[10px] rounded opacity-0 group-hover:opacity-100 pointer-events-none transition-opacity border border-white/5 shadow-xl whitespace-nowrap z-10"
              >
                {{ cp.time }}ms
              </div>
            </div>
          </div>

          <div
            v-if="mode === 'practice' && status !== 'judging'"
            class="px-6 py-4 bg-zinc-950/50 border-t border-white/5 flex items-center justify-end gap-3 animate-in fade-in slide-in-from-bottom-2"
          >
            <button
              @click="emit('to-problems')"
              class="px-5 py-2 text-zinc-400 hover:text-white hover:bg-white/5 rounded-lg transition-colors text-sm font-medium active:scale-95 duration-200"
            >
              回到题库
            </button>
            <button
              @click="emit('close')"
              class="px-5 py-2 text-zinc-400 hover:text-white hover:bg-white/5 rounded-lg transition-colors text-sm font-medium active:scale-95 duration-200"
            >
              继续研究
            </button>
            <button
              v-if="status === 'accepted'"
              @click="emit('next-problem')"
              class="px-6 py-2 bg-[#FF4C00] text-white rounded-lg transition-all text-sm font-bold shadow-[0_0_15px_rgba(255,76,0,0.3)] hover:shadow-[0_0_25px_rgba(255,76,0,0.5)] hover:bg-[#ff5f1f] active:scale-95 duration-200"
            >
              刷下一题
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { Loader2, CheckCircle2, XCircle, X } from 'lucide-vue-next'

type CheckpointStatus = 'pending' | 'running' | 'AC' | 'WA' | 'TLE' | 'MLE' | 'RE'

interface Checkpoint {
  id: number
  status: CheckpointStatus
  time?: number
}

interface Props {
  show: boolean
  status: 'judging' | 'accepted' | 'rejected'
  checkpoints: Checkpoint[]
  mode?: 'practice' | 'battle' // 核心：区分对战还是刷题
}

const props = withDefaults(defineProps<Props>(), {
  mode: 'practice',
})

const emit = defineEmits(['close', 'to-problems', 'next-problem'])

const getCheckpointClass = (status: CheckpointStatus) => {
  switch (status) {
    case 'pending':
      return 'bg-white/5 border-white/5 text-zinc-500'
    case 'running':
      return 'bg-orange-500/10 border-orange-500/50 text-orange-500 animate-pulse'
    case 'AC':
      return 'bg-emerald-500/10 border-emerald-500/50 text-emerald-500 shadow-[0_0_10px_rgba(16,185,129,0.2)]'
    default:
      return 'bg-rose-500/10 border-rose-500/50 text-rose-500 shadow-[0_0_10px_rgba(244,63,94,0.2)]'
  }
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
</style>
