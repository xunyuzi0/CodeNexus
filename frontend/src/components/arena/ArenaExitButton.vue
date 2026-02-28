<template>
  <div class="absolute top-6 left-6 z-50">
    <button
      @click="handleClick"
      class="group flex items-center gap-2 px-3 py-2 rounded-lg text-zinc-500 hover:text-white hover:bg-white/5 transition-all duration-300"
    >
      <ArrowLeft class="w-4 h-4 transition-transform group-hover:-translate-x-1" />
      <span class="text-xs font-mono font-bold tracking-widest">EXIT</span>
    </button>
  </div>
</template>

<script setup lang="ts">
import { ArrowLeft } from 'lucide-vue-next'

const emit = defineEmits(['click'])

const handleClick = () => {
  emit('click')
}
</script>
