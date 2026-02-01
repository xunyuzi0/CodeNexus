<template>
  <RouterView />
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router'

// [架构师修正]
// 移除了全局 Window 级 Lenis。
// 原因：CodeNexus 是 Dashboard 布局，Window 本身不滚动，
// 全局 Lenis 会拦截滚轮事件，导致内部页面的局部滚动失效。
</script>

<style scoped></style>
