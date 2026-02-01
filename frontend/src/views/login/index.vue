<template>
  <div
    class="min-h-screen w-full flex overflow-hidden bg-zinc-50 dark:bg-[#05050A] text-zinc-900 dark:text-white selection:bg-[#FF4C00] selection:text-white relative transition-colors duration-500 select-none"
  >
    <div class="absolute inset-0 z-0 pointer-events-none">
      <div
        class="absolute inset-0 bg-[radial-gradient(ellipse_80%_80%_at_top_left,_var(--tw-gradient-stops))] from-zinc-200/50 via-zinc-100 to-white dark:from-zinc-800/50 dark:via-zinc-950 dark:to-black transition-colors duration-500"
      ></div>

      <div
        class="absolute inset-0 bg-grid-black/[0.02] dark:bg-grid-white/[0.04] bg-[size:60px_60px] transition-all duration-500"
      ></div>

      <div
        class="absolute bottom-0 left-0 w-full h-[45%] bg-gradient-to-t from-white via-white/50 to-transparent dark:from-black dark:via-[#05050A]/80 dark:to-transparent transition-all duration-500"
      ></div>
      <div
        class="absolute -bottom-[20%] -left-[10%] w-[800px] h-[800px] rounded-full bg-zinc-200/40 dark:bg-[#FF4C00]/[0.03] blur-[120px] transition-all duration-500"
      ></div>
    </div>

    <div class="hidden lg:flex w-[65%] relative z-10 flex-col justify-center pl-24 overflow-hidden">
      <div
        class="absolute left-0 top-[30%] -translate-y-1/2 -translate-x-1/6 w-[1200px] h-[1200px] pointer-events-none select-none"
        style="z-index: 0"
        v-motion
        :initial="{ opacity: 0, scale: 0.8 }"
        :enter="{ opacity: 1, scale: 1, transition: { duration: 1500, ease: 'easeOut' } }"
      >
        <svg
          viewBox="0 0 1000 1000"
          class="w-full h-full overflow-visible transition-colors duration-500"
        >
          <defs>
            <radialGradient id="ring-orange-gradient" cx="50%" cy="50%" r="50%" fx="50%" fy="50%">
              <stop offset="40%" stop-color="#FF5F1F" stop-opacity="0.8" />
              <stop offset="100%" stop-color="#FF5F1F" stop-opacity="0.4" />
            </radialGradient>
            <radialGradient id="core-spill-gradient" cx="50%" cy="50%" r="50%" fx="50%" fy="50%">
              <stop offset="0%" stop-color="#FF5F1F" stop-opacity="0.7" />
              <stop offset="20%" stop-color="#FF5F1F" stop-opacity="0.5" />
              <stop offset="50%" stop-color="#FF5F1F" stop-opacity="0.2" />
              <stop offset="100%" stop-color="#FF5F1F" stop-opacity="0" />
            </radialGradient>
          </defs>

          <g class="origin-center animate-[spin_60s_linear_infinite]">
            <circle
              cx="500"
              cy="500"
              r="100"
              fill="none"
              stroke-width="2"
              stroke-opacity="0.85"
              class="stroke-zinc-300 dark:stroke-[url(#ring-orange-gradient)] transition-colors duration-500"
            />
            <circle
              cx="500"
              cy="500"
              r="165"
              fill="none"
              stroke-width="2"
              stroke-opacity="0.6"
              class="stroke-zinc-300 dark:stroke-[url(#ring-orange-gradient)] transition-colors duration-500"
            />
            <circle
              cx="500"
              cy="500"
              r="280"
              fill="none"
              stroke-width="1.8"
              stroke-dasharray="40 20"
              stroke-opacity="0.5"
              class="stroke-zinc-300/60 dark:stroke-[url(#ring-orange-gradient)] transition-colors duration-500"
            />
            <circle
              cx="500"
              cy="500"
              r="400"
              fill="none"
              stroke-width="1"
              stroke-opacity="0.3"
              class="stroke-zinc-300/40 dark:stroke-[url(#ring-orange-gradient)] transition-colors duration-500"
            />
          </g>
          <g class="origin-center animate-[spin_80s_linear_infinite_reverse]">
            <circle
              cx="500"
              cy="500"
              r="220"
              fill="none"
              stroke-width="2.5"
              stroke-dasharray="25 12"
              stroke-opacity="0.55"
              class="stroke-zinc-300/70 dark:stroke-[url(#ring-orange-gradient)] transition-colors duration-500"
            />
            <circle
              cx="500"
              cy="500"
              r="340"
              fill="none"
              stroke-width="1.4"
              stroke-dasharray="20 10"
              stroke-opacity="0.45"
              class="stroke-zinc-300/50 dark:stroke-[url(#ring-orange-gradient)] transition-colors duration-500"
            />
          </g>

          <g class="origin-center animate-pulse-slow">
            <circle
              cx="500"
              cy="500"
              r="320"
              class="fill-zinc-200/50 dark:fill-[url(#core-spill-gradient)] transition-colors duration-500"
            />
            <circle
              cx="500"
              cy="500"
              r="15"
              class="fill-zinc-400 dark:fill-[#FF5F1F] transition-colors duration-500"
            />
          </g>
        </svg>
      </div>

      <div class="relative z-20 space-y-8 mt-200">
        <div
          class="flex items-center space-x-3 mb-4"
          v-motion
          :initial="{ opacity: 0, x: -50 }"
          :enter="{ opacity: 1, x: 0, transition: { duration: 800 } }"
        >
          <div
            class="h-[2px] w-12 bg-orange-600 dark:bg-[#FF5F1F] shadow-none dark:shadow-[0_0_10px_#FF5F1F] transition-all duration-500"
          ></div>
          <span
            class="text-orange-700 dark:text-[#FF5F1F] font-mono tracking-widest text-sm uppercase font-bold transition-colors duration-500"
            >CodeNexus Platform</span
          >
        </div>

        <div
          v-motion
          :initial="{ opacity: 0, y: 20 }"
          :enter="{ opacity: 1, y: 0, transition: { duration: 800, delay: 100 } }"
        >
          <h1 class="flex items-baseline gap-0 leading-none">
            <span
              class="text-8xl font-black tracking-tighter text-zinc-900 dark:text-white drop-shadow-none dark:drop-shadow-xl transition-all duration-500"
              >CODE</span
            >
            <span
              class="text-8xl font-black tracking-tighter text-orange-600 dark:text-[#FF5F1F] drop-shadow-none dark:drop-shadow-[0_0_25px_rgba(255,95,31,0.4)] transition-all duration-500"
              >NEXUS</span
            >
          </h1>
          <h2
            class="mt-6 text-2xl font-light text-zinc-500 dark:text-zinc-300 tracking-wide flex items-center gap-4 transition-colors duration-500"
          >
            代码枢纽 · 下一代在线评测沙箱
          </h2>
        </div>

        <p
          class="max-w-lg text-zinc-500 dark:text-zinc-400 text-base leading-relaxed pt-8 border-t border-zinc-200 dark:border-white/10 mt-8 transition-colors duration-500"
          v-motion
          :initial="{ opacity: 0 }"
          :enter="{ opacity: 1, transition: { duration: 1000, delay: 300 } }"
        >
          基于 Docker 容器化技术的实时协同编程平台。<br />
          Experience latency-free coding with military-grade security.
        </p>
        <div
          class="absolute bottom-[-100px] left-0 text-xs text-zinc-400 dark:text-zinc-600 font-mono"
          v-motion
          :initial="{ opacity: 0 }"
          :enter="{ opacity: 1, transition: { delay: 500 } }"
        >
          © Made by CodeNexus xunyu 2026
        </div>
      </div>
    </div>

    <div
      class="w-full lg:w-[35%] relative z-30 flex flex-col justify-center px-8 lg:px-16 bg-white/80 dark:bg-zinc-900/80 backdrop-blur-xl border-l border-zinc-200 dark:border-white/10 shadow-[-20px_0_40px_rgba(0,0,0,0.05)] dark:shadow-[-20px_0_40px_rgba(0,0,0,0.5)] transition-all duration-500"
    >
      <div class="absolute top-0 right-0 p-8">
        <button
          @click="toggleTheme"
          class="relative w-10 h-10 rounded-full flex items-center justify-center text-zinc-500 dark:text-zinc-400 hover:bg-zinc-100 dark:hover:bg-white/5 transition-all duration-300 focus:outline-none"
        >
          <div class="relative w-5 h-5 overflow-hidden">
            <Sun
              class="absolute inset-0 w-full h-full transition-all duration-500 ease-[cubic-bezier(0.23,1,0.32,1)]"
              :class="isDark ? 'rotate-90 opacity-0 scale-50' : 'rotate-0 opacity-100 scale-100'"
            />
            <Moon
              class="absolute inset-0 w-full h-full transition-all duration-500 ease-[cubic-bezier(0.23,1,0.32,1)]"
              :class="isDark ? 'rotate-0 opacity-100 scale-100' : '-rotate-90 opacity-0 scale-50'"
            />
          </div>
        </button>
      </div>

      <div
        class="w-full max-w-sm mx-auto space-y-10"
        v-motion
        :initial="{ opacity: 0, x: 50 }"
        :enter="{ opacity: 1, x: 0, transition: { duration: 600, delay: 200 } }"
      >
        <div class="space-y-1">
          <h3
            class="text-2xl font-bold text-zinc-900 dark:text-white tracking-tight transition-colors"
          >
            {{ titleMap[mode] }}
          </h3>
          <p class="text-sm text-zinc-500 dark:text-zinc-400 transition-colors">
            {{ descMap[mode] }}
          </p>
        </div>

        <form @submit.prevent="handleSubmit" class="space-y-6">
          <div class="space-y-2">
            <label class="text-xs font-bold text-zinc-500 uppercase tracking-wider ml-1">{{
              mode === 'reset' ? '注册邮箱' : '账号'
            }}</label>
            <div class="relative group">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Mail
                  v-if="mode === 'reset'"
                  class="h-4 w-4 text-zinc-400 group-focus-within:text-[#FF5F1F] transition-colors"
                />
                <User
                  v-else
                  class="h-4 w-4 text-zinc-400 group-focus-within:text-[#FF5F1F] transition-colors"
                />
              </div>
              <input
                v-model="form.username"
                :type="mode === 'reset' ? 'email' : 'text'"
                required
                class="w-full bg-zinc-100 dark:bg-black/40 border border-zinc-200 dark:border-white/10 rounded-sm px-10 py-4 text-sm text-zinc-900 dark:text-white placeholder-zinc-400 dark:placeholder-zinc-600 focus:outline-none focus:border-[#FF5F1F] focus:ring-1 focus:ring-[#FF5F1F]/20 focus:bg-white dark:focus:bg-black/60 transition-all duration-300"
                :placeholder="mode === 'reset' ? '请输入您的注册邮箱' : '请输入开发者账号 / 邮箱'"
              />
            </div>
          </div>
          <div
            v-if="mode !== 'reset'"
            class="space-y-2"
            v-motion
            :initial="{ opacity: 0, height: 0 }"
            :enter="{ opacity: 1, height: 'auto' }"
          >
            <label class="text-xs font-bold text-zinc-500 uppercase tracking-wider ml-1"
              >密码</label
            >
            <div class="relative group">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Lock
                  class="h-4 w-4 text-zinc-400 group-focus-within:text-[#FF5F1F] transition-colors"
                />
              </div>
              <input
                v-model="form.password"
                type="password"
                required
                class="w-full bg-zinc-100 dark:bg-black/40 border border-zinc-200 dark:border-white/10 rounded-sm px-10 py-4 text-sm text-zinc-900 dark:text-white placeholder-zinc-400 dark:placeholder-zinc-600 focus:outline-none focus:border-[#FF5F1F] focus:ring-1 focus:ring-[#FF5F1F]/20 focus:bg-white dark:focus:bg-black/60 transition-all duration-300"
                placeholder="请输入密码"
              />
            </div>
          </div>
          <div
            v-if="mode === 'register'"
            class="space-y-2"
            v-motion
            :initial="{ opacity: 0, height: 0 }"
            :enter="{ opacity: 1, height: 'auto' }"
          >
            <label class="text-xs font-bold text-zinc-500 uppercase tracking-wider ml-1"
              >确认密码</label
            >
            <div class="relative group">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <CheckCircle
                  class="h-4 w-4 text-zinc-400 group-focus-within:text-[#FF5F1F] transition-colors"
                />
              </div>
              <input
                v-model="form.checkPassword"
                type="password"
                required
                class="w-full bg-zinc-100 dark:bg-black/40 border border-zinc-200 dark:border-white/10 rounded-sm px-10 py-4 text-sm text-zinc-900 dark:text-white placeholder-zinc-400 dark:placeholder-zinc-600 focus:outline-none focus:border-[#FF5F1F] focus:ring-1 focus:ring-[#FF5F1F]/20 focus:bg-white dark:focus:bg-black/60 transition-all duration-300"
                placeholder="请再次输入密码"
              />
            </div>
          </div>

          <button
            type="submit"
            :disabled="loading"
            class="w-full relative overflow-hidden bg-[#FF5F1F] hover:bg-[#ff7b42] disabled:opacity-50 disabled:cursor-not-allowed text-white font-bold h-14 tracking-widest uppercase rounded-sm shadow-[0_0_20px_rgba(255,95,31,0.4)] hover:shadow-[0_0_40px_rgba(255,95,31,0.6)] transition-all duration-300 group mt-8"
          >
            <span class="relative z-10 flex items-center justify-center gap-2">
              <span v-if="!loading">{{ btnTextMap[mode] }}</span>
              <span v-else>处理中...</span>
              <ArrowRight
                v-if="!loading"
                class="w-4 h-4 group-hover:translate-x-1 transition-transform"
              />
            </span>
          </button>
        </form>

        <div class="text-center space-y-4">
          <div v-if="mode === 'login'">
            <a
              href="#"
              @click.prevent="switchMode('reset')"
              class="inline-block text-xs text-zinc-500 hover:text-[#FF5F1F] transition-colors border-b border-transparent hover:border-[#FF5F1F]/30 pb-0.5"
              >忘记账号密码了吗?</a
            >
          </div>
          <div v-if="mode !== 'login'">
            <a
              href="#"
              @click.prevent="switchMode('login')"
              class="inline-block text-xs text-zinc-500 hover:text-[#FF5F1F] transition-colors border-b border-transparent hover:border-[#FF5F1F]/30 pb-0.5"
              >返回登录</a
            >
          </div>
          <p
            class="text-xs text-zinc-500 pt-4 border-t border-zinc-200 dark:border-white/10 transition-colors"
          >
            {{ mode === 'register' ? '已有账号?' : mode === 'login' ? '还没有账号?' : '' }}
            <button
              v-if="mode !== 'reset'"
              @click="switchMode(mode === 'login' ? 'register' : 'login')"
              class="text-[#FF5F1F] hover:text-zinc-900 dark:hover:text-white transition-colors font-bold ml-1 border-b border-transparent hover:border-[#FF5F1F] pb-0.5"
            >
              {{ mode === 'login' ? '点击注册' : '直接登录' }}
            </button>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { register } from '@/api/auth'
