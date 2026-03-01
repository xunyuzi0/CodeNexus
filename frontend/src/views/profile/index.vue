<template>
  <div class="min-h-[85vh] w-full relative p-6 md:p-12 overflow-hidden">
    <div class="absolute inset-0 bg-grid-white/[0.02] pointer-events-none"></div>
    <div
      class="absolute top-0 right-0 w-[500px] h-[500px] bg-[#FF4C00]/10 blur-[120px] rounded-full pointer-events-none"
    ></div>

    <div class="max-w-[1600px] mx-auto relative z-10">
      <div
        class="relative flex flex-col md:flex-row items-center gap-8 p-8 bg-zinc-900/60 backdrop-blur-xl border border-white/10 rounded-3xl mb-12 group hover:border-[#FF4C00]/50 hover:shadow-[0_0_40px_rgba(255,76,0,0.15)] transition-all duration-500"
        v-motion-slide-visible-top
      >
        <div class="relative">
          <div
            class="w-32 h-32 rounded-full border-4 border-zinc-800 p-1 bg-zinc-900 shadow-xl group-hover:border-[#FF4C00]/30 transition-all duration-500 cursor-pointer hover:scale-105 hover:shadow-[0_0_30px_rgba(255,76,0,0.3)] relative group/avatar"
            @click="showAvatarDialog = true"
          >
            <img
              :src="userStore.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix'"
              class="w-full h-full rounded-full object-cover"
            />
            <div
              class="absolute inset-0 rounded-full bg-black/50 flex items-center justify-center opacity-0 group-hover/avatar:opacity-100 transition-opacity duration-300"
            >
              <span class="text-xs font-bold text-white tracking-wider">更换</span>
            </div>
          </div>
          <div
            class="absolute bottom-2 right-2 w-6 h-6 bg-emerald-500 border-4 border-zinc-900 rounded-full pointer-events-none"
          ></div>
        </div>

        <div class="text-center md:text-left flex-1 min-w-0">
          <h1 class="text-3xl font-black text-white mb-2 tracking-tight">
            {{ userStore.nickname }}
          </h1>
          <div
            class="flex items-center justify-center md:justify-start gap-4 text-zinc-400 text-sm font-mono"
          >
            <span class="flex items-center gap-2">
              <span class="text-zinc-500 font-bold">ID:</span>
              <span class="text-zinc-300">{{ displayAccount }}</span>
            </span>
            <span class="w-1 h-1 bg-zinc-600 rounded-full shrink-0"></span>
            <span
              class="truncate max-w-[200px] md:max-w-[500px] text-zinc-500 italic"
              :title="displayBio"
            >
              {{ displayBio }}
            </span>
          </div>
        </div>

        <div class="flex gap-4 shrink-0">
          <div class="text-center px-6 py-2 bg-white/5 rounded-2xl border border-white/5">
            <p class="text-2xl font-black text-[#FF4C00] font-mono">{{ displayRank }}</p>
            <p class="text-xs text-zinc-500 uppercase font-bold tracking-wider">排名</p>
          </div>
          <div class="text-center px-6 py-2 bg-white/5 rounded-2xl border border-white/5">
            <p class="text-2xl font-black text-white font-mono">{{ displayWinRate }}</p>
            <p class="text-xs text-zinc-500 uppercase font-bold tracking-wider">胜率</p>
          </div>
        </div>
      </div>

      <div class="flex flex-col md:flex-row gap-8">
        <div class="w-full md:w-64 shrink-0 space-y-2" v-motion-slide-visible-left>
          <button
            v-for="tab in tabs"
            :key="tab.id"
            @click="currentTab = tab.id"
            class="w-full flex items-center gap-3 px-4 py-3 rounded-xl font-medium transition-all duration-300"
            :class="
              currentTab === tab.id
                ? 'bg-[#FF4C00] text-white shadow-lg shadow-[#FF4C00]/20'
                : 'text-zinc-400 hover:bg-white/5 hover:text-white'
            "
          >
            <component :is="tab.icon" class="w-5 h-5" />
            {{ tab.label }}
          </button>
        </div>

        <div
          class="flex-1 min-w-0 bg-zinc-900/40 backdrop-blur-md border border-white/5 rounded-3xl p-8"
        >
          <transition mode="out-in" name="fade">
            <ProfileTab v-if="currentTab === 'profile'" />
            <SecurityTab v-else-if="currentTab === 'security'" />
            <PreferencesTab v-else-if="currentTab === 'preferences'" />
          </transition>
        </div>
      </div>
    </div>

    <ArenaDialog
      v-model="showAvatarDialog"
      title="选择指挥官头像"
      :confirm-text="selectedAvatarSeed ? '确认更换' : '请选择'"
      :confirm-disabled="!selectedAvatarSeed"
      @confirm="handleAvatarUpdate"
    >
      <div class="max-h-[50vh] overflow-y-auto custom-scrollbar pr-2">
        <div class="grid grid-cols-4 md:grid-cols-5 gap-4">
          <div
            v-for="seed in PRESET_AVATARS"
            :key="seed"
            @click="selectedAvatarSeed = seed"
            class="aspect-square rounded-xl p-2 cursor-pointer transition-all duration-200 border-2"
            :class="
              selectedAvatarSeed === seed
                ? 'border-[#FF4C00] bg-[#FF4C00]/10 shadow-[0_0_15px_rgba(255,76,0,0.3)] scale-105'
                : 'border-transparent hover:bg-white/5 hover:scale-105'
            "
          >
            <img :src="getAvatarUrl(seed)" class="w-full h-full rounded-full object-cover" />
          </div>
        </div>
      </div>
    </ArenaDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { User, Shield, Settings } from 'lucide-vue-next'
