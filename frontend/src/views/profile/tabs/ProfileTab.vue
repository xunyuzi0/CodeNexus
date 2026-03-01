<template>
  <div class="space-y-6" v-motion-slide-visible-bottom>
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <div class="space-y-2">
        <label class="text-xs font-bold text-zinc-500 uppercase tracking-wider">账号 ID</label>
        <div class="relative group">
          <User
            class="absolute left-3 top-3 w-5 h-5 text-zinc-500 group-focus-within:text-[#FF4C00] transition-colors"
          />
          <input
            v-model="formData.userAccount"
            disabled
            type="text"
            class="w-full bg-zinc-900/50 border border-zinc-700 text-zinc-400 rounded-xl py-3 pl-10 pr-4 focus:outline-none cursor-not-allowed"
          />
        </div>
      </div>

      <div class="space-y-2">
        <label class="text-xs font-bold text-zinc-500 uppercase tracking-wider">昵称</label>
        <div class="relative group">
          <User
            class="absolute left-3 top-3 w-5 h-5 text-zinc-500 group-focus-within:text-[#FF4C00] transition-colors"
          />
          <input
            v-model="formData.userName"
            type="text"
            class="w-full bg-zinc-900/50 border border-zinc-700 text-white rounded-xl py-3 pl-10 pr-4 focus:outline-none focus:border-[#FF4C00] focus:ring-1 focus:ring-[#FF4C00] transition-all"
          />
        </div>
      </div>

      <div class="space-y-2">
        <label class="text-xs font-bold text-zinc-500 uppercase tracking-wider">邮箱</label>
        <div class="relative group">
          <Mail
            class="absolute left-3 top-3 w-5 h-5 text-zinc-500 group-focus-within:text-[#FF4C00] transition-colors"
          />
          <input
            v-model="formData.email"
            type="email"
            class="w-full bg-zinc-900/50 border border-zinc-700 text-white rounded-xl py-3 pl-10 pr-4 focus:outline-none focus:border-[#FF4C00] focus:ring-1 focus:ring-[#FF4C00] transition-all"
          />
        </div>
      </div>

      <div class="space-y-2">
        <label class="text-xs font-bold text-zinc-500 uppercase tracking-wider">手机号</label>
        <div class="relative group">
          <Phone
            class="absolute left-3 top-3 w-5 h-5 text-zinc-500 group-focus-within:text-[#FF4C00] transition-colors"
          />
          <input
            v-model="formData.phone"
            type="tel"
            class="w-full bg-zinc-900/50 border border-zinc-700 text-white rounded-xl py-3 pl-10 pr-4 focus:outline-none focus:border-[#FF4C00] focus:ring-1 focus:ring-[#FF4C00] transition-all"
          />
        </div>
      </div>
    </div>

    <div class="space-y-2">
      <label class="text-xs font-bold text-zinc-500 uppercase tracking-wider">个人简介</label>
      <div class="relative group">
        <FileText
          class="absolute left-3 top-3 w-5 h-5 text-zinc-500 group-focus-within:text-[#FF4C00] transition-colors"
        />
        <textarea
          v-model="formData.userProfile"
          rows="4"
          placeholder="暂无个人资料"
          class="w-full bg-zinc-900/50 border border-zinc-700 text-white rounded-xl py-3 pl-10 pr-4 focus:outline-none focus:border-[#FF4C00] focus:ring-1 focus:ring-[#FF4C00] transition-all resize-none placeholder-zinc-600"
        ></textarea>
      </div>
    </div>

    <div class="flex justify-end pt-4">
      <button
        @click="handleSave"
        :disabled="saving"
        class="flex items-center gap-2 px-8 py-3 bg-[#FF4C00] hover:bg-[#FF4C00]/90 text-white rounded-xl font-bold transition-all disabled:opacity-50 disabled:cursor-not-allowed shadow-lg shadow-[#FF4C00]/20 active:scale-95"
      >
        <Save v-if="!saving" class="w-5 h-5" />
        <div
          v-else
          class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"
        ></div>
        <span>{{ saving ? '保存中...' : '保存修改' }}</span>
      </button>
    </div>

    <ArenaDialog
      v-model="showDialog"
      title="保存个人资料？"
      confirm-text="确认更新"
      @confirm="confirmSave"
    >
      <p class="text-zinc-400 text-sm">
        您即将更新个人档案信息。请确认输入的内容准确无误，更新后将立即生效。
      </p>
    </ArenaDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { User, Mail, Phone, FileText, Save } from 'lucide-vue-next'
import { useUserStore } from '@/stores/user'
import { updateProfile } from '@/api/user'
import ArenaDialog from '@/components/arena/ArenaDialog.vue'

const userStore = useUserStore()
const saving = ref(false)
const showDialog = ref(false)

const formData = reactive({
  userAccount: '',
  userName: '',
  email: '',
  phone: '',
  userProfile: '',
})

// 回显数据，兼容新老字段
onMounted(() => {
  const info = userStore.userInfo as any
  if (info) {
    formData.userAccount = info.userAccount || info.username || ''
    formData.userName = info.nickname || info.userName || ''
    formData.email = info.email || ''
    formData.phone = info.phone || ''
    formData.userProfile = info.bio || info.userProfile || ''
  }
})

const handleSave = () => {
  showDialog.value = true
}

const confirmSave = async () => {
  showDialog.value = false
  saving.value = true

  try {
    // 映射表单字段到后端 DTO
    await updateProfile({
      nickname: formData.userName,
      bio: formData.userProfile,
      email: formData.email,
      phone: formData.phone,
    })

    // 成功后重新拉取最新数据，刷新全局状态
    await userStore.fetchUserProfile()
  } catch (error) {
    console.error('Update profile failed:', error)
  } finally {
    saving.value = false
  }
}
</script>
