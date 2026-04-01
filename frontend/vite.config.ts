import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 3000,
    strictPort: false,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: true,
        // --- 智能路径重写引擎 ---
        rewrite: (path) => {
          // 1. 如果是竞技场模块或 WebSocket，保留 /api 原样转发给后端
          if (path.startsWith('/api/arena') || path.startsWith('/api/ws')) {
            return path
          }
          // 2. 其他老接口，保留历史行为：砍掉 /api
          return path.replace(/^\/api/, '')
        },
      },
    },
  },
})
