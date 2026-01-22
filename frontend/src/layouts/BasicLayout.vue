<template>
  <div class="basic-layout">
    <GlobalHeader />
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import GlobalHeader from '@/components/GlobalHeader.vue'
</script>

<style scoped>
.basic-layout {
  min-height: 100vh;
  background-color: var(--bg-primary);
  display: flex;
  flex-direction: column;
}

.main-content {
  /* 留出 Header 的高度 */
  padding-top: 64px;
  flex: 1;
  width: 100%;
  max-width: 1400px; /* 限制最大宽度，保持大屏美观 */
  margin: 0 auto;
  padding-left: 24px;
  padding-right: 24px;
  /* 页面切换动画容器 */
  position: relative;
}
</style>
