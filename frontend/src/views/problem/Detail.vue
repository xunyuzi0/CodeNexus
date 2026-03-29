<template>
  <div class="h-screen w-full bg-zinc-950 text-white flex flex-col overflow-hidden relative">
    <div
      class="absolute inset-0 pointer-events-none z-0 opacity-20"
      style="
        background-image: radial-gradient(#3f3f46 1px, transparent 1px);
        background-size: 24px 24px;
      "
    ></div>

    <ProblemHeader class="relative z-20" :is-timer-paused="isTimerPaused" :problem="problem" />

    <div class="flex-1 min-h-0 relative z-10 w-full max-w-[1920px] mx-auto">
      <div v-if="loading" class="w-full h-full flex p-4 gap-4">
        <div
          class="flex-1 bg-zinc-900/40 backdrop-blur-sm border border-white/5 rounded-2xl p-8 animate-pulse"
        >
          <div class="h-8 bg-zinc-800 rounded-lg w-1/3 mb-6"></div>
          <div class="flex gap-2 mb-8">
            <div class="h-6 bg-zinc-800 rounded-md w-16"></div>
            <div class="h-6 bg-zinc-800 rounded-md w-20"></div>
          </div>
          <div class="space-y-3">
            <div class="h-4 bg-zinc-800 rounded w-full"></div>
            <div class="h-4 bg-zinc-800 rounded w-5/6"></div>
            <div class="h-4 bg-zinc-800 rounded w-4/6"></div>
          </div>
        </div>
        <div
          class="flex-1 bg-[#1e1e1e] border border-white/5 rounded-2xl p-8 animate-pulse hidden md:block"
        >
          <div class="flex gap-2 mb-4 border-b border-white/5 pb-4">
            <div class="h-6 bg-zinc-800 rounded-md w-24"></div>
          </div>
          <div class="space-y-2 mt-8">
            <div class="h-4 bg-zinc-800 rounded w-1/2 ml-4"></div>
            <div class="h-4 bg-zinc-800 rounded w-1/3 ml-8"></div>
          </div>
        </div>
      </div>

      <splitpanes v-else class="zeekr-theme" horizontal>
        <pane size="100">
          <splitpanes vertical @resize="handleResize" class="h-full">
            <pane
              v-show="paneSize.left > 0"
              :size="paneSize.left"
              :min-size="maximizedPane !== 'none' ? 0 : 20"
              class="flex flex-col min-w-0 transition-[width] duration-300 ease-in-out bg-zinc-900/40 backdrop-blur-sm border-r border-white/5"
            >
              <ProblemDescription
                v-if="problem"
                :problem="problem"
                :is-maximized="maximizedPane === 'left'"
                @toggle-maximize="toggleMaximizeLeft"
              />
            </pane>

            <pane
              v-show="paneSize.right > 0"
              :size="paneSize.right"
              :min-size="maximizedPane !== 'none' ? 0 : 30"
              class="flex flex-col relative min-w-0 transition-[width] duration-300 ease-in-out bg-[#1e1e1e]"
            >
              <CodeWorkspace
                v-model="code"
                :problem="problem"
                :is-maximized="maximizedPane === 'right'"
                @toggle-maximize="toggleMaximizeRight"
                @success="handleProblemSuccess"
              />
            </pane>
          </splitpanes>
        </pane>
      </splitpanes>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Splitpanes, Pane } from 'splitpanes'
import 'splitpanes/dist/splitpanes.css'
import ProblemHeader from './components/ProblemHeader.vue'
import ProblemDescription from './components/ProblemDescription.vue'
import CodeWorkspace from './components/CodeWorkspace.vue'

// 引入真实的 API
import { getProblemDetail, type Problem } from '@/api/problem'

const route = useRoute()

// --- Data State ---
const loading = ref(true)
const problem = ref<Problem | null>(null)

