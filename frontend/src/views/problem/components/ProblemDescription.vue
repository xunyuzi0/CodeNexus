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
                class="px-2 py-0.5 rounded text-[10px] font-bold border border-emerald-500/20 bg-emerald-500/10 text-emerald-500 uppercase tracking-wider"
              >
                Easy
              </span>
              <span class="text-zinc-500 flex items-center gap-1">
                <CheckCircle2 class="w-3 h-3" /> 通过率 86.4%
              </span>
            </div>
          </div>

          <div
            class="prose prose-invert prose-sm max-w-none text-zinc-400 leading-relaxed prose-headings:text-zinc-100 prose-headings:font-bold prose-headings:tracking-tight prose-p:my-4 prose-p:leading-7 prose-strong:text-zinc-200 prose-strong:font-semibold prose-code:text-[#FF4C00] prose-code:bg-zinc-800/50 prose-code:px-1.5 prose-code:py-0.5 prose-code:rounded prose-code:font-mono prose-code:text-xs prose-code:before:content-none prose-code:after:content-none prose-pre:bg-[#0d0d0d] prose-pre:border prose-pre:border-white/10 prose-pre:rounded-xl prose-pre:p-4 prose-a:text-[#FF4C00] prose-a:no-underline hover:prose-a:underline prose-li:marker:text-zinc-600"
            v-html="problem.content"
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
          <div class="flex items-center justify-between">
            <h2 class="text-lg font-bold text-white">官方题解</h2>
            <span class="text-xs text-zinc-500">阅读量 12.5k</span>
          </div>
          <div
            class="h-32 w-full bg-zinc-800/20 border border-white/5 rounded-xl flex items-center justify-center"
          >
            <p class="text-zinc-500 text-sm">题解内容加载中...</p>
          </div>
        </div>

        <div v-else key="submissions" class="space-y-3 animate-in fade-in slide-in-from-bottom-2">
          <h2 class="text-lg font-bold text-white mb-4">我的提交</h2>
          <div
            v-for="i in 3"
            :key="i"
            class="p-4 bg-zinc-900/40 border border-white/5 rounded-xl flex items-center justify-between group hover:border-white/10 transition-colors"
          >
            <div class="flex items-center gap-3">
              <div
                class="w-2 h-2 rounded-full"
                :class="i === 1 ? 'bg-emerald-500' : 'bg-red-500'"
              ></div>
              <div>
                <p
                  class="text-sm font-medium"
                  :class="i === 1 ? 'text-emerald-500' : 'text-red-500'"
                >
                  {{ i === 1 ? 'Accepted' : 'Wrong Answer' }}
                </p>
                <p class="text-xs text-zinc-500">2 months ago</p>
              </div>
            </div>
            <span class="text-xs font-mono text-zinc-400">12ms / 42.1MB</span>
          </div>
        </div>
      </Transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import {
  FileText,
  FlaskConical,
  History,
  Maximize2,
  Minimize2,
  CheckCircle2,
  Tag,
} from 'lucide-vue-next'

interface Props {
  problem: {
    title: string
    content: string
    examples: { input: string; output: string; explanation?: string }[]
    tags: string[]
  }
  isMaximized: boolean
  hideTabs?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  hideTabs: false,
})
defineEmits(['toggle-maximize'])

const currentTab = ref('desc')

const tabs = [
  { id: 'desc', label: '描述', icon: FileText },
  { id: 'solution', label: '题解', icon: FlaskConical },
  { id: 'submissions', label: '记录', icon: History },
]

// 支持在对战模式下动态隐藏多余 Tab
const filteredTabs = computed(() => {
  if (props.hideTabs) return tabs.filter((t) => t.id === 'desc')
  return tabs
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
</style>
