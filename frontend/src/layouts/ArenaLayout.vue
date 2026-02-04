<template>
  <div
    class="min-h-screen w-full bg-zinc-950 relative overflow-hidden font-sans selection:bg-[#FF4C00] selection:text-white"
  >
    <div class="fixed inset-0 z-0 bg-grid-white/[0.02] pointer-events-none"></div>
    <div
      class="fixed inset-0 z-0 bg-gradient-to-b from-black/0 via-black/0 to-black/80 pointer-events-none"
    ></div>

    <div class="relative z-10 w-full h-full">
      <RouterView v-slot="{ Component }">
        <Transition name="fade" mode="out-in">
          <component :is="Component" />
        </Transition>
      </RouterView>
    </div>
  </div>
</template>

<script setup lang="ts">
// 逻辑已清空，纯布局组件
</script>

<style scoped>
.bg-grid-white {
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 32 32' width='32' height='32' fill='none' stroke='rgba(255, 255, 255, 0.05)'%3e%3cpath d='M0 .5H31.5V32' stroke-width='1' /%3e%3c/svg%3e");
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  filter: blur(4px);
}
</style>
