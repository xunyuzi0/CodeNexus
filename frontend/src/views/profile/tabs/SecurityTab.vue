<template>
  <div class="space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-500">
    <div class="bg-zinc-900/30 backdrop-blur-md border border-white/5 rounded-2xl p-6 md:p-8">
      <div class="flex items-center gap-3 mb-6">
        <div class="p-2 rounded-lg bg-zinc-800/50">
          <KeyRound class="w-5 h-5 text-zinc-300" />
        </div>
        <div>
          <h3 class="text-lg font-bold text-white">修改密码</h3>
          <p class="text-xs text-zinc-500">定期更新密码以保护账号安全</p>
        </div>
      </div>

      <form @submit.prevent="handleUpdatePassword" class="max-w-md space-y-4">
        <div class="space-y-2">
          <label class="text-xs font-bold text-zinc-500 uppercase tracking-wider ml-1">当前密码</label>
          <input
            v-model="form.currentPassword"
            type="password"
            class="w-full bg-black/20 border border-white/10 rounded-lg px-4 py-3 text-sm text-white focus:outline-none focus:border-[#FF4C00] focus:ring-1 focus:ring-[#FF4C00]/20 transition-all"
            placeholder="••••••••"
          />
        </div>
        
        <div class="space-y-2">
          <label class="text-xs font-bold text-zinc-500 uppercase tracking-wider ml-1">新密码</label>
          <input
            v-model="form.newPassword"
            type="password"
            class="w-full bg-black/20 border border-white/10 rounded-lg px-4 py-3 text-sm text-white focus:outline-none focus:border-[#FF4C00] focus:ring-1 focus:ring-[#FF4C00]/20 transition-all"
            placeholder="••••••••"
          />
        </div>

        <div class="space-y-2">
          <label class="text-xs font-bold text-zinc-500 uppercase tracking-wider ml-1">确认新密码</label>
          <input
            v-model="form.confirmPassword"
            type="password"
            class="w-full bg-black/20 border border-white/10 rounded-lg px-4 py-3 text-sm text-white focus:outline-none focus:border-[#FF4C00] focus:ring-1 focus:ring-[#FF4C00]/20 transition-all"
            placeholder="••••••••"
          />
        </div>

        <div class="pt-4">
          <button
            type="submit"
            :disabled="loading"
            class="bg-[#FF4C00] hover:bg-[#ff5f1f] text-white px-6 py-2.5 rounded-lg text-sm font-bold shadow-[0_0_15px_rgba(255,76,0,0.3)] hover:shadow-[0_0_25px_rgba(255,76,0,0.5)] transition-all active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
          >
            <span v-if="loading" class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
            更新密码
          </button>
        </div>
      </form>
    </div>

    <div class="bg-red-500/5 border border-red-500/20 rounded-2xl p-6 md:p-8 flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
      <div>
        <h3 class="text-lg font-bold text-red-500 mb-1 flex items-center gap-2">
          <AlertTriangle class="w-5 h-5" /> 注销账号
        </h3>
        <p class="text-sm text-red-400/70">此操作不可逆，将永久删除您的账户及所有做题记录。</p>
      </div>
      
      <button
        class="px-5 py-2.5 rounded-lg border border-red-500/30 text-red-500 text-sm font-bold hover:bg-red-500/10 transition-colors whitespace-nowrap"
      >
        永久注销
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { KeyRound, AlertTriangle } from 'lucide-vue-next'

const loading = ref(false)
const form = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const handleUpdatePassword = async () => {
  if (!form.currentPassword || !form.newPassword) return
  if (form.newPassword !== form.confirmPassword) {
    alert("新密码输入不一致")
    return
  }
  
  loading.value = true
  // 模拟 API 请求
  await new Promise(resolve => setTimeout(resolve, 1500))
  loading.value = false
  alert("密码已更新")
  form.currentPassword = ''
  form.newPassword = ''
  form.confirmPassword = ''
}
</script>