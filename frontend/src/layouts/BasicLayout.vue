<template>
  <div
    class="h-screen w-full flex bg-zinc-50 dark:bg-[#050505] transition-colors duration-300 overflow-hidden"
  >
    <div
      class="absolute inset-0 pointer-events-none z-0 bg-grid-pattern opacity-[0.03] dark:opacity-[0.05]"
    ></div>

    <Sidebar />

    <main class="flex-1 ml-[240px] relative z-10 flex flex-col h-full">
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import Lenis from 'lenis'
import Sidebar from './components/Sidebar.vue'
import Navbar from './components/Navbar.vue'

const scrollContainerRef = ref<HTMLElement | null>(null)
let lenis: Lenis | null = null
let rafId: number | null = null
const router = useRouter()

// 初始化全局阻尼滚动
onMounted(() => {
  if (scrollContainerRef.value) {
    lenis = new Lenis({
      wrapper: scrollContainerRef.value, // 接管此容器
      content: scrollContainerRef.value.firstElementChild as HTMLElement, // 内容层
      // [架构师调教] 极客手感参数
      duration: 1.8, // 极高阻尼 (默认1.0)，制造"重力感"
      easing: (t) => Math.min(1, 1.001 - Math.pow(2, -10 * t)), // 指数衰减
      smoothWheel: true,
      touchMultiplier: 2,
    })

    const raf = (time: number) => {
      lenis?.raf(time)
      rafId = requestAnimationFrame(raf)
    }
    rafId = requestAnimationFrame(raf)

    // 路由切换时重置滚动位置
    router.afterEach(() => {
      // 等待 Transition 动画开始后再重置，或立即重置
      nextTick(() => {
        lenis?.scrollTo(0, { immediate: true })
      })
    })
  }
})

onUnmounted(() => {
  if (lenis) lenis.destroy()
  if (rafId) cancelAnimationFrame(rafId)
})
</script>

<style scoped>
/* 网格背景 */
.bg-grid-pattern {
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 32 32' width='32' height='32' fill='none' stroke='currentColor'%3e%3cpath d='M0 .5H31.5V32' stroke-width='1' /%3e%3c/svg%3e");
  color: black;
}
.dark .bg-grid-pattern {
  color: white;
}

/* 页面切换动画 */
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
