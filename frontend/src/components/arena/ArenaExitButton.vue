<template>
  <div class="absolute top-1/2 left-6 -translate-y-1/2 z-50">
    <button
      @click="handleClick"
      :disabled="disabled"
      class="group flex items-center gap-2 px-3 py-2 rounded-lg transition-all duration-300"
      :class="
        disabled
          ? 'text-zinc-400 dark:text-zinc-600 opacity-50 cursor-not-allowed'
          : 'text-zinc-500 hover:text-zinc-900 dark:hover:text-white hover:bg-zinc-100 dark:hover:bg-white/5'
      "
    >
      <ArrowLeft
        class="w-4 h-4 transition-transform"
        :class="!disabled ? 'group-hover:-translate-x-1' : ''"
      />
      <span class="text-xs font-mono font-bold tracking-widest">EXIT</span>
    </button>
  </div>
</template>

<script setup lang="ts">
import { ArrowLeft } from 'lucide-vue-next'

const props = withDefaults(
  defineProps<{
    disabled?: boolean
  }>(),
  {
    disabled: false,
  },
)

const emit = defineEmits(['click'])

const handleClick = () => {
  if (props.disabled) return
  emit('click')
}
</script>
