<template>
  <div class="h-screen w-full bg-zinc-950 text-white flex flex-col overflow-hidden relative">
    <div
      class="absolute inset-0 pointer-events-none z-0 opacity-20"
      style="
        background-image: radial-gradient(#3f3f46 1px, transparent 1px);
        background-size: 24px 24px;
      "
    ></div>

    <ProblemHeader class="relative z-20" :is-timer-paused="isTimerPaused" />

    <div class="flex-1 min-h-0 relative z-10 w-full max-w-[1920px] mx-auto">
      <splitpanes class="zeekr-theme" horizontal>
        <pane size="100">
          <splitpanes vertical @resize="handleResize" class="h-full">
            <pane
              v-show="paneSize.left > 0"
              :size="paneSize.left"
              :min-size="maximizedPane !== 'none' ? 0 : 20"
              class="flex flex-col min-w-0 transition-[width] duration-300 ease-in-out bg-zinc-900/40 backdrop-blur-sm border-r border-white/5"
            >
              <ProblemDescription
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
import { ref, reactive } from 'vue'
import { Splitpanes, Pane } from 'splitpanes'
import 'splitpanes/dist/splitpanes.css'
import ProblemHeader from './components/ProblemHeader.vue'
import ProblemDescription from './components/ProblemDescription.vue'
import CodeWorkspace from './components/CodeWorkspace.vue'

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

// --- Mock Data ---
const code = ref(
  `class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        \n    }\n}`,
)
const problem = {
  title: '1. 两数之和',
  content: `<p>给定一个整数数组 <code>nums</code> 和一个整数目标值 <code>target</code>，请你在该数组中找出 <strong>和为目标值</strong> <em><code>target</code></em>  的那 <strong>两个</strong> 整数，并返回它们的数组下标。</p>
  <p>你可以假设每种输入只会对应一个答案。但是，数组中同一个元素在答案里不能重复出现。</p>`,
  examples: [
    {
      input: 'nums = [2,7,11,15], target = 9',
      output: '[0,1]',
      explanation: '因为 nums[0] + nums[1] == 9 ，返回 [0, 1] 。',
    },
    { input: 'nums = [3,2,4], target = 6', output: '[1,2]' },
  ],
  tags: ['数组', '哈希表'],
}
</script>

<style>
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
