import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { MotionPlugin } from '@vueuse/motion' // 动画库

import App from './App.vue'
import router from './router'
import './styles/main.css' // 全局样式
import './router/guard'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(MotionPlugin) // 注册动画

app.mount('#app')
