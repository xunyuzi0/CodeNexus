<template>
  <div
    class="flex h-[calc(100vh-80px)] -mx-8 -mb-8 w-[calc(100%+64px)] overflow-hidden bg-black font-sans"
  >
    <div
      class="w-2/5 flex flex-col border-r border-white/5 bg-zinc-900/30 backdrop-blur-3xl relative z-10"
    >
      <header
        class="h-14 shrink-0 flex items-center justify-between px-4 border-b border-white/5 bg-zinc-950/50 backdrop-blur-md"
      >
        <div class="flex items-center gap-4">
          <button
            @click="router.back()"
            class="p-2 -ml-2 rounded-lg text-zinc-400 hover:text-white hover:bg-white/5 transition-colors"
          >
            <ChevronLeft class="w-5 h-5" />
          </button>
          <div class="h-4 w-[1px] bg-white/10"></div>
          <div class="flex items-center gap-2">
            <span class="text-sm font-bold text-white">#{{ problem.id }}</span>
            <span
              class="text-xs px-2 py-0.5 rounded-md bg-emerald-500/10 text-emerald-500 font-medium border border-emerald-500/20"
            >
              {{ problem.difficulty }}
            </span>
          </div>
        </div>

        <div class="flex items-center gap-1 bg-black/20 p-1 rounded-lg">
          <button
            v-for="tab in ['描述', '题解', '提交记录']"
            :key="tab"
            @click="activeTab = tab"
            class="px-3 py-1 text-xs font-medium rounded-md transition-all"
            :class="
              activeTab === tab
                ? 'bg-zinc-800 text-white shadow-sm'
                : 'text-zinc-500 hover:text-zinc-300'
            "
          >
            {{ tab }}
          </button>
        </div>
      </header>

      <div class="flex-1 overflow-y-auto custom-scrollbar p-6">
        <div class="prose prose-invert prose-sm max-w-none">
          <h1 class="text-2xl font-bold mb-4">{{ problem.title }}</h1>

          <div v-html="problem.description"></div>

          <div class="mt-8 space-y-4">
            <div
              v-for="(ex, i) in problem.examples"
              :key="i"
              class="bg-zinc-900/80 border border-white/5 rounded-xl p-4"
            >
              <p class="text-xs font-bold text-zinc-500 mb-2 uppercase tracking-wider">
                Example {{ i + 1 }}
              </p>
              <div class="space-y-1 font-mono text-sm">
                <div>
                  <span class="text-zinc-500">Input:</span>
                  <span class="text-zinc-300">{{ ex.input }}</span>
                </div>
                <div>
                  <span class="text-zinc-500">Output:</span>
                  <span class="text-white">{{ ex.output }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="mt-8 flex gap-2">
            <span
              v-for="tag in problem.tags"
              :key="tag"
              class="px-2 py-1 text-xs bg-zinc-800 text-zinc-400 rounded hover:bg-zinc-700 cursor-pointer transition-colors"
            >
              #{{ tag }}
            </span>
          </div>
        </div>

        <div class="h-20"></div>
      </div>
    </div>

    <div class="w-3/5 flex flex-col relative bg-[#1e1e1e]">
      <div
        class="h-12 shrink-0 flex items-center justify-between px-4 border-b border-[#2b2b2b] bg-[#1e1e1e]"
      >
        <div class="flex items-center gap-4">
          <div
            class="flex items-center gap-2 text-xs text-zinc-300 hover:text-white cursor-pointer transition-colors px-2 py-1 rounded hover:bg-white/5"
          >
            <Code2 class="w-4 h-4 text-blue-400" />
            <span>Java (JDK 17)</span>
          </div>
        </div>
        <div class="flex items-center gap-2">
          <button
            class="p-2 rounded text-zinc-500 hover:text-white hover:bg-white/5 transition-colors"
          >
            <Settings class="w-4 h-4" />
          </button>
        </div>
      </div>

      <div class="flex-1 relative font-mono text-sm">
        <div class="absolute inset-0 p-4 text-[#d4d4d4] leading-relaxed selection:bg-[#264f78]">
          <span class="text-[#569cd6]">class</span>
          <span class="text-[#4ec9b0]">Solution</span> {<br />
          &nbsp;&nbsp;&nbsp;&nbsp;<span class="text-[#569cd6]">public</span>
          <span class="text-[#569cd6]">int</span> <span class="text-[#dcdcaa]">solve</span>(<span
            class="text-[#569cd6]"
            >int</span
          >[] <span class="text-[#9cdcfe]">nums</span>) {<br />
          &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="text-[#6a9955]"
            >// Write your code here...</span
          ><br />
          <br />
          &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="text-[#c586c0]">return</span>
          0;<br />
          &nbsp;&nbsp;&nbsp;&nbsp;}<br />
          }
        </div>
      </div>

      <div class="absolute bottom-6 right-6 flex items-center gap-3">
        <button
          @click="toggleZenMode"
          class="h-10 w-10 flex items-center justify-center rounded-xl bg-zinc-800 text-zinc-400 border border-white/5 hover:text-white hover:bg-zinc-700 hover:border-white/20 transition-all shadow-lg"
          :title="isZenMode ? 'Exit Zen Mode' : 'Enter Zen Mode'"
        >
          <Minimize2 v-if="isZenMode" class="w-5 h-5" />
          <Maximize2 v-else class="w-5 h-5" />
        </button>

        <div class="h-6 w-[1px] bg-white/10 mx-1"></div>

        <button
          class="h-10 px-4 rounded-xl bg-zinc-800 text-zinc-300 border border-white/5 hover:bg-zinc-700 hover:text-white transition-all font-medium text-sm flex items-center gap-2"
        >
          <Play class="w-4 h-4" /> 运行
        </button>

        <button
          class="h-10 px-6 rounded-xl bg-[#FF4C00] text-white font-bold text-sm flex items-center gap-2 shadow-[0_0_20px_rgba(255,76,0,0.4)] hover:shadow-[0_0_30px_rgba(255,76,0,0.6)] hover:bg-[#ff5f1f] transition-all active:scale-95"
        >
          <Send class="w-4 h-4" /> 提交
        </button>
      </div>

      <div
        class="absolute bottom-0 left-0 right-0 h-9 bg-[#1e1e1e] border-t border-[#2b2b2b] flex items-center justify-between px-4 cursor-pointer hover:bg-[#252526] transition-colors"
      >
        <div class="flex items-center gap-2 text-xs text-zinc-400">
          <Terminal class="w-3 h-3" />
          <span>Console</span>
          <ChevronUp class="w-3 h-3 ml-1" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useUiStore } from '@/stores/ui'
