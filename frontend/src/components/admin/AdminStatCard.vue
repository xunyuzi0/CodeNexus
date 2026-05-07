<template>
  <div
    class="bg-zinc-900/60 backdrop-blur-xl border border-white/5 rounded-2xl p-6 group hover:border-white/10 transition-colors"
  >
    <div class="flex items-center justify-between">
      <div>
        <p class="text-zinc-400 text-sm">{{ title }}</p>
        <p class="text-3xl font-bold text-white mt-1">{{ formattedValue }}</p>
      </div>
      <div
        :class="
          cn(
            'w-12 h-12 rounded-xl flex items-center justify-center transition-transform group-hover:scale-110',
            iconBgClass,
          )
        "
      >
        <component :is="icon" :class="cn('w-6 h-6', iconColorClass)" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Component } from 'vue'
import { computed } from 'vue'
import { cn } from '@/lib/utils'

const props = withDefaults(
  defineProps<{
    title: string
    value: string | number
    icon: Component
    color?: string
  }>(),
  {
    color: 'orange',
  },
)

const formattedValue = computed(() => {
  if (typeof props.value === 'number') {
    return props.value.toLocaleString()
  }
  return props.value
})

const iconBgClass = computed(() => {
  const map: Record<string, string> = {
    orange: 'bg-orange-500/10',
    blue: 'bg-blue-500/10',
    green: 'bg-emerald-500/10',
    purple: 'bg-purple-500/10',
    red: 'bg-red-500/10',
    cyan: 'bg-cyan-500/10',
  }
  return map[props.color] ?? map.orange
})

const iconColorClass = computed(() => {
  const map: Record<string, string> = {
    orange: 'text-[#FF4C00]',
    blue: 'text-blue-400',
    green: 'text-emerald-400',
    purple: 'text-purple-400',
    red: 'text-red-400',
    cyan: 'text-cyan-400',
  }
  return map[props.color] ?? map.orange
})
</script>
