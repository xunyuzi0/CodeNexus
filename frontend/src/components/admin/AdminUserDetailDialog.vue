<template>
  <Teleport to="body">
    <Transition name="fade">
      <div
        v-if="modelValue && user"
        class="fixed inset-0 z-[100] flex items-center justify-center bg-black/80 backdrop-blur-sm px-4"
        @click="close"
      >
        <div
          class="w-full max-w-2xl max-h-[85vh] overflow-y-auto rounded-3xl bg-[#09090b] border border-white/10 shadow-[0_0_50px_rgba(0,0,0,0.8)] relative"
          v-motion
          :initial="{ opacity: 0, scale: 0.95, y: 20 }"
          :enter="{
            opacity: 1,
            scale: 1,
            y: 0,
            transition: { type: 'spring', stiffness: 300, damping: 25 },
          }"
          @click.stop
        >
          <!-- 顶部装饰条 -->
          <div
            class="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-transparent via-[#FF4C00] to-transparent opacity-50"
          />
          <div
            class="absolute -top-20 -right-20 w-40 h-40 bg-[#FF4C00]/10 blur-[50px] rounded-full pointer-events-none"
          />

          <!-- 头部 -->
          <div class="relative z-10 p-6 pb-0">
            <div class="flex items-start justify-between mb-6">
              <div class="flex items-center gap-4">
                <img
                  :src="user.avatarUrl || '/default-avatar.png'"
                  :alt="user.nickname"
                  class="w-16 h-16 rounded-2xl object-cover border border-white/10"
                />
                <div>
                  <h3 class="text-xl font-bold text-white">{{ user.nickname || user.username }}</h3>
                  <p class="text-zinc-500 text-sm mt-1">@{{ user.username }}</p>
                  <div class="flex items-center gap-2 mt-2">
                    <span
                      :class="cn('text-xs px-2 py-0.5 rounded-full font-medium', roleBadgeClass)"
                    >
                      {{ roleLabel }}
                    </span>
                  </div>
                </div>
              </div>
              <button
                class="w-8 h-8 rounded-lg flex items-center justify-center text-zinc-500 hover:text-white hover:bg-white/5 transition-colors"
                @click="close"
              >
                <X class="w-4 h-4" />
              </button>
            </div>
          </div>

          <!-- 统计数据（管理员不显示） -->
          <div v-if="user.role !== 'admin'" class="relative z-10 px-6">
            <div class="grid grid-cols-3 gap-3 mb-6">
              <div class="bg-zinc-800/50 rounded-xl p-3 text-center">
                <p class="text-lg font-bold text-white">{{ user.solvedCount ?? 0 }}</p>
                <p class="text-zinc-500 text-xs mt-0.5">解题数</p>
              </div>
              <div class="bg-zinc-800/50 rounded-xl p-3 text-center">
                <p class="text-lg font-bold text-white">{{ user.arenaScore ?? 0 }}</p>
                <p class="text-zinc-500 text-xs mt-0.5">竞技分</p>
              </div>
              <div class="bg-zinc-800/50 rounded-xl p-3 text-center">
                <p class="text-lg font-bold text-white">{{ user.globalRank ?? '-' }}</p>
                <p class="text-zinc-500 text-xs mt-0.5">全球排名</p>
              </div>
              <div class="bg-zinc-800/50 rounded-xl p-3 text-center">
                <p class="text-lg font-bold text-white">{{ user.arenaMatches ?? 0 }}</p>
                <p class="text-zinc-500 text-xs mt-0.5">对战场次</p>
              </div>
              <div class="bg-zinc-800/50 rounded-xl p-3 text-center">
                <p class="text-lg font-bold text-white">{{ user.arenaWins ?? 0 }}</p>
                <p class="text-zinc-500 text-xs mt-0.5">胜场</p>
              </div>
              <div class="bg-zinc-800/50 rounded-xl p-3 text-center">
                <p class="text-lg font-bold text-[#FF4C00]">
                  {{ user.winRate != null ? `${user.winRate}%` : '-' }}
                </p>
                <p class="text-zinc-500 text-xs mt-0.5">胜率</p>
              </div>
            </div>
          </div>

          <!-- 基本信息（管理员不显示） -->
          <div v-if="user.role !== 'admin'" class="relative z-10 px-6 mb-6">
            <h4 class="text-sm font-medium text-zinc-400 mb-3">基本信息</h4>
            <div class="space-y-2">
              <div class="flex items-center justify-between py-2 border-b border-white/5">
                <span class="text-zinc-500 text-sm">邮箱</span>
                <span class="text-white text-sm">{{ user.email || '-' }}</span>
              </div>
              <div class="flex items-center justify-between py-2 border-b border-white/5">
                <span class="text-zinc-500 text-sm">手机</span>
                <span class="text-white text-sm">{{ user.phone || '-' }}</span>
              </div>
              <div class="flex items-center justify-between py-2 border-b border-white/5">
                <span class="text-zinc-500 text-sm">简介</span>
                <span class="text-white text-sm text-right max-w-[60%] truncate">{{
                  user.bio || '-'
                }}</span>
              </div>
              <div class="flex items-center justify-between py-2 border-b border-white/5">
                <span class="text-zinc-500 text-sm">连续签到</span>
                <span class="text-white text-sm">{{ user.continuousCheckinDays ?? 0 }} 天</span>
              </div>
              <div class="flex items-center justify-between py-2">
                <span class="text-zinc-500 text-sm">注册时间</span>
                <span class="text-white text-sm">{{ formatTime(user.createTime) }}</span>
              </div>
            </div>
          </div>

          <!-- 管理员：仅显示注册时间 -->
          <div v-if="user.role === 'admin'" class="relative z-10 px-6 mb-6">
            <div
              class="flex items-center justify-between py-3 px-4 bg-zinc-800/50 rounded-xl border border-white/5"
            >
              <span class="text-zinc-500 text-sm">注册时间</span>
              <span class="text-white text-sm font-mono">{{ formatTime(user.createTime) }}</span>
            </div>
          </div>

          <!-- 最近登录日志 -->
          <div class="relative z-10 px-6 pb-6">
            <h4 class="text-sm font-medium text-zinc-400 mb-3">最近登录日志</h4>
            <div v-if="user.recentLoginLogs?.length" class="space-y-2 max-h-48 overflow-y-auto">
              <div
                v-for="log in user.recentLoginLogs"
                :key="log.id"
                class="flex items-center justify-between py-2 px-3 bg-zinc-800/30 rounded-lg text-sm"
              >
                <div class="flex items-center gap-3">
                  <span :class="log.loginStatus === 1 ? 'text-emerald-400' : 'text-red-400'">
                    {{ log.loginStatus === 1 ? '成功' : '失败' }}
                  </span>
                  <span class="text-zinc-400">{{ log.loginIp }}</span>
                </div>
                <span class="text-zinc-500 text-xs">{{ formatTime(log.createTime) }}</span>
              </div>
            </div>
            <p v-else class="text-zinc-600 text-sm text-center py-4">暂无登录记录</p>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { X } from 'lucide-vue-next'
import { cn } from '@/lib/utils'
import type { AdminUserDetailVO } from '@/types/api'

const props = defineProps<{
  modelValue: boolean
  user: AdminUserDetailVO | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const close = () => {
  emit('update:modelValue', false)
}

const roleLabel = computed(() => {
  const map: Record<string, string> = {
    ADMIN: '管理员',
    USER: '普通用户',
    BAN: '已封禁',
  }
  return map[props.user?.role ?? ''] ?? props.user?.role ?? ''
})

const roleBadgeClass = computed(() => {
  const map: Record<string, string> = {
    ADMIN: 'bg-purple-500/15 text-purple-400',
    USER: 'bg-blue-500/15 text-blue-400',
    BAN: 'bg-red-500/15 text-red-400',
  }
  return map[props.user?.role ?? ''] ?? 'bg-zinc-500/15 text-zinc-400'
})

const formatTime = (time?: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
