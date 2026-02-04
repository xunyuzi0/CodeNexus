<template>
  <div class="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
    <section class="bg-zinc-900/30 backdrop-blur-md border border-white/5 rounded-2xl p-6 md:p-8">
      <div class="flex items-center gap-3 mb-6">
        <div class="p-2 rounded-lg bg-zinc-800/50">
          <Palette class="w-5 h-5 text-zinc-300" />
        </div>
        <div>
          <h3 class="text-lg font-bold text-white">编辑器主题</h3>
          <p class="text-xs text-zinc-500">选择代码编辑器的配色风格</p>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div 
          v-for="theme in themes" 
          :key="theme.value"
          @click="settingsStore.settings.editorTheme = theme.value"
          class="cursor-pointer relative group"
        >
          <div 
            class="h-24 rounded-xl border-2 transition-all duration-300 flex items-center justify-center relative overflow-hidden"
            :class="settingsStore.settings.editorTheme === theme.value ? 'border-[#FF4C00] bg-zinc-900' : 'border-zinc-800 bg-black/20 hover:border-zinc-600'"
          >
            <div class="absolute inset-x-4 top-4 h-2 rounded-full" :class="theme.previewBg"></div>
            <div class="absolute inset-x-4 top-8 h-2 w-2/3 rounded-full opacity-50" :class="theme.previewBg"></div>
            
            <div v-if="settingsStore.settings.editorTheme === theme.value" class="absolute right-2 bottom-2 text-[#FF4C00]">
              <CheckCircle2 class="w-5 h-5" />
            </div>
          </div>
          <p class="mt-2 text-center text-sm font-medium" :class="settingsStore.settings.editorTheme === theme.value ? 'text-white' : 'text-zinc-500'">{{ theme.label }}</p>
        </div>
      </div>
    </section>

    <section class="bg-zinc-900/30 backdrop-blur-md border border-white/5 rounded-2xl p-6 md:p-8">
      <div class="flex items-center gap-3 mb-6">
        <div class="p-2 rounded-lg bg-zinc-800/50">
          <Code2 class="w-5 h-5 text-zinc-300" />
        </div>
        <div>
          <h3 class="text-lg font-bold text-white">默认编程语言</h3>
          <p class="text-xs text-zinc-500">进入题目时默认选中的语言</p>
        </div>
      </div>

      <div class="flex flex-wrap gap-3">
        <button
          v-for="lang in languages"
          :key="lang.value"
          @click="settingsStore.settings.defaultLanguage = lang.value"
          class="px-6 py-3 rounded-xl border transition-all duration-300 text-sm font-bold flex items-center gap-2"
          :class="settingsStore.settings.defaultLanguage === lang.value 
            ? 'bg-[#FF4C00] border-[#FF4C00] text-white shadow-[0_0_15px_rgba(255,76,0,0.3)]' 
            : 'bg-zinc-900/50 border-white/5 text-zinc-400 hover:border-white/20 hover:text-white'"
        >
          {{ lang.label }}
        </button>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { Palette, Code2, CheckCircle2 } from 'lucide-vue-next'
import { useSettingsStore, type SettingsState } from '@/stores/settings'

const settingsStore = useSettingsStore()

const themes: { label: string, value: SettingsState['editorTheme'], previewBg: string }[] = [
  { label: 'VS Dark', value: 'vs-dark', previewBg: 'bg-blue-500' },
  { label: 'Dracula', value: 'dracula', previewBg: 'bg-purple-500' },
  { label: 'One Dark', value: 'one-dark', previewBg: 'bg-emerald-500' },
]

const languages: { label: string, value: SettingsState['defaultLanguage'] }[] = [
  { label: 'Java', value: 'java' },
  { label: 'Python', value: 'python' },
  { label: 'C++', value: 'cpp' },
]
</script>