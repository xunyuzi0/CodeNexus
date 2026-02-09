<template>
  <div class="space-y-8" v-motion-slide-visible-bottom>
    <div class="space-y-4">
      <h3 class="text-sm font-bold text-zinc-500 uppercase tracking-wider">编辑器主题</h3>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div
          v-for="theme in themes"
          :key="theme.id"
          @click="selectTheme(theme)"
          class="relative group rounded-xl border p-3 cursor-pointer transition-all duration-300 overflow-hidden flex flex-col gap-3"
          :class="[
            currentTheme === theme.id
              ? 'bg-[#FF4C00]/10 border-[#FF4C00] shadow-[0_0_20px_rgba(255,76,0,0.1)]'
              : 'bg-zinc-900/50 border-white/5',
            // [修改点 1]: 禁用状态使用透明度+灰度，移除遮罩层，提升视觉通透感
            theme.disabled
              ? 'opacity-30 grayscale cursor-not-allowed'
              : 'hover:border-white/20 hover:scale-[1.02]',
          ]"
        >
          <div
            class="w-full aspect-[4/3] rounded-lg p-2 flex flex-col gap-2 shadow-inner"
            :style="{ background: theme.color }"
          >
            <div class="flex items-center gap-1.5 opacity-50">
              <div class="w-1.5 h-1.5 rounded-full bg-red-500"></div>
              <div class="w-1.5 h-1.5 rounded-full bg-yellow-500"></div>
              <div class="w-1.5 h-1.5 rounded-full bg-emerald-500"></div>
            </div>
            <div class="space-y-1.5 mt-1 opacity-40">
              <div class="h-1 w-1/2 bg-white/50 rounded-full"></div>
              <div class="h-1 w-3/4 bg-white/30 rounded-full"></div>
              <div class="h-1 w-2/3 bg-white/30 rounded-full"></div>
              <div class="h-1 w-full bg-white/20 rounded-full"></div>
            </div>
          </div>

          <p
            class="text-xs font-bold text-center tracking-wide"
            :class="currentTheme === theme.id ? 'text-[#FF4C00]' : 'text-zinc-400'"
          >
            {{ theme.name }}
          </p>
        </div>
      </div>
    </div>

    <div class="space-y-4">
      <h3 class="text-sm font-bold text-zinc-500 uppercase tracking-wider">偏好语言</h3>
      <div class="grid grid-cols-3 gap-4">
        <button
          v-for="lang in languages"
          :key="lang.id"
          @click="selectLang(lang)"
          class="relative py-4 px-6 rounded-xl font-mono font-bold border transition-all duration-300 overflow-hidden"
          :class="[
            currentLang === lang.id
              ? 'bg-white text-zinc-900 border-white shadow-lg'
              : 'bg-zinc-900/50 text-zinc-500 border-white/5',
            // [修改点 3]: 语言选项同样应用新的禁用样式
            lang.disabled
              ? 'opacity-30 grayscale cursor-not-allowed'
              : 'hover:bg-white/5 hover:text-zinc-300',
          ]"
        >
          {{ lang.name }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const currentTheme = ref('vs-dark')
const currentLang = ref('java')

interface OptionItem {
  id: string
  name: string
  color?: string
  disabled: boolean
}

// 主题选项
const themes: OptionItem[] = [
  { id: 'vs-dark', name: 'VS Dark', color: '#1e1e1e', disabled: false },
  { id: 'monokai', name: 'Monokai', color: '#272822', disabled: true },
  { id: 'github-light', name: 'Github Light', color: '#e1e1e1', disabled: true }, // 微调颜色以适应深色背景下的预览
  { id: 'dracula', name: 'Dracula', color: '#282a36', disabled: true },
]

// 语言选项
const languages: OptionItem[] = [
  { id: 'java', name: 'Java', disabled: false },
  { id: 'python', name: 'Python', disabled: true },
  { id: 'cpp', name: 'C++', disabled: true },
]

const selectTheme = (theme: OptionItem) => {
  if (theme.disabled) return
  currentTheme.value = theme.id
}

const selectLang = (lang: OptionItem) => {
  if (lang.disabled) return
  currentLang.value = lang.id
}
</script>
