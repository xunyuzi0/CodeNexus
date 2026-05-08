<template>
  <div
    ref="cardRef"
    class="will-change-transform transform-gpu group relative h-80 rounded-3xl backdrop-blur-md border transition-colors duration-500 cursor-pointer flex flex-col items-center justify-center text-center p-6 overflow-hidden"
    :class="containerClass"
    @click="$emit('click')"
  >
    <div
      v-if="type === 'MATCH'"
      class="absolute inset-x-0 -top-[1px] h-[1px] bg-gradient-to-r from-transparent via-[#FF4C00] to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500"
    ></div>
    <div
      v-if="type === 'MATCH'"
      class="absolute inset-0 bg-[#FF4C00]/5 opacity-0 group-hover:opacity-100 transition-opacity duration-500 mix-blend-overlay"
    ></div>

    <div
      class="w-16 h-16 rounded-2xl flex items-center justify-center mb-6 border transition-transform duration-500 group-hover:scale-110 transform-gpu"
      :class="[
        iconBgClass,
        type === 'MATCH'
          ? 'w-20 h-20 shadow-[0_0_15px_rgba(255,76,0,0.15)] group-hover:shadow-[0_0_30px_rgba(255,76,0,0.6)]'
          : 'bg-zinc-100/50 dark:bg-zinc-800/50 border-white/5 group-hover:bg-[#FF4C00]/10 group-hover:border-[#FF4C00]/30',
      ]"
    >
      <component
        :is="icon"
        class="w-8 h-8 transition-colors duration-300"
        :class="[
          iconClass,
          type === 'MATCH' ? 'w-10 h-10 drop-shadow-[0_0_8px_rgba(255,76,0,0.8)]' : '',
        ]"
      />
    </div>

    <h3
      class="text-xl font-bold text-zinc-900 dark:text-white mb-2 transition-colors"
      :class="[
        type === 'MATCH'
          ? 'text-2xl font-black italic group-hover:text-[#FF4C00]'
          : 'group-hover:text-[#FF4C00]',
      ]"
    >
      {{ title }}
    </h3>

    <p
      class="text-xs text-zinc-500 leading-relaxed transition-colors"
      :class="type !== 'MATCH' ? 'group-hover:text-zinc-400' : ''"
    >
      <span v-html="desc"></span>
    </p>

    <div
      v-if="type === 'MATCH'"
      class="absolute bottom-0 left-1/4 right-1/4 h-[1px] bg-[#FF4C00]/50 blur-[2px] opacity-50 group-hover:opacity-100 transition-opacity animate-pulse"
    ></div>

    <div
      class="absolute bottom-6 text-xs font-bold font-mono text-[#FF4C00] opacity-0 group-hover:opacity-100 transition-all tracking-widest translate-y-2 group-hover:translate-y-0 duration-500 ease-[cubic-bezier(0.25,0.8,0.25,1)]"
    >
      {{ footerText }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useMouseInElement, useRafFn } from '@vueuse/core'

const props = defineProps<{
  type: string
  title: string
  desc: string
  icon: any
  footerText: string
  containerClass: string
  iconBgClass: string
  iconClass: string
}>()

defineEmits(['click'])

// --- 独立的 3D 倾斜逻辑 ---
const cardRef = ref<HTMLElement | null>(null)
const { elementX, elementY, elementWidth, elementHeight, isOutside } = useMouseInElement(cardRef)
const DAMPING_FACTOR = 0.08
const MAX_ROTATION = 12
const currentRotation = { x: 0, y: 0 }

useRafFn(() => {
  const el = cardRef.value
  if (!el) return
  let targetX = 0
  let targetY = 0

  if (!isOutside.value) {
    // 计算目标角度
    targetX =
      ((elementY.value - elementHeight.value / 2) / (elementHeight.value / 2)) * -MAX_ROTATION
    targetY = ((elementX.value - elementWidth.value / 2) / (elementWidth.value / 2)) * MAX_ROTATION
  }

  // 平滑插值
  currentRotation.x += (targetX - currentRotation.x) * DAMPING_FACTOR
  currentRotation.y += (targetY - currentRotation.y) * DAMPING_FACTOR

  // 应用变换
  // Optimization: 保持 scale3d(1,1,1) 以启用 3D 加速
  el.style.transform = `perspective(1000px) rotateX(${-currentRotation.x}deg) rotateY(${currentRotation.y}deg) scale3d(1, 1, 1)`
})
</script>

<style scoped>
.will-change-transform {
  will-change: transform;
  transform-style: preserve-3d;
  backface-visibility: hidden; /* 防止旋转时的闪烁 */
}
</style>
