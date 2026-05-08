<template>
  <button
    @click="toggleTheme"
    class="fixed bottom-6 right-6 z-[90] p-2.5 rounded-full border backdrop-blur-md transition-all duration-300 hover:scale-110 active:scale-95"
    :class="
      isDark
        ? 'bg-zinc-900/80 border-white/10 text-zinc-400 hover:text-white hover:border-white/20'
        : 'bg-white border-zinc-300 text-zinc-500 hover:text-zinc-900 hover:border-zinc-400 shadow-md'
    "
    title="切换日间/夜间模式"
  >
    <Sun v-if="isDark" class="w-4 h-4" />
    <Moon v-else class="w-4 h-4" />
  </button>
</template>

<script setup lang="ts">
import { Sun, Moon } from 'lucide-vue-next'
import { isDark, toggleTheme } from '@/composables/useTheme'
</script>