import {
  ChevronLeft,
  Settings,
  Play,
  Send,
  Code2,
  Maximize2,
  Minimize2,
  Terminal,
  ChevronUp,
} from 'lucide-vue-next'

const router = useRouter()
const uiStore = useUiStore()
const { isSidebarCollapsed } = storeToRefs(uiStore)

// --- State ---
const activeTab = ref('描述')
const isZenMode = computed(() => isSidebarCollapsed.value)

// --- Mock Data ---
const problem = {
  id: '1001',
  title: '两数之和 (Two Sum)',
  difficulty: 'EASY',
  description: `
    <p>给定一个整数数组 <code>nums</code> 和一个整数目标值 <code>target</code>，请你在该数组中找出 <strong>和为目标值</strong> <em>target</em>  的那 <strong>两个</strong> 整数，并返回它们的数组下标。</p>
    <p>你可以假设每种输入只会对应一个答案。但是，数组中同一个元素在答案里不能重复出现。</p>
  `,
  examples: [
    { input: 'nums = [2,7,11,15], target = 9', output: '[0,1]' },
    { input: 'nums = [3,2,4], target = 6', output: '[1,2]' },
  ],
  tags: ['数组', '哈希表'],
}

// --- Methods ---
const toggleZenMode = () => {
  uiStore.toggleSidebar()
}

// --- Lifecycle ---
// 离开页面时，强制恢复侧边栏显示，防止影响其他页面
onUnmounted(() => {
  if (isSidebarCollapsed.value) {
    uiStore.setSidebar(false)
  }
})
</script>

<style scoped>
/* 隐藏滚动条但保留滚动功能 */
.custom-scrollbar::-webkit-scrollbar {
  width: 0px;
  background: transparent;
}
.custom-scrollbar {
  scrollbar-width: none;
}
</style>
