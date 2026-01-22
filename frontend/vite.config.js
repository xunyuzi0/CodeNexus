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
  // 开发服务器配置
  server: {
    // 前端端口(默认为5173,可不写)
    port: 5173,
    // 启动后自动打开浏览器
    open: true,
    proxy: {
      // 代理配置：凡是 '/api' 开头的请求，都转发给后端
      '/api': {
        target: 'http://localhost:8080',
        // 允许跨域
        changeOrigin: true,
        // 去掉路径里的 /api
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
})