// 默认应用 ACM 模式模板
const code = ref(`import java.util.*;
import java.io.*;

public class Solution {

    /**
     * 核心算法逻辑写在这里
     */
    public void solve() {
        // TODO: 在这里编写你的算法逻辑
        
    }

    /**
     * CodeNexus ACM 模式程序入口
     * 请在此处解析标准输入 (Standard Input) 并打印标准输出 (Standard Output)
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // 示例：如果题目需要读取多行或特定格式的输入
        /*
        if (scanner.hasNextLine()) {
            // 1. 读取并解析输入数据
            String line = scanner.nextLine();
            
            // 2. 实例化并调用核心算法
            Solution sol = new Solution();
            // Object result = sol.solve(...);
            
            // 3. 按照题目要求的格式打印输出
            // System.out.println(result);
        }
        */
    }
}
`)

// 控制计时器的开关状态
const isTimerPaused = ref(false)

const handleProblemSuccess = () => {
  isTimerPaused.value = true // 全通后暂停顶部的时间
}

// --- Layout State Management ---
const maximizedPane = ref<'none' | 'left' | 'right'>('none')
const paneSize = reactive({ left: 40, right: 60 })
const lastSize = reactive({ left: 40, right: 60 })

const handleResize = (event: { min: number; max: number; size: number }[]) => {
  if (maximizedPane.value === 'none' && event && event.length >= 2 && event[0] && event[1]) {
    paneSize.left = event[0].size
    paneSize.right = event[1].size
  }
}

const toggleMaximizeLeft = () => {
  if (maximizedPane.value === 'left') {
    maximizedPane.value = 'none'
    paneSize.left = lastSize.left
    paneSize.right = lastSize.right
  } else {
    if (maximizedPane.value === 'none') {
      lastSize.left = paneSize.left
      lastSize.right = paneSize.right
    }
    maximizedPane.value = 'left'
    paneSize.left = 100
    paneSize.right = 0
  }
}

const toggleMaximizeRight = () => {
  if (maximizedPane.value === 'right') {
    maximizedPane.value = 'none'
    paneSize.left = lastSize.left
    paneSize.right = lastSize.right
  } else {
    if (maximizedPane.value === 'none') {
      lastSize.left = paneSize.left
      lastSize.right = paneSize.right
    }
    maximizedPane.value = 'right'
    paneSize.left = 0
    paneSize.right = 100
  }
}

// --- Lifecycle ---
onMounted(async () => {
  const problemId = route.params.id as string
  if (!problemId) return

  loading.value = true
  try {
    const data = await getProblemDetail(problemId)
    problem.value = data
  } catch (error) {
    console.error('获取题目详情失败:', error)
  } finally {
    loading.value = false
  }
})
</script>

<style>
/* Splitpanes 的极氪风定制样式保持不变 */
.zeekr-theme.splitpanes {
  background-color: transparent !important;
}
.zeekr-theme .splitpanes__pane {
  background-color: transparent !important;
  overflow: hidden;
}
.zeekr-theme .splitpanes__splitter {
  background-color: #09090b !important;
  border: none !important;
  box-sizing: border-box;
  position: relative;
  z-index: 20;
  transition: all 0.2s ease;
}
.zeekr-theme.splitpanes--vertical > .splitpanes__splitter {
  width: 6px !important;
  border-left: 1px solid rgba(255, 255, 255, 0.05) !important;
  border-right: 1px solid rgba(255, 255, 255, 0.05) !important;
  margin-left: -1px;
}
.zeekr-theme.splitpanes--horizontal > .splitpanes__splitter {
  height: 6px !important;
  border-top: 1px solid rgba(255, 255, 255, 0.05) !important;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05) !important;
  margin-top: -1px;
}
.zeekr-theme .splitpanes__splitter::after {
  content: '';
  position: absolute;
  background-color: #ff4c00;
  opacity: 0;
  transition: opacity 0.2s ease;
  pointer-events: none;
}
.zeekr-theme.splitpanes--vertical > .splitpanes__splitter::after {
  top: 10%;
  bottom: 10%;
  left: 50%;
  width: 1px;
  transform: translateX(-50%);
}
.zeekr-theme.splitpanes--horizontal > .splitpanes__splitter::after {
  left: 10%;
  right: 10%;
  top: 50%;
  height: 1px;
  transform: translateY(-50%);
}
.zeekr-theme .splitpanes__splitter:hover::after,
.zeekr-theme .splitpanes__splitter:active::after {
  opacity: 1;
  box-shadow: 0 0 8px rgba(255, 76, 0, 0.6);
}
</style>
