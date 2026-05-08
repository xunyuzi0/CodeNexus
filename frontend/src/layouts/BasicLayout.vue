<template>
  <div
    class="h-screen w-full flex bg-zinc-50 dark:bg-[#050505] transition-colors duration-300 overflow-hidden"
  >
    <div
      class="absolute inset-0 pointer-events-none z-0 bg-grid-pattern opacity-[0.03] dark:opacity-[0.05]"
    ></div>

    <Sidebar />

    <main
      class="flex-1 relative z-10 flex flex-col h-full transition-all duration-500 ease-[cubic-bezier(0.25,0.8,0.25,1)]"
      :class="isSidebarCollapsed ? 'ml-0' : 'ml-[240px]'"
    >
      <Navbar class="shrink-0" />

      <div
        ref="scrollContainerRef"
        class="flex-1 overflow-y-auto overflow-x-hidden relative no-scrollbar"
      >
        <div class="p-8 pt-0 min-h-full">
          <RouterView v-slot="{ Component }">
            <Transition name="fade-slide" mode="out-in">
              <component :is="Component" />
            </Transition>
          </RouterView>
        </div>
      </div>
    </main>

    <button
      v-if="isSidebarCollapsed"
      @click="uiStore.toggleSidebar()"
      class="fixed bottom-6 left-6 z-50 p-3 rounded-full bg-white/90 dark:bg-zinc-900/80 border border-zinc-200 dark:border-white/10 text-zinc-500 dark:text-zinc-400 hover:text-[#FF4C00] hover:border-[#FF4C00]/50 transition-all duration-300 shadow-lg dark:shadow-xl"
    >
      <svg
        xmlns="http://www.w3.org/2000/svg"
        width="24"
        height="24"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
        class="w-5 h-5"
      >
        <rect width="18" height="18" x="3" y="3" rx="2" ry="2" />
        <path d="M9 3v18" />
      </svg>
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, provide } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia' // [Added]
import Lenis from 'lenis'
import Sidebar from './components/Sidebar.vue'
import Navbar from './components/Navbar.vue'
import { useUiStore } from '@/stores/ui' // [Added]

const scrollContainerRef = ref<HTMLElement | null>(null)
const lenisRef = ref<Lenis | null>(null)
let rafId: number | null = null
const router = useRouter()

const uiStore = useUiStore() // [Added]
const { isSidebarCollapsed } = storeToRefs(uiStore) // [Added]

// 提供 Lenis 实例给子组件
provide('lenis', lenisRef)

// 初始化全局阻尼滚动
onMounted(() => {
  if (scrollContainerRef.value) {
    lenisRef.value = new Lenis({
      wrapper: scrollContainerRef.value,
      content: scrollContainerRef.value.firstElementChild as HTMLElement,
      duration: 1.8,
      easing: (t) => Math.min(1, 1.001 - Math.pow(2, -10 * t)),
      smoothWheel: true,
      touchMultiplier: 2,
    })

    const raf = (time: number) => {
      lenisRef.value?.raf(time)
      rafId = requestAnimationFrame(raf)
    }
    rafId = requestAnimationFrame(raf)

    router.afterEach(() => {
      nextTick(() => {
        lenisRef.value?.scrollTo(0, { immediate: true })
      })
    })
  }
})

onUnmounted(() => {
  if (lenisRef.value) lenisRef.value.destroy()
  if (rafId) cancelAnimationFrame(rafId)
})
</script>

<style scoped>
/* ... (Existing styles) ... */
.bg-grid-pattern {
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 32 32' width='32' height='32' fill='none' stroke='currentColor'%3e%3cpath d='M0 .5H31.5V32' stroke-width='1' /%3e%3c/svg%3e");
  color: black;
}
.dark .bg-grid-pattern {
  color: white;
}
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.98);
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-20px) scale(0.98);
  filter: blur(4px);
}
</style>
