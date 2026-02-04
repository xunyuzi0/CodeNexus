<template>
  <div class="min-h-[85vh] w-full relative flex flex-col gap-8 pb-12">
    <div
      class="absolute top-[-10%] left-1/2 -translate-x-1/2 w-[60%] h-[300px] bg-[#FF4C00]/10 blur-[150px] rounded-full pointer-events-none z-0"
    ></div>

    <div
      class="relative z-10 w-full rounded-3xl bg-zinc-900/60 backdrop-blur-xl border border-white/5 p-8 md:p-10 overflow-hidden group"
      v-motion
      :initial="{ opacity: 0, y: 30 }"
      :enter="{ opacity: 1, y: 0, transition: { duration: 600, ease: 'easeOut' } }"
    >
      <div
        class="absolute top-0 right-0 w-[300px] h-[300px] bg-gradient-to-br from-white/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-700 pointer-events-none rounded-tr-3xl"
      ></div>

      <div
        class="flex flex-col md:flex-row items-center md:items-start justify-between gap-8 relative"
      >
        <div class="flex flex-col md:flex-row items-center gap-8">
          <div class="relative">
            <div class="absolute inset-0 bg-[#FF4C00]/20 blur-xl rounded-full"></div>
            <div
              class="relative w-32 h-32 rounded-full ring-4 ring-white/5 shadow-[0_0_30px_rgba(255,76,0,0.25)] overflow-hidden bg-zinc-800"
            >
              <img
                :src="userStore.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix'"
                alt="Avatar"
                class="w-full h-full object-cover hover:scale-110 transition-transform duration-500"
              />
            </div>
            <div
              class="absolute bottom-2 right-2 w-5 h-5 bg-emerald-500 border-4 border-zinc-900 rounded-full"
            ></div>
          </div>

          <div class="text-center md:text-left space-y-2">
            <h2 class="text-3xl font-black text-white tracking-tight">
              {{ userStore.nickname }}
            </h2>
            <div class="flex items-center justify-center md:justify-start gap-2">
              <span
                class="px-2 py-0.5 rounded text-[10px] font-bold bg-[#FF4C00]/10 text-[#FF4C00] border border-[#FF4C00]/20 tracking-wider"
              >
                {{ (userStore.roles?.[0] || '指挥官').toUpperCase() }}
              </span>
              <span class="text-xs text-zinc-500 font-mono">
                UID: {{ userStore.userInfo?.id || '---' }}
              </span>
            </div>
            <p class="text-sm text-zinc-400 max-w-md mt-2 leading-relaxed">
              {{ formData.userProfile || '这个人很神秘，还没有写下任何简介...' }}
            </p>
          </div>
        </div>

        <div class="flex items-center gap-8 md:gap-12 mt-4 md:mt-0">
          <div class="text-center">
            <p class="text-xs text-zinc-500 uppercase tracking-widest font-bold mb-1">解题总数</p>
            <p class="text-4xl font-black font-mono text-white tabular-nums">
              {{ Math.round(stats.totalSolved) }}
            </p>
          </div>

          <div class="w-[1px] h-12 bg-white/10 hidden md:block"></div>

          <div class="text-center relative">
            <p class="text-xs text-zinc-500 uppercase tracking-widest font-bold mb-1">战力指数</p>
            <p
              class="text-4xl font-black font-mono text-[#FF4C00] drop-shadow-[0_0_10px_rgba(255,76,0,0.4)] tabular-nums"
            >
              {{ Math.round(stats.rankScore) }}
            </p>
          </div>

          <div class="w-[1px] h-12 bg-white/10 hidden md:block"></div>

          <div class="text-center">
            <p class="text-xs text-zinc-500 uppercase tracking-widest font-bold mb-1">全站排名</p>
            <p class="text-2xl font-bold text-zinc-400 font-mono tabular-nums">
              #{{ Math.round(stats.globalRank) }}
            </p>
          </div>
        </div>
      </div>
    </div>

    <div
      class="w-full"
      v-motion
      :initial="{ opacity: 0, y: 20 }"
      :enter="{ opacity: 1, y: 0, transition: { duration: 600, delay: 100 } }"
    >
      <div class="flex items-center gap-8 border-b border-white/5 mb-8 px-2">
        <button
          v-for="tab in tabs"
          :key="tab.id"
          @click="currentTab = tab.id"
          class="relative pb-4 text-sm font-bold tracking-wide transition-colors duration-300"
          :class="currentTab === tab.id ? 'text-white' : 'text-zinc-500 hover:text-zinc-300'"
        >
          <div class="flex items-center gap-2">
            <component :is="tab.icon" class="w-4 h-4" />
            {{ tab.label }}
          </div>
          <div
            class="absolute bottom-0 left-0 w-full h-[2px] bg-[#FF4C00] shadow-[0_0_10px_#FF4C00] transition-all duration-300"
            :class="currentTab === tab.id ? 'opacity-100 scale-x-100' : 'opacity-0 scale-x-0'"
          ></div>
        </button>
      </div>

      <Transition name="fade" mode="out-in">
        <div
          v-if="currentTab === 'profile'"
          class="bg-zinc-900/30 backdrop-blur-md border border-white/5 rounded-2xl p-8"
        >
          <div class="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-6">
            <div class="space-y-2">
              <label class="text-xs font-bold text-zinc-500 uppercase tracking-wider ml-1"
                >登录账号</label
              >
              <div class="relative group cursor-not-allowed">
                <User class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-zinc-500" />
                <input
                  v-model="formData.userAccount"
                  type="text"
                  disabled
                  class="w-full bg-zinc-950/50 border border-white/5 rounded-lg px-10 py-3 text-sm text-zinc-500 cursor-not-allowed focus:outline-none"
                />
              </div>
            </div>

            <div class="space-y-2">
              <label class="text-xs font-bold text-zinc-500 uppercase tracking-wider ml-1"
                >用户昵称</label
              >
              <div class="relative group">
                <Smile
                  class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-zinc-400 group-focus-within:text-[#FF4C00] transition-colors"
                />
                <input
                  v-model="formData.userName"
                  type="text"
                  placeholder="请输入昵称"
                  class="w-full bg-black/20 border border-white/10 rounded-lg px-10 py-3 text-sm text-white placeholder-zinc-600 focus:outline-none focus:border-[#FF4C00] focus:ring-1 focus:ring-[#FF4C00]/20 transition-all duration-300"
                />
              </div>
            </div>

            <div class="space-y-2">
              <label class="text-xs font-bold text-zinc-500 uppercase tracking-wider ml-1"
                >电子邮箱</label
              >
              <div class="relative group">
                <Mail
                  class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-zinc-400 group-focus-within:text-[#FF4C00] transition-colors"
                />
                <input
                  v-model="formData.email"
                  type="email"
                  placeholder="未绑定邮箱"
                  class="w-full bg-black/20 border border-white/10 rounded-lg px-10 py-3 text-sm text-white placeholder-zinc-600 focus:outline-none focus:border-[#FF4C00] focus:ring-1 focus:ring-[#FF4C00]/20 transition-all duration-300"
                />
              </div>
            </div>

            <div class="space-y-2">
              <label class="text-xs font-bold text-zinc-500 uppercase tracking-wider ml-1"
                >手机号码</label
              >
              <div class="relative group">
                <Smartphone
                  class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-zinc-400 group-focus-within:text-[#FF4C00] transition-colors"
                />
                <input
                  v-model="formData.phone"
                  type="tel"
                  placeholder="未绑定手机号"
                  class="w-full bg-black/20 border border-white/10 rounded-lg px-10 py-3 text-sm text-white placeholder-zinc-600 focus:outline-none focus:border-[#FF4C00] focus:ring-1 focus:ring-[#FF4C00]/20 transition-all duration-300"
                />
              </div>
            </div>

            <div class="md:col-span-2 space-y-2">
              <label class="text-xs font-bold text-zinc-500 uppercase tracking-wider ml-1"
                >个人简介</label
              >
              <textarea
                v-model="formData.userProfile"
                rows="4"
                placeholder="Talk is cheap. Show me the code."
                class="w-full bg-black/20 border border-white/10 rounded-lg p-4 text-sm text-white placeholder-zinc-600 focus:outline-none focus:border-[#FF4C00] focus:ring-1 focus:ring-[#FF4C00]/20 transition-all duration-300 resize-none"
              ></textarea>
            </div>
          </div>

          <div class="mt-8 flex justify-end border-t border-white/5 pt-6">
            <Button
              @click="handleSave"
              :disabled="saving"
              class="bg-[#FF4C00] hover:bg-[#ff5f1f] text-white px-8 py-2 rounded-lg font-bold shadow-[0_0_20px_rgba(255,76,0,0.3)] hover:shadow-[0_0_30px_rgba(255,76,0,0.5)] transition-all active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span v-if="!saving" class="flex items-center gap-2">
                <Save class="w-4 h-4" /> 保存修改
              </span>
              <span v-else class="flex items-center gap-2">
                <Loader2 class="w-4 h-4 animate-spin" /> 保存中...
              </span>
            </Button>
          </div>
        </div>

        <SecurityTab v-else-if="currentTab === 'security'" />

        <PreferencesTab v-else-if="currentTab === 'preference'" />
      </Transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useTransition, TransitionPresets } from '@vueuse/core'
import { User, Shield, Settings, Mail, Smartphone, Smile, Save, Loader2 } from 'lucide-vue-next'
import { Button } from '@/components/ui/button'

import SecurityTab from './tabs/SecurityTab.vue'
import PreferencesTab from './tabs/PreferencesTab.vue'

const userStore = useUserStore()

// --- Tab 配置 ---
const currentTab = ref('profile')
const tabs = [
  { id: 'profile', label: '个人资料', icon: User },
  { id: 'security', label: '账号安全', icon: Shield },
  { id: 'preference', label: '偏好设置', icon: Settings },
]

// --- 统计数据与动效 ---
const sourceStats = ref({
  rankScore: 0,
  totalSolved: 0,
  globalRank: 99999,
})

const stats = reactive({
  rankScore: useTransition(sourceStats.value.rankScore, {
    duration: 1500,
    transition: TransitionPresets.easeOutCubic,
  }),
  totalSolved: useTransition(sourceStats.value.totalSolved, {
    duration: 1200,
    transition: TransitionPresets.easeOutExpo,
  }),
  globalRank: useTransition(sourceStats.value.globalRank, {
    duration: 2000,
    transition: TransitionPresets.easeOutQuart,
  }),
})

// --- 表单数据 ---
const formData = reactive({
  userAccount: userStore.userInfo?.userAccount || '',
  userName: userStore.userInfo?.userName || '',
  userProfile: userStore.userInfo?.userProfile || '',
  email: '',
  phone: '',
})

const saving = ref(false)

onMounted(() => {
  // 模拟加载
  setTimeout(() => {
    sourceStats.value = {
      rankScore: userStore.userInfo?.rankScore || 1500,
      totalSolved: userStore.userInfo?.totalSolved || 42,
      globalRank: userStore.userInfo?.globalRank || 8848,
    }
  }, 300)
})

const handleSave = async () => {
  saving.value = true
  await new Promise((resolve) => setTimeout(resolve, 1000))
  saving.value = false
}
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition:
    opacity 0.2s ease,
    transform 0.2s ease;
}
.fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}
.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