import { toggleTheme, isDark } from '@/composables/useTheme'
import { ArrowRight, User, Lock, CheckCircle, Sun, Moon, Mail } from 'lucide-vue-next'

// 类型定义
type AuthMode = 'login' | 'register' | 'reset'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 状态管理
const mode = ref<AuthMode>('login')
const loading = ref(false)
const form = reactive({
  username: '',
  password: '',
  checkPassword: '',
})

// 文案映射配置
const titleMap: Record<AuthMode, string> = {
  login: '欢迎回来',
  register: '创建账号',
  reset: '重置密码',
}
const descMap: Record<AuthMode, string> = {
  login: '请输入您的凭证以继续',
  register: '填写以下信息开启编程之旅',
  reset: '我们将向您的邮箱发送验证链接',
}
const btnTextMap: Record<AuthMode, string> = {
  login: '立即进入',
  register: '立即注册',
  reset: '发送重置链接',
}

// 方法：切换模式
const switchMode = (target: AuthMode) => {
  mode.value = target
  // 重置表单
  form.username = ''
  form.password = ''
  form.checkPassword = ''
}

// 方法：处理提交
const handleSubmit = async () => {
  if (mode.value === 'register' && form.password !== form.checkPassword) {
    alert('两次输入的密码不一致')
    return
  }

  loading.value = true
  try {
    if (mode.value === 'register') {
      await register({
        userAccount: form.username,
        userPassword: form.password,
        checkPassword: form.checkPassword,
      })
      alert('注册成功，请登录')
      switchMode('login')
    } else if (mode.value === 'login') {
      await userStore.login({ userAccount: form.username, userPassword: form.password })
      const redirect = (route.query.redirect as string) || '/'
      router.push(redirect)
    } else if (mode.value === 'reset') {
      await new Promise((resolve) => setTimeout(resolve, 1500)) // 模拟 API
      alert(`重置链接已发送至 ${form.username}，请查收邮件`)
      switchMode('login')
    }
  } catch (error: any) {
    console.error(error)
    alert(error.message || '操作失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 背景网格图案定义 */
.bg-grid-white {
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 32 32' width='32' height='32' fill='none' stroke='rgba(255, 255, 255, 0.05)'%3e%3cpath d='M0 .5H31.5V32' stroke-width='1' /%3e%3c/svg%3e");
}
.bg-grid-black {
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 32 32' width='32' height='32' fill='none' stroke='rgba(0, 0, 0, 0.05)'%3e%3cpath d='M0 .5H31.5V32' stroke-width='1' /%3e%3c/svg%3e");
}

/* 核心呼吸动画 */
.animate-pulse-slow {
  animation: pulse-deep 8s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse-deep {
  0%,
  100% {
    opacity: 0.6;
    transform: scale(1);
  }
  40%,
  60% {
    opacity: 0.8;
    transform: scale(1.4);
  }
}
</style>
