<template>
  <Transition name="fade">
    <button
      v-show="visible"
      @click="scrollToTop"
      class="fixed bottom-24 right-6 z-50 w-10 h-10 bg-[#FF4C00]/90 hover:bg-[#FF4C00] border border-[#FF4C00]/50 rounded-lg shadow-lg shadow-[#FF4C00]/20 backdrop-blur-sm flex items-center justify-center transition-all duration-300 hover:scale-110"
      title="回到顶部"
    >
      <ChevronUp class="w-5 h-5 text-white" />
    </button>
  </Transition>
</template>

<script setup lang="ts">
import { ref, onMounted, inject, type Ref, watch } from 'vue'
import { ChevronUp } from 'lucide-vue-next'
import type Lenis from 'lenis'

const props = withDefaults(
  defineProps<{
    threshold?: number
  }>(),
  {
    threshold: 300,
  },
)

const visible = ref(false)

// 从 BasicLayout 注入 Lenis 实例的 ref
const lenisRef = inject<Ref<Lenis | null>>('lenis')

const checkScroll = (scroll: number) => {
  visible.value = scroll > props.threshold
}

const scrollToTop = () => {
  if (lenisRef?.value) {
    lenisRef.value.scrollTo(0, { duration: 1.5 })
  }
}

onMounted(() => {
  // 监听 lenisRef 的变化
  if (lenisRef) {
    watch(
      lenisRef,
      (newLenis) => {
        if (newLenis) {
          newLenis.on('scroll', ({ scroll }: { scroll: number }) => {
            checkScroll(scroll)
          })
        }
      },
      { immediate: true },
    )
  }
})
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