import { useUserStore } from '@/stores/user'
import ArenaDialog from '@/components/arena/ArenaDialog.vue'

import ProfileTab from './tabs/ProfileTab.vue'
import SecurityTab from './tabs/SecurityTab.vue'
import PreferencesTab from './tabs/PreferencesTab.vue'

const userStore = useUserStore()
const currentTab = ref('profile')

const tabs = [
  { id: 'profile', label: '个人资料', icon: User },
  { id: 'security', label: '账号安全', icon: Shield },
  { id: 'preferences', label: '偏好设置', icon: Settings },
]

// --- 新增：兼容新老数据的计算属性 ---
const displayAccount = computed(() => {
  const info = userStore.userInfo as any
  return info?.username || info?.userAccount || 'Guest'
})

const displayBio = computed(() => {
  const info = userStore.userInfo as any
  return info?.bio || info?.userProfile || '暂无个人简介'
})

const displayRank = computed(() => {
  const info = userStore.userInfo as any
  return info?.globalRank ? info.globalRank.toLocaleString() : '-'
})

const displayWinRate = computed(() => {
  const info = userStore.userInfo as any
  return info?.winRate ? `${info.winRate}%` : '-'
})

// --- Avatar Logic ---
const showAvatarDialog = ref(false)
const selectedAvatarSeed = ref('')
const PRESET_AVATARS = [
  'Felix',
  'Aneka',
  'Zoe',
  'Jack',
  'Bella',
  'Charlie',
  'Daisy',
  'Ethan',
  'Fiona',
  'George',
  'Hannah',
  'Ian',
  'Julia',
  'Kevin',
  'Luna',
  'Max',
  'Nora',
  'Oliver',
  'Penny',
  'Quinn',
]

const getAvatarUrl = (seed: string) => `https://api.dicebear.com/7.x/avataaars/svg?seed=${seed}`

const handleAvatarUpdate = () => {
  if (!selectedAvatarSeed.value) return
  const newAvatarUrl = getAvatarUrl(selectedAvatarSeed.value)

  // 目前仅在前端 mock 更新，实际需要调用 updateProfile API
  if (userStore.userInfo) {
    ;(userStore.userInfo as any).avatarUrl = newAvatarUrl
  }
  showAvatarDialog.value = false
  selectedAvatarSeed.value = ''
}
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.2);
}
</style>
